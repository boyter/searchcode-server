package com.searchcode.app.config;

import com.searchcode.app.service.Singleton;
import junit.framework.TestCase;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class SphinxSearchConfigTest extends TestCase {
    public void testConnectionSphinx() throws Exception {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var ssc = new SphinxSearchConfig();
        var connection = ssc.getConnection("127.0.0.1");
        assertThat(connection.get()).isNotNull();
    }

    public void testMultipleConnectionSphinx() throws Exception {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var ssc = new SphinxSearchConfig();

        for (int i = 0; i < 1000; i++) {
            var connection = ssc.getConnection("127.0.0.1");
            assertThat(connection.get()).isNotNull();
        }
    }

    public void testGetShardCountExpectingZero() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var ssc = new SphinxSearchConfig();
        assertThat(ssc.getShardCount("")).isZero();
        assertThat(ssc.getShardCount("localhost:")).isZero();
    }

    public void testGetShardCountExpectingTwo() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var ssc = new SphinxSearchConfig();
        assertThat(ssc.getShardCount("localhost:1,2")).isEqualTo(2);
        assertThat(ssc.getShardCount("localhost:1;localhost:2")).isEqualTo(2);
    }

    public void testGetShardCountExpectingFour() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var ssc = new SphinxSearchConfig();
        assertThat(ssc.getShardCount("localhost:1,2,3,4")).isEqualTo(4);
        assertThat(ssc.getShardCount("localhost:1,2;localhost:3,4")).isEqualTo(4);
    }

    public void testGetShardCountExpectingEight() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var ssc = new SphinxSearchConfig();
        assertThat(ssc.getShardCount("localhost:1,2,3,4,5,6,7,8")).isEqualTo(8);
        assertThat(ssc.getShardCount("localhost:1,2,3,4;localhost:5,6,7,8")).isEqualTo(8);
    }
}
