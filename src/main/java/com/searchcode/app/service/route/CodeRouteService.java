/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.10
 */

package com.searchcode.app.service.route;

import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.searchcode.app.App;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dao.Repo;
import com.searchcode.app.dto.*;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.CodeMatcher;
import com.searchcode.app.service.CodeSearcher;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.service.route.CommonRouteService;
import com.searchcode.app.util.*;
import com.searchcode.app.util.Properties;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import static spark.Spark.halt;

public class CodeRouteService {

    private CodeSearcher codeSearcher;

    public CodeRouteService() {
        this.codeSearcher = new CodeSearcher();
    }

    public CodeRouteService(CodeSearcher codeSearcher) {
        this.codeSearcher = codeSearcher;
    }

    public ModelAndView root(Request request, Response response) {
        Map<String, Object> map = new HashMap<>();
        Repo repo = Singleton.getRepo();
        Gson gson = new Gson();

        map.put("repoCount", repo.getRepoCount());

        if (request.queryParams().contains("q") && !request.queryParams("q").trim().equals("")) {
            String query = request.queryParams("q").trim();
            int page = 0;

            if (request.queryParams().contains("p")) {
                try {
                    page = Integer.parseInt(request.queryParams("p"));
                    page = page > 19 ? 19 : page;
                }
                catch (NumberFormatException ex) {
                    page = 0;
                }
            }

            List<String> reposList = new ArrayList<>();
            List<String> langsList = new ArrayList<>();
            List<String> ownsList = new ArrayList<>();

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

            map.put("searchValue", query);
            map.put("searchResultJson", gson.toJson(new CodePreload(query, page, langsList, reposList, ownsList)));

            map.put("logoImage", CommonRouteService.getLogo());
            map.put("isCommunity", App.ISCOMMUNITY);
            return new ModelAndView(map, "search_ajax.ftl");
        }

        CodeSearcher cs = new CodeSearcher();

        map.put("photoId", CommonRouteService.getPhotoId());
        map.put("numDocs", cs.getTotalNumberDocumentsIndexed());
        map.put("logoImage", CommonRouteService.getLogo());
        map.put("isCommunity", App.ISCOMMUNITY);
        return new ModelAndView(map, "index.ftl");
    }

    public Map<String, Object> getCode(Request request, Response response) {
        Map<String, Object> map = new HashMap<>();

        Repo repo = Singleton.getRepo();
        Data data = Singleton.getData();

        SearchcodeLib scl = Singleton.getSearchcodeLib(data);
        OWASPClassifier owaspClassifier = new OWASPClassifier();

        Cocomo2 coco = new Cocomo2();

        String codeId = request.params(":codeid");
        CodeResult codeResult = this.codeSearcher.getByCodeId(codeId);

        if (codeResult == null) {
            response.redirect("/404/");
            halt();
        }

        List<String> codeLines = codeResult.code;
        StringBuilder code = new StringBuilder();
        StringBuilder lineNos = new StringBuilder();
        String padStr = "";
        for (int total = codeLines.size() / 10; total > 0; total = total / 10) {
            padStr += " ";
        }
        for (int i=1, d=10, len=codeLines.size(); i<=len; i++) {
            if (i/d > 0)
            {
                d *= 10;
                padStr = padStr.substring(0, padStr.length()-1);  // Del last char
            }
            code.append("<span id=\"")
                    .append(i)
                    .append("\"></span>")
                    .append(StringEscapeUtils.escapeHtml4(codeLines.get(i - 1)))
                    .append("\n");
            lineNos.append(padStr)
                    .append("<a href=\"#")
                    .append(i)
                    .append("\">")
                    .append(i)
                    .append("</a>")
                    .append("\n");
        }

        List<OWASPMatchingResult> owaspResults = new ArrayList<OWASPMatchingResult>();
        if (CommonRouteService.owaspAdvisoriesEnabled()) {
            if (!codeResult.languageName.equals("Text") && !codeResult.languageName.equals("Unknown")) {
                owaspResults = owaspClassifier.classifyCode(codeLines, codeResult.languageName);
            }
        }

        int limit = Integer.parseInt(
                Properties.getProperties().getProperty(
                        Values.HIGHLIGHT_LINE_LIMIT, Values.DEFAULT_HIGHLIGHT_LINE_LIMIT));
        boolean highlight = Singleton.getHelpers().tryParseInt(codeResult.codeLines, "0") <= limit;

        RepoResult repoResult = repo.getRepoByName(codeResult.repoName);

        if (repoResult != null) {
            map.put("source", repoResult.getSource());
        }

        map.put("fileName", codeResult.fileName);

        // TODO fix this properly code path includes the repo name and should be removed
        String codePath = codeResult.codePath;

        if (codeResult.codePath.contains("/")) {
            codePath = codeResult.codePath.substring(codeResult.codePath.indexOf('/'), codeResult.codePath.length());
        }

        if (!codePath.startsWith("/")) {
            codePath = "/" + codePath;
        }
        map.put("codePath", codePath);
        map.put("codeLength", codeResult.codeLines);

        map.put("linenos", lineNos.toString());

        map.put("languageName", codeResult.languageName);
        map.put("md5Hash", codeResult.md5hash);
        map.put("repoName", codeResult.repoName);
        map.put("highlight", highlight);
        map.put("repoLocation", codeResult.getRepoLocation());

        map.put("codeValue", code.toString());
        map.put("highligher", CommonRouteService.getSyntaxHighlighter());
        map.put("codeOwner", codeResult.getCodeOwner());
        map.put("owaspResults", owaspResults);

        double estimatedEffort = coco.estimateEffort(scl.countFilteredLines(codeResult.getCode()));
        int estimatedCost = (int)coco.estimateCost(estimatedEffort, CommonRouteService.getAverageSalary());
        if (estimatedCost != 0 && !scl.languageCostIgnore(codeResult.getLanguageName())) {
            map.put("estimatedCost", estimatedCost);
        }

        map.put("logoImage", CommonRouteService.getLogo());
        map.put("isCommunity", App.ISCOMMUNITY);

        return map;
    }

