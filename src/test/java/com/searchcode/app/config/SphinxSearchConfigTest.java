package com.searchcode.app.config;

import com.searchcode.app.service.Singleton;
import junit.framework.TestCase;

import java.sql.Connection;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class SphinxSearchConfigTest extends TestCase {
    public void testConnectionSphinx() throws Exception {
        if (Singleton.getHelpers().isLocalInstance()) return;
        
        SphinxSearchConfig ssc = new SphinxSearchConfig();
        Optional<Connection> connection = ssc.getConnection("127.0.0.1");
        assertThat(connection.get()).isNotNull();
    }
}
