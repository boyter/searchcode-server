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

import com.searchcode.app.util.Properties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SphinxSearchConfig implements IDatabaseConfig {

    private Connection connection = null;

    @Override
    public synchronized Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.jdbc.Driver");
                String connectionString = "jdbc:mysql://127.0.0.1:9306?characterEncoding=utf8&maxAllowedPacket=1024000";
                connection = DriverManager.getConnection(connectionString, "", "");
            }
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return connection;
    }
}