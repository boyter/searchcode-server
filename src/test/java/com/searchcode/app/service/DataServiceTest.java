package com.searchcode.app.service;

import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;
import junit.framework.TestCase;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataServiceTest extends TestCase {

    public void testTestAddToPersistent() {
        Data mockData = Mockito.mock(Data.class);
        DataService dataService = new DataService(mockData);
        when(mockData.getDataByName(Values.PERSISTENT_DELETE_QUEUE, "[]")).thenReturn("[]");

        dataService.addToPersistentDelete("test");
        verify(mockData, times(1)).saveData(any(), any());
    }

    public void testTestRemoveFromPersistent() {
        Data mockData = Mockito.mock(Data.class);
        DataService dataService = new DataService(mockData);
        when(mockData.getDataByName(Values.PERSISTENT_DELETE_QUEUE, "[]")).thenReturn("[]");

        dataService.removeFromPersistentDelete("test");
        verify(mockData, times(1)).saveData(any(), any());
    }
}
