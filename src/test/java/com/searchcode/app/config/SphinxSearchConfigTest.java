package com.searchcode.app.config;

import com.searchcode.app.service.Singleton;
import junit.framework.TestCase;

import java.sql.Connection;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class SphinxSearchConfigTest extends TestCase {
    public void testConnectionSphinx() throws Exception {
        if (Singleton.getHelpers().isLocalInstance()) return;

        var ssc = new SphinxSearchConfig();
        var connection = ssc.getConnection("127.0.0.1");
        assertThat(connection.get()).isNotNull();
    }

    public void testMultipleConnectionSphinx() throws Exception {
        if (Singleton.getHelpers().isLocalInstance()) return;

        var ssc = new SphinxSearchConfig();

        for (int i = 0; i< 1000; i++) {
            var connection = ssc.getConnection("127.0.0.1");
            assertThat(connection.get()).isNotNull();
        }
    }
}
