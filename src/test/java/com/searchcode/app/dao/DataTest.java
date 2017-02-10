package com.searchcode.app.dao;

import com.searchcode.app.config.Values;
import com.searchcode.app.service.Singleton;
import junit.framework.TestCase;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class DataTest extends TestCase {

    public DataTest() {
        // Tests need to bootstrap themselves
        Data data = Singleton.getData();
        data.createTableIfMissing();
    }

    public void testDataSaveUpdate() throws InterruptedException {
        Data data = Singleton.getData();

        String expected = Values.EMPTYSTRING + System.currentTimeMillis();
        String actual = data.getDataByName("testDataSaveUpdate");
        assertThat(actual).as("Checking value before saving").isNotEqualTo(expected);

        boolean isNew = data.saveData("testDataSaveUpdate", expected);
        Thread.sleep(10000);
        actual = data.getDataByName("testDataSaveUpdate");
        assertThat(actual).as("Checking value after saving isNew=%s, actual=%s, expected=%s", isNew, actual, expected).isEqualTo(expected);
    }

    public void testSingleSaveManyGet() {
        Data data = Singleton.getData();

        String expected = "" + System.currentTimeMillis();
        data.saveData("testSingleSaveManyGet", expected);

        for(int i=0; i<200; i++) {
            assertThat(expected).as("Get with no default").isEqualTo(data.getDataByName("testSingleSaveManyGet"));
            assertThat(expected).as("Get with default").isEqualTo(data.getDataByName("testSingleSaveManyGet", "default"));
        }
    }

    /**
     * Stress test the saving to check if we are closing connections properly
     */
    public void testManySaveAndGet() {
        Data data = Singleton.getData();

        for(int i=0; i < 200; i++) {
            String expected = "" + System.currentTimeMillis();
            data.saveData("testManySaveAndGet", expected);

            assertThat(expected).as("Get with no default").isEqualTo(data.getDataByName("testManySaveAndGet"));
            assertThat(expected).as("Get with default").isEqualTo(data.getDataByName("testManySaveAndGet", "default"));
        }
    }

    public void testDefaultReturns() {
        Data data = Singleton.getData();
        double actual = Double.parseDouble(data.getDataByName("THISSHOULDNEVEREXISTIHOPE", "0"));
        assertThat(actual).isEqualTo(0);
    }

    public void testCreateTable() {
        Data data = Singleton.getData();

        data.createTableIfMissing();
        data.createTableIfMissing();
        data.createTableIfMissing();
        data.createTableIfMissing();
    }
}
