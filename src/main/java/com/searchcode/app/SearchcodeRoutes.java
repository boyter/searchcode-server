package com.searchcode.app;

import com.searchcode.app.service.route.CodeRouteService;
import com.searchcode.app.util.JsonTransformer;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

import static spark.Spark.get;
import static spark.Spark.path;

/**
 * Provides all the routes for the searchcode.com version of searchcode
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
               return new JsonTransformer().render(null);
            });

            // All new API endpoints should go in here to allow public exposure and versioning
            path("/v1", () -> {
                get("/version/", (request, response) -> {
                    return new JsonTransformer().render("");
                });

                get("/health-check/", (request, response) -> {
                    return new JsonTransformer().render("");
                });
            });
        });
    }
}
