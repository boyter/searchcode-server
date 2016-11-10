package com.searchcode.app.config;

import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLDatabaseConfigTest extends TestCase {
    public void testConnection() throws SQLException {
        MySQLDatabaseConfig mySQLDatabaseConfig = new MySQLDatabaseConfig();
        mySQLDatabaseConfig.getConnection();
    }

    public void testConnectionSphinx() throws SQLException {
        SphinxSearchConfig ssc = new SphinxSearchConfig();
        Connection connection = ssc.getConnection();
        connection.close();
    }
}
