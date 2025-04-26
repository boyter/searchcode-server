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

import com.searchcode.app.config.Values;
import com.searchcode.app.dao.IRepo;
import com.searchcode.app.jobs.DeleteRepositoryJob;
import com.searchcode.app.jobs.PopulateSpellingCorrectorJob;
import com.searchcode.app.jobs.enqueue.EnqueueFileRepositoryJob;
import com.searchcode.app.jobs.enqueue.EnqueueRepositoryJob;
import com.searchcode.app.jobs.enqueue.EnqueueSearchcodeRepositoryJob;
import com.searchcode.app.jobs.repository.IndexDocumentsJob;
import com.searchcode.app.jobs.repository.IndexFileRepoJob;
import com.searchcode.app.jobs.repository.IndexGitRepoJob;
import com.searchcode.app.jobs.repository.IndexSvnRepoJob;
import com.searchcode.app.jobs.searchcode.ReindexerJob;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.index.IIndexService;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;
import com.searchcode.app.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Starts all of the quartz jobs which perform background tasks such as cloning/updating from GIT/SVN and
 * the jobs which delete repositories and which add repositories to the queue to be indexed.
 * TODO implement using below for the stopping and starting of jobs
 * http://stackoverflow.com/questions/7159080/how-to-interrupt-or-stop-currently-running-quartz-job#7159719
 */
public class JobService {

