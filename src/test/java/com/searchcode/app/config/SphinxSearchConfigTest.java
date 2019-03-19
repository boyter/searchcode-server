package com.searchcode.app.config;

import com.searchcode.app.service.Singleton;
import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.ArrayList;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class SphinxSearchConfigTest extends TestCase {
    public void testConnectionSphinx() throws Exception {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var ssc = new SphinxSearchConfig();
        var connection = ssc.getDefaultConnection();
        assertThat(connection.isEmpty()).isFalse();
        connection.ifPresent(x -> Singleton.getHelpers().closeQuietly(x));
    }

    public void testMultipleConnectionSphinx() throws Exception {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var ssc = new SphinxSearchConfig();

        for (int i = 0; i < 1000; i++) {
            var connection = ssc.getDefaultConnection();
            assertThat(connection.isEmpty()).isFalse();
            connection.ifPresent(x -> Singleton.getHelpers().closeQuietly(x));
        }
    }

    public void testGetConnectionParallel() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var stringTypes = new ArrayList<SphinxSearchConfig>();
        for (int i = 0; i < 100; i++) {
            stringTypes.add(new SphinxSearchConfig());
        }

        stringTypes.parallelStream().forEach(x -> {
            try {
                var con = x.getDefaultConnection();
                assertThat(con.isEmpty()).isFalse();
                con.ifPresent(y -> Singleton.getHelpers().closeQuietly(y));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void testGetAllConnection() throws SQLException {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var ssc = new SphinxSearchConfig();
        ssc.setSphinxServersShards("localhost:1,2");

        var con = ssc.getAllConnection();

        assertThat(con.size()).isEqualTo(2);
        for (var key : con.keySet()) {
            con.get(key).ifPresent(x -> Singleton.getHelpers().closeQuietly(x));
        }
    }

    public void testGetAllConnectionMultiple() throws SQLException {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var ssc = new SphinxSearchConfig();
        ssc.setSphinxServersShards("localhost:1,2,3,4;127.0.0.1:5,6,7,8");

        var con = ssc.getAllConnection();

        assertThat(con.size()).isEqualTo(8);
        assertThat(con.get("1")).isNotEqualTo(con.get("8"));

        for (var key : con.keySet()) {
            con.get(key).ifPresent(x -> Singleton.getHelpers().closeQuietly(x));
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

    public void testGetServerForShard() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var ssc = new SphinxSearchConfig();
        ssc.setSphinxServersShards("localhost:1,2,3,4;localhost2:5,6,7,8");
        assertThat(ssc.getServerForShard(1)).isEqualTo("localhost");
        assertThat(ssc.getServerForShard(2)).isEqualTo("localhost");
        assertThat(ssc.getServerForShard(3)).isEqualTo("localhost");
        assertThat(ssc.getServerForShard(4)).isEqualTo("localhost");

        assertThat(ssc.getServerForShard(5)).isEqualTo("localhost2");
        assertThat(ssc.getServerForShard(6)).isEqualTo("localhost2");
        assertThat(ssc.getServerForShard(7)).isEqualTo("localhost2");
        assertThat(ssc.getServerForShard(8)).isEqualTo("localhost2");
    }
}
