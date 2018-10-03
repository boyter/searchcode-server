/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app;

import com.searchcode.app.service.Singleton;
import spark.Spark;

import static com.searchcode.app.SearchcodeRoutes.RegisterSearchcodeRoutes;
import static com.searchcode.app.ServerRoutes.RegisterServerRoutes;

/**
 * Main entry point for the application.
 */
public class App {

    public static final boolean ISCOMMUNITY = true;
    public static final String VERSION = "1.3.15";

    public static void main(String[] args) {
        Singleton.getLogger().info(String.format("c234cbe8::starting searchcode server version %s on port %d", App.VERSION, Singleton.getHelpers().getServerPort()));

        if (Singleton.getHelpers().getOnlyLocalhost()) {
            Singleton.getLogger().info(String.format("db9699c3::binding to 127.0.0.1:%d", Singleton.getHelpers().getServerPort()));
            Spark.ipAddress("127.0.0.1");
        } else {
            Singleton.getLogger().info(String.format("3711ea12::binding to 0.0.0.0:%d", Singleton.getHelpers().getServerPort()));
        }

        Spark.port(Singleton.getHelpers().getServerPort());
        Spark.staticFileLocation("/public");

        Singleton.getJobService().initialJobs();

        if (Singleton.getHelpers().isLocalInstance()) {
            RegisterServerRoutes();
        } else {
            RegisterSearchcodeRoutes();
        }
    }
}
