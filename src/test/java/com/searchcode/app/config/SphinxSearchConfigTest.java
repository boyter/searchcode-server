package com.searchcode.app.config;

import junit.framework.TestCase;

import java.sql.Connection;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class SphinxSearchConfigTest extends TestCase {
    public void testConnectionSphinx() throws Exception {
        SphinxSearchConfig ssc = new SphinxSearchConfig();
        Optional<Connection> connection = ssc.getConnection("127.0.0.1");
        assertThat(connection.get()).isNotNull();
    }
}
