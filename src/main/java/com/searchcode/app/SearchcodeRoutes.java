package com.searchcode.app;

import com.searchcode.app.service.route.CodeRouteService;
import com.searchcode.app.util.JsonTransformer;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

import static spark.Spark.get;

public class SearchcodeRoutes {
    public static void RegisterSearchcodeRoutes() {
        get("/", (request, response) -> {
            CodeRouteService codeRouteService = new CodeRouteService();
            return new FreeMarkerEngine().render(codeRouteService.html(request, response));
        });

        get("/healthcheck/", (request, response) -> new JsonTransformer().render(true));

        get("/file/:codeid/*", (request, response) -> {
            CodeRouteService codeRouteService = new CodeRouteService();
            return new FreeMarkerEngine().render(new ModelAndView(codeRouteService.getCode(request, response), "coderesult.ftl"));
        });

        get("/repository/overview/:reponame/", (request, response) -> {
            CodeRouteService codeRouteService = new CodeRouteService();
            return new FreeMarkerEngine().render(new ModelAndView(codeRouteService.getProject(request, response), "repository_overview.ftl"));
        });
    }
}
