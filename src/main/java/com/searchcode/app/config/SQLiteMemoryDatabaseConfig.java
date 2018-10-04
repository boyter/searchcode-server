/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.config;

import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.LoggerWrapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteMemoryDatabaseConfig implements IDatabaseConfig {
    private final LoggerWrapper logger;
    private Connection connection = null;

    public SQLiteMemoryDatabaseConfig() {
        this.logger = Singleton.getLogger();
    }

    public synchronized Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                Singleton.getHelpers().closeQuietly(connection);
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite::memory:");
            }
        }
        catch (ClassNotFoundException ex) {
            this.logger.severe(String.format("6310e883::error in class %s exception %s it appears searchcode is unable to connect sqlite as the driver is missing", ex.getClass(), ex.getMessage()));
        }

        return connection;
    }
}
