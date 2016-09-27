/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.2
 */

package com.searchcode.app.config;

import com.google.inject.AbstractModule;
import com.searchcode.app.dao.*;
import com.searchcode.app.service.ApiService;
import com.searchcode.app.service.IApiService;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.LoggerWrapper;
import com.searchcode.app.util.Properties;

import java.util.logging.Logger;

/**
 * TODO Deprecate this. The dependancies are easy to manage ourselves and its just an extra bunch of dependencies
 */
public class InjectorConfig extends AbstractModule {

    private static final LoggerWrapper LOGGER = Singleton.getLogger();

    @Override
    protected void configure() {
        String databaseType = Properties.getProperties().getProperty("database", "sqlite");

        if("sqlite".equals(databaseType)) {
            bind(IDatabaseConfig.class).to(SQLiteDatabaseConfig.class);
            LOGGER.info("Using SQLITE database");
        }
        else {
            bind(IDatabaseConfig.class).to(DatabaseConfig.class);
            LOGGER.info("Using alternative database");
        }

        bind(IRepo.class).to(Repo.class);
        bind(IData.class).to(Data.class);
        bind(IApi.class).to(Api.class);

        bind(IApiService.class).to(ApiService.class);
    }
}
