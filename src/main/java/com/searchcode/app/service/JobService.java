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
import com.searchcode.app.dao.Repo;
import com.searchcode.app.jobs.DeleteRepositoryJob;
import com.searchcode.app.jobs.PopulateSpellingCorrectorJob;
import com.searchcode.app.jobs.enqueue.EnqueueFileRepositoryJob;
import com.searchcode.app.jobs.enqueue.EnqueueRepositoryJob;
import com.searchcode.app.jobs.repository.IndexDocumentsJob;
import com.searchcode.app.jobs.repository.IndexFileRepoJob;
import com.searchcode.app.jobs.repository.IndexGitRepoJob;
import com.searchcode.app.jobs.repository.IndexSvnRepoJob;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.Properties;
import com.searchcode.app.util.UniqueRepoQueue;
import org.apache.commons.io.FileUtils;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Starts all of the quartz jobs which perform background tasks such as cloning/updating from GIT/SVN and
 * the jobs which delete repositories and which add repositories to the queue to be indexed.
 *
 * TODO implement using below for the stopping and starting of jobs
 * http://stackoverflow.com/questions/7159080/how-to-interrupt-or-stop-currently-running-quartz-job#7159719
 */
public class JobService {

    private final Helpers helpers;
    private int UPDATETIME;
    private int FILEINDEXUPDATETIME;
    private int INDEXTIME;
    private int NUMBERGITPROCESSORS = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.NUMBER_GIT_PROCESSORS, Values.DEFAULT_NUMBER_GIT_PROCESSORS), Values.DEFAULT_NUMBER_GIT_PROCESSORS);
    private int NUMBERSVNPROCESSORS = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.NUMBER_SVN_PROCESSORS, Values.DEFAULT_NUMBER_SVN_PROCESSORS), Values.DEFAULT_NUMBER_SVN_PROCESSORS);
    private int NUMBERFILEPROCESSORS = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.NUMBER_FILE_PROCESSORS, Values.DEFAULT_NUMBER_FILE_PROCESSORS), Values.DEFAULT_NUMBER_FILE_PROCESSORS);;

    private String REPOLOCATION = Properties.getProperties().getProperty(Values.REPOSITORYLOCATION, Values.DEFAULTREPOSITORYLOCATION);
    private String TRASHLOCATION = Properties.getProperties().getProperty(Values.TRASH_LOCATION, Values.DEFAULT_TRASH_LOCATION);
    private boolean LOWMEMORY = Boolean.parseBoolean(com.searchcode.app.util.Properties.getProperties().getProperty(Values.LOWMEMORY, Values.DEFAULTLOWMEMORY));
    private boolean SVNENABLED = Boolean.parseBoolean(com.searchcode.app.util.Properties.getProperties().getProperty(Values.SVNENABLED, Values.DEFAULTSVNENABLED));

    public JobService() {
        this.helpers = Singleton.getHelpers();
        this.UPDATETIME = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.CHECKREPOCHANGES, Values.DEFAULTCHECKREPOCHANGES), Values.DEFAULTCHECKREPOCHANGES);
        this.FILEINDEXUPDATETIME = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.CHECKFILEREPOCHANGES, Values.DEFAULTCHECKFILEREPOCHANGES), Values.DEFAULTCHECKFILEREPOCHANGES);
        this.INDEXTIME = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.INDEXTIME, Values.DEFAULTINDEXTIME), Values.DEFAULTINDEXTIME);
    }

    /**
     * Creates a git repo indexer job which will pull from the list of git repositories and start
     * indexing them
     */
    public void startIndexGitRepoJobs(String uniquename) {
        try {
            Scheduler scheduler = Singleton.getScheduler();


            JobDetail job = newJob(IndexGitRepoJob.class)
                    .withIdentity("updateindex-git-" + uniquename)
                    .build();

            SimpleTrigger trigger = newTrigger()
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

            scheduler.scheduleJob(job, trigger);

            scheduler.start();
        }
        catch (SchedulerException ex) {
            Singleton.getLogger().severe(String.format("93ef44ae::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }

    /**
     * Creates a file repo indexer job which will pull from the file queue and index
     */
    public void startIndexFileRepoJobs(String uniquename) {
        try {
            Scheduler scheduler = Singleton.getScheduler();


            JobDetail job = newJob(IndexFileRepoJob.class)
                    .withIdentity("updateindex-file-" + uniquename)
                    .build();

            SimpleTrigger trigger = newTrigger()
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

            scheduler.scheduleJob(job, trigger);

            scheduler.start();
        }
        catch (SchedulerException ex) {
            Singleton.getLogger().severe(String.format("c0f207cd::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }

    /**
     * Creates a svn repo indexer job which will pull from the list of git repositories and start
     * indexing them
     */
    public void startIndexSvnRepoJobs(String uniquename) {
        try {
            Scheduler scheduler = Singleton.getScheduler();


            JobDetail job = newJob(IndexSvnRepoJob.class)
                    .withIdentity("updateindex-svn-" + uniquename)
                    .build();

            SimpleTrigger trigger = newTrigger()
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

            scheduler.scheduleJob(job, trigger);

            scheduler.start();
        }
        catch (SchedulerException ex) {
            Singleton.getLogger().severe(String.format("70845099::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }

    /**
     * Starts a background job which pulls all repositories from the database and adds them to the
     * queue to be indexed
     */
    private void startEnqueueJob() {
        try {
            Scheduler scheduler = Singleton.getScheduler();

            // Setup the indexer which runs forever adding documents to be indexed
            JobDetail job = newJob(EnqueueRepositoryJob.class)
                    .withIdentity("enqueuejob")
                    .build();

            SimpleTrigger trigger = newTrigger()
                    .withIdentity("enqueuejob")
                    .withSchedule(
                        simpleSchedule()
                        .withIntervalInSeconds(this.UPDATETIME)
                        .repeatForever()
                    )
                    .withPriority(2)
                    .build();

            scheduler.scheduleJob(job, trigger);
            scheduler.start();


            Scheduler scheduler2 = Singleton.getScheduler();

            // Setup the indexer which runs forever adding documents to be indexed
            JobDetail job2 = newJob(EnqueueFileRepositoryJob.class)
                    .withIdentity("enqueuefilejob")
                    .build();

            SimpleTrigger trigger2 = newTrigger()
                    .withIdentity("enqueuefilejob")
                    .withSchedule(
                        simpleSchedule()
                        .withIntervalInSeconds(this.FILEINDEXUPDATETIME)
                        .repeatForever()
                    )
                    .withPriority(2)
                    .build();

            scheduler2.scheduleJob(job2, trigger2);
            scheduler2.start();
        } catch (SchedulerException ex) {
            Singleton.getLogger().severe(String.format("40f20408::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }

    /**
     * Starts a background job which deletes repositories from the database, index and checked out disk
     */
    private void startDeleteJob() {
        try {
            Scheduler scheduler = Singleton.getScheduler();

            // Setup the indexer which runs forever adding documents to be indexed
            JobDetail job = newJob(DeleteRepositoryJob.class)
                    .withIdentity("deletejob")
                    .build();

            SimpleTrigger trigger = newTrigger()
                    .withIdentity("deletejob")
                    .withSchedule(
                        simpleSchedule()
                        .withIntervalInSeconds(1)
                        .repeatForever()
                    )
                    .withPriority(2)
                    .build();

            scheduler.scheduleJob(job, trigger);
            scheduler.start();
        } catch (SchedulerException ex) {
            Singleton.getLogger().severe(String.format("703d6d7f::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }

    /**
     * Starts a background job which updates the spelling corrector
     */
    private void startSpellingJob() {
        try {
            Scheduler scheduler = Singleton.getScheduler();

            // Setup the indexer which runs forever adding documents to be indexed
            JobDetail job = newJob(PopulateSpellingCorrectorJob.class)
                    .withIdentity("spellingjob")
                    .build();

            SimpleTrigger trigger = newTrigger()
                    .withIdentity("spellingjob")
                    .withSchedule(
                        simpleSchedule()
                        .withIntervalInSeconds(3600)
                        .repeatForever()
                    )
                    .withPriority(1)
                    .build();

            scheduler.scheduleJob(job, trigger);
            scheduler.start();
        } catch (SchedulerException ex) {
            Singleton.getLogger().severe(String.format("6e131da2::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }

    /**
     * Starts all of the above jobs as per their unique requirements
     * TODO fix so this can only run once
     * TODO move the indexer job start into method like the above ones
     */
    public void initialJobs() {
        try {
            this.startDeleteJob();
            this.startSpellingJob();
            this.startIndexerJob();
            this.startRepositoryJobs();
            this.startEnqueueJob();
        } catch (SchedulerException ex) {
            Singleton.getLogger().severe(String.format("8c3cd302::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }

    private void startIndexerJob() throws SchedulerException {
        Scheduler scheduler = Singleton.getScheduler();
        // Setup the indexer which runs forever indexing
        JobDetail job = newJob(IndexDocumentsJob.class)
                .withIdentity("indexerjob")
                .build();

        SimpleTrigger trigger = newTrigger()
                .withIdentity("indexerjob")
                .withSchedule(
                    simpleSchedule()
                    .withIntervalInSeconds(this.INDEXTIME)
                    .repeatForever()
                )
                .withPriority(15)
                .build();

        scheduler.scheduleJob(job, trigger);
        scheduler.start();
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
            Singleton.getScheduler().shutdown();
        } catch (SchedulerException ex) {
            Singleton.getLogger().severe(String.format("12cce757::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }

    private boolean attemptMoveToTrash(String repoLocation, String indexLocation) {
        boolean successful;
        Singleton.getLogger().severe(String.format("e71a5492::searchcode was unable to remove files or folders in the index %s or repository %s they have been moved to trash and must be removed manually", indexLocation, repoLocation));
        successful = true;

        try {
            if (new File(repoLocation).exists()) {
                this.moveDirectoryToTrash(repoLocation);
            }
        }
        catch (IOException ex){
            successful = false;
            Singleton.getLogger().severe(String.format("dfd26713::error in class %s exception %s it is unlikely that searchcode can recover from this remove all please remove the folder %s manually and restart searchcode", ex.getClass(), ex.getMessage(), repoLocation));
        }

        try {
            if (new File(repoLocation).exists()) {
                this.moveDirectoryToTrash(indexLocation);
            }
        }
        catch (IOException ex){
            successful = false;
            Singleton.getLogger().severe(String.format("fa274f76::error in class %s exception %s it is unlikely that searchcode can recover from this remove all please remove the folder %s manually and restart searchcode", ex.getClass(), ex.getMessage(), indexLocation));
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
        if (Singleton.getIndexService().shouldPause(IIndexService.JobType.REPO_ADDER)) {
            return false;
        }

        List<RepoResult> repoResultList = this.helpers.filterRunningAndDeletedRepoJobs(Singleton.getRepo().getAllRepo());
        Singleton.getLogger().info(String.format("ea4fc311::adding %d repositories to be indexed", repoResultList.size()));
        repoResultList.forEach(this::enqueueRepository);

        return true;
    }

    public int forceEnqueueWithCount() {
        // Get all of the repositories and enqueue them
        List<RepoResult> repoResultList = this.helpers.filterRunningAndDeletedRepoJobs(Singleton.getRepo().getAllRepo());
        Singleton.getLogger().info(String.format("de4d4b59::adding %d repositories to be indexed", repoResultList.size()));
        repoResultList.forEach(this::enqueueRepository);

        return repoResultList.size();
    }
    
    public boolean forceEnqueue(RepoResult repoResult) {
        if (Singleton.getIndexService().shouldPause(IIndexService.JobType.REPO_ADDER)) {
            return false;
        }

        if (Singleton.getDataService().getPersistentDelete().contains(repoResult.getName()) ||
                Singleton.getRunningIndexRepoJobs().keySet().contains(repoResult.getName())) {
            return false;
        }

        this.enqueueRepository(repoResult);

        return true;
    }

    private void enqueueRepository(RepoResult rr) {
        UniqueRepoQueue repoGitQueue = Singleton.getUniqueGitRepoQueue();
        UniqueRepoQueue repoSvnQueue = Singleton.getUniqueSvnRepoQueue();
        UniqueRepoQueue repoFileQueue = Singleton.getUniqueFileRepoQueue();

        Singleton.getLogger().info(String.format("e30a1dca::adding %s to %s queue", rr.getName(), rr.getScm()));

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
                Singleton.getLogger().severe(String.format("e9cc3dd6::unknown scm type for %s type %s queue, this should be removed from the list of repositories", rr.getName(), rr.getScm()));
                break;
        }
    }
}
