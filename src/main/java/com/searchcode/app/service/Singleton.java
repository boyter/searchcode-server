/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.service;

import com.searchcode.app.config.IDatabaseConfig;
import com.searchcode.app.config.SQLiteDatabaseConfig;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.*;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.dto.RunningIndexJob;
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

    private static SearchCodeLib searchcodeLib = null;
    private static FileClassifier fileClassifier = null;
    private static LoggerWrapper loggerWrapper = null;
    private static Scheduler scheduler = null;
    private static Repo repo = null;
    private static Data data = null;
    private static Api api = null;
    private static SourceCode sourceCode = null;
    private static LanguageType languageType = null;
    private static ApiService apiService = null;
    private static DataService dataService = null;
    private static TimeSearchRouteService timeSearchRouteService = null;
    private static StatsService statsService = null;
    private static JobService jobService = null;
    private static IDatabaseConfig databaseConfig = null;
    private static Helpers helpers = null;
    private static ValidatorService validatorService = null;

    private static IIndexService indexService = null;
    private static CodeMatcher codematcher = null;
    private static SlocCounter slocCounter = null;

    private static OWASPClassifier owaspClassifier = null;
    private static RepositorySource repositorySource = null;

    private static UniqueRepoQueue uniqueGitRepoQueue = null; // Used to queue the next repository to be indexed
    private static UniqueRepoQueue uniqueFileRepoQueue = null; // Used to queue the next repository to be indexed
    private static UniqueRepoQueue uniqueSvnRepoQueue = null; // Used to queue the next repository to be indexed

    private static boolean enqueueRepositoryJobFirstRun = true;
    private static boolean enqueueFileRepositoryJobFirstRun = true;


    public static synchronized void setEnqueueRepositoryJobFirstRun(boolean value) {
        enqueueRepositoryJobFirstRun = value;
    }

    public static synchronized void setEnqueueFileRepositoryJob(boolean value) {
        enqueueFileRepositoryJobFirstRun = value;
    }

    public static synchronized boolean getEnqueueRepositoryJobFirstRun() {
        return enqueueRepositoryJobFirstRun;
    }

    public static synchronized boolean getEnqueueFileRepositoryJobFirstRun() {
        return enqueueFileRepositoryJobFirstRun;
    }

    public static Timer getNewTimer() {
        return new Timer();
    }

    public static synchronized CodeMatcher getCodeMatcher() {
        if (codematcher == null) {
            codematcher = new CodeMatcher();
        }
        return codematcher;
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

    public static synchronized IIndexService getIndexService() {
        if (indexService == null) {
            String index = Properties.getProperties().getProperty(Values.INDEX_SERVICE, Values.DEFAULT_INDEX_SERVICE);

            switch (index) {
                case "sphinx":
                    indexService = new SphinxIndexService();
                    break;
                case "internal":
                default:
                    indexService = new IndexService();
                    break;
            }
        }

        return indexService;
    }

    public static synchronized SlocCounter getSlocCounter() {
        if (slocCounter == null) {
            slocCounter = new SlocCounter();
        }

        return slocCounter;
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

    public static synchronized RepositorySource getRepositorySource() {
        if (repositorySource == null) {
            repositorySource = new RepositorySource();
        }

        return repositorySource;
    }

    public static synchronized OWASPClassifier getOwaspClassifier() {
        if (owaspClassifier == null) {
            owaspClassifier = new OWASPClassifier();
        }

        return owaspClassifier;
    }

    public static synchronized Queue<CodeIndexDocument> getCodeIndexQueue() {
        if (codeIndexQueue == null) {
            codeIndexQueue = new ConcurrentLinkedQueue<>();
        }

        return codeIndexQueue;
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

    public static synchronized SearchCodeLib getSearchCodeLib() {
        if (searchcodeLib == null) {
            searchcodeLib = new SearchCodeLib();
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
    public static synchronized SearchCodeLib getSearchcodeLib() {
        searchcodeLib = new SearchCodeLib();

        return searchcodeLib;
    }


    public static synchronized StatsService getStatsService() {
        if (statsService == null) {
            statsService = new StatsService();
        }

        return statsService;
    }

    public static synchronized ValidatorService getValidatorService() {
        if (validatorService == null) {
            validatorService = new ValidatorService();
        }

        return validatorService;
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

    public static synchronized LanguageType getLanguageType() {
        if (languageType == null) {
            languageType = new LanguageType();
        }

        return languageType;
    }

    public static synchronized SourceCode getSourceCode() {
        if (sourceCode == null) {
            sourceCode = new SourceCode();
        }

        return sourceCode;
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
