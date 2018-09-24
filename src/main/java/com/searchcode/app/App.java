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

import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Api;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dao.Repo;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Properties;
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
        // Database migrations happen before we start
        preStart();

        Singleton.getLogger().info("Starting searchcode server on port " + getServerPort());

        if (getOnlyLocalhost()) {
            Singleton.getLogger().info("Only listening on 127.0.0.1");
            Spark.ipAddress("127.0.0.1");
        }

        Spark.port(getServerPort());
        Spark.staticFileLocation("/public");

        Singleton.getJobService().initialJobs();

        if (Singleton.getHelpers().isLocalInstance()) {
            RegisterServerRoutes();
        } else {
            RegisterSearchcodeRoutes();
        }
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

    private static int getServerPort() {
        return Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.SERVER_PORT, Values.DEFAULT_SERVER_PORT), Values.DEFAULT_SERVER_PORT);
    }

    private static boolean getOnlyLocalhost() {
        return Boolean.parseBoolean(Properties.getProperties().getProperty("only_localhost", "false"));
    }
}
