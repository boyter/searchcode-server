/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.service.route;

import com.google.gson.Gson;
import com.searchcode.app.App;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dao.IRepo;
import com.searchcode.app.dto.CodePreload;
import com.searchcode.app.dto.OWASPMatchingResult;
import com.searchcode.app.dto.ProjectStats;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.CodeMatcher;
import com.searchcode.app.service.Highlight;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.service.index.IIndexService;
import com.searchcode.app.util.Properties;
import com.searchcode.app.util.*;
import org.apache.commons.lang3.StringUtils;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import static spark.Spark.halt;

public class CodeRouteService {

    private final IRepo repo;
    private final Data data;
    private final Gson gson;
    private final SearchCodeLib searchCodeLib;
    private final CodeMatcher codeMatcher;
    private final OWASPClassifier owaspClassifier;
    private final RepositorySource repositorySource;
    private final Helpers helpers;
    private final IIndexService indexService;
    private final String highlighter;
    private final int highlightLimit;
    private final Highlight highlight;

    public CodeRouteService() {
        this(Singleton.getIndexService(), Singleton.getHelpers(), Singleton.getRepo(), Singleton.getData(), Singleton.getSearchCodeLib(), Singleton.getCodeMatcher(), Singleton.getOwaspClassifier(), Singleton.getRepositorySource(), Singleton.getHighlight());
    }

    public CodeRouteService(IIndexService indexService, Helpers helpers, IRepo repo, Data data, SearchCodeLib searchCodeLib, CodeMatcher codeMatcher, OWASPClassifier owaspClassifier, RepositorySource repositorySource, Highlight highlight) {
        this.indexService = indexService;
        this.helpers = helpers;
        this.repo = repo;
        this.data = data;
        this.searchCodeLib = searchCodeLib;
        this.codeMatcher = codeMatcher;
        this.owaspClassifier = owaspClassifier;
        this.repositorySource = repositorySource;
        this.gson = new Gson();
        this.highlight = highlight;

        this.highlighter = Properties.getProperties().getProperty(Values.HIGHLIGHTER, Values.DEFAULT_HIGHLIGHTER);
        this.highlightLimit = Integer.parseInt(Properties.getProperties().getProperty(Values.HIGHLIGHT_LINE_LIMIT, Values.DEFAULT_HIGHLIGHT_LINE_LIMIT));
    }

    // TODO this should not be static
    public static int getPage(Request request, int page) {
        if (request.queryParams().contains("p")) {
            try {
                page = Integer.parseInt(request.queryParams("p"));
                page = page > 19 ? 19 : page;
            } catch (NumberFormatException ex) {
                page = 0;
            }
        }
        return page;
    }

    public ModelAndView root(Request request, Response response) {
        var map = this.getMap(request);
        map.put("repoCount", this.repo.getRepoCount());

        if (request.queryParams().contains("q") && !request.queryParams("q").trim().equals("")) {
            String query = request.queryParams("q").trim();
            int page = getPage(request, 0);

            List<String> reposList = new ArrayList<>();
            List<String> langsList = new ArrayList<>();
            List<String> ownsList = new ArrayList<>();
            List<String> srcsList = new ArrayList<>();

            if (request.queryParams().contains("repo")) {
                String[] repos;
                repos = request.queryParamsValues("repo");

                if (repos.length != 0) {
                    reposList = Arrays.asList(repos);
                }
            }

            if (request.queryParams().contains("lan")) {
                String[] langs;
                langs = request.queryParamsValues("lan");

                if (langs.length != 0) {
                    langsList = Arrays.asList(langs);
                }
            }

            if (request.queryParams().contains("own")) {
                String[] owns;
                owns = request.queryParamsValues("own");

                if (owns.length != 0) {
                    ownsList = Arrays.asList(owns);
                }
            }

            if (request.queryParams().contains("src")) {
                String[] srcs;
                srcs = request.queryParamsValues("src");

                if (srcs.length != 0) {
                    srcsList = Arrays.asList(srcs);
                }
            }


            String pathValue = Values.EMPTYSTRING;
            if (request.queryParams().contains("path")) {
                pathValue = request.queryParams("path").trim();
            }

            var isLiteral = false;
            if (request.queryParams().contains("lit")) {
                isLiteral = Boolean.parseBoolean(request.queryParams("lit").trim());
            }

            map.put("searchValue", query);
            map.put("searchResultJson", gson.toJson(new CodePreload(query, page, langsList, reposList, ownsList, srcsList, pathValue, isLiteral)));

            return new ModelAndView(map, "search_ajax.ftl");
        }

        map.put("photoId", CommonRouteService.getPhotoId(Calendar.getInstance().get(Calendar.DAY_OF_YEAR)));
        if (request.queryParams().contains("photoId")) {
            map.put("photoId", request.queryParams("photoId"));
        }

        map.put("numDocs", this.indexService.getIndexedDocumentCount());

        return new ModelAndView(map, "index.ftl");
    }

