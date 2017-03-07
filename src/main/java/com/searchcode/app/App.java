/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.8
 */

package com.searchcode.app;

import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Api;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dao.Repo;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.service.route.*;
import com.searchcode.app.util.JsonTransformer;
import com.searchcode.app.util.Properties;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

/**
 * Main entry point for the application.
 */
public class App {

    public static final boolean ISCOMMUNITY = false;
    public static final String VERSION = "1.3.8";

    public static void main (String[] args) {

        // Database migrations happen before we start
        preStart();

        Singleton.getLogger().info("Starting searchcode server on port " + getServerPort());

        if (getOnlyLocalhost()) {
            Singleton.getLogger().info("Only listening on 127.0.0.1 ");
            Spark.ipAddress("127.0.0.1");
        }

        Spark.port(getServerPort());
        Spark.staticFileLocation("/public");

        Singleton.getJobService().initialJobs();


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

        get("/repository/list/", (request, response) -> {
            response.header("Content-Encoding", "gzip");
            CodeRouteService codeRouteService = new CodeRouteService();
            return new ModelAndView(codeRouteService.getRepositoryList(request, response), "repository_list.ftl");
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

        path("/api", () -> {
            get("/codesearch/", (request, response) -> {
                addJsonHeaders(response);
                SearchRouteService searchRouteService = new SearchRouteService();
                return searchRouteService.codeSearch(request, response);
            }, new JsonTransformer());

            get("/timecodesearch/", (request, response) -> {
                addJsonHeaders(response);
                TimeSearchRouteService ars = new TimeSearchRouteService();
                return ars.getTimeSearch(request, response);
            }, new JsonTransformer());

            path("/repo", () -> {
                get("/add/", "application/json", (request, response) -> {
                    addJsonHeaders(response);
                    ApiRouteService apiRouteService = new ApiRouteService();
                    return apiRouteService.repoAdd(request, response);
                }, new JsonTransformer());

                get("/delete/", "application/json", (request, response) -> {
                    addJsonHeaders(response);
                    ApiRouteService apiRouteService = new ApiRouteService();
                    return apiRouteService.repoDelete(request, response);
                }, new JsonTransformer());

                get("/list/", "application/json", (request, response) -> {
                    addJsonHeaders(response);
                    ApiRouteService apiRouteService = new ApiRouteService();
                    return apiRouteService.repoList(request, response);
                }, new JsonTransformer());

                get("/reindex/", "application/json", (request, response) -> {
                    addJsonHeaders(response);
                    ApiRouteService apiRouteService = new ApiRouteService();
                    return apiRouteService.repositoryReindex(request, response);
                }, new JsonTransformer());

                ////////////////////////////////////////////////////
                //          Unsecured API Routes Below
                ////////////////////////////////////////////////////

                get("/index/", "application/json", (request, response) -> {
                    addJsonHeaders(response);
                    ApiRouteService apiRouteService = new ApiRouteService();
                    return apiRouteService.repositoryIndex(request, response);
                }, new JsonTransformer());

                get("/filecount/", "application/json", (request, response) -> {
                    ApiRouteService apiRouteService = new ApiRouteService();
                    return apiRouteService.getFileCount(request, response);
                });

                get("/indextime/", "application/json", (request, response) -> {
                    ApiRouteService apiRouteService = new ApiRouteService();
                    return apiRouteService.getIndexTime(request, response);
                });
            });
        });


        ////////////////////////////////////////////////////
        //              Admin Routes Below
        ////////////////////////////////////////////////////

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

        path("/admin", () -> {
            get("/", (request, response) -> {
                checkLoggedIn(request, response);
                AdminRouteService adminRouteService = new AdminRouteService();
                Map<String, Object> map = adminRouteService.adminPage(request, response);

                return new ModelAndView(map, "admin.ftl");
            }, new FreeMarkerEngine());

            get("/repo/", (request, response) -> {
                checkLoggedIn(request, response);
                AdminRouteService adminRouteService = new AdminRouteService();
                Map<String, Object> map = adminRouteService.adminRepo(request, response);

                return new ModelAndView(map, "admin_repo.ftl");
            }, new FreeMarkerEngine());

            get("/repolist/", (request, response) -> {
                checkLoggedIn(request, response);
                AdminRouteService adminRouteService = new AdminRouteService();
                Map<String, Object> map = adminRouteService.adminRepo(request, response);

                return new ModelAndView(map, "admin_repolist.ftl");
            }, new FreeMarkerEngine());

            get("/bulk/", (request, response) -> {
                checkLoggedIn(request, response);
                Map<String, Object> map = new HashMap<>();

                map.put("logoImage", CommonRouteService.getLogo());
                map.put("isCommunity", ISCOMMUNITY);
                return new ModelAndView(map, "admin_bulk.ftl");
            }, new FreeMarkerEngine());

            get("/settings/", (request, response) -> {
                checkLoggedIn(request, response);
                AdminRouteService adminRouteService = new AdminRouteService();
                Map<String, Object> map = adminRouteService.adminSettings(request, response);

                return new ModelAndView(map, "admin_settings.ftl");
            }, new FreeMarkerEngine());

            get("/logs/", (request, response) -> {
                checkLoggedIn(request, response);
                AdminRouteService adminRouteService = new AdminRouteService();
                Map<String, Object> map = adminRouteService.adminLogs(request, response);

                return new ModelAndView(map, "admin_logs.ftl");
            }, new FreeMarkerEngine());

            post("/settings/", (request, response) -> {
                checkLoggedIn(request, response);
                if (ISCOMMUNITY) {
                    response.redirect("/admin/settings/");
                    halt();
                }

                AdminRouteService adminRouteService = new AdminRouteService();
                adminRouteService.postSettings(request, response);

                response.redirect("/admin/settings/");
                halt();
                return null;
            }, new FreeMarkerEngine());

            post("/bulk/", (request, response) -> {
                checkLoggedIn(request, response);
                AdminRouteService adminRouteService = new AdminRouteService();
                adminRouteService.postBulk(request, response);

                response.redirect("/admin/bulk/");
                halt();
                return null;
            }, new FreeMarkerEngine());

            post("/repo/", (request, response) -> {
                checkLoggedIn(request, response);
                AdminRouteService adminRouteService = new AdminRouteService();
                adminRouteService.postRepo(request, response);

                response.redirect("/admin/repo/");
                halt();
                return null;
            }, new FreeMarkerEngine());

            get("/delete/", "application/json", (request, response) -> {
                checkLoggedIn(request, response);
                String repoName = request.queryParams("repoName");
                Repo repo = Singleton.getRepo();
                RepoResult rr = repo.getRepoByName(repoName);

                if (rr != null) {
                    Singleton.getUniqueDeleteRepoQueue().add(rr);
                }

                return true;
            }, new JsonTransformer());

            post("/rebuild/", "application/json", (request, response) -> {
                checkLoggedIn(request, response);
                boolean result = Singleton.getJobService().rebuildAll();
                if (result) {
                    Singleton.getJobService().forceEnqueue();
                }
                return result;
            }, new JsonTransformer());

            post("/forcequeue/", "application/json", (request, response) -> {
                checkLoggedIn(request, response);
                return Singleton.getJobService().forceEnqueue();
            }, new JsonTransformer());

            post("/togglepause/", "application/json", (request, response) -> {
                checkLoggedIn(request, response);
                Singleton.setPauseBackgroundJobs(!Singleton.getPauseBackgroundJobs());
                return Singleton.getPauseBackgroundJobs();
            }, new JsonTransformer());

            post("/clearsearchcount/", "application/json", (request, response) -> {
                checkLoggedIn(request, response);
                Singleton.getStatsService().clearSearchCount();
                return Values.EMPTYSTRING;
            }, new JsonTransformer());

            post("/resetspellingcorrector/", "application/json", (request, response) -> {
                checkLoggedIn(request, response);
                Singleton.getSpellingCorrector().reset();
                return Values.EMPTYSTRING;
            }, new JsonTransformer());

            get("/checkversion/", "application/json", (request, response) -> {
                checkLoggedIn(request, response);
                AdminRouteService adminRouteService = new AdminRouteService();
                return adminRouteService.checkVersion();
            });

            path("/api", () -> {
                get("/", (request, response) -> {
                    checkLoggedIn(request, response);
                    AdminRouteService adminRouteService = new AdminRouteService();
                    Map<String, Object> map = adminRouteService.adminApi(request, response);

                    return new ModelAndView(map, "admin_api.ftl");
                }, new FreeMarkerEngine());

                post("/", (request, response) -> {
                    checkLoggedIn(request, response);
                    Singleton.getApiService().createKeys();

                    response.redirect("/admin/api/");
                    halt();
                    return null;
                }, new FreeMarkerEngine());

                get("/delete/", "application/json", (request, response) -> {
                    checkLoggedIn(request, response);
                    if (getAuthenticatedUser(request) == null || !request.queryParams().contains("publicKey")) {
                        response.redirect("/login/");
                        halt();
                        return false;
                    }

                    String publicKey = request.queryParams("publicKey");
                    Singleton.getApiService().deleteKey(publicKey);

                    return true;
                }, new JsonTransformer());

                get("/getstat/", "application/json", (request, response) -> {
                    checkLoggedIn(request, response);
                    AdminRouteService adminRouteService = new AdminRouteService();
                    return adminRouteService.getStat(request, response);
                });

                get("/getstatjson/", "application/json", (request, response) -> {
                    checkLoggedIn(request, response);
                    AdminRouteService adminRouteService = new AdminRouteService();
                    return adminRouteService.getStat(request, response);
                }, new JsonTransformer());

                get("/checkindexstatus/", "application/json", (request, response) -> {
                    //checkLoggedIn(request, response);
                    AdminRouteService adminRouteService = new AdminRouteService();
                    return adminRouteService.checkIndexStatus(request, response);
                });
            });
        });

    }


    /**
     * Called on startup to run all the DAO object table creation/migration logic. Slight overhead using this technique.
     * TODO Do the migrations inside the sqlite database so the application does not need to
     */
    public static void preStart() {
        // Database migrations
        Data data = Singleton.getData();
        Repo repo = Singleton.getRepo();
        Api api = Singleton.getApi();

        data.createTableIfMissing();
        api.createTableIfMissing();
        repo.addSourceToTable();
        repo.addBranchToTable();
        repo.addDataToTable();
    }

    private static void checkLoggedIn(Request request, Response response) {
        if (getAuthenticatedUser(request) == null) {
            response.redirect("/login/");
            halt();
        }
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

    private static int getServerPort() {
        return Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.SERVER_PORT, Values.DEFAULT_SERVER_PORT), Values.DEFAULT_SERVER_PORT);
    }

    private static boolean getOnlyLocalhost() {
        return Boolean.parseBoolean(Properties.getProperties().getProperty("only_localhost", "false"));
    }
}
