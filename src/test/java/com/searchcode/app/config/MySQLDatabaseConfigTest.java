package com.searchcode.app.config;

import junit.framework.TestCase;

import java.sql.SQLException;

public class MySQLDatabaseConfigTest extends TestCase {
    public void testConnection() throws SQLException {
        MySQLDatabaseConfig mySQLDatabaseConfig = new MySQLDatabaseConfig();
        mySQLDatabaseConfig.getConnection();
    }
}