    public Map<String, Object> getCode(Request request, Response response) {
        var map = this.getMap(request);

        var codeId = request.params(":codeid");
        var codeResult = this.indexService.getCodeResultByCodeId(codeId);

        if (codeResult == null) {
            response.redirect("/404/");
            halt();
        }

        map.putAll(this.highlight.highlightCodeResult(codeResult));

        var owaspResults = new ArrayList<OWASPMatchingResult>();
        if (CommonRouteService.owaspAdvisoriesEnabled()) {
            if (!codeResult.languageName.equals("Text") && !codeResult.languageName.equals("Unknown")) {
                owaspResults = this.owaspClassifier.classifyCode(codeResult.code, codeResult.languageName);
            }
        }

        var highlight = this.helpers.tryParseInt(codeResult.codeLines, "0") <= this.highlightLimit;

        Optional<RepoResult> repoResult = this.repo.getRepoByName(codeResult.repoName);
        repoResult.map(x -> map.put("source", x.getSource()));
        repoResult.map(x -> map.put("fileLink", this.repositorySource.getLink(x.getData().source,
                new HashMap<String, String>() {{
                    put("user", x.getData().user);
                    put("project", x.getData().project);
                    put("branch", x.getBranch());
                    put("filepath", codeResult.getDisplayLocation());
                }})));


        map.put("fileName", codeResult.fileName);
        map.put("codePath", codeResult.getDisplayLocation());
        map.put("codeLength", codeResult.lines);

        map.put("languageName", codeResult.languageName);
        map.put("md5Hash", codeResult.md5hash);
        map.put("repoName", codeResult.repoName);
        map.put("highlight", highlight);
        map.put("repoLocation", codeResult.getRepoLocation());
        map.put("highligher", CommonRouteService.getSyntaxHighlighter());
        map.put("codeOwner", codeResult.getCodeOwner());
        map.put("owaspResults", owaspResults);

        var coco = new Cocomo2();
        var estimatedEffort = coco.estimateEffort(this.helpers.tryParseDouble(codeResult.getCodeLines(), "0"));
        var estimatedCost = (int) coco.estimateCost(estimatedEffort, CommonRouteService.getAverageSalary());
        map.put("estimatedCost", estimatedCost);

        return map;
    }

    public Map<String, Object> getProject(Request request, Response response) {
        var map = this.getMap(request);

        String repoName = request.params(":reponame");
        Optional<RepoResult> repository = this.repo.getRepoByName(repoName);
        Cocomo2 coco = new Cocomo2();
        Gson gson = new Gson();

        if (!repository.isPresent()) {
            response.redirect("/404/");
            halt();
        }

        ProjectStats projectStats = repository.map(x -> this.indexService.getProjectStats(x.getName()))
                .orElseGet(() -> this.indexService.getProjectStats(Values.EMPTYSTRING));

        map.put("busBlurb", this.searchCodeLib.generateBusBlurb(projectStats));
        repository.ifPresent(x -> map.put("repoLocation", x.getUrl()));
        repository.ifPresent(x -> map.put("repoBranch", x.getBranch()));

        map.put("totalFiles", projectStats.getTotalFiles());
        map.put("totalCodeLines", projectStats.getTotalCodeLines());
        map.put("languageFacet", projectStats.getCodeFacetLanguages());
        map.put("ownerFacet", projectStats.getRepoFacetOwner());
        map.put("codeByLines", projectStats.getCodeByLines());

        double estimatedEffort = coco.estimateEffort(projectStats.getTotalCodeLines());
        map.put("estimatedEffort", estimatedEffort);
        map.put("estimatedCost", (int) coco.estimateCost(estimatedEffort, CommonRouteService.getAverageSalary()));

        map.put("totalOwners", projectStats.getRepoFacetOwner().size());
        map.put("totalLanguages", projectStats.getCodeFacetLanguages().size());

        map.put("ownerFacetJson", gson.toJson(projectStats.getRepoFacetOwner()));
        map.put("languageFacetJson", gson.toJson(projectStats.getCodeFacetLanguages()));
        repository.ifPresent(x -> map.put("source", x.getSource()));

        map.put("repoName", repoName);

        return map;
    }

    public Map<String, Object> getRepositoryList(Request request, Response response) {
        var map = this.getMap(request);

        String offSet = request.queryParams("offset");

        int pageSize = 20;
        int indexOffset = this.helpers.tryParseInt(offSet, "0");

        List<RepoResult> pagedRepo = this.repo.getPagedRepo(pageSize * indexOffset, pageSize + 1);
        boolean hasNext = pagedRepo.size() == (pageSize + 1);
        boolean hasPrevious = indexOffset != 0;

        if (hasNext) {
            pagedRepo = pagedRepo.subList(0, pageSize);
        }

        map.put("hasPrevious", hasPrevious);
        map.put("hasNext", hasNext);
        map.put("repoList", pagedRepo);
        map.put("nextOffset", indexOffset + 1);
        map.put("previousOffset", indexOffset - 1);

        return map;
    }

    public Map<String, Object> html(Request request, Response response) {
        var map = this.getMap(request);

        if (request.queryParams().contains("q")) {
            var query = request.queryParams("q").trim();
            var altQuery = query.replaceAll("[^A-Za-z0-9 ]", " ").trim().replaceAll(" +", " ");
            var page = getPage(request, 0);

            // Contains the filters that we want to apply
            var repos = new String[0];
            var langs = new String[0];
            var owners = new String[0];
            var sources = new String[0];

            // These are needed to create the query string again for HTML pages
            // so that links preserve the filters
            var reposQueryString = Values.EMPTYSTRING;
            var langsQueryString = Values.EMPTYSTRING;
            var ownsQueryString = Values.EMPTYSTRING;
            var sourceQueryString = Values.EMPTYSTRING;

            var facets = new HashMap<String, String[]>();

            if (request.queryParams().contains("repo")) {
                repos = request.queryParamsValues("repo");

                if (repos.length != 0) {
                    facets.put("repo", repos);

                    var reposQueryList = Arrays.asList(repos).stream()
                            .map((s) -> {
                                try {
                                    return "&repo=" + URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8.toString());
                                } catch (UnsupportedEncodingException ex) {
                                    return Values.EMPTYSTRING;
                                }
                            })
                            .collect(Collectors.toList());

                    reposQueryString = StringUtils.join(reposQueryList, "");
                }
            }

            if (request.queryParams().contains("lan")) {
                langs = request.queryParamsValues("lan");

                if (langs.length != 0) {
                    facets.put("lan", langs);

                    var langsQueryList = Arrays.asList(langs).stream()
                            .map((s) -> {
                                try {
                                    return "&lan=" + URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8.toString());
                                } catch (UnsupportedEncodingException e) {
                                    return Values.EMPTYSTRING;
                                }
                            })
                            .collect(Collectors.toList());

                    langsQueryString = StringUtils.join(langsQueryList, "");
                }
            }

            if (request.queryParams().contains("own")) {
                owners = request.queryParamsValues("own");

                if (owners.length != 0) {
                    facets.put("own", owners);
                    var ownsQueryList = Arrays.asList(owners).stream()
                            .map((s) -> {
                                try {
                                    return "&own=" + URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8.toString());
                                } catch (UnsupportedEncodingException e) {
                                    return Values.EMPTYSTRING;
                                }
                            })
                            .collect(Collectors.toList());

                    ownsQueryString = StringUtils.join(ownsQueryList, "");
                }
            }

            if (request.queryParams().contains("source")) {
                sources = request.queryParamsValues("source");

                if (sources.length != 0) {
                    facets.put("source", sources);
                    var sourcesQueryList = Arrays.asList(sources).stream()
                            .map((s) -> {
                                try {
                                    return "&source=" + URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8.toString());
                                } catch (UnsupportedEncodingException e) {
                                    return Values.EMPTYSTRING;
                                }
                            })
                            .collect(Collectors.toList());

                    sourceQueryString = StringUtils.join(sourcesQueryList, "");
                }
            }

            // TODO check if the format query string needs to be modified for sphinx
            var searchResult = this.indexService.search(this.searchCodeLib.formatQueryString(query), facets, page, false);
            searchResult.setCodeResultList(this.codeMatcher.formatResults(searchResult.getCodeResultList(), query, true));

            // Set chosen filters to be selected
            for (var f : searchResult.getRepoFacetResults()) {
                if (Arrays.asList(repos).contains(f.getRepoName())) {
                    f.setSelected(true);
                }
            }

            for (var f : searchResult.getLanguageFacetResults()) {
                if (Arrays.asList(langs).contains(f.getLanguageName())) {
                    f.setSelected(true);
                }
            }

            for (var f : searchResult.getOwnerFacetResults()) {
                if (Arrays.asList(owners).contains(f.getOwner())) {
                    f.setSelected(true);
                }
            }

            for (var f : searchResult.getCodeFacetSources()) {
                if (Arrays.asList(sources).contains(f.source)) {
                    f.setSelected(true);
                }
            }

            map.put("searchValue", query);
            map.put("searchResult", searchResult);

            map.put("reposQueryString", reposQueryString);
            map.put("langsQueryString", langsQueryString);
            map.put("ownsQueryString", ownsQueryString);
            map.put("sourceQueryString", sourceQueryString);

            map.put("altQuery", altQuery);
            map.put("totalPages", searchResult.getPages().size());

            map.put("isHtml", true);
            map.put("isIndex", false);

            return map;
        } else {
            map.put("repoCount", this.repo.getRepoCount());
        }

        map.put("photoId", CommonRouteService.getPhotoId(Calendar.getInstance().get(Calendar.DAY_OF_YEAR)));
        map.put("numDocs", this.indexService.getCodeIndexLinesCount());

        map.put("isIndex", true);
        return map;
    }

    private HashMap<String, Object> getMap(Request request) {
        var map = new HashMap<String, Object>();

        map.put("currentUrl", request.url());
        map.put("logoImage", CommonRouteService.getLogo());
        map.put("isCommunity", App.IS_COMMUNITY);
        map.put(Values.EMBED, this.data.getDataByName(Values.EMBED, Values.EMPTYSTRING));

        return map;
    }
}