    public Map<String, Object> getProject(Request request, Response response) {
        Map<String, Object> map = new HashMap<>();

        String repoName = request.params(":reponame");
        RepoResult repository = Singleton.getRepo().getRepoByName(repoName);
        SearchcodeLib searchcodeLib = Singleton.getSearchCodeLib();
        Cocomo2 coco = new Cocomo2();
        Gson gson = new Gson();

        if (repository == null) {
            response.redirect("/404/");
            halt();
        }

        ProjectStats projectStats = this.codeSearcher.getProjectStats(repository.getName());


        map.put("busBlurb", searchcodeLib.generateBusBlurb(projectStats));
        map.put("repoLocation", repository.getUrl());
        map.put("repoBranch", repository.getBranch());

        map.put("totalFiles", projectStats.getTotalFiles());
        map.put("totalCodeLines", projectStats.getTotalCodeLines());
        map.put("languageFacet", projectStats.getCodeFacetLanguages());
        map.put("ownerFacet", projectStats.getRepoFacetOwner());
        map.put("codeByLines", projectStats.getCodeByLines());

        double estimatedEffort = coco.estimateEffort(projectStats.getTotalCodeLines());
        map.put("estimatedEffort", estimatedEffort);
        map.put("estimatedCost", (int)coco.estimateCost(estimatedEffort, CommonRouteService.getAverageSalary()));

        map.put("totalOwners", projectStats.getRepoFacetOwner().size());
        map.put("totalLanguages", projectStats.getCodeFacetLanguages().size());

        map.put("ownerFacetJson", gson.toJson(projectStats.getRepoFacetOwner()));
        map.put("languageFacetJson", gson.toJson(projectStats.getCodeFacetLanguages()));

        map.put("repoName", repoName);
        map.put("logoImage", CommonRouteService.getLogo());
        map.put("isCommunity", App.ISCOMMUNITY);

        return map;
    }

    public Map<String, Object> getRepositoryList(Request request, Response response) {
        Map<String, Object> map = new HashMap<>();

        Repo repo = Singleton.getRepo();
        String offSet = request.queryParams("offset");

        int pageSize = 20;
        int indexOffset = Singleton.getHelpers().tryParseInt(offSet, "0");

        List<RepoResult> pagedRepo = repo.getPagedRepo(pageSize * indexOffset, pageSize + 1);
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
        map.put("logoImage", CommonRouteService.getLogo());
        map.put("isCommunity", App.ISCOMMUNITY);

        return map;
    }

