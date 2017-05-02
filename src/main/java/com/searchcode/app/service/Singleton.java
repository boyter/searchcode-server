/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.10
 */

package com.searchcode.app.service;

import com.searchcode.app.config.IDatabaseConfig;
import com.searchcode.app.config.SQLiteDatabaseConfig;
import com.searchcode.app.dao.Api;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dao.Repo;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.dto.RunningIndexJob;
import com.searchcode.app.model.ApiResult;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.route.TimeSearchRouteService;
import com.searchcode.app.util.*;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import java.util.AbstractMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Lazy Singleton Implementation
 * Generally used to share anything that the Quartz jobs require and to create all the shared objects
 * No idea if it really needs to be lazy other than it saves us creating everything on start although it all
 * gets called pretty quickly anyway so thats probably a moot point
 */
public final class Singleton {

    private static AbstractMap<String, RunningIndexJob> runningIndexRepoJobs = null; // Used to know which jobs are currently running
    private static ISpellingCorrector spellingCorrectorInstance = null;
    private static Queue<CodeIndexDocument> codeIndexQueue = null; // Documents ready to be indexed
    private static int codeIndexLinesCount = 0; // Used to store how many lines we have ready to index for throttling

    private static SearchcodeLib searchcodeLib = null;
    private static FileClassifier fileClassifier = null;
    private static AbstractMap<String, String> dataCache = null;
    private static AbstractMap<String, ApiResult> apiCache = null;
    private static AbstractMap<String, RepoResult> repoCache = null;
    private static AbstractMap<String, Object> genericCache = null;
    private static LoggerWrapper loggerWrapper = null;
    private static Scheduler scheduler = null;
    private static Repo repo = null;
    private static Data data = null;
    private static Api api = null;
    private static ApiService apiService = null;
    private static DataService dataService = null;
    private static TimeSearchRouteService timeSearchRouteService = null;
    private static StatsService statsService = null;
    private static JobService jobService = null;
    private static IDatabaseConfig databaseConfig = null;
    private static CodeIndexer codeIndexer = null;
    private static Helpers helpers = null;

    private static boolean backgroundJobsEnabled = true; // Controls if all background queue jobs should run or not
    private static boolean pauseBackgroundJobs = false; // Controls if all jobs should pause
    private static UniqueRepoQueue uniqueGitRepoQueue = null; // Used to queue the next repository to be indexed
    private static UniqueRepoQueue uniqueFileRepoQueue = null; // Used to queue the next repository to be indexed
    private static UniqueRepoQueue uniqueSvnRepoQueue = null; // Used to queue the next repository to be indexed
    private static UniqueRepoQueue uniqueDeleteRepoQueue = null; // Used to queue the next repository to be deleted

    public static synchronized void incrementCodeIndexLinesCount(int incrementBy) {
        codeIndexLinesCount = codeIndexLinesCount + incrementBy;
    }

    public static synchronized void decrementCodeIndexLinesCount(int decrementBy) {
        codeIndexLinesCount = codeIndexLinesCount - decrementBy;

        if (codeIndexLinesCount < 0) {
            codeIndexLinesCount = 0;
        }
    }

    public static synchronized void setCodeIndexLinesCount(int value) {
        codeIndexLinesCount = value;
    }

    public static synchronized int getCodeIndexLinesCount() {
        return codeIndexLinesCount;
    }

    public static synchronized UniqueRepoQueue getUniqueGitRepoQueue() {
        if (uniqueGitRepoQueue == null) {
            uniqueGitRepoQueue = new UniqueRepoQueue(new ConcurrentLinkedQueue<>());
        }
        return uniqueGitRepoQueue;
    }

    public static synchronized UniqueRepoQueue getUniqueFileRepoQueue() {
        if (uniqueFileRepoQueue == null) {
            uniqueFileRepoQueue = new UniqueRepoQueue(new ConcurrentLinkedQueue<>());
        }
        return uniqueFileRepoQueue;
    }

    public static synchronized UniqueRepoQueue getUniqueSvnRepoQueue() {
        if (uniqueSvnRepoQueue == null) {
            uniqueSvnRepoQueue = new UniqueRepoQueue(new ConcurrentLinkedQueue<>());
        }

        return uniqueSvnRepoQueue;
    }

    public static synchronized UniqueRepoQueue getUniqueDeleteRepoQueue() {
        if (uniqueDeleteRepoQueue == null) {
            uniqueDeleteRepoQueue = new UniqueRepoQueue(new ConcurrentLinkedQueue<>());
        }

        return uniqueDeleteRepoQueue;
    }

    /**
     * Used as cheap attempt to not have all the jobs trying to process the same thing note this has a race condition
     * and should be resolved at some point
     * TODO investigate usage and resolve race conditions
     */
    public static synchronized AbstractMap<String, RunningIndexJob> getRunningIndexRepoJobs() {
        if (runningIndexRepoJobs == null) {
            runningIndexRepoJobs = new ConcurrentHashMap<>();
        }

        return runningIndexRepoJobs;
    }

    public static synchronized Repo getRepo() {
        if (repo == null) {
            repo = new Repo();
        }

        return repo;
    }

