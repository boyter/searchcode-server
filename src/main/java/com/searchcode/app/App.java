/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.14
 */

package com.searchcode.app;

import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Api;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dao.Repo;
import com.searchcode.app.dto.api.VersionResponse;
import com.searchcode.app.model.ValidatorResult;
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
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

/**
 * Main entry point for the application.
 */
public class App {

    public static final boolean ISCOMMUNITY = true;
    public static final String VERSION = "1.3.14";

    public static void main(String[] args) {
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
            return new FreeMarkerEngine().render(codeRouteService.root(request, response));
        });

        get("/healthcheck/", (request, response) -> {
            response.header("Content-Encoding", "gzip");
            return new JsonTransformer().render(true);
        });

        get("/html/", (request, response) -> {
            response.header("Content-Encoding", "gzip");
            CodeRouteService codeRouteService = new CodeRouteService();
            return new FreeMarkerEngine().render(codeRouteService.html(request, response));
        });

        get("/file/:codeid/:reponame/*", (request, response) -> {
            response.header("Content-Encoding", "gzip");
            CodeRouteService codeRouteService = new CodeRouteService();
            return new FreeMarkerEngine().render(new ModelAndView(codeRouteService.getCode(request, response), "coderesult.ftl"));
        });

        get("/repository/overview/:reponame/", (request, response) -> {
            response.header("Content-Encoding", "gzip");
            CodeRouteService codeRouteService = new CodeRouteService();
            return new FreeMarkerEngine().render(new ModelAndView(codeRouteService.getProject(request, response), "repository_overview.ftl"));
        });

        get("/repository/list/", (request, response) -> {
            response.header("Content-Encoding", "gzip");
            CodeRouteService codeRouteService = new CodeRouteService();
            return new FreeMarkerEngine().render(new ModelAndView(codeRouteService.getRepositoryList(request, response), "repository_list.ftl"));
        });

        ////////////////////////////////////////////////////
        //              Page Routes Below
        ////////////////////////////////////////////////////

        get("/documentation/", (request, response) -> {
            Map<String, Object> map = new HashMap<>();

            map.put("logoImage", CommonRouteService.getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            map.put(Values.EMBED, Singleton.getData().getDataByName(Values.EMBED, Values.EMPTYSTRING));
            return new FreeMarkerEngine().render(new ModelAndView(map, "documentation.ftl"));
        });

        get("/404/", (request, response) -> {
            Map<String, Object> map = new HashMap<>();

            map.put("logoImage", CommonRouteService.getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            map.put(Values.EMBED, Singleton.getData().getDataByName(Values.EMBED, Values.EMPTYSTRING));
            return new FreeMarkerEngine().render(new ModelAndView(map, "404.ftl"));
        });

        ////////////////////////////////////////////////////
        //              API Routes Below
        ////////////////////////////////////////////////////

        path("/api", () -> {
            // All new API endpoints should go in here to allow public exposure and versioning
            path("/v1", () -> {
                get("/version/", (request, response) -> {
                    addJsonHeaders(response);
                    return new JsonTransformer().render(new VersionResponse().setVersion(VERSION));
                });

                get("/health-check/", (request, response) -> {
                    addJsonHeaders(response);
                    return new JsonTransformer().render(new VersionResponse().setVersion(VERSION));
                });
            });

            get("/codesearch/", (request, response) -> {
                addJsonHeaders(response);
                SearchRouteService searchRouteService = new SearchRouteService();
                return new JsonTransformer().render(searchRouteService.codeSearch(request, response));
            });

            get("/literalcodesearch/", (request, response) -> {
                addJsonHeaders(response);
                SearchRouteService searchRouteService = new SearchRouteService();
                return new JsonTransformer().render(searchRouteService.literalCodeSearch(request, response));
            });

            get("/codesearch/rss/", (request, response) -> {
                addXmlHeaders(response);
                SearchRouteService searchRouteService = new SearchRouteService();
                Map<String, Object> map = new HashMap<>();
                map.put("result", searchRouteService.codeSearch(request, response));
                map.put("hostname", Properties.getProperties().getProperty(Values.HOST_NAME, Values.DEFAULT_HOST_NAME));

                return new FreeMarkerEngine().render(new ModelAndView(map, "codesearchrss.ftl"));
            });

            get("/timecodesearch/", (request, response) -> {
                addJsonHeaders(response);
                TimeSearchRouteService ars = new TimeSearchRouteService();
                return new JsonTransformer().render(ars.getTimeSearch(request, response));
            });

            path("/repo", () -> {
                get("/add/", "application/json", (request, response) -> {
                    addJsonHeaders(response);
                    ApiRouteService apiRouteService = new ApiRouteService();
                    return new JsonTransformer().render(apiRouteService.repoAdd(request, response));
                });

                get("/delete/", "application/json", (request, response) -> {
                    addJsonHeaders(response);
                    ApiRouteService apiRouteService = new ApiRouteService();
                    return new JsonTransformer().render(apiRouteService.repoDelete(request, response));
                });

                get("/list/", "application/json", (request, response) -> {
                    addJsonHeaders(response);
                    ApiRouteService apiRouteService = new ApiRouteService();
                    return new JsonTransformer().render(apiRouteService.repoList(request, response));
                });

                get("/reindex/", "application/json", (request, response) -> {
                    addJsonHeaders(response);
                    ApiRouteService apiRouteService = new ApiRouteService();
                    return new JsonTransformer().render(apiRouteService.repositoryReindex(request, response));
                });

                ////////////////////////////////////////////////////
                //          Unsecured API Routes Below
                ////////////////////////////////////////////////////

                get("/index/", "application/json", (request, response) -> {
                    addJsonHeaders(response);
                    ApiRouteService apiRouteService = new ApiRouteService();
                    return new JsonTransformer().render(apiRouteService.repositoryIndex(request, response));
                });

                get("/filecount/", "application/json", (request, response) -> {
                    ApiRouteService apiRouteService = new ApiRouteService();
                    return apiRouteService.getFileCount(request, response);
                });

                get("/indextime/", "application/json", (request, response) -> {
                    ApiRouteService apiRouteService = new ApiRouteService();
                    return apiRouteService.getIndexTime(request, response);
                });

                get("/indextimeseconds/", "application/json", (request, response) -> {
                    ApiRouteService apiRouteService = new ApiRouteService();
                    return apiRouteService.getAverageIndexTimeSeconds(request, response);
                });

                get("/repo/", "application/json", (request, response) -> {
                    addJsonHeaders(response);
                    ApiRouteService apiRouteService = new ApiRouteService();
                    return new JsonTransformer().render(apiRouteService.getRepo(request, response));
                });

                get("/repotree/", "application/json", (request, response) -> {
                    addJsonHeaders(response);
                    ApiRouteService apiRouteService = new ApiRouteService();
                    return new JsonTransformer().render(apiRouteService.repoTree(request, response));
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
            map.put(Values.EMBED, Singleton.getData().getDataByName(Values.EMBED, Values.EMPTYSTRING));

            return new FreeMarkerEngine().render(new ModelAndView(map, "login.ftl"));
        });

        post("/login/", (request, response) -> {
            if (request.queryParams().contains("password") && request.queryParams("password").equals(com.searchcode.app.util.Properties.getProperties().getProperty("password"))) {
                addAuthenticatedUser(request);
                response.redirect("/admin/");
                halt();
            }

            Map<String, Object> map = new HashMap<>();
            map.put("logoImage", CommonRouteService.getLogo());
            map.put("isCommunity", ISCOMMUNITY);
            map.put(Values.EMBED, Singleton.getData().getDataByName(Values.EMBED, Values.EMPTYSTRING));

            if (request.queryParams().contains("password")) {
                map.put("passwordInvalid", true);
            }

            return new FreeMarkerEngine().render(new ModelAndView(map, "login.ftl"));
        });

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

                return new FreeMarkerEngine().render(new ModelAndView(map, "admin.ftl"));
            });

            get("/repo/", (request, response) -> {
                checkLoggedIn(request, response);
                AdminRouteService adminRouteService = new AdminRouteService();
                Map<String, Object> map = adminRouteService.adminRepo(request, response);

                return new FreeMarkerEngine().render(new ModelAndView(map, "admin_repo.ftl"));
            });

            post("/repo/", (request, response) -> {
                checkLoggedIn(request, response);
                AdminRouteService adminRouteService = new AdminRouteService();
                ValidatorResult validatorResult = adminRouteService.postRepo(request, response, false);

                if (!validatorResult.isValid) {
                    Map<String, Object> map = adminRouteService.adminRepo(request, response);
                    map.put("validatorResult", validatorResult);
                    return new FreeMarkerEngine().render(new ModelAndView(map, "admin_repo.ftl"));
                }

                String[] returns = request.queryParamsValues("return");

                if (returns != null) {
                    response.redirect("/admin/repo/");
                } else {
                    response.redirect("/admin/repolist/");
                }

                halt();
                return null;
            });

            get("/repo/edit/:reponame/", (request, response) -> {
                checkLoggedIn(request, response);
                AdminRouteService adminRouteService = new AdminRouteService();
                Map<String, Object> map = adminRouteService.adminGetRepo(request, response);

                return new FreeMarkerEngine().render(new ModelAndView(map, "admin_repo_edit.ftl"));
            });

            post("/repo/edit/:reponame/", (request, response) -> {
                checkLoggedIn(request, response);
                AdminRouteService adminRouteService = new AdminRouteService();
                ValidatorResult validatorResult = adminRouteService.postRepo(request, response, true);

                if (!validatorResult.isValid) {
                    Map<String, Object> map = adminRouteService.adminRepo(request, response);
                    map.put("validatorResult", validatorResult);
                    map.put("repoResult", validatorResult.getRepoResult());
                    return new FreeMarkerEngine().render(new ModelAndView(map, "admin_repo_edit.ftl"));
                }

                response.redirect(request.url());
                halt();
                return null;
            });

            get("/repolist/", (request, response) -> {
                checkLoggedIn(request, response);
                AdminRouteService adminRouteService = new AdminRouteService();
                Map<String, Object> map = adminRouteService.adminRepo(request, response);

                return new FreeMarkerEngine().render(new ModelAndView(map, "admin_repolist.ftl"));
            });

            get("/repo/error/:reponame/", (request, response) -> {
                checkLoggedIn(request, response);
                AdminRouteService adminRouteService = new AdminRouteService();
                Map<String, Object> map = adminRouteService.adminGetRepo(request, response);
                return new FreeMarkerEngine().render(new ModelAndView(map, "admin_repo_error.ftl"));
            });

            get("/bulk/", (request, response) -> {
                checkLoggedIn(request, response);
                AdminRouteService adminRouteService = new AdminRouteService();
                Map<String, Object> map = new HashMap<>();

                map.put("logoImage", CommonRouteService.getLogo());
                map.put("isCommunity", ISCOMMUNITY);
                map.put(Values.EMBED, Singleton.getData().getDataByName(Values.EMBED, Values.EMPTYSTRING));
                map.put("repoCount", adminRouteService.getStat("repoCount"));
                return new FreeMarkerEngine().render(new ModelAndView(map, "admin_bulk.ftl"));
            });

            post("/bulk/", (request, response) -> {
                checkLoggedIn(request, response);
                AdminRouteService adminRouteService = new AdminRouteService();
                List<ValidatorResult> validatorResults = adminRouteService.postBulk(request, response);

                if (!validatorResults.isEmpty()) {
                    Map<String, Object> map = new HashMap<>();

                    map.put("logoImage", CommonRouteService.getLogo());
                    map.put("isCommunity", ISCOMMUNITY);
                    map.put(Values.EMBED, Singleton.getData().getDataByName(Values.EMBED, Values.EMPTYSTRING));
                    map.put("repoCount", adminRouteService.getStat("repoCount"));
                    map.put("validatorResults", validatorResults);

                    return new FreeMarkerEngine().render(new ModelAndView(map, "admin_bulk.ftl"));
                }

                response.redirect("/admin/repolist/");
                halt();
                return null;
            });

            get("/settings/", (request, response) -> {
                checkLoggedIn(request, response);
                AdminRouteService adminRouteService = new AdminRouteService();
                Map<String, Object> map = adminRouteService.adminSettings(request, response);

                return new FreeMarkerEngine().render(new ModelAndView(map, "admin_settings.ftl"));
            });

            get("/logs/", (request, response) -> {
                checkLoggedIn(request, response);
                AdminRouteService adminRouteService = new AdminRouteService();
                Map<String, Object> map = adminRouteService.adminLogs(request, response);

                return new FreeMarkerEngine().render(new ModelAndView(map, "admin_logs.ftl"));
            });

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
            });

            get("/delete/", "application/json", (request, response) -> {
                checkLoggedIn(request, response);
                AdminRouteService adminRouteService = new AdminRouteService();
                adminRouteService.deleteRepo(request, response);
                return new JsonTransformer().render(true);
            });

            get("/reindex/", "application/json", (request, response) -> {
                checkLoggedIn(request, response);
                AdminRouteService adminRouteService = new AdminRouteService();
                adminRouteService.reindexRepo(request, response);
                return new JsonTransformer().render(true);
            });


            post("/enableadder/", "application/json", (request, response) -> {
                checkLoggedIn(request, response);
                Singleton.getIndexService().setRepoAdderPause(false);
                Singleton.getIndexService().resetReindexingAll();
                return new JsonTransformer().render(true);
            });

            post("/rebuild/", "application/json", (request, response) -> {
                checkLoggedIn(request, response);
                Singleton.getIndexService().reindexAll();
                return new JsonTransformer().render(true);
            });

            post("/deleteindex/", "application/json", (request, response) -> {
                checkLoggedIn(request, response);
                Singleton.getIndexService().deleteAll();
                return new JsonTransformer().render(true);
            });

            post("/forcequeue/", "application/json", (request, response) -> {
                checkLoggedIn(request, response);
                return Singleton.getJobService().forceEnqueue();
            }, new JsonTransformer());

            post("/togglepause/", "application/json", (request, response) -> {
                checkLoggedIn(request, response);
                Singleton.getIndexService().toggleRepoAdderPause();
                return new JsonTransformer().render(Singleton.getIndexService().getRepoAdderPause());
            });

            post("/flipreadindex/", "application/json", (request, response) -> {
                checkLoggedIn(request, response);
                Singleton.getIndexService().flipReadIndex();
                return new JsonTransformer().render(Values.EMPTYSTRING);
            });

            post("/resetindexlinescount/", "application/json", (request, response) -> {
                checkLoggedIn(request, response);
                Singleton.getIndexService().setCodeIndexLinesCount(0);
                return new JsonTransformer().render(Values.EMPTYSTRING);
            });

            post("/clearsearchcount/", "application/json", (request, response) -> {
                checkLoggedIn(request, response);
                Singleton.getStatsService().clearSearchCount();
                return new JsonTransformer().render(Values.EMPTYSTRING);
            });

            post("/resetspellingcorrector/", "application/json", (request, response) -> {
                checkLoggedIn(request, response);
                Singleton.getSpellingCorrector().reset();
                return new JsonTransformer().render(Values.EMPTYSTRING);
            });

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

                    return new FreeMarkerEngine().render(new ModelAndView(map, "admin_api.ftl"));
                });

                post("/", (request, response) -> {
                    checkLoggedIn(request, response);
                    Singleton.getApiService().createKeys();

                    response.redirect("/admin/api/");
                    halt();
                    return null;
                });

                get("/delete/", "application/json", (request, response) -> {
                    checkLoggedIn(request, response);
                    if (getAuthenticatedUser(request) == null || !request.queryParams().contains("publicKey")) {
                        response.redirect("/login/");
                        halt();
                        return new JsonTransformer().render(false);
                    }

                    String publicKey = request.queryParams("publicKey");
                    Singleton.getApiService().deleteKey(publicKey);

                    return new JsonTransformer().render(true);
                });

                get("/getstat/", (request, response) -> {
                    checkLoggedIn(request, response);
                    AdminRouteService adminRouteService = new AdminRouteService();
                    return adminRouteService.getStat(request, response);
                });

                get("/getstatjson/", "application/json", (request, response) -> {
                    checkLoggedIn(request, response);
                    AdminRouteService adminRouteService = new AdminRouteService();
                    return new JsonTransformer().render(adminRouteService.getStat(request, response));
                });

                get("/checkindexstatus/", "application/json", (request, response) -> {
                    // TODO move this to unsecured routes
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
        repo.createTableIfMissing();
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

    private static void addXmlHeaders(Response response) {
        response.header("Content-Encoding", "gzip");
        response.header("Content-Type", "application/rss+xml; charset=UTF-8");
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
