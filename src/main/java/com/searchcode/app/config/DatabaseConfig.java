/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.3
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
