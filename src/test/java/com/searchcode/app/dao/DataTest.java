package com.searchcode.app.dao;

import com.searchcode.app.config.SQLiteMemoryDatabaseConfig;
import com.searchcode.app.config.Values;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;
import junit.framework.TestCase;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class DataTest extends TestCase {

    private Data data;

    public void setUp() throws Exception {
        super.setUp();
        this.data = new Data(new SQLiteMemoryDatabaseConfig(), new Helpers(), new LoggerWrapper());
        this.data.createTableIfMissing();
    }

    public void testDataSaveUpdate() {
        String expected = Values.EMPTYSTRING + System.currentTimeMillis();
        String actual = this.data.getDataByName("testDataSaveUpdate");
        assertThat(actual).as("Checking value before saving").isNotEqualTo(expected);

        boolean isNew = this.data.saveData("testDataSaveUpdate", expected);
        actual = this.data.getDataByName("testDataSaveUpdate");
        assertThat(actual).as("Checking value after saving isNew=%s, actual=%s, expected=%s", isNew, actual, expected).isEqualTo(expected);
    }

    public void testGetWithoutSave() {
        String actual = data.getDataByName("testSingleSaveManyGet");
        for(int i = 0; i < 200; i++) {
            assertThat(actual).as("Get without save").isEqualTo(data.getDataByName("testSingleSaveManyGet"));
            assertThat(actual).as("Get without save").isEqualTo(data.getDataByName("testSingleSaveManyGet", actual));
        }
    }

    public void testGetWithRandomValuesExpectingNull() {
        Random random = new Random();

        for(int i = 0; i < 200; i++) {
            String actual = data.getDataByName(RandomStringUtils.randomAscii(random.nextInt(20) + 20));
            assertThat(actual).isNull();
        }
    }

    public void testSaveWithRandomValuesAndGet() {
        Random random = new Random();

        for(int i = 0; i < 200; i++) {
            String randomString = RandomStringUtils.randomAscii(random.nextInt(5) + 1);
            data.saveData(randomString, randomString);
            String actual = data.getDataByName(randomString);

            assertThat(actual).isEqualTo(randomString);
        }
    }

    public void testSingleSaveManyGet() {
        String expected = "" + System.currentTimeMillis();
        data.saveData("testSingleSaveManyGet", expected);

        for(int i = 0; i < 200; i++) {
            assertThat(expected).as("Get with no default").isEqualTo(data.getDataByName("testSingleSaveManyGet"));
            assertThat(expected).as("Get with default").isEqualTo(data.getDataByName("testSingleSaveManyGet", "default"));
        }
    }

    /**
     * Stress test the saving to check if we are closing connections properly
     */
    public void testManySaveAndGet() {
        for(int i=0; i < 200; i++) {
            String expected = "" + System.currentTimeMillis();
            data.saveData("testManySaveAndGet", expected);

            assertThat(expected).as("Get with no default").isEqualTo(data.getDataByName("testManySaveAndGet"));
            assertThat(expected).as("Get with default").isEqualTo(data.getDataByName("testManySaveAndGet", "default"));
        }
    }

    public void testDefaultReturns() {
        double actual = Double.parseDouble(data.getDataByName("THISSHOULDNEVEREXISTIHOPE", "0"));
        assertThat(actual).isEqualTo(0);
    }

    public void testGetAll() {
        assertThat(data.getAllData().size()).isZero();

        for(int i = 0; i < 200; i++) {
            String randomString = "" + i;
            data.saveData(randomString, randomString);
        }

        assertThat(data.getAllData().size()).isEqualTo(200);
    }

    public void testCreateTable() {
        data.createTableIfMissing();
        data.createTableIfMissing();
        data.createTableIfMissing();
        data.createTableIfMissing();
    }
}
