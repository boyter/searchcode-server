package com.searchcode.app.dao;

import com.searchcode.app.service.Singleton;
import junit.framework.TestCase;

public class DataTest extends TestCase {
    public void testRepoSaveUpdate() {
        Data data = Singleton.getData();

        String expected = "" + System.currentTimeMillis();
        String actual = data.getDataByName("test_case_data_ignore");
        assertFalse(expected.equals(actual));

        data.saveData("test_case_data_ignore", expected);
        actual = data.getDataByName("test_case_data_ignore");
        assertEquals(expected, actual);
    }

    public void testManyGetCacheOk() {
        Data data = Singleton.getData();

        String expected = "" + System.currentTimeMillis();
        data.saveData("test_case_data_ignore", expected);

        for(int i=0; i<10000; i++) {
            assertEquals(expected, data.getDataByName("test_case_data_ignore"));
            assertEquals(expected, data.getDataByName("test_case_data_ignore", "default"));
        }
    }

    public void testCreateTable() {
        Data data = Singleton.getData();

        data.createTableIfMissing();
        data.createTableIfMissing();
        data.createTableIfMissing();
        data.createTableIfMissing();
    }
}