    private final Helpers helpers;
    private final LoggerWrapper logger;
    private final Scheduler scheduler;
    private final IRepo repo;
    private final DataService dataservice;
    private int UPDATETIME;
    private int FILEINDEXUPDATETIME;
    private int INDEXTIME;
    private int NUMBERGITPROCESSORS = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.NUMBER_GIT_PROCESSORS, Values.DEFAULT_NUMBER_GIT_PROCESSORS), Values.DEFAULT_NUMBER_GIT_PROCESSORS);
    private int NUMBERSVNPROCESSORS = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.NUMBER_SVN_PROCESSORS, Values.DEFAULT_NUMBER_SVN_PROCESSORS), Values.DEFAULT_NUMBER_SVN_PROCESSORS);
    private int NUMBERFILEPROCESSORS = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.NUMBER_FILE_PROCESSORS, Values.DEFAULT_NUMBER_FILE_PROCESSORS), Values.DEFAULT_NUMBER_FILE_PROCESSORS);

    private String REPOLOCATION = Properties.getProperties().getProperty(Values.REPOSITORYLOCATION, Values.DEFAULTREPOSITORYLOCATION);
    private String TRASHLOCATION = Properties.getProperties().getProperty(Values.TRASH_LOCATION, Values.DEFAULT_TRASH_LOCATION);
    private boolean LOWMEMORY = Boolean.parseBoolean(com.searchcode.app.util.Properties.getProperties().getProperty(Values.LOWMEMORY, Values.DEFAULTLOWMEMORY));
    private boolean SVNENABLED = Boolean.parseBoolean(com.searchcode.app.util.Properties.getProperties().getProperty(Values.SVNENABLED, Values.DEFAULTSVNENABLED));
    private String HIGHLIGHTER_BINARY_LOCATION = Properties.getProperties().getProperty(Values.HIGHLIGHTER_BINARY_LOCATION, Values.DEFAULT_HIGHLIGHTER_BINARY_LOCATION);
    private boolean initialJobsRun = false;

    public JobService() {
        this.logger = Singleton.getLogger();
        this.scheduler = Singleton.getScheduler();
        this.helpers = Singleton.getHelpers();
        this.repo = Singleton.getRepo();
        this.dataservice = Singleton.getDataService();
        this.UPDATETIME = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.CHECKREPOCHANGES, Values.DEFAULTCHECKREPOCHANGES), Values.DEFAULTCHECKREPOCHANGES);
        this.FILEINDEXUPDATETIME = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.CHECKFILEREPOCHANGES, Values.DEFAULTCHECKFILEREPOCHANGES), Values.DEFAULTCHECKFILEREPOCHANGES);
        this.INDEXTIME = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.INDEXTIME, Values.DEFAULTINDEXTIME), Values.DEFAULTINDEXTIME);
    }

    /**
     * Starts all of the above jobs as per their unique requirements between searchcode.com
     * and local runner
     */
    public synchronized void initialJobs() {
        // Having this run multiple times can be an issue so ensure it can not happen
        if (this.initialJobsRun) {
            return;
        }

        this.initialJobsRun = true;

        if (Singleton.getHelpers().isStandaloneInstance()) {
            this.startDeleteJob();
            this.startSpellingJob();
            this.startRepositoryJobs();
            this.startEnqueueJob();
        } else {
            // searchcode.com path
            this.startHighlighter();
            this.startReIndexer();
            this.startRepositoryJobs();
            this.startSearchcodeEnqueueJob();
        }

        // This will determine itself what index to use so no need to if condition it
        this.startIndexerJob();
    }


    /**
     * Creates a git repo indexer job which will pull from the list of git repositories and start
     * indexing them
     */
    public void startIndexGitRepoJobs(String uniquename) {
        try {
            var job = newJob(IndexGitRepoJob.class)
                    .withIdentity("updateindex-git-" + uniquename)
                    .build();

            var trigger = newTrigger()
                    .withIdentity("updateindex-git-" + uniquename)
                    .withSchedule(
                            simpleSchedule()
                                    .withIntervalInSeconds(this.INDEXTIME)
                                    .repeatForever()
                    )
                    .withPriority(1)
                    .build();

            job.getJobDataMap().put("REPOLOCATIONS", this.REPOLOCATION);
            job.getJobDataMap().put("LOWMEMORY", this.LOWMEMORY);

            this.scheduler.scheduleJob(job, trigger);
            this.scheduler.start();
        } catch (SchedulerException ex) {
            this.logger.severe(String.format("93ef44ae::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }

    /**
     * Creates a file repo indexer job which will pull from the file queue and index
     */
    public void startIndexFileRepoJobs(String uniquename) {
        try {
            var job = newJob(IndexFileRepoJob.class)
                    .withIdentity("updateindex-file-" + uniquename)
                    .build();

            var trigger = newTrigger()
                    .withIdentity("updateindex-file-" + uniquename)
                    .withSchedule(
                            simpleSchedule()
                                    .withIntervalInSeconds(this.INDEXTIME)
                                    .repeatForever()
                    )
                    .withPriority(1)
                    .build();

            job.getJobDataMap().put("REPOLOCATIONS", this.REPOLOCATION);
            job.getJobDataMap().put("LOWMEMORY", this.LOWMEMORY);

            this.scheduler.scheduleJob(job, trigger);
            this.scheduler.start();
        } catch (SchedulerException ex) {
            this.logger.severe(String.format("c0f207cd::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }

    /**
     * Creates a svn repo indexer job which will pull from the list of git repositories and start
     * indexing them
     */
    public void startIndexSvnRepoJobs(String uniquename) {
        try {
            var job = newJob(IndexSvnRepoJob.class)
                    .withIdentity("updateindex-svn-" + uniquename)
                    .build();

            var trigger = newTrigger()
                    .withIdentity("updateindex-svn-" + uniquename)
                    .withSchedule(
                            simpleSchedule()
                                    .withIntervalInSeconds(this.INDEXTIME)
                                    .repeatForever()
                    )
                    .withPriority(1)
                    .build();

            job.getJobDataMap().put("REPOLOCATIONS", this.REPOLOCATION);
            job.getJobDataMap().put("LOWMEMORY", this.LOWMEMORY);

            this.scheduler.scheduleJob(job, trigger);
            this.scheduler.start();
        } catch (SchedulerException ex) {
            this.logger.severe(String.format("70845099::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }

    /**
     * Starts a background job which pulls all repositories from the database and adds them to the
     * queue to be indexed
     */
    private void startEnqueueJob() {
        try {
            // Setup the indexer which runs forever adding documents to be indexed
            var job = newJob(EnqueueRepositoryJob.class)
                    .withIdentity("enqueuejob")
                    .build();

            var trigger = newTrigger()
                    .withIdentity("enqueuejob")
                    .withSchedule(
                            simpleSchedule()
                                    .withIntervalInSeconds(this.UPDATETIME)
                                    .repeatForever()
                    )
                    .withPriority(2)
                    .build();

            this.scheduler.scheduleJob(job, trigger);
            this.scheduler.start();

            // Setup the indexer which runs forever adding documents to be indexed
            var job2 = newJob(EnqueueFileRepositoryJob.class)
                    .withIdentity("enqueuefilejob")
                    .build();

            var trigger2 = newTrigger()
                    .withIdentity("enqueuefilejob")
                    .withSchedule(
                            simpleSchedule()
                                    .withIntervalInSeconds(this.FILEINDEXUPDATETIME)
                                    .repeatForever()
                    )
                    .withPriority(2)
                    .build();

            this.scheduler.scheduleJob(job2, trigger2);
            this.scheduler.start();
        } catch (SchedulerException ex) {
            this.logger.severe(String.format("40f20408::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }

    private void startSearchcodeEnqueueJob() {
        try {
            // Setup the indexer which runs forever adding documents to be indexed
            var job = newJob(EnqueueSearchcodeRepositoryJob.class)
                    .withIdentity("enqueuesearchcodejob")
                    .build();

            var trigger = newTrigger()
                    .withIdentity("enqueuesearchcodejob")
                    .withSchedule(
                            simpleSchedule()
                                    .withIntervalInSeconds(this.UPDATETIME)
                                    .repeatForever()
                    )
                    .withPriority(2)
                    .build();

            this.scheduler.scheduleJob(job, trigger);
            this.scheduler.start();
        } catch (SchedulerException ex) {
            this.logger.severe(String.format("9c4b9ccc::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }

    /**
     * Starts a background job which deletes repositories from the database, index and checked out disk
     */
    private void startDeleteJob() {
        try {
            var job = newJob(DeleteRepositoryJob.class)
                    .withIdentity("deletejob")
                    .build();

            var trigger = newTrigger()
                    .withIdentity("deletejob")
                    .withSchedule(
                            simpleSchedule()
                                    .withIntervalInSeconds(1)
                                    .repeatForever()
                    )
                    .withPriority(2)
                    .build();

            this.scheduler.scheduleJob(job, trigger);
            this.scheduler.start();
        } catch (SchedulerException ex) {
            this.logger.severe(String.format("703d6d7f::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }

    /**
     * Starts a background job which updates the spelling corrector
     */
    private void startSpellingJob() {
        try {
            var job = newJob(PopulateSpellingCorrectorJob.class)
                    .withIdentity("spellingjob")
                    .build();

            var trigger = newTrigger()
                    .withIdentity("spellingjob")
                    .withSchedule(
                            simpleSchedule()
                                    .withIntervalInSeconds(3600)
                                    .repeatForever()
                    )
                    .withPriority(1)
                    .build();

            this.scheduler.scheduleJob(job, trigger);
            this.scheduler.start();
        } catch (SchedulerException ex) {
            this.logger.severe(String.format("6e131da2::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }

    /**
     * This job runs in the background connecting to the searchcode.com database
     * pulling out files and adding them to the queue to be indexed
     */
    public void startReIndexer() {
        var job = newJob(ReindexerJob.class)
                .withIdentity("reindexer")
                .build();

        var trigger = newTrigger()
                .withIdentity("reindexer")
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(this.INDEXTIME)
                        .repeatForever()
                )
                .withPriority(15)
                .build();

        try {
            this.scheduler.scheduleJob(job, trigger);
            this.scheduler.start();
        } catch (SchedulerException ex) {
            this.logger.severe(String.format("5e7b51d8::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }

    /**
     * Starts a background process used to highlight code
     */
    public void startHighlighter() {
        try {
            if (SystemUtils.IS_OS_LINUX) {
                new ProcessExecutor().command(HIGHLIGHTER_BINARY_LOCATION + "/searchcode-server-highlighter-x86_64-unknown-linux").destroyOnExit().start().getFuture();
            } else if (SystemUtils.IS_OS_WINDOWS) {
                new ProcessExecutor().command(HIGHLIGHTER_BINARY_LOCATION + "/searchcode-server-highlighter-x86_64-pc-windows.exe").destroyOnExit().start().getFuture();
            } else if (SystemUtils.IS_OS_MAC) {
                new ProcessExecutor().command(HIGHLIGHTER_BINARY_LOCATION + "/searchcode-server-highlighter-x86_64-apple-darwin").destroyOnExit().start().getFuture();
            }
        } catch (IOException ex) {
            this.logger.severe(String.format("947e8a85::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }

    /**
     * Sets up the indexer job which runs in the background forever
     * indexing files that are added to the index queue
     */
    private void startIndexerJob() {
        var job = newJob(IndexDocumentsJob.class)
                .withIdentity("indexerjob")
                .build();

        var trigger = newTrigger()
                .withIdentity("indexerjob")
                .withSchedule(
                        simpleSchedule()
                                .withIntervalInSeconds(this.INDEXTIME)
                                .repeatForever()
                )
                .withPriority(15)
                .build();

        try {
            this.scheduler.scheduleJob(job, trigger);
            this.scheduler.start();
        } catch (SchedulerException ex) {
            this.logger.severe(String.format("8c3cd302::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }

    private void startRepositoryJobs() {
        // Create a pool of crawlers which read from the queue
        for (int i = 0; i < this.NUMBERGITPROCESSORS; i++) {
            this.startIndexGitRepoJobs(Values.EMPTYSTRING + i);
        }

        if (SVNENABLED) {
            for (int i = 0; i < this.NUMBERSVNPROCESSORS; i++) {
                this.startIndexSvnRepoJobs(Values.EMPTYSTRING + i);
            }
        }

        for (int i = 0; i < this.NUMBERFILEPROCESSORS; i++) {
            this.startIndexFileRepoJobs(Values.EMPTYSTRING + i);
        }
    }

    private void shutdownScheduler() {
        try {
            this.scheduler.shutdown();
        } catch (SchedulerException ex) {
            this.logger.severe(String.format("12cce757::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }

    private boolean attemptMoveToTrash(String repoLocation, String indexLocation) {
        boolean successful;
        this.logger.severe(String.format("e71a5492::searchcode was unable to remove files or folders in the index %s or repository %s they have been moved to trash and must be removed manually", indexLocation, repoLocation));
        successful = true;

        try {
            if (new File(repoLocation).exists()) {
                this.moveDirectoryToTrash(repoLocation);
            }
        } catch (IOException ex) {
            successful = false;
            this.logger.severe(String.format("dfd26713::error in class %s exception %s it is unlikely that searchcode can recover from this remove all please remove the folder %s manually and restart searchcode", ex.getClass(), ex.getMessage(), repoLocation));
        }

        try {
            if (new File(repoLocation).exists()) {
                this.moveDirectoryToTrash(indexLocation);
            }
        } catch (IOException ex) {
            successful = false;
            this.logger.severe(String.format("fa274f76::error in class %s exception %s it is unlikely that searchcode can recover from this remove all please remove the folder %s manually and restart searchcode", ex.getClass(), ex.getMessage(), indexLocation));
        }

        return successful;
    }

    public void moveDirectoryToTrash(String troublesome) throws IOException {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String newLocation = this.TRASHLOCATION + "/" + dateFormat.format(date);
        FileUtils.moveDirectory(new File(troublesome), new File(newLocation));
    }

    public boolean forceEnqueue() {
        // TODO refactor this dependency, because we have circular dependencies
        if (Singleton.getIndexService().shouldPause(IIndexService.JobType.REPO_ADDER)) {
            return false;
        }

        var repoResultList = this.helpers.filterRunningAndDeletedRepoJobs(this.repo.getAllRepo());
        this.logger.info(String.format("ea4fc311::adding %d repositories to be indexed", repoResultList.size()));
        repoResultList.forEach(this::enqueueRepository);

        return true;
    }

    public int forceEnqueueWithCount() {
        // Get all of the repositories and enqueue them
        var repoResultList = this.helpers.filterRunningAndDeletedRepoJobs(this.repo.getAllRepo());
        this.logger.info(String.format("de4d4b59::adding %d repositories to be indexed", repoResultList.size()));
        repoResultList.forEach(this::enqueueRepository);

        return repoResultList.size();
    }

    public boolean forceEnqueue(RepoResult repoResult) {
        // TODO refactor this dependency, because we have circular dependencies
        if (Singleton.getIndexService().shouldPause(IIndexService.JobType.REPO_ADDER)) {
            return false;
        }

        if (this.dataservice.getPersistentDelete().contains(repoResult.getName()) ||
                Singleton.getRunningIndexRepoJobs().containsKey(repoResult.getName())) {
            return false;
        }

        this.enqueueRepository(repoResult);

        return true;
    }

    private void enqueueRepository(RepoResult rr) {
        var repoGitQueue = Singleton.getUniqueGitRepoQueue();
        var repoSvnQueue = Singleton.getUniqueSvnRepoQueue();
        var repoFileQueue = Singleton.getUniqueFileRepoQueue();

        this.logger.info(String.format("e30a1dca::adding %s to %s queue", rr.getName(), rr.getScm()));

        switch (rr.getScm().toLowerCase()) {
            case "git":
                repoGitQueue.add(rr);
                break;
            case "svn":
                repoSvnQueue.add(rr);
                break;
            case "file":
                repoFileQueue.add(rr);
                break;
            default:
                this.logger.severe(String.format("e9cc3dd6::unknown scm type for %s type %s queue, this should be removed from the list of repositories", rr.getName(), rr.getScm()));
                break;
        }
    }
}
