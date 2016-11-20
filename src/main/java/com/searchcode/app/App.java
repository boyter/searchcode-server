/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.5
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
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import spark.ModelAndView;
import spark.Request;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

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

    public static final boolean ISCOMMUNITY = false;
    public static final String VERSION = "1.3.5";
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

        Repo repo = Singleton.getRepo();
        Data data = Singleton.getData();
        Api api = Singleton.getApi();
        ApiService apiService = Singleton.getApiService();

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

            map.put("photoId", 1);
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
            res.header("Content-Type", "application/json");
            CodeSearcher cs = new CodeSearcher();
            CodeMatcher cm = new CodeMatcher(data);

            if(req.queryParams().contains("q") && req.queryParams("q").trim() != Values.EMPTYSTRING) {
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
            TimeSearchRouteService ars = new TimeSearchRouteService();
            return ars.getTimeSearch(request, response);
        }, new JsonTransformer());

        get("/api/repo/add/", "application/json", (request, response) -> {
            response.header("Content-Type", "application/json");

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
            response.header("Content-Type", "application/json");

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
            response.header("Content-Type", "application/json");

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
            response.header("Content-Type", "application/json");
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

            AdminRouteService ars = new AdminRouteService();
            CodeSearcher cs = new CodeSearcher();
            Map<String, Object> map = ars.AdminPage(request, response);


            map.put("repoCount", repo.getRepoCount());
            map.put("numDocs", cs.getTotalNumberDocumentsIndexed());
            map.put("numSearches", statsService.getSearchCount());
            map.put("uptime", statsService.getUptime());
            map.put("loadAverage", statsService.getLoadAverage());
            map.put("sysArch", statsService.getArch());
            map.put("sysVersion", statsService.getOsVersion());
            map.put("processorCount", statsService.getProcessorCount());
            map.put("memoryUsage", statsService.getMemoryUsage("<br>"));
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

            AdminRouteService adminRouteService = new AdminRouteService();
            Map<String, Object> map = adminRouteService.AdminSettings(request, response);

            map.put("logoImage", getLogo());
            map.put("syntaxHighlighter", getSyntaxHighlighter());
            map.put("averageSalary", "" + (int)getAverageSalary());
            map.put("matchLines", "" + (int)getMatchLines());
            map.put("maxLineDepth", "" + (int)getMaxLineDepth());
            map.put("minifiedLength", "" + (int)getMinifiedLength());
            map.put("owaspenabled", owaspAdvisoriesEnabled());
            map.put("backoffValue", (double) getBackoffValue());
            map.put("isCommunity", App.ISCOMMUNITY);

            return new ModelAndView(map, "admin_settings.ftl");
        }, new FreeMarkerEngine());

        get("/admin/logs/", (request, response) -> {
            if(getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return null;
            }

            AdminRouteService adminRouteService = new AdminRouteService();
            Map<String, Object> map = adminRouteService.AdminLogs(request, response);

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

            AdminRouteService adminRouteService = new AdminRouteService();
            adminRouteService.PostSettings(request, response);

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

            AdminRouteService adminRouteService = new AdminRouteService();
            adminRouteService.PostBulk(request, response);

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

            AdminRouteService adminRouteService = new AdminRouteService();
            adminRouteService.PostRepo(request, response);

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

        post("/admin/togglepause/", "application/json", (request, response) -> {
            if (getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return false;
            }
            Singleton.setPauseBackgroundJobs(!Singleton.getPauseBackgroundJobs());
            return Singleton.getPauseBackgroundJobs();
        }, new JsonTransformer());

        get("/admin/checkversion/", "application/json", (request, response) -> {
            if(getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return false;
            }

            AdminRouteService adminRouteService = new AdminRouteService();
            return adminRouteService.CheckVersion();
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

        get("/documentation/", (request, response) -> {
            Map<String, Object> map = new HashMap<>();

            map.put("logoImage", getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "documentation.ftl");
        }, new FreeMarkerEngine());

        get("/404/", (request, response) -> {
            Map<String, Object> map = new HashMap<>();

            map.put("logoImage", getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "404.ftl");

        }, new FreeMarkerEngine());
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

    public static double getBackoffValue() {
        if(ISCOMMUNITY) {
            return Double.parseDouble(Values.DEFAULTBACKOFFVALUE);
        }

        Data data = injector.getInstance(Data.class);
        String backoffValue = data.getDataByName(Values.BACKOFFVALUE);

        if(backoffValue == null) {
            data.saveData(Values.BACKOFFVALUE, Values.DEFAULTBACKOFFVALUE);
            backoffValue = Values.DEFAULTBACKOFFVALUE;
        }

        return Double.parseDouble(backoffValue);
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
