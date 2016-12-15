package com.searchcode.app.service;

import com.searchcode.app.config.Values;
import junit.framework.TestCase;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class StatsServiceTest extends TestCase {
    public void testIncrementSearchCount() {
        int cacheValue = (Integer)Singleton.getGenericCache().getOrDefault(Values.CACHE_TOTAL_SEARCH, 0);
        StatsService statsService = new StatsService();

        statsService.incrementSearchCount();
        int newCacheValue = (Integer)Singleton.getGenericCache().getOrDefault(Values.CACHE_TOTAL_SEARCH, 0);

        assertThat(cacheValue).isNotEqualTo(newCacheValue);
        assertThat(++cacheValue).isEqualTo(newCacheValue);
    }

    public void testIncrementSearchCountOverflow() {
        Singleton.getGenericCache().put(Values.CACHE_TOTAL_SEARCH, Integer.MAX_VALUE);
        StatsService statsService = new StatsService();

        statsService.incrementSearchCount();
        int result = (Integer)Singleton.getGenericCache().getOrDefault(Values.CACHE_TOTAL_SEARCH, 0);

        assertThat(result).isEqualTo(1);
    }
}
