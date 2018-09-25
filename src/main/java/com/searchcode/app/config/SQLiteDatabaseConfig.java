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
import com.searchcode.app.util.Properties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDatabaseConfig implements IDatabaseConfig {

    private Connection connection = null;

    public synchronized Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                Singleton.getHelpers().closeQuietly(connection);
                String sqliteFile = (String)Properties.getProperties().getOrDefault(Values.SQLITE_FILE, Values.DEFAULT_SQLITE_FILE);

                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFile);

                // WAL write ahead logging supposedly helps with performance but did not notice a difference
                // PreparedStatement stmt = connection.prepareStatement("PRAGMA journal_mode=WAL;");
                // stmt.execute();
            }
        }
        catch (ClassNotFoundException ex) {
            Singleton.getLogger().severe(String.format("0c59f5f2::error in class %s exception %s it appears searchcode is unable to connect sqlite as the driver is missing", ex.getClass(), ex.getMessage()));
        }

        return connection;
    }
}
