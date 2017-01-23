/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.6
 */

package com.searchcode.app;

import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Api;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dao.Repo;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.*;
import com.searchcode.app.service.route.*;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.JsonTransformer;
import com.searchcode.app.util.LoggerWrapper;
import com.searchcode.app.util.Properties;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

/**
 * Main entry point for the application.
 */
public class App {

    public static final boolean ISCOMMUNITY = false;
    public static final String VERSION = "1.3.6";
    private static final LoggerWrapper LOGGER = Singleton.getLogger();

    public static void main (String[] args) {
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

        JobService js = new JobService();

        ApiService apiService = Singleton.getApiService();
        js.initialJobs();

        Spark.staticFileLocation("/public");

        before((request, response) -> {
            if (onlyLocalhost) {
                if (!request.ip().equals("127.0.0.1")) {
                    halt(204);
                }
            }
        });

        ////////////////////////////////////////////////////
        //          Search/Code Routes Below
        ////////////////////////////////////////////////////

        get("/", (request, response) -> {
            response.header("Content-Encoding", "gzip");
            CodeRouteService codeRouteService = new CodeRouteService();
            return codeRouteService.root(request, response);

        }, new FreeMarkerEngine());

        get("/html/", (request, response) -> {
            response.header("Content-Encoding", "gzip");
            CodeRouteService codeRouteService = new CodeRouteService();
            return codeRouteService.html(request, response);
        }, new FreeMarkerEngine());

        get("/literal/", (request, response) -> {
            CodeRouteService codeRouteService = new CodeRouteService();
            return new ModelAndView(codeRouteService.literalSearch(request, response), "index.ftl");
        }, new FreeMarkerEngine());

        get("/file/:codeid/:reponame/*", (request, response) -> {
            response.header("Content-Encoding", "gzip");
            CodeRouteService codeRouteService = new CodeRouteService();
            return new ModelAndView(codeRouteService.getCode(request, response), "coderesult.ftl");
        }, new FreeMarkerEngine());

        get("/repository/overview/:reponame/", (request, response) -> {
            response.header("Content-Encoding", "gzip");
            CodeRouteService codeRouteService = new CodeRouteService();
            return new ModelAndView(codeRouteService.getProject(request, response), "repository_overview.ftl");
        }, new FreeMarkerEngine());

        ////////////////////////////////////////////////////
        //              Page Routes Below
        ////////////////////////////////////////////////////

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
            addJsonHeaders(response);
            SearchRouteService searchRouteService = new SearchRouteService();
            return searchRouteService.CodeSearch(request, response);
        }, new JsonTransformer());


        get("/api/timecodesearch/", (request, response) -> {
            addJsonHeaders(response);
            TimeSearchRouteService ars = new TimeSearchRouteService();
            return ars.getTimeSearch(request, response);
        }, new JsonTransformer());

        get("/api/repo/add/", "application/json", (request, response) -> {
            addJsonHeaders(response);
            ApiRouteService apiRouteService = new ApiRouteService();
            return apiRouteService.repoAdd(request, response);
        }, new JsonTransformer());

        get("/api/repo/delete/", "application/json", (request, response) -> {
            addJsonHeaders(response);
            ApiRouteService apiRouteService = new ApiRouteService();
            return apiRouteService.repoDelete(request, response);
        }, new JsonTransformer());

        get("/api/repo/list/", "application/json", (request, response) -> {
            addJsonHeaders(response);
            ApiRouteService apiRouteService = new ApiRouteService();
            return apiRouteService.repoList(request, response);
        }, new JsonTransformer());

        get("/api/repo/reindex/", "application/json", (request, response) -> {
            addJsonHeaders(response);
            ApiRouteService apiRouteService = new ApiRouteService();
            return apiRouteService.repositoryReindex(request, response);
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
            Repo repo = Singleton.getRepo();
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

        // Experimental method to restart the application
        get("/admin/restart/", "application/json", (request, response) -> {
            if (getAuthenticatedUser(request) == null) {
                response.redirect("/login/");
                halt();
                return false;
            }

            final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            final File currentJar = new File(App.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            if(!currentJar.getName().endsWith(".jar")) {
                return false;
            }

            /* Build command: java -jar application.jar */
            final ArrayList<String> command = new ArrayList<>();
            command.add(javaBin);
            command.add("-jar");
            command.add(currentJar.getPath());

            final ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();
            System.exit(0);

            return true;
        }, new JsonTransformer());

    }

    /**
     * Called on startup to run all the DAO object table creation/migration logic. Slight overhead using this technique.
     * TODO Do the migrations inside the sqlite database so the application does not need to
     */
    public static void databaseMigrations() {
        Data data = Singleton.getData();
        Repo repo = Singleton.getRepo();
        Api api = Singleton.getApi();

        data.createTableIfMissing(); // Added data key/value table
        repo.addSourceToTable(); // Added source to repo
        repo.addBranchToTable(); // Add branch to repo
        api.createTableIfMissing();
    }

    private static void addJsonHeaders(Response response) {
        response.header("Content-Encoding", "gzip");
        response.header("Content-Type", "application/json");
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
