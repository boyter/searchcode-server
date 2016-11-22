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
    public static Map<String, SearchResult> cache = ExpiringMap.builder()
                                                               .expirationPolicy(ExpirationPolicy.ACCESSED)
                                                               .expiration(60, TimeUnit.SECONDS)
                                                               .build();
    public static Injector injector;
    public static SearchcodeLib scl;

    public static void main( String[] args ) {
        injector = Guice.createInjector(new InjectorConfig());
        int server_port = Helpers.tryParseInt(Properties.getProperties().getProperty(Values.SERVERPORT, Values.DEFAULTSERVERPORT), Values.DEFAULTSERVERPORT);
        boolean onlyLocalhost = Boolean.parseBoolean(Properties.getProperties().getProperty("only_localhost", "false"));

        // Database migrations happen before we start
        databaseMigrations();

        LOGGER.info("Starting searchcode server on port " + server_port);

        if (onlyLocalhost) {
            LOGGER.info("Only listening on 127.0.0.1 ");
            Spark.ipAddress("127.0.0.1");
        }
        Spark.port(server_port);

        JobService js = injector.getInstance(JobService.class);

        Repo repo = Singleton.getRepo();
        Data data = Singleton.getData();
        ApiService apiService = Singleton.getApiService();

        OWASPClassifier owaspClassifier = new OWASPClassifier();

        scl = Singleton.getSearchcodeLib(data);
        js.initialJobs();

        Gson gson = new Gson();

        Spark.staticFileLocation("/public");

        before((request, response) -> {
            if (onlyLocalhost) {
                if (!request.ip().equals("127.0.0.1")) {
                    halt(204);
                }
            }
        });

        get("/", (request, response) -> {
            response.header("Content-Encoding", "gzip");
            Map<String, Object> map = new HashMap<>();

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
                    String[] repos = new String[0];
                    repos = request.queryParamsValues("repo");

                    if (repos.length != 0) {
                        reposList = Arrays.asList(repos);
                    }
                }

                if (request.queryParams().contains("lan")) {
                    String[] langs = new String[0];
                    langs = request.queryParamsValues("lan");

                    if (langs.length != 0) {
                        langsList = Arrays.asList(langs);
                    }
                }

                if (request.queryParams().contains("own")) {
                    String[] owns = new String[0];
                    owns = request.queryParamsValues("own");

                    if (owns.length != 0) {
                        ownsList = Arrays.asList(owns);
                    }
                }

                map.put("searchValue", query);
                map.put("searchResultJson", gson.toJson(new CodePreload(query, page, langsList, reposList, ownsList)));


                map.put("logoImage", CommonRouteService.getLogo());
                map.put("isCommunity", ISCOMMUNITY);
                return new ModelAndView(map, "search_test.ftl");
            }

            CodeSearcher cs = new CodeSearcher();

            map.put("photoId", CommonRouteService.getPhotoId());
            map.put("numDocs", cs.getTotalNumberDocumentsIndexed());
            map.put("logoImage", CommonRouteService.getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "index.ftl");
        }, new FreeMarkerEngine());

        get("/html/", (request, response) -> {
            response.header("Content-Encoding", "gzip");
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
                map.put("isCommunity", ISCOMMUNITY);
                return new ModelAndView(map, "searchresults.ftl");
            }

            map.put("photoId", CommonRouteService.getPhotoId());
            map.put("numDocs", cs.getTotalNumberDocumentsIndexed());
            map.put("logoImage", CommonRouteService.getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "index.ftl");
        }, new FreeMarkerEngine());

        /**
         * Allows one to write literal lucene search queries against the index
         * TODO This is still very much WIP
         */
        get("/literal/", (request, response) -> {
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
                map.put("isCommunity", ISCOMMUNITY);
                return new ModelAndView(map, "searchresults.ftl");
            }

            map.put("photoId", 1);
            map.put("numDocs", cs.getTotalNumberDocumentsIndexed());
            map.put("logoImage", CommonRouteService.getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "index.ftl");
        }, new FreeMarkerEngine());


        get("/file/:codeid/:reponame/*", (request, response) -> {
            Map<String, Object> map = new HashMap<>();

            CodeSearcher cs = new CodeSearcher();
            Cocomo2 coco = new Cocomo2();

            String fileName = Values.EMPTYSTRING;
            if (request.splat().length != 0) {
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
            if (CommonRouteService.owaspAdvisoriesEnabled()) {
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
            map.put("highligher", CommonRouteService.getSyntaxHighlighter());
            map.put("codeOwner", codeResult.getCodeOwner());
            map.put("owaspResults", owaspResults);

            double estimatedEffort = coco.estimateEffort(scl.countFilteredLines(codeResult.getCode()));
            int estimatedCost = (int)coco.estimateCost(estimatedEffort, CommonRouteService.getAverageSalary());
            if (estimatedCost != 0 && !scl.languageCostIgnore(codeResult.getLanguageName())) {
                map.put("estimatedCost", estimatedCost);
            }

            map.put("logoImage", CommonRouteService.getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "coderesult.ftl");
        }, new FreeMarkerEngine());

        get("/documentation/", (request, response) -> {
            Map<String, Object> map = new HashMap<>();

            map.put("logoImage", CommonRouteService.getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "documentation.ftl");
        }, new FreeMarkerEngine());

        get("/404/", (request, response) -> {
            Map<String, Object> map = new HashMap<>();

            map.put("logoImage", CommonRouteService.getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "404.ftl");

        }, new FreeMarkerEngine());

        ////////////////////////////////////////////////////
        //              API Routes Below
        ////////////////////////////////////////////////////

        get("/api/codesearch/", (request, response) -> {
            // This is the endpoint used by the frontend for AJAX search
            response.header("Content-Encoding", "gzip");
            response.header("Content-Type", "application/json");
            SearchRouteService searchRouteService = new SearchRouteService();
            return searchRouteService.CodeSearch(request, response);
        }, new JsonTransformer());


        get("/api/timecodesearch/", (request, response) -> {
            TimeSearchRouteService ars = new TimeSearchRouteService();
            return ars.getTimeSearch(request, response);
        }, new JsonTransformer());

        get("/api/repo/add/", "application/json", (request, response) -> {
            response.header("Content-Type", "application/json");
            ApiRouteService apiRouteService = new ApiRouteService();

            return apiRouteService.RepoAdd(request, response);

        }, new JsonTransformer());

        get("/api/repo/delete/", "application/json", (request, response) -> {
            response.header("Content-Type", "application/json");
            ApiRouteService apiRouteService = new ApiRouteService();

            return apiRouteService.RepoDelete(request, response);
        }, new JsonTransformer());

        get("/api/repo/list/", "application/json", (request, response) -> {
            response.header("Content-Type", "application/json");
            ApiRouteService apiRouteService = new ApiRouteService();

            return apiRouteService.RepoList(request, response);
        }, new JsonTransformer());

        get("/api/repo/reindex/", "application/json", (request, response) -> {
            response.header("Content-Type", "application/json");
            ApiRouteService apiRouteService = new ApiRouteService();

            return apiRouteService.RepositoryReindex(request, response);
        }, new JsonTransformer());

        ////////////////////////////////////////////////////
        //              Admin Routes Below
        ////////////////////////////////////////////////////

        get("/admin/", (request, response) -> {
            if (getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return null;
            }

            AdminRouteService adminRouteService = new AdminRouteService();
            Map<String, Object> map = adminRouteService.AdminPage(request, response);

            return new ModelAndView(map, "admin.ftl");
        }, new FreeMarkerEngine());

        get("/admin/repo/", (request, response) -> {
            if (getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return null;
            }

            AdminRouteService adminRouteService = new AdminRouteService();
            Map<String, Object> map = adminRouteService.AdminRepo(request, response);

            return new ModelAndView(map, "admin_repo.ftl");
        }, new FreeMarkerEngine());

        get("/admin/bulk/", (request, response) -> {
            if (getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return null;
            }

            Map<String, Object> map = new HashMap<>();

            map.put("logoImage", CommonRouteService.getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            return new ModelAndView(map, "admin_bulk.ftl");
        }, new FreeMarkerEngine());

        get("/admin/api/", (request, response) -> {
            if (getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return null;
            }

            AdminRouteService adminRouteService = new AdminRouteService();
            Map<String, Object> map = adminRouteService.AdminApi(request, response);

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
            if (getAuthenticatedUser(request) == null || !request.queryParams().contains("publicKey")) {
                response.redirect("/login/");
                halt();
                return false;
            }

            String publicKey = request.queryParams("publicKey");
            apiService.deleteKey(publicKey);

            return true;
        }, new JsonTransformer());

        get("/admin/settings/", (request, response) -> {
            if (getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return null;
            }

            AdminRouteService adminRouteService = new AdminRouteService();
            Map<String, Object> map = adminRouteService.AdminSettings(request, response);

            return new ModelAndView(map, "admin_settings.ftl");
        }, new FreeMarkerEngine());

        get("/admin/logs/", (request, response) -> {
            if (getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return null;
            }

            AdminRouteService adminRouteService = new AdminRouteService();
            Map<String, Object> map = adminRouteService.AdminLogs(request, response);

            return new ModelAndView(map, "admin_logs.ftl");
        }, new FreeMarkerEngine());

        post("/admin/settings/", (request, response) -> {
            if (getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return null;
            }

            if (ISCOMMUNITY) {
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
            if (getAuthenticatedUser(request) != null) {
                response.redirect("/admin/");
                halt();
                return null;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("logoImage", CommonRouteService.getLogo());
            map.put("isCommunity", ISCOMMUNITY);

            return new ModelAndView(map, "login.ftl");
        }, new FreeMarkerEngine());

        post("/login/", (request, response) -> {
            if (request.queryParams().contains("password") && request.queryParams("password").equals(com.searchcode.app.util.Properties.getProperties().getProperty("password"))) {
                addAuthenticatedUser(request);
                response.redirect("/admin/");
                halt();
            }

            Map<String, Object> map = new HashMap<>();
            map.put("logoImage", CommonRouteService.getLogo());
            map.put("isCommunity", ISCOMMUNITY);

            if (request.queryParams().contains("password")) {
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
            if (getAuthenticatedUser(request) == null || !request.queryParams().contains("repoName")) {
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
            if (getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return false;
            }

            AdminRouteService adminRouteService = new AdminRouteService();
            return adminRouteService.CheckVersion();
        }, new JsonTransformer());
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
