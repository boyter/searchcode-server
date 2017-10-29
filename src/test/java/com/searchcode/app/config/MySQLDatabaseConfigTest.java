package com.searchcode.app.config;

import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class MySQLDatabaseConfigTest extends TestCase {
    public void testConnection() throws SQLException {
        MySQLDatabaseConfig mySQLDatabaseConfig = new MySQLDatabaseConfig();
        mySQLDatabaseConfig.getConnection();
    }

    public void testConnectionSphinx() throws Exception {
        SphinxSearchConfig ssc = new SphinxSearchConfig();
        Optional<Connection> connection = ssc.getConnection("localhost");
        assertThat(connection.get()).isNotNull();
    }
}
