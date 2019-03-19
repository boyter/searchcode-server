package com.searchcode.app.config;

import com.searchcode.app.service.Singleton;
import junit.framework.TestCase;

import java.sql.SQLException;

public class MySQLDatabaseConfigTest extends TestCase {
    public void testConnection() throws SQLException {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var mySQLDatabaseConfig = Singleton.getDatabaseConfig();
        var connection = mySQLDatabaseConfig.getConnection();
        connection.close();
    }

    /**
     * Ensure that connection pooling is working as expected
     */
    public void testManyConnection() throws SQLException {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var mySQLDatabaseConfig = Singleton.getDatabaseConfig();

        for (var i=0; i< 10_000; i++) {
            var connection = mySQLDatabaseConfig.getConnection();
            connection.close();
        }
    }
}