    public static synchronized ISpellingCorrector getSpellingCorrector() {
        if (spellingCorrectorInstance == null) {
            spellingCorrectorInstance = new SearchcodeSpellingCorrector();
        }

        return spellingCorrectorInstance;
    }

    public static synchronized Queue<CodeIndexDocument> getCodeIndexQueue() {
        if (codeIndexQueue == null) {
            codeIndexQueue = new ConcurrentLinkedQueue<CodeIndexDocument>();
        }

        return codeIndexQueue;
    }

    public static synchronized AbstractMap<String, String> getDataCache() {
        if (dataCache == null) {
            dataCache = new ConcurrentHashMap<String, String>();
        }

        return dataCache;
    }

    public static synchronized AbstractMap<String, ApiResult> getApiCache() {
        if (apiCache == null) {
            apiCache = new ConcurrentHashMap<String, ApiResult>();
        }

        return apiCache;
    }

    public static synchronized AbstractMap<String, RepoResult> getRepoCache() {
        if (repoCache == null) {
            repoCache = new ConcurrentHashMap<String, RepoResult>();
        }

        return repoCache;
    }

    public static synchronized AbstractMap<String, Object> getGenericCache() {
        if (genericCache == null) {
            genericCache = new ConcurrentHashMap<String, Object>();
        }

        return genericCache;
    }

    public static synchronized Scheduler getScheduler() {

        try {
            if (scheduler == null || scheduler.isShutdown()) {
                try {
                    SchedulerFactory sf = new StdSchedulerFactory();
                    scheduler = sf.getScheduler();
                } catch (SchedulerException ex) {
                    Singleton.getLogger().severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
                }
            }
        } catch (SchedulerException e) {}

        return scheduler;
    }

    public static synchronized LoggerWrapper getLogger() {
        if (loggerWrapper == null) {
            loggerWrapper = new LoggerWrapper();
        }

        return loggerWrapper;
    }

    public static synchronized SearchcodeLib getSearchCodeLib() {
        if (searchcodeLib == null) {
            searchcodeLib = new SearchcodeLib();
        }

        return searchcodeLib;
    }

    public static synchronized TimeSearchRouteService getTimeSearchRouteService() {
        if (timeSearchRouteService == null) {
            timeSearchRouteService = new TimeSearchRouteService();
        }

        return timeSearchRouteService;
    }

    /**
     * Overwrites the internal searchcode lib with the new one which will refresh the data it needs. Mainly used to
     * change the minified settings.
     */
    public static synchronized SearchcodeLib getSearchcodeLib(Data data) {
        searchcodeLib = new SearchcodeLib(data);

        return searchcodeLib;
    }

    public static synchronized boolean getBackgroundJobsEnabled() {
        return backgroundJobsEnabled;
    }

    public static synchronized void setBackgroundJobsEnabled(boolean jobsEnabled) {
        backgroundJobsEnabled = jobsEnabled;
    }


    public static synchronized boolean getPauseBackgroundJobs() {
        return pauseBackgroundJobs;
    }

    public static synchronized void setPauseBackgroundJobs(boolean pauseBackgroundJobs) {
        Singleton.pauseBackgroundJobs = pauseBackgroundJobs;
    }

    public static synchronized StatsService getStatsService() {
        if (statsService == null) {
            statsService = new StatsService();
        }

        return statsService;
    }

    public static synchronized void setStatsService(StatsService statsService) {
        Singleton.statsService = statsService;
    }

    public static synchronized Data getData() {
        if (data == null) {
            data = new Data();
        }

        return data;
    }

    public static synchronized void setData(Data data) {
        Singleton.data = data;
    }

    public static synchronized Api getApi() {
        if (api == null) {
            api = new Api();
        }

        return api;
    }

    public static synchronized void setApi(Api api) {
        Singleton.api = api;
    }

    public static synchronized ApiService getApiService() {
        if (apiService == null) {
            apiService = new ApiService();
        }

        return apiService;
    }

    public static synchronized DataService getDataService() {
        if (dataService == null) {
            dataService = new DataService();
        }

        return dataService;
    }

    public static synchronized void setJobService(JobService jobService) {
        Singleton.jobService = jobService;
    }

    public static synchronized JobService getJobService() {
        if (jobService == null) {
            jobService = new JobService();
        }

        return jobService;
    }

    public static synchronized FileClassifier getFileClassifier() {
        if (fileClassifier == null) {
            fileClassifier = new FileClassifier();
        }

        return fileClassifier;
    }

    public static synchronized IDatabaseConfig getDatabaseConfig() {
        if (databaseConfig == null) {
            databaseConfig = new SQLiteDatabaseConfig();
        }

        return databaseConfig;
    }

    public static synchronized CodeIndexer getCodeIndexer() {
        if (codeIndexer == null) {
            codeIndexer = new CodeIndexer();
        }

        return codeIndexer;
    }

    public static synchronized Helpers getHelpers() {
        if (helpers == null) {
            helpers = new Helpers();
        }

        return helpers;
    }

    public static synchronized void setDatabaseConfig(IDatabaseConfig databaseConfig) {
        Singleton.databaseConfig = databaseConfig;
    }
}
