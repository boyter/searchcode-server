/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 *
 * After the following date 27 August 2019 this software version '1.2.3' or '1.2.4' is dual licenced under the
 * Fair Source Licence included in the LICENSE.txt file or under the GNU General Public License Version 3 with terms
 * specified at https://www.gnu.org/licenses/gpl-3.0.txt
 */

package com.searchcode.app.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDatabaseConfig implements IDatabaseConfig {

    private Connection connection = null;

    public synchronized Connection getConnection() throws SQLException {
        try {
            if(connection == null || connection.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:./searchcode.sqlite");

                // WAL write ahead logging supposedly helps with performance but did not notice a difference
                // PreparedStatement stmt = connection.prepareStatement("PRAGMA journal_mode=WAL;");
                // stmt.execute();
            }
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return connection;
    }
}