    public ModelAndView html(Request request, Response response) {
        Repo repo = Singleton.getRepo();
        Data data = Singleton.getData();

        SearchcodeLib scl = Singleton.getSearchcodeLib(data);
        CodeSearcher cs = new CodeSearcher();
        CodeMatcher cm = new CodeMatcher(data);
        Map<String, Object> map = new HashMap<>();

        map.put("repoCount", repo.getRepoCount());

        if (request.queryParams().contains("q")) {
            String query = request.queryParams("q").trim();
            String altquery = query.replaceAll("[^A-Za-z0-9 ]", " ").trim().replaceAll(" +", " ");
            int page = 0;

            if (request.queryParams().contains("p")) {
                try {
                    page = Integer.parseInt(request.queryParams("p"));
                    page = page > 19 ? 19 : page;
                }
                catch (NumberFormatException ex) {
                    page = 0;
                }
            }

            String[] repos = new String[0];
            String[] langs = new String[0];
            String[] owners = new String[0];
            String reposFilter = Values.EMPTYSTRING;
            String langsFilter = Values.EMPTYSTRING;
            String ownersFilter = Values.EMPTYSTRING;
            String reposQueryString = Values.EMPTYSTRING;
            String langsQueryString = Values.EMPTYSTRING;
            String ownsQueryString = Values.EMPTYSTRING;


            if (request.queryParams().contains("repo")) {
                repos = request.queryParamsValues("repo");

                if (repos.length != 0) {
                    List<String> reposList = Arrays.asList(repos).stream()
                            .map((s) -> "reponame:" + QueryParser.escape(s))
                            .collect(Collectors.toList());

                    reposFilter = " && (" + StringUtils.join(reposList, " || ") + ")";

                    List<String> reposQueryList = Arrays.asList(repos).stream()
                            .map((s) -> "&repo=" + URLEncoder.encode(s))
                            .collect(Collectors.toList());

                    reposQueryString = StringUtils.join(reposQueryList, "");
                }
            }

            if (request.queryParams().contains("lan")) {
                langs = request.queryParamsValues("lan");

                if (langs.length != 0) {
                    List<String> langsList = Arrays.asList(langs).stream()
                            .map((s) -> "languagename:" + QueryParser.escape(s))
                            .collect(Collectors.toList());

                    langsFilter = " && (" + StringUtils.join(langsList, " || ") + ")";

                    List<String> langsQueryList = Arrays.asList(langs).stream()
                            .map((s) -> "&lan=" + URLEncoder.encode(s))
                            .collect(Collectors.toList());

                    langsQueryString = StringUtils.join(langsQueryList, "");
                }
            }

            if (request.queryParams().contains("own")) {
                owners = request.queryParamsValues("own");

                if (owners.length != 0) {
                    List<String> ownersList = Arrays.asList(owners).stream()
                            .map((s) -> "codeowner:" + QueryParser.escape(s))
                            .collect(Collectors.toList());

                    ownersFilter = " && (" + StringUtils.join(ownersList, " || ") + ")";

                    List<String> ownsQueryList = Arrays.asList(owners).stream()
                            .map((s) -> "&own=" + URLEncoder.encode(s))
                            .collect(Collectors.toList());

                    ownsQueryString = StringUtils.join(ownsQueryList, "");
                }
            }

            // split the query escape it and and it together
            String cleanQueryString = scl.formatQueryString(query);

            SearchResult searchResult = cs.search(cleanQueryString + reposFilter + langsFilter + ownersFilter, page);
            searchResult.setCodeResultList(cm.formatResults(searchResult.getCodeResultList(), query, true));

            for(CodeFacetRepo f: searchResult.getRepoFacetResults()) {
                if (Arrays.asList(repos).contains(f.getRepoName())) {
                    f.setSelected(true);
                }
            }

            for(CodeFacetLanguage f: searchResult.getLanguageFacetResults()) {
                if (Arrays.asList(langs).contains(f.getLanguageName())) {
                    f.setSelected(true);
                }
            }

            for(CodeFacetOwner f: searchResult.getOwnerFacetResults()) {
                if (Arrays.asList(owners).contains(f.getOwner())) {
                    f.setSelected(true);
                }
            }

            map.put("searchValue", query);
            map.put("searchResult", searchResult);
            map.put("reposQueryString", reposQueryString);
            map.put("langsQueryString", langsQueryString);
            map.put("ownsQueryString", ownsQueryString);

            map.put("altQuery", altquery);

            map.put("totalPages", searchResult.getPages().size());


            map.put("isHtml", true);
            map.put("logoImage", CommonRouteService.getLogo());
            map.put("isCommunity", App.ISCOMMUNITY);
            return new ModelAndView(map, "searchresults.ftl");
        }

        map.put("photoId", CommonRouteService.getPhotoId());
        map.put("numDocs", cs.getTotalNumberDocumentsIndexed());
        map.put("logoImage", CommonRouteService.getLogo());
        map.put("isCommunity", App.ISCOMMUNITY);
        return new ModelAndView(map, "index.ftl");
    }

    // This is very alpha and just for testing
    // TODO improve or remove this
    public Map<String, Object> literalSearch(Request request, Response response) {
        Repo repo = Singleton.getRepo();
        Data data = Singleton.getData();

        CodeSearcher cs = new CodeSearcher();
        CodeMatcher cm = new CodeMatcher(data);
        Map<String, Object> map = new HashMap<>();

        map.put("repoCount", repo.getRepoCount());

        if (request.queryParams().contains("q")) {
            String query = request.queryParams("q").trim();

            int page = 0;

            if (request.queryParams().contains("p")) {
                try {
                    page = Integer.parseInt(request.queryParams("p"));
                    page = page > 19 ? 19 : page;
                }
                catch(NumberFormatException ex) {
                    page = 0;
                }
            }

            String altquery = query.replaceAll("[^A-Za-z0-9 ]", " ").trim().replaceAll(" +", " ");

            SearchResult searchResult = cs.search(query, page);
            searchResult.setCodeResultList(cm.formatResults(searchResult.getCodeResultList(), altquery, false));


            map.put("searchValue", query);
            map.put("searchResult", searchResult);
            map.put("reposQueryString", "");
            map.put("langsQueryString", "");

            map.put("altQuery", "");

            map.put("logoImage", CommonRouteService.getLogo());
            map.put("isCommunity", App.ISCOMMUNITY);
            return map;
        }

        map.put("photoId", 1);
        map.put("numDocs", cs.getTotalNumberDocumentsIndexed());
        map.put("logoImage", CommonRouteService.getLogo());
        map.put("isCommunity", App.ISCOMMUNITY);

        return map;
    }
}
