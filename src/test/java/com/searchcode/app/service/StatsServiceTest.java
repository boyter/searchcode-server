package com.searchcode.app.service;

import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;
import com.searchcode.app.util.Helpers;
import junit.framework.TestCase;
import org.mockito.Mockito;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StatsServiceTest extends TestCase {
    public void testIncrementSearchCount() {
        Data dataMock = Mockito.mock(Data.class);

        when(dataMock.getDataByName(Values.CACHE_TOTAL_SEARCH, "0")).thenReturn(null);

        StatsService statsService = new StatsService(dataMock, new Helpers());
        statsService.incrementSearchCount();

        verify(dataMock, times(1)).saveData(Values.CACHE_TOTAL_SEARCH, "1");
    }

    public void testIncrementSearchCountTwo() {
        Data dataMock = Mockito.mock(Data.class);

        when(dataMock.getDataByName(Values.CACHE_TOTAL_SEARCH, "0")).thenReturn("100");

        StatsService statsService = new StatsService(dataMock, new Helpers());
        statsService.incrementSearchCount();

        verify(dataMock, times(1)).saveData(Values.CACHE_TOTAL_SEARCH, "101");
    }

    public void testIncrementSearchIntergerOverflow() {
        Data dataMock = Mockito.mock(Data.class);

        when(dataMock.getDataByName(Values.CACHE_TOTAL_SEARCH, "0")).thenReturn("" + Integer.MAX_VALUE);

        StatsService statsService = new StatsService(dataMock, new Helpers());
        statsService.incrementSearchCount();

        verify(dataMock, times(1)).saveData(Values.CACHE_TOTAL_SEARCH, "1");
    }
}
