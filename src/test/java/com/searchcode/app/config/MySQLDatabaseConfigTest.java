package com.searchcode.app.config;

import com.searchcode.app.service.Singleton;
import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class MySQLDatabaseConfigTest extends TestCase {
    public void testConnection() throws SQLException {
        if (Singleton.getHelpers().isLocalInstance()) return;

        MySQLDatabaseConfig mySQLDatabaseConfig = new MySQLDatabaseConfig();
        mySQLDatabaseConfig.getConnection();
    }
}
