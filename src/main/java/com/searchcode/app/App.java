/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.4.0
 */

package com.searchcode.app;

import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.searchcode.app.config.InjectorConfig;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Api;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dao.Repo;
import com.searchcode.app.dto.*;
import com.searchcode.app.dto.api.ApiResponse;
import com.searchcode.app.dto.api.RepoResultApiResponse;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.*;
import com.searchcode.app.util.*;
import com.searchcode.app.util.Properties;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import spark.ModelAndView;
import spark.Request;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static spark.Spark.*;

/**
 * Main entry point for the application.
 * TODO break parts of this out so things are easier to maintain
 * TODO remove the Guice injector since we don't really use it enough to justify its existance
 * TODO add config override for the cache setting of 60
 */
public class App {

    private static final boolean ISCOMMUNITY = true;
    private static final String VERSION = "1.4.0";
    private static final LoggerWrapper LOGGER = Singleton.getLogger();
    public static Map<String, SearchResult> cache = ExpiringMap.builder().expirationPolicy(ExpirationPolicy.ACCESSED).expiration(60, TimeUnit.SECONDS).build();
    public static Injector injector;
    public static SearchcodeLib scl;

    public static void main( String[] args ) {
        injector = Guice.createInjector(new InjectorConfig());
        int server_port = Helpers.tryParseInt(Properties.getProperties().getProperty(Values.SERVERPORT, Values.DEFAULTSERVERPORT), Values.DEFAULTSERVERPORT);
        boolean onlyLocalhost = Boolean.parseBoolean(Properties.getProperties().getProperty("only_localhost", "false"));

        // Database migrations happen before we start
        databaseMigrations();

        LOGGER.info("Starting searchcode server on port " + server_port);
        Spark.port(server_port);

        JobService js = injector.getInstance(JobService.class);
        Repo repo = injector.getInstance(Repo.class);
        Data data = injector.getInstance(Data.class);
        Api api = injector.getInstance(Api.class);

        ApiService apiService = injector.getInstance(ApiService.class);
        StatsService statsService = new StatsService();
        OWASPClassifier owaspClassifier = new OWASPClassifier();

        scl = Singleton.getSearchcodeLib(data);
        js.initialJobs();

        Gson gson = new Gson();

        Spark.staticFileLocation("/public");

        before((request, response) -> {
            if(onlyLocalhost) {
                if (!request.ip().equals("127.0.0.1")) {
                    halt(204);
                }
            }
        });

        get("/", (req, res) -> {
            res.header("Content-Encoding", "gzip");
            Map<String, Object> map = new HashMap<>();

            map.put("repoCount", repo.getRepoCount());

            if(req.queryParams().contains("q") && !req.queryParams("q").trim().equals("")) {
                String query = req.queryParams("q").trim();
                int page = 0;

                if(req.queryParams().contains("p")) {
                    try {
                        page = Integer.parseInt(req.queryParams("p"));
                        page = page > 19 ? 19 : page;
                    }
                    catch(NumberFormatException ex) {
                        page = 0;
                    }
                }

                List<String> reposList = new ArrayList<>();
                List<String> langsList = new ArrayList<>();
                List<String> ownsList = new ArrayList<>();

                if(req.queryParams().contains("repo")) {
                    String[] repos = new String[0];
                    repos = req.queryParamsValues("repo");

                    if (repos.length != 0) {
                        reposList = Arrays.asList(repos);
                    }
                }

                if(req.queryParams().contains("lan")) {
                    String[] langs = new String[0];
                    langs = req.queryParamsValues("lan");

                    if (langs.length != 0) {
                        langsList = Arrays.asList(langs);
                    }
                }

                if(req.queryParams().contains("own")) {
                    String[] owns = new String[0];
                    owns = req.queryParamsValues("own");

                    if (owns.length != 0) {
                        ownsList = Arrays.asList(owns);
                    }
                }

                map.put("searchValue", query);
                map.put("searchResultJson", gson.toJson(new CodePreload(query, page, langsList, reposList, ownsList)));


                map.put("logoImage", getLogo());
                map.put("isCommunity", ISCOMMUNITY);
                return new ModelAndView(map, "search_test.ftl");
            }

            // Totally pointless vanity but lets rotate the image every week
            int photoId = getWeekOfMonth();

            if (photoId <= 0) {
                photoId = 3;
            }
            if (photoId > 4) {
                photoId = 2;
            }

            CodeSearcher cs = new CodeSearcher();

            map.put("photoId", photoId);
            map.put("numDocs", cs.getTotalNumberDocumentsIndexed());
            map.put("logoImage", getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "index.ftl");
        }, new FreeMarkerEngine());

        get("/html/", (req, res) -> {
            res.header("Content-Encoding", "gzip");
            CodeSearcher cs = new CodeSearcher();
            CodeMatcher cm = new CodeMatcher(data);
            Map<String, Object> map = new HashMap<>();

            map.put("repoCount", repo.getRepoCount());

            if(req.queryParams().contains("q")) {
                String query = req.queryParams("q").trim();
                String altquery = query.replaceAll("[^A-Za-z0-9 ]", " ").trim().replaceAll(" +", " ");
                int page = 0;

                if(req.queryParams().contains("p")) {
                    try {
                        page = Integer.parseInt(req.queryParams("p"));
                        page = page > 19 ? 19 : page;
                    }
                    catch(NumberFormatException ex) {
                        page = 0;
                    }
                }

                String[] repos = new String[0];
                String[] langs = new String[0];
                String[] owners = new String[0];
                String reposFilter = "";
                String langsFilter = "";
                String ownersFilter = "";
                String reposQueryString = "";
                String langsQueryString = "";
                String ownsQueryString = "";


                if(req.queryParams().contains("repo")) {
                    repos = req.queryParamsValues("repo");

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

                if(req.queryParams().contains("lan")) {
                    langs = req.queryParamsValues("lan");

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

                if(req.queryParams().contains("own")) {
                    owners = req.queryParamsValues("own");

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
                    if(Arrays.asList(repos).contains(f.getRepoName())) {
                        f.setSelected(true);
                    }
                }

                for(CodeFacetLanguage f: searchResult.getLanguageFacetResults()) {
                    if(Arrays.asList(langs).contains(f.getLanguageName())) {
                        f.setSelected(true);
                    }
                }

                for(CodeFacetOwner f: searchResult.getOwnerFacetResults()) {
                    if(Arrays.asList(owners).contains(f.getOwner())) {
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
                map.put("logoImage", getLogo());
                map.put("isCommunity", ISCOMMUNITY);
                return new ModelAndView(map, "searchresults.ftl");
            }

            // Totally pointless vanity but lets rotate the image every week
            int photoId = getWeekOfMonth();

            if (photoId <= 0) {
                photoId = 3;
            }
            if (photoId > 4) {
                photoId = 2;
            }

            map.put("photoId", photoId);
            map.put("numDocs", cs.getTotalNumberDocumentsIndexed());
            map.put("logoImage", getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "index.ftl");
        }, new FreeMarkerEngine());

        /**
         * Allows one to write literal lucene search queries against the index
         * TODO This is still very much WIP
         */
        get("/literal/", (req, res) -> {
            CodeSearcher cs = new CodeSearcher();
            CodeMatcher cm = new CodeMatcher(data);
            Map<String, Object> map = new HashMap<>();

            map.put("repoCount", repo.getRepoCount());

            if(req.queryParams().contains("q")) {
                String query = req.queryParams("q").trim();

                int page = 0;

                if(req.queryParams().contains("p")) {
                    try {
                        page = Integer.parseInt(req.queryParams("p"));
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

                map.put("logoImage", getLogo());
                map.put("isCommunity", ISCOMMUNITY);
                return new ModelAndView(map, "searchresults.ftl");
            }

            map.put("numDocs", cs.getTotalNumberDocumentsIndexed());
            map.put("logoImage", getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "index.ftl");
        }, new FreeMarkerEngine());

        /**
         * This is the endpoint used by the frontend.
         */
        get("/api/codesearch/", (req, res) -> {
            res.header("Content-Encoding", "gzip");
            CodeSearcher cs = new CodeSearcher();
            CodeMatcher cm = new CodeMatcher(data);

            if(req.queryParams().contains("q") && req.queryParams("q").trim() != "") {
                String query = req.queryParams("q").trim();

                int page = 0;

                if(req.queryParams().contains("p")) {
                    try {
                        page = Integer.parseInt(req.queryParams("p"));
                        page = page > 19 ? 19 : page;
                    }
                    catch(NumberFormatException ex) {
                        page = 0;
                    }
                }

                String[] repos = new String[0];
                String[] langs = new String[0];
                String[] owners = new String[0];
                String reposFilter = "";
                String langsFilter = "";
                String ownersFilter = "";


                if(req.queryParams().contains("repo")) {
                    repos = req.queryParamsValues("repo");

                    if (repos.length != 0) {
                        List<String> reposList = Arrays.asList(repos).stream()
                                .map((s) -> "reponame:" + QueryParser.escape(s))
                                .collect(Collectors.toList());

                        reposFilter = " && (" + StringUtils.join(reposList, " || ") + ")";
                    }
                }

                if(req.queryParams().contains("lan")) {
                    langs = req.queryParamsValues("lan");

                    if (langs.length != 0) {
                        List<String> langsList = Arrays.asList(langs).stream()
                                .map((s) -> "languagename:" + QueryParser.escape(s))
                                .collect(Collectors.toList());

                        langsFilter = " && (" + StringUtils.join(langsList, " || ") + ")";
                    }
                }

                if(req.queryParams().contains("own")) {
                    owners = req.queryParamsValues("own");

                    if (owners.length != 0) {
                        List<String> ownersList = Arrays.asList(owners).stream()
                                .map((s) -> "codeowner:" + QueryParser.escape(s))
                                .collect(Collectors.toList());

                        ownersFilter = " && (" + StringUtils.join(ownersList, " || ") + ")";
                    }
                }

                // Need to pass in the filters into this query
                String cacheKey = query + page + reposFilter + langsFilter + ownersFilter;

                if(cache.containsKey(cacheKey)) {
                    return cache.get(cacheKey);
                }

                // split the query escape it and and it together
                String cleanQueryString = scl.formatQueryString(query);

                SearchResult searchResult = cs.search(cleanQueryString + reposFilter + langsFilter + ownersFilter, page);
                searchResult.setCodeResultList(cm.formatResults(searchResult.getCodeResultList(), query, true));

                searchResult.setQuery(query);

                for(String altQuery: scl.generateAltQueries(query)) {
                    searchResult.addAltQuery(altQuery);
                }

                // Null out code as it isnt required and there is no point in bloating our ajax requests
                for(CodeResult codeSearchResult: searchResult.getCodeResultList()) {
                    codeSearchResult.setCode(null);
                }

                cache.put(cacheKey, searchResult);
                return searchResult;
            }

            return null;
        }, new JsonTransformer());


        get("/api/timecodesearch/", (request, response) -> {
            ApiRouteService ars = new ApiRouteService();
            return ars.getTimeSearch(request, response);
        }, new JsonTransformer());

        get("/api/repo/add/", "application/json", (request, response) -> {
            boolean apiEnabled = Boolean.parseBoolean(Properties.getProperties().getProperty("api_enabled", "false"));
            boolean apiAuth = Boolean.parseBoolean(Properties.getProperties().getProperty("api_key_authentication", "true"));

            if (!apiEnabled) {
                return new ApiResponse(false, "API not enabled");
            }

            String publicKey = request.queryParams("pub");
            String signedKey = request.queryParams("sig");
            String reponames = request.queryParams("reponame");
            String repourls = request.queryParams("repourl");
            String repotype = request.queryParams("repotype");
            String repousername = request.queryParams("repousername");
            String repopassword = request.queryParams("repopassword");
            String reposource = request.queryParams("reposource");
            String repobranch = request.queryParams("repobranch");

            if (reponames == null || reponames.trim().equals(Values.EMPTYSTRING)) {
                return new ApiResponse(false, "reponame is a required parameter");
            }

            if (repourls == null || repourls.trim().equals(Values.EMPTYSTRING)) {
                return new ApiResponse(false, "repourl is a required parameter");
            }

            if (repotype == null) {
                return new ApiResponse(false, "repotype is a required parameter");
            }

            if (repousername == null) {
                return new ApiResponse(false, "repousername is a required parameter");
            }

            if (repopassword == null) {
                return new ApiResponse(false, "repopassword is a required parameter");
            }

            if (reposource == null) {
                return new ApiResponse(false, "reposource is a required parameter");
            }

            if (repobranch == null) {
                return new ApiResponse(false, "repobranch is a required parameter");
            }

            if (apiAuth) {
                if (publicKey == null || publicKey.trim().equals(Values.EMPTYSTRING)) {
                    return new ApiResponse(false, "pub is a required parameter");
                }

                if (signedKey == null || signedKey.trim().equals(Values.EMPTYSTRING)) {
                    return new ApiResponse(false, "sig is a required parameter");
                }

                String toValidate = String.format("pub=%s&reponame=%s&repourl=%s&repotype=%s&repousername=%s&repopassword=%s&reposource=%s&repobranch=%s",
                        URLEncoder.encode(publicKey),
                        URLEncoder.encode(reponames),
                        URLEncoder.encode(repourls),
                        URLEncoder.encode(repotype),
                        URLEncoder.encode(repousername),
                        URLEncoder.encode(repopassword),
                        URLEncoder.encode(reposource),
                        URLEncoder.encode(repobranch));

                boolean validRequest = apiService.validateRequest(publicKey, signedKey, toValidate);

                if (!validRequest) {
                    return new ApiResponse(false, "invalid signed url");
                }
            }


            // Clean
            if (repobranch == null || repobranch.trim().equals(Values.EMPTYSTRING)) {
                repobranch = "master";
            }

            repotype = repotype.trim().toLowerCase();
            if (!"git".equals(repotype) && !"svn".equals(repotype) && !"file".equals(repotype)) {
                repotype = "git";
            }

            RepoResult repoResult = repo.getRepoByName(reponames);

            if (repoResult != null) {
                return new ApiResponse(false, "repository name already exists");
            }

            repo.saveRepo(new RepoResult(-1, reponames, repotype, repourls, repousername, repopassword, reposource, repobranch));

            return new ApiResponse(true, "added repository successfully");
        }, new JsonTransformer());

        get("/api/repo/delete/", "application/json", (request, response) -> {
            boolean apiEnabled = Boolean.parseBoolean(Properties.getProperties().getProperty("api_enabled", "false"));
            boolean apiAuth = Boolean.parseBoolean(Properties.getProperties().getProperty("api_key_authentication", "true"));

            if (!apiEnabled) {
                return new ApiResponse(false, "API not enabled");
            }

            String publicKey = request.queryParams("pub");
            String signedKey = request.queryParams("sig");
            String reponames = request.queryParams("reponame");

            if (reponames == null || reponames.trim().equals(Values.EMPTYSTRING)) {
                return new ApiResponse(false, "reponame is a required parameter");
            }

            if (apiAuth) {
                if (publicKey == null || publicKey.trim().equals(Values.EMPTYSTRING)) {
                    return new ApiResponse(false, "pub is a required parameter");
                }

                if (signedKey == null || signedKey.trim().equals(Values.EMPTYSTRING)) {
                    return new ApiResponse(false, "sig is a required parameter");
                }

                String toValidate = String.format("pub=%s&reponame=%s",
                        URLEncoder.encode(publicKey),
                        URLEncoder.encode(reponames));

                boolean validRequest = apiService.validateRequest(publicKey, signedKey, toValidate);

                if (!validRequest) {
                    return new ApiResponse(false, "invalid signed url");
                }
            }

            RepoResult rr = repo.getRepoByName(reponames);
            if (rr == null) {
                return new ApiResponse(false, "repository already deleted");
            }

            Singleton.getUniqueDeleteRepoQueue().add(rr);

            return new ApiResponse(true, "repository queued for deletion");
        }, new JsonTransformer());

        get("/api/repo/list/", "application/json", (request, response) -> {
            boolean apiEnabled = Boolean.parseBoolean(Properties.getProperties().getProperty("api_enabled", "false"));
            boolean apiAuth = Boolean.parseBoolean(Properties.getProperties().getProperty("api_key_authentication", "true"));

            if (!apiEnabled) {
                return new ApiResponse(false, "API not enabled");
            }

            String publicKey = request.queryParams("pub");
            String signedKey = request.queryParams("sig");

            if (apiAuth) {
                if (publicKey == null || publicKey.trim().equals(Values.EMPTYSTRING)) {
                    return new ApiResponse(false, "pub is a required parameter");
                }

                if (signedKey == null || signedKey.trim().equals(Values.EMPTYSTRING)) {
                    return new ApiResponse(false, "sig is a required parameter");
                }

                String toValidate = String.format("pub=%s",
                        URLEncoder.encode(publicKey));

                boolean validRequest = apiService.validateRequest(publicKey, signedKey, toValidate);

                if (!validRequest) {
                    return new ApiResponse(false, "invalid signed url");
                }
            }

            List<RepoResult> repoResultList = repo.getAllRepo();

            return new RepoResultApiResponse(true, Values.EMPTYSTRING, repoResultList);
        }, new JsonTransformer());

        get("/api/repo/reindex/", "application/json", (request, response) -> {
            boolean apiEnabled = Boolean.parseBoolean(Properties.getProperties().getProperty("api_enabled", "false"));
            boolean apiAuth = Boolean.parseBoolean(Properties.getProperties().getProperty("api_key_authentication", "true"));

            if (!apiEnabled) {
                return new ApiResponse(false, "API not enabled");
            }

            String publicKey = request.queryParams("pub");
            String signedKey = request.queryParams("sig");

            if (apiAuth) {
                if (publicKey == null || publicKey.trim().equals(Values.EMPTYSTRING)) {
                    return new ApiResponse(false, "pub is a required parameter");
                }

                if (signedKey == null || signedKey.trim().equals(Values.EMPTYSTRING)) {
                    return new ApiResponse(false, "sig is a required parameter");
                }

                String toValidate = String.format("pub=%s",
                        URLEncoder.encode(publicKey));

                boolean validRequest = apiService.validateRequest(publicKey, signedKey, toValidate);

                if (!validRequest) {
                    return new ApiResponse(false, "invalid signed url");
                }
            }

            boolean result = js.rebuildAll();
            if (result) {
                js.forceEnqueue();
            }

            return new ApiResponse(result, "reindex forced");
        }, new JsonTransformer());

        get("/admin/", (request, response) -> {
            if(getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return null;
            }

            CodeSearcher cs = new CodeSearcher();

            Map<String, Object> map = new HashMap<>();

            map.put("repoCount", repo.getRepoCount());
            map.put("numDocs", cs.getTotalNumberDocumentsIndexed());

            map.put("numSearches", statsService.getSearchCount());
            map.put("uptime", statsService.getUptime());

            // Put all properties here
            map.put(Values.SQLITEFILE, Properties.getProperties().getProperty(Values.SQLITEFILE, Values.DEFAULTSQLITEFILE));
            map.put(Values.SERVERPORT, Properties.getProperties().getProperty(Values.SERVERPORT, Values.DEFAULTSERVERPORT));
            map.put(Values.REPOSITORYLOCATION, Properties.getProperties().getProperty(Values.REPOSITORYLOCATION, Values.DEFAULTREPOSITORYLOCATION));
            map.put(Values.INDEXLOCATION, Properties.getProperties().getProperty(Values.INDEXLOCATION, Values.DEFAULTINDEXLOCATION));
            map.put(Values.FACETSLOCATION, Properties.getProperties().getProperty(Values.FACETSLOCATION, Values.DEFAULTFACETSLOCATION));
            map.put(Values.CHECKREPOCHANGES, Properties.getProperties().getProperty(Values.CHECKREPOCHANGES, Values.DEFAULTCHECKREPOCHANGES));
            map.put(Values.CHECKFILEREPOCHANGES, Properties.getProperties().getProperty(Values.CHECKFILEREPOCHANGES, Values.DEFAULTCHECKFILEREPOCHANGES));
            map.put(Values.ONLYLOCALHOST, Properties.getProperties().getProperty(Values.ONLYLOCALHOST, Values.DEFAULTONLYLOCALHOST));
            map.put(Values.LOWMEMORY, Properties.getProperties().getProperty(Values.LOWMEMORY, Values.DEFAULTLOWMEMORY));
            map.put(Values.SPELLINGCORRECTORSIZE, Properties.getProperties().getProperty(Values.SPELLINGCORRECTORSIZE, Values.DEFAULTSPELLINGCORRECTORSIZE));
            map.put(Values.USESYSTEMGIT, Properties.getProperties().getProperty(Values.USESYSTEMGIT, Values.DEFAULTUSESYSTEMGIT));
            map.put(Values.GITBINARYPATH, Properties.getProperties().getProperty(Values.GITBINARYPATH, Values.DEFAULTGITBINARYPATH));
            map.put(Values.APIENABLED, Properties.getProperties().getProperty(Values.APIENABLED, Values.DEFAULTAPIENABLED));
            map.put(Values.APIKEYAUTH, Properties.getProperties().getProperty(Values.APIKEYAUTH, Values.DEFAULTAPIKEYAUTH));
            map.put(Values.SVNBINARYPATH, Properties.getProperties().getProperty(Values.SVNBINARYPATH, Values.DEFAULTSVNBINARYPATH));
            map.put(Values.SVNENABLED, Properties.getProperties().getProperty(Values.SVNENABLED, Values.DEFAULTSVNENABLED));
            map.put(Values.MAXDOCUMENTQUEUESIZE, Properties.getProperties().getProperty(Values.MAXDOCUMENTQUEUESIZE, Values.DEFAULTMAXDOCUMENTQUEUESIZE));
            map.put(Values.MAXDOCUMENTQUEUELINESIZE, Properties.getProperties().getProperty(Values.MAXDOCUMENTQUEUELINESIZE, Values.DEFAULTMAXDOCUMENTQUEUELINESIZE));
            map.put(Values.MAXFILELINEDEPTH, Properties.getProperties().getProperty(Values.MAXFILELINEDEPTH, Values.DEFAULTMAXFILELINEDEPTH));
            map.put(Values.OWASPDATABASELOCATION, Properties.getProperties().getProperty(Values.OWASPDATABASELOCATION, Values.DEFAULTOWASPDATABASELOCATION));
            map.put(Values.HIGHLIGHT_LINE_LIMIT, Properties.getProperties().getProperty(Values.HIGHLIGHT_LINE_LIMIT, Values.DEFAULT_HIGHLIGHT_LINE_LIMIT));
            map.put(Values.BINARY_WHITE_LIST, Properties.getProperties().getProperty(Values.BINARY_WHITE_LIST, Values.DEFAULT_BINARY_WHITE_LIST));
            map.put(Values.BINARY_BLACK_LIST, Properties.getProperties().getProperty(Values.BINARY_BLACK_LIST, Values.DEFAULT_BINARY_BLACK_LIST));

            map.put("deletionQueue", Singleton.getUniqueDeleteRepoQueue().size());
            map.put("version", VERSION);
            map.put("currentdatetime", new Date().toString());

            map.put("logoImage", getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "admin.ftl");
        }, new FreeMarkerEngine());

        get("/admin/repo/", (request, response) -> {
            if(getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return null;
            }

            int repoCount = repo.getRepoCount();
            String offSet = request.queryParams("offset");
            String searchQuery = request.queryParams("q");
            int indexOffset = 0;

            Map<String, Object> map = new HashMap<>();

            if (offSet != null) {
                try {
                    indexOffset = Integer.parseInt(offSet);
                    if (indexOffset > repoCount || indexOffset < 0) {
                        indexOffset = 0;
                    }
                }
                catch(NumberFormatException ex) {
                    indexOffset = 0;
                }
            }

            if (searchQuery != null) {
                map.put("repoResults", repo.searchRepo(searchQuery));
            }
            else {
                map.put("repoResults", repo.getPagedRepo(indexOffset, 100));
            }

            map.put("searchQuery", searchQuery);
            map.put("hasPrevious", indexOffset > 0);
            map.put("hasNext", (indexOffset + 100) < repoCount);
            map.put("previousOffset", "" + (indexOffset - 100));
            map.put("nextOffset", "" + (indexOffset + 100));

            map.put("logoImage", getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "admin_repo.ftl");
        }, new FreeMarkerEngine());

        get("/admin/bulk/", (request, response) -> {
            if(getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return null;
            }

            Map<String, Object> map = new HashMap<>();

            map.put("logoImage", getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "admin_bulk.ftl");
        }, new FreeMarkerEngine());

        get("/admin/api/", (request, response) -> {
            if (getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return null;
            }

            Map<String, Object> map = new HashMap<>();

            map.put("apiKeys", api.getAllApi());

            boolean apiEnabled = Boolean.parseBoolean(Properties.getProperties().getProperty("api_enabled", "false"));
            boolean apiAuth = Boolean.parseBoolean(Properties.getProperties().getProperty("api_key_authentication", "true"));

            map.put("apiAuthentication", apiEnabled && apiAuth);
            map.put("logoImage", getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "admin_api.ftl");
        }, new FreeMarkerEngine());

        post("/admin/api/", (request, response) -> {
            if (getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return null;
            }

            apiService.createKeys();

            response.redirect("/admin/api/");
            halt();
            return null;
        }, new FreeMarkerEngine());

        get("/admin/api/delete/", "application/json", (request, response) -> {
            if(getAuthenticatedUser(request) == null || !request.queryParams().contains("publicKey")) {
                response.redirect("/login/");
                halt();
                return false;
            }

            String publicKey = request.queryParams("publicKey");
            apiService.deleteKey(publicKey);

            return true;
        }, new JsonTransformer());

        get("/admin/settings/", (request, response) -> {
            if(getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return null;
            }

            String[] highlighters = "agate,androidstudio,arta,ascetic,atelier-cave.dark,atelier-cave.light,atelier-dune.dark,atelier-dune.light,atelier-estuary.dark,atelier-estuary.light,atelier-forest.dark,atelier-forest.light,atelier-heath.dark,atelier-heath.light,atelier-lakeside.dark,atelier-lakeside.light,atelier-plateau.dark,atelier-plateau.light,atelier-savanna.dark,atelier-savanna.light,atelier-seaside.dark,atelier-seaside.light,atelier-sulphurpool.dark,atelier-sulphurpool.light,brown_paper,codepen-embed,color-brewer,dark,darkula,default,docco,far,foundation,github-gist,github,googlecode,grayscale,hopscotch,hybrid,idea,ir_black,kimbie.dark,kimbie.light,magula,mono-blue,monokai,monokai_sublime,obsidian,paraiso.dark,paraiso.light,pojoaque,railscasts,rainbow,school_book,solarized_dark,solarized_light,sunburst,tomorrow-night-blue,tomorrow-night-bright,tomorrow-night-eighties,tomorrow-night,tomorrow,vs,xcode,zenburn".split(",");

            Map<String, Object> map = new HashMap<>();
            map.put("logoImage", getLogo());
            map.put("syntaxHighlighter", getSyntaxHighlighter());
            map.put("highlighters", highlighters);
            map.put("averageSalary", "" + (int)getAverageSalary());
            map.put("matchLines", "" + (int)getMatchLines());
            map.put("maxLineDepth", "" + (int)getMaxLineDepth());
            map.put("minifiedLength", "" + (int)getMinifiedLength());
            map.put("owaspenabled", owaspAdvisoriesEnabled());
            map.put("isCommunity", ISCOMMUNITY);

            return new ModelAndView(map, "admin_settings.ftl");
        }, new FreeMarkerEngine());

        get("/admin/reports/", (request, response) -> {
            if(getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return null;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("logoImage", getLogo());
            map.put("isCommunity", ISCOMMUNITY);

            return new ModelAndView(map, "admin_reports.ftl");
        }, new FreeMarkerEngine());

        get("/admin/logs/", (request, response) -> {
            if(getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return null;
            }

            Map<String, Object> map = new HashMap<>();
            String level = Properties.getProperties().getOrDefault("log_level", "SEVERE").toString().toUpperCase();

            if(request.queryParams().contains("level") && !request.queryParams("level").trim().equals("")) {
                level = request.queryParams("level").trim().toUpperCase();
            }

            List<String> logs = new ArrayList<>();
            switch(level) {
                case "INFO":
                    logs = Singleton.getLogger().getInfoLogs();
                    break;
                case "WARNING":
                    logs = Singleton.getLogger().getWarningLogs();
                    break;
                case "ALL":
                    logs = Singleton.getLogger().getAllLogs();
                    break;
                case "SEVERE":
                default:
                    logs = Singleton.getLogger().getSevereLogs();
                    break;
            }

            map.put("level", level);
            map.put("logs", logs.size() > 1000 ? logs.subList(0,1000) : logs);
            map.put("logoImage", getLogo());
            map.put("isCommunity", ISCOMMUNITY);

            return new ModelAndView(map, "admin_logs.ftl");
        }, new FreeMarkerEngine());

        post("/admin/settings/", (request, response) -> {
            if (getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return null;
            }

            if(ISCOMMUNITY) {
                response.redirect("/admin/settings/");
                halt();
            }

            String logo = request.queryParams("logo").trim();
            String syntaxHighlighter = request.queryParams("syntaxhighligher");

            try {
                double averageSalary = Double.parseDouble(request.queryParams("averagesalary"));
                data.saveData(Values.AVERAGESALARY, "" + (int)averageSalary);
            }
            catch(NumberFormatException ex) {
                data.saveData(Values.AVERAGESALARY, Values.DEFAULTAVERAGESALARY);
            }

            try {
                double averageSalary = Double.parseDouble(request.queryParams("matchlines"));
                data.saveData(Values.MATCHLINES, "" + (int)averageSalary);
            }
            catch(NumberFormatException ex) {
                data.saveData(Values.MATCHLINES, Values.DEFAULTMATCHLINES);
            }

            try {
                double averageSalary = Double.parseDouble(request.queryParams("maxlinedepth"));
                data.saveData(Values.MAXLINEDEPTH, "" + (int)averageSalary);
            }
            catch(NumberFormatException ex) {
                data.saveData(Values.MAXLINEDEPTH, Values.DEFAULTMAXLINEDEPTH);
            }

            try {
                double minifiedlength = Double.parseDouble(request.queryParams("minifiedlength"));
                data.saveData(Values.MINIFIEDLENGTH, "" + (int)minifiedlength);
            }
            catch(NumberFormatException ex) {
                data.saveData(Values.MINIFIEDLENGTH, Values.DEFAULTMINIFIEDLENGTH);
            }

            boolean owaspadvisories = Boolean.parseBoolean(request.queryParams("owaspadvisories"));
            data.saveData(Values.OWASPENABLED, "" + owaspadvisories);

            data.saveData(Values.LOGO, logo);
            data.saveData(Values.SYNTAXHIGHLIGHTER, syntaxHighlighter);

            // Redo anything that requires updates at this point
            scl = Singleton.getSearchcodeLib(data);

            response.redirect("/admin/settings/");
            halt();
            return null;
        }, new FreeMarkerEngine());

        post("/admin/bulk/", (request, response) -> {
            if (getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return null;
            }

            String repos = request.queryParams("repos");
            String repolines[] = repos.split("\\r?\\n");

            for(String line: repolines) {
                String[] repoparams = line.split(",", -1);

                if(repoparams.length == 7) {

                    String branch = repoparams[6].trim();
                    if (branch.equals(Values.EMPTYSTRING)) {
                        branch = "master";
                    }

                    String scm = repoparams[1].trim().toLowerCase();
                    if(scm.equals(Values.EMPTYSTRING)) {
                        scm = "git";
                    }

                    RepoResult rr = repo.getRepoByName(repoparams[0]);

                    if (rr == null) {
                        repo.saveRepo(new RepoResult(-1, repoparams[0], scm, repoparams[2], repoparams[3], repoparams[4], repoparams[5], branch));
                    }
                }
            }

            response.redirect("/admin/bulk/");
            halt();
            return null;
        }, new FreeMarkerEngine());

        post("/admin/repo/", (request, response) -> {
            if (getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return null;
            }

            String[] reponames = request.queryParamsValues("reponame");
            String[] reposcms = request.queryParamsValues("reposcm");
            String[] repourls = request.queryParamsValues("repourl");
            String[] repousername = request.queryParamsValues("repousername");
            String[] repopassword = request.queryParamsValues("repopassword");
            String[] reposource = request.queryParamsValues("reposource");
            String[] repobranch = request.queryParamsValues("repobranch");


            for(int i=0;i<reponames.length; i++) {
                if(reponames[i].trim().length() != 0) {

                    String branch = repobranch[i].trim();
                    if (branch.equals(Values.EMPTYSTRING)) {
                        branch = "master";
                    }

                    repo.saveRepo(new RepoResult(-1, reponames[i], reposcms[i], repourls[i], repousername[i], repopassword[i], reposource[i], branch));
                }
            }

            response.redirect("/admin/repo/");
            halt();
            return null;
        }, new FreeMarkerEngine());

        get("/login/", (request, response) -> {
            if(getAuthenticatedUser(request) != null) {
                response.redirect("/admin/");
                halt();
                return null;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("logoImage", getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "login.ftl");
        }, new FreeMarkerEngine());

        post("/login/", (req, res) -> {
            if(req.queryParams().contains("password") && req.queryParams("password").equals(com.searchcode.app.util.Properties.getProperties().getProperty("password"))) {
                addAuthenticatedUser(req);
                res.redirect("/admin/");
                halt();
            }
            Map<String, Object> map = new HashMap<>();
            map.put("logoImage", getLogo());
            map.put("isCommunity", ISCOMMUNITY);

            if (req.queryParams().contains("password")) {
                map.put("passwordInvalid", true);
            }

            return new ModelAndView(map, "login.ftl");
        }, new FreeMarkerEngine());

        get("/logout/", (req, res) -> {
            removeAuthenticatedUser(req);
            res.redirect("/");
            return null;
        });

        get("/admin/delete/", "application/json", (request, response) -> {
            if(getAuthenticatedUser(request) == null || !request.queryParams().contains("repoName")) {
                response.redirect("/login/");
                halt();
                return false;
            }

            String repoName = request.queryParams("repoName");
            RepoResult rr = repo.getRepoByName(repoName);

            if (rr != null) {
                Singleton.getUniqueDeleteRepoQueue().add(rr);
            }

            return true;
        }, new JsonTransformer());

        post("/admin/rebuild/", "application/json", (request, response) -> {
            if (getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return false;
            }

            boolean result = js.rebuildAll();
            if (result) {
                js.forceEnqueue();
            }
            return result;
        }, new JsonTransformer());

        post("/admin/forcequeue/", "application/json", (request, response) -> {
            if (getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return false;
            }

            return js.forceEnqueue();
        }, new JsonTransformer());

        get("/admin/checkversion/", "application/json", (request, response) -> {
            if(getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return false;
            }

            String version;
            try {
                version = IOUtils.toString(new URL("https://searchcode.com/product/version/")).replace("\"", Values.EMPTYSTRING);
            }
            catch(IOException ex) {
                return "Unable to determine if running the latest version. Check https://searchcode.com/product/download/ for the latest release.";
            }

            if (App.VERSION.equals(version)) {
                return "Your searchcode server version " + version + " is the latest.";
            }
            else {
                return "Your searchcode server version " + App.VERSION + " instance is out of date. The latest version is " + version + ".";
            }
        }, new JsonTransformer());


        get("/file/:codeid/:reponame/*", (request, response) -> {
            Map<String, Object> map = new HashMap<>();

            CodeSearcher cs = new CodeSearcher();
            Cocomo2 coco = new Cocomo2();

            String fileName = Values.EMPTYSTRING;
            if(request.splat().length != 0) {
                fileName = request.splat()[0];
            }

            String codeId = request.params(":codeid");
            CodeResult codeResult = cs.getByCodeId(codeId);

            if (codeResult == null) {
                try {
                    int codeid = Integer.parseInt(request.params(":codeid"));
                    codeResult = cs.getById(codeid);
                }
                catch(NumberFormatException ex) {}
            }

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
            if (owaspAdvisoriesEnabled()) {
                if (!codeResult.languageName.equals("Text") && !codeResult.languageName.equals("Unknown")) {
                    owaspResults = owaspClassifier.classifyCode(codeLines, codeResult.languageName);
                }
            }

            int limit = Integer.parseInt(
                    Properties.getProperties().getProperty(
                            Values.HIGHLIGHT_LINE_LIMIT, Values.DEFAULT_HIGHLIGHT_LINE_LIMIT));
            boolean highlight = Integer.parseInt(codeResult.codeLines) <= limit;

            RepoResult repoResult = repo.getRepoByName(codeResult.repoName);

            if (repoResult != null) {
                map.put("source", repoResult.getSource());
            }

            map.put("fileName", codeResult.fileName);

            // TODO fix this properly code path includes the repo name and should be removed
            String codePath = codeResult.codePath.substring(codeResult.codePath.indexOf('/'), codeResult.codePath.length());
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
            map.put("highligher", getSyntaxHighlighter());
            map.put("codeOwner", codeResult.getCodeOwner());
            map.put("owaspResults", owaspResults);

            double estimatedEffort = coco.estimateEffort(scl.countFilteredLines(codeResult.getCode()));
            int estimatedCost = (int)coco.estimateCost(estimatedEffort, getAverageSalary());
            if (estimatedCost != 0 && !scl.languageCostIgnore(codeResult.getLanguageName())) {
                map.put("estimatedCost", estimatedCost);
            }

            map.put("logoImage", getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "coderesult.ftl");
        }, new FreeMarkerEngine());


        /**
         * Deprecated should not be used
         * TODO delete this method
         */
        get("/codesearch/view/:codeid", (request, response) -> {
            Map<String, Object> map = new HashMap<>();

            int codeid = Integer.parseInt(request.params(":codeid"));
            CodeSearcher cs = new CodeSearcher();
            Cocomo2 coco = new Cocomo2();

            StringBuilder code = new StringBuilder();

            // escape all the lines and include deeplink for line number
            CodeResult codeResult = cs.getById(codeid);

            if (codeResult == null) {
                response.redirect("/404/");
                halt();
            }

            List<String> codeLines = codeResult.code;
            for (int i = 0; i < codeLines.size(); i++) {
                code.append("<span id=\"" + (i + 1) + "\"></span>");
                code.append(StringEscapeUtils.escapeHtml4(codeLines.get(i)));
                code.append("\n");
            }

            int limit = Integer.parseInt(
                    Properties.getProperties().getProperty(
                            Values.HIGHLIGHT_LINE_LIMIT, Values.DEFAULT_HIGHLIGHT_LINE_LIMIT));
            boolean highlight = Integer.parseInt(codeResult.codeLines) <= limit;

            RepoResult repoResult = repo.getRepoByName(codeResult.repoName);

            if (repoResult != null) {
                map.put("source", repoResult.getSource());
            }

            map.put("fileName", codeResult.fileName);
            map.put("codePath", codeResult.codePath);
            map.put("codeLength", codeResult.codeLines);
            map.put("languageName", codeResult.languageName);
            map.put("md5Hash", codeResult.md5hash);
            map.put("repoName", codeResult.repoName);
            map.put("highlight", highlight);
            map.put("repoLocation", codeResult.getRepoLocation());

            map.put("codeValue", code.toString());
            map.put("highligher", getSyntaxHighlighter());
            map.put("codeOwner", codeResult.getCodeOwner());

            double estimatedEffort = coco.estimateEffort(scl.countFilteredLines(codeResult.getCode()));
            int estimatedCost = (int) coco.estimateCost(estimatedEffort, getAverageSalary());
            if (estimatedCost != 0 && !scl.languageCostIgnore(codeResult.getLanguageName())) {
                map.put("estimatedCost", estimatedCost);
            }

            map.put("logoImage", getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "coderesult.ftl");
        }, new FreeMarkerEngine());


        get("/documentation/", (request, response) -> {
            Map<String, Object> map = new HashMap<>();

            map.put("logoImage", getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "documentation.ftl");
        }, new FreeMarkerEngine());

        get("/search_test/", (request, response) -> {
            Map<String, Object> map = new HashMap<>();

            map.put("logoImage", getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "search_test.ftl");
        }, new FreeMarkerEngine());

        get("/404/", (request, response) -> {
            Map<String, Object> map = new HashMap<>();

            map.put("logoImage", getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "404.ftl");

        }, new FreeMarkerEngine());

        /**
         * Test that was being used to display blame information
         */
//        get("/test/:reponame/*", (request, response) -> {
//            User user = injector.getInstance(User.class);
//            user.Blame(request.params(":reponame"), request.splat()[0]);
//            return "";
//        }, new JsonTransformer());
    }

    private static String getLogo() {
        if(ISCOMMUNITY) {
            return "";
        }

        Data data = injector.getInstance(Data.class);
        return data.getDataByName(Values.LOGO, Values.EMPTYSTRING);
    }

    public static double getAverageSalary() {
        if(ISCOMMUNITY) {
            return Double.parseDouble(Values.DEFAULTAVERAGESALARY);
        }

        Data data = injector.getInstance(Data.class);
        String salary = data.getDataByName(Values.AVERAGESALARY);

        if(salary == null) {
            data.saveData(Values.AVERAGESALARY, Values.DEFAULTAVERAGESALARY);
            salary = Values.DEFAULTAVERAGESALARY;
        }

        return Double.parseDouble(salary);
    }

    public static double getMatchLines() {
        if(ISCOMMUNITY) {
            return Double.parseDouble(Values.DEFAULTMATCHLINES);
        }

        Data data = injector.getInstance(Data.class);
        String matchLines = data.getDataByName(Values.MATCHLINES);

        if(matchLines == null) {
            data.saveData(Values.MATCHLINES, Values.DEFAULTMATCHLINES);
            matchLines = Values.DEFAULTMATCHLINES;
        }

        return Double.parseDouble(matchLines);
    }

    public static double getMaxLineDepth() {
        if(ISCOMMUNITY) {
            return Double.parseDouble(Values.DEFAULTMAXLINEDEPTH);
        }

        Data data = injector.getInstance(Data.class);
        String matchLines = data.getDataByName(Values.MAXLINEDEPTH);

        if(matchLines == null) {
            data.saveData(Values.MAXLINEDEPTH, Values.DEFAULTMAXLINEDEPTH);
            matchLines = Values.DEFAULTMAXLINEDEPTH;
        }

        return Double.parseDouble(matchLines);
    }

    public static double getMinifiedLength() {
        if(ISCOMMUNITY) {
            return Double.parseDouble(Values.DEFAULTMINIFIEDLENGTH);
        }

        Data data = injector.getInstance(Data.class);
        String minifiedLength = data.getDataByName(Values.MINIFIEDLENGTH);

        if(minifiedLength == null) {
            data.saveData(Values.MINIFIEDLENGTH, Values.DEFAULTMINIFIEDLENGTH);
            minifiedLength = Values.DEFAULTMINIFIEDLENGTH;
        }

        return Double.parseDouble(minifiedLength);
    }

    public static boolean owaspAdvisoriesEnabled() {
        if(ISCOMMUNITY) {
            return false;
        }

        Data data = injector.getInstance(Data.class);
        Boolean owaspEnabled = Boolean.parseBoolean(data.getDataByName(Values.OWASPENABLED));

        if(owaspEnabled == null) {
            data.saveData(Values.OWASPENABLED, "false");
            owaspEnabled = false;
        }

        return owaspEnabled;
    }

    public static String getSyntaxHighlighter() {
        if(ISCOMMUNITY) {
            return Values.DEFAULTSYNTAXHIGHLIGHTER;
        }

        Data data = injector.getInstance(Data.class);
        String highlighter = data.getDataByName(Values.SYNTAXHIGHLIGHTER);

        if(highlighter == null || highlighter.trim().equals("")) {
            highlighter = Properties.getProperties().getProperty(Values.SYNTAXHIGHLIGHTER, Values.DEFAULTSYNTAXHIGHLIGHTER);
            data.saveData(Values.SYNTAXHIGHLIGHTER, highlighter);
        }

        return highlighter;
    }

    /**
     * Called on startup to run all the DAO object table creation/migration logic. Slight overhead using this technique.
     * TODO Do the migrations inside the sqlite database so the application does not need to
     */
    public static void databaseMigrations() {
        Data data = injector.getInstance(Data.class);
        Repo repo = injector.getInstance(Repo.class);
        Api api = injector.getInstance(Api.class);

        data.createTableIfMissing(); // Added data key/value table
        repo.addSourceToTable(); // Added source to repo
        repo.addBranchToTable(); // Add branch to repo
        api.createTableIfMissing();
    }

    /**
     * Used to know what week of the month it is to display a different image on the main page
     */
    private static int getWeekOfMonth() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.WEEK_OF_MONTH);
    }

    private static void addAuthenticatedUser(Request request) {
        request.session().attribute(Values.USERSESSIONID, true);
    }

    private static void removeAuthenticatedUser(Request request) {
        request.session().removeAttribute(Values.USERSESSIONID);
    }

    private static Object getAuthenticatedUser(Request request) {
        return request.session().attribute(Values.USERSESSIONID);
    }
}
