package com.searchcode.app.service;

import com.searchcode.app.config.Values;
import com.searchcode.app.dto.*;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.model.SourceResult;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Lazy singleton used only for creating the caches that we use throughout the application because
 * cache2k does not like it if you create caches with the same name repeatedly
 */
public final class CacheSingleton {
    private static Cache<String, Object> genericCache = null;
    private static Cache<String, Optional<LanguageTypeDTO>> typeCache = null;
    private static Cache<String, Optional<SourceResult>> sourceCache = null;
    private static Cache<String, HighlighterResponse> highlightCache = null;
    private static Cache<String, Optional<SourceCodeDTO>> sourceCodeCache = null;
    private static Cache<String, Optional<RepoResult>> repoResultCache = null;
    private static Cache<String, ProjectStats> projectStatsCache = null;

    /**
     * This is intended as a generic L1 cache for searchcode which has no network
     * overhead and as such is very fast. Of course it is cleared on service
     * shutdown and intended to be used in collaboration with another process
     * such as memcached or redis.
     */
    public static synchronized Cache<String, Object> getGenericCache() {
        if (genericCache == null) {
            // See https://cache2k.org/ for details
            genericCache = new Cache2kBuilder<String, Object>() {}
                    .name("genericCache")
                    .expireAfterWrite(Values.LOW_CACHE_MINUTES, TimeUnit.MINUTES)
                    .entryCapacity(Values.DEFAULT_CACHE_SIZE)
                    .build();
        }

        return genericCache;
    }

    public static synchronized Cache<String, Optional<LanguageTypeDTO>> getLanguageTypeCache() {
        if (typeCache == null) {
            typeCache = new Cache2kBuilder<String, Optional<LanguageTypeDTO>>() {}
                    .name("typeCache")
                    .expireAfterWrite(Values.HIGH_CACHE_DAYS, TimeUnit.DAYS)
                    .entryCapacity(Values.SMALL_CACHE_SIZE)
                    .build();
        }

        return typeCache;
    }

    public static synchronized Cache<String, Optional<SourceResult>> getSourceCache() {
        if (sourceCache == null) {
            sourceCache = new Cache2kBuilder<String, Optional<SourceResult>>() {}
                    .name("sourceCache")
                    .expireAfterWrite(Values.HIGH_CACHE_DAYS, TimeUnit.DAYS)
                    .entryCapacity(Values.SMALL_CACHE_SIZE)
                    .build();
        }

        return sourceCache;
    }

    public static synchronized Cache<String, HighlighterResponse> getHighlightCache() {
        if (highlightCache == null) {
            highlightCache = new Cache2kBuilder<String, HighlighterResponse>() {}
                    .name("highlightCache")
                    .expireAfterWrite(Values.LOW_CACHE_DAYS, TimeUnit.DAYS)
                    .entryCapacity(Values.DEFAULT_CACHE_SIZE)
                    .build();
        }

        return highlightCache;
    }

    public static synchronized Cache<String, Optional<SourceCodeDTO>> getSourceCodeCache() {
        if (sourceCodeCache == null) {
            sourceCodeCache = new Cache2kBuilder<String, Optional<SourceCodeDTO>>() {}
                    .name("sourceCodeCache")
                    .expireAfterWrite(Values.LOW_CACHE_DAYS, TimeUnit.DAYS)
                    .entryCapacity(Values.DEFAULT_CACHE_SIZE)
                    .build();
        }

        return sourceCodeCache;
    }

    public static synchronized Cache<String, Optional<RepoResult>> getRepoResultCache() {
        if (repoResultCache == null) {
            repoResultCache = new Cache2kBuilder<String, Optional<RepoResult>>() {}
                    .name("repoResultCache")
                    .expireAfterWrite(Values.LOW_CACHE_DAYS, TimeUnit.DAYS)
                    .entryCapacity(Values.DEFAULT_CACHE_SIZE)
                    .build();
        }

        return repoResultCache;
    }

    public static synchronized Cache<String, ProjectStats> getProjectStatsCache() {
        if (projectStatsCache == null) {
            projectStatsCache = new Cache2kBuilder<String, ProjectStats>() {}
                    .name("projectStatsCache")
                    .expireAfterWrite(Values.LOW_CACHE_DAYS, TimeUnit.DAYS)
                    .entryCapacity(Values.DEFAULT_CACHE_SIZE)
                    .build();
        }

        return projectStatsCache;
    }
}
