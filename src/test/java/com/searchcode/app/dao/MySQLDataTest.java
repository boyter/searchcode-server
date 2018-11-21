package com.searchcode.app.dao;

import com.searchcode.app.config.MySQLDatabaseConfig;
import com.searchcode.app.config.SQLiteMemoryDatabaseConfig;
import com.searchcode.app.config.Values;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;
import junit.framework.TestCase;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.Random;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * Tests the Data DAO when running against MySQL as an integration test
 * as it requires that MySQL be running and connections work.
 * This is used as part of the searchcode.com test suite.
 */
public class MySQLDataTest extends TestCase {

    private Data data;

    public void setUp() throws Exception {
        super.setUp();
        this.data = new Data(new MySQLDatabaseConfig(), new Helpers(), new LoggerWrapper());
    }

    public void testDataSaveUpdate() {
        if (Singleton.getHelpers().isLocalInstance()) return;

        var expected = Values.EMPTYSTRING + System.currentTimeMillis();
        var actual = this.data.getDataByName("testDataSaveUpdate");
        assertThat(actual).as("Checking value before saving").isNotEqualTo(expected);

        var isNew = this.data.saveData("testDataSaveUpdate", expected);
        actual = this.data.getDataByName("testDataSaveUpdate");
        assertThat(actual).as("Checking value after saving isNew=%s, actual=%s, expected=%s", isNew, actual, expected).isEqualTo(expected);
    }

    public void testGetWithoutSave() {
        if (Singleton.getHelpers().isLocalInstance()) return;

        var actual = data.getDataByName("testSingleSaveManyGet");
        for (int i = 0; i < 100; i++) {
            assertThat(actual).as("Get without save").isEqualTo(data.getDataByName("testSingleSaveManyGet"));
            assertThat(actual).as("Get without save").isEqualTo(data.getDataByName("testSingleSaveManyGet", actual));
        }
    }

    public void testGetWithRandomValuesExpectingNull() {
        if (Singleton.getHelpers().isLocalInstance()) return;

        var random = new Random();

        for (int i = 0; i < 100; i++) {
            var actual = data.getDataByName(RandomStringUtils.randomAscii(random.nextInt(20) + 20));
            assertThat(actual).isNull();
        }
    }

    public void testSingleSaveManyGet() {
        if (Singleton.getHelpers().isLocalInstance()) return;

        var expected = "" + System.currentTimeMillis();
        data.saveData("testSingleSaveManyGet", expected);

        for (int i = 0; i < 100; i++) {
            assertThat(expected).as("Get with no default").isEqualTo(data.getDataByName("testSingleSaveManyGet"));
            assertThat(expected).as("Get with default").isEqualTo(data.getDataByName("testSingleSaveManyGet", "default"));
        }
    }

    /**
     * Stress test the saving to check if we are closing connections properly
     */
    public void testManySaveAndGet() {
        if (Singleton.getHelpers().isLocalInstance()) return;

        for (int i = 0; i < 100; i++) {
            var expected = "" + System.currentTimeMillis();
            data.saveData("testManySaveAndGet", expected);

            assertThat(expected).as("Get with no default").isEqualTo(data.getDataByName("testManySaveAndGet"));
            assertThat(expected).as("Get with default").isEqualTo(data.getDataByName("testManySaveAndGet", "default"));
        }
    }

    public void testDefaultReturns() {
        if (Singleton.getHelpers().isLocalInstance()) return;

        var actual = Double.parseDouble(data.getDataByName("THISSHOULDNEVEREXISTIHOPE", "0"));
        assertThat(actual).isEqualTo(0);
    }

    public void testGetAllData() {
        if (Singleton.getHelpers().isLocalInstance()) return;

        data.saveData("testAllData", "anything");

        var actual = data.getAllData();
        assertThat(actual.size()).isGreaterThanOrEqualTo(1);
    }

    public void testValuesParallel() {
        var someList = new ArrayList<String>();

        for (int i = 0; i < 100; i++) {
            someList.add("" + i);
        }

        someList.parallelStream()
                .forEach(x -> {
                    this.data.getDataByName(x);
                    this.data.getDataByName(x, "");
                    this.data.getAllData();
                });
    }
}
