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

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConfig implements IDatabaseConfig {

    public Connection getConnection() throws SQLException {
        BasicDataSource ds = new BasicDataSource(); // pooling data source
        
        ds.setDriverClassName("org.hsqldb.jdbcDriver");
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setUrl("jdbc:hsqldb:file:searchcode;shutdown=true");


        return ds.getConnection();
    }
}
