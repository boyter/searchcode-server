package com.searchcode.app.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class RecentCache<A, B> extends LinkedHashMap<A, B> {
    private final int maxLength;

    public RecentCache(final int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry<A, B> eldest) {
        return this.size() > maxLength;
    }
}
