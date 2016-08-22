package com.searchcode.app.dao;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.searchcode.app.config.InjectorConfig;
import junit.framework.TestCase;

public class DataTest extends TestCase {
    public void testRepoSaveUpdate() {
        Injector injector = Guice.createInjector(new InjectorConfig());
        Data data = injector.getInstance(Data.class);

        String expected = "" + System.currentTimeMillis();
        String actual = data.getDataByName("test_case_data_ignore");
        assertFalse(expected.equals(actual));

        data.saveData("test_case_data_ignore", expected);
        actual = data.getDataByName("test_case_data_ignore");
        assertEquals(expected, actual);
    }

    public void testManyGetCacheOk() {
        Injector injector = Guice.createInjector(new InjectorConfig());
        Data data = injector.getInstance(Data.class);

        String expected = "" + System.currentTimeMillis();
        data.saveData("test_case_data_ignore", expected);

        for(int i=0; i<10000; i++) {
            assertEquals(expected, data.getDataByName("test_case_data_ignore"));
            assertEquals(expected, data.getDataByName("test_case_data_ignore", "default"));
        }
    }

    public void testCreateTable() {
        Injector injector = Guice.createInjector(new InjectorConfig());
        Data data = injector.getInstance(Data.class);

        data.createTableIfMissing();
        data.createTableIfMissing();
        data.createTableIfMissing();
        data.createTableIfMissing();
    }
}
