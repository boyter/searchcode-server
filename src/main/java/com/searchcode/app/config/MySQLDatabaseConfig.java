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
import com.searchcode.app.util.Properties;

import java.sql.*;

public class MySQLDatabaseConfig implements IDatabaseConfig {

    private final Helpers helpers;
    private Connection connection = null;

    public MySQLDatabaseConfig() {
        this.helpers = Singleton.getHelpers();
    }

    @Override
    public synchronized Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(1)) {
                this.helpers.closeQuietly(connection);
                Class.forName("com.mysql.jdbc.Driver");
                String connectionString = (String)Properties.getProperties().getOrDefault("searchcode_connection_string", "jdbc:mysql://localhost:3306/searchcode?serverTimezone=UTC");
                String user = (String)Properties.getProperties().getOrDefault("searchcode_connection_user", "root");
                String pass = (String)Properties.getProperties().getOrDefault("searchcode_connection_password", "root");
                connection = DriverManager.getConnection(connectionString, user, pass);
            }
        }
        catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        return connection;
    }
}