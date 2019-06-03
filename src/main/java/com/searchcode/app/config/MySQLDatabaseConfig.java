/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.4
 */

package com.searchcode.app.config;

import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;
import com.searchcode.app.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLDatabaseConfig implements IDatabaseConfig {

    private final Helpers helpers;
    private final LoggerWrapper logger;
    private HikariDataSource datasource = null;

    public MySQLDatabaseConfig() {
        this.helpers = Singleton.getHelpers();
        this.logger = Singleton.getLogger();

        var config = new HikariConfig();
        config.setJdbcUrl((String)Properties.getProperties().getOrDefault("searchcode_connection_string", "jdbc:mysql://localhost:3306/searchcode?serverTimezone=UTC"));
        config.setUsername((String) Properties.getProperties().getOrDefault("searchcode_connection_user", "root"));
        config.setPassword((String) Properties.getProperties().getOrDefault("searchcode_connection_password", "root"));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        try {
            this.datasource = new HikariDataSource(config);
        }
        catch (Exception ex) {
            this.logger.severe(String.format("16ca6fd9::error in class %s exception %s unable to connect to mysql", ex.getClass(), ex.getMessage()));
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.datasource.getConnection();
    }

    @Override
    public boolean closeConnection() {
        return true;
    }
}