package com.searchcode.app;

import com.searchcode.app.service.route.CodeRouteService;
import com.searchcode.app.service.route.SearchRouteService;
import com.searchcode.app.util.JsonTransformer;
import spark.ModelAndView;
import spark.Response;
import spark.template.freemarker.FreeMarkerEngine;

import static spark.Spark.get;
import static spark.Spark.path;

/**
 * Provides all the routes for the searchcode.com version of searchcode
 * Some comparison searches
 * http://localhost:8080/?q=wookieeRunnerPath
 * https://searchcode.com/?q=wookieeRunnerPath
 */
public class SearchcodeRoutes {
    public static void RegisterSearchcodeRoutes() {
        get("/", (request, response) -> {
            var codeRouteService = new CodeRouteService();
            var map = codeRouteService.html(request, response);

            if ((Boolean) map.getOrDefault("isIndex", Boolean.TRUE)) {
                return new FreeMarkerEngine().render(
                        new ModelAndView(map,
                                "index.ftl"));
            }

            return new FreeMarkerEngine().render(
                    new ModelAndView(map,
                            "searchcode_searchresults.ftl"));
        });

        get("/healthcheck/", (request, response) -> new JsonTransformer().render(true));
        get("/health-check/", (request, response) -> new JsonTransformer().render(true));

        get("/file/:codeid/*", (request, response) -> {
            var codeRouteService = new CodeRouteService();
            return new FreeMarkerEngine().render(
                    new ModelAndView(codeRouteService.getCode(request, response),
                            "coderesult.ftl"));
        });

        get("/repository/overview/:reponame/:repoid/", (request, response) -> {
            var codeRouteService = new CodeRouteService();
            return new FreeMarkerEngine().render(
                    new ModelAndView(codeRouteService.getProject(request, response),
                            "repository_overview.ftl"));
        });

        ///api/codesearch_I/
        path("/api", () -> {
            // Older endpoints maintained for backwards compatibility
            get("/codesearch_I/", (request, response) -> {
                addJsonHeaders(response);
                var searchRouteService = new SearchRouteService();
                return new JsonTransformer().render(searchRouteService.codeSearch_I(request, response));
            });

            get("/result/:codeid/", (request, response) -> {
                addJsonHeaders(response);
                var searchRouteService = new SearchRouteService();
                return new JsonTransformer().render(searchRouteService.codeResult(request, response));
            });

            // All new API endpoints should go in here to allow public exposure and versioning
            path("/v1", () -> {
                get("/version/", (request, response) -> {
                    addJsonHeaders(response);
                    return new JsonTransformer().render("");
                });

                get("/health-check/", (request, response) -> {
                    addJsonHeaders(response);
                    return new JsonTransformer().render("");
                });
            });
        });
    }

    private static void addJsonHeaders(Response response) {
        response.header("Content-Type", "application/json");
    }
}
