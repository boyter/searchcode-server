/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.1
 */

package com.searchcode.app.service;

import com.google.inject.Inject;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.IRepo;
import com.searchcode.app.jobs.*;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.util.LoggerWrapper;
import com.searchcode.app.util.Properties;
import com.searchcode.app.util.UniqueRepoQueue;
import org.apache.commons.io.FileUtils;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Starts all of the quartz jobs which perform background tasks such as cloning/updating from GIT/SVN and
 * the jobs which delete repositories and which add repositories to the queue to be indexed.
 */
public class JobService implements IJobService {

    private static final LoggerWrapper LOGGER = Singleton.getLogger();

    private IRepo repo = null;
    private int UPDATETIME = 600;
    private int FILEINDEXUPDATETIME = 3600;
    private int INDEXTIME = 1; // TODO allow this to be configurable
    private int NUMBERPROCESSORS = 5; // TODO allow this to be configurable

    private String REPOLOCATION = Properties.getProperties().getProperty(Values.REPOSITORYLOCATION, Values.DEFAULTREPOSITORYLOCATION);
    private boolean LOWMEMORY = Boolean.parseBoolean(com.searchcode.app.util.Properties.getProperties().getProperty(Values.LOWMEMORY, Values.DEFAULTLOWMEMORY));
    private boolean SVNENABLED = Boolean.parseBoolean(com.searchcode.app.util.Properties.getProperties().getProperty(Values.SVNENABLED, Values.DEFAULTSVNENABLED));

    @Inject
    public JobService(IRepo repo) {
        this.repo = repo;
        try {
            this.UPDATETIME = Integer.parseInt(Properties.getProperties().getProperty(Values.CHECKREPOCHANGES, Values.DEFAULTCHECKREPOCHANGES));
        }
        catch(NumberFormatException ex) {}

        try {
            this.FILEINDEXUPDATETIME = Integer.parseInt(Properties.getProperties().getProperty(Values.CHECKFILEREPOCHANGES, Values.DEFAULTCHECKFILEREPOCHANGES));
        }
        catch(NumberFormatException ex) {}
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
                    .withSchedule(simpleSchedule()
                                    .withIntervalInSeconds(this.INDEXTIME)
                                    .repeatForever()
                    )
                    .build();

            job.getJobDataMap().put("REPOLOCATIONS", this.REPOLOCATION);
            job.getJobDataMap().put("LOWMEMORY", this.LOWMEMORY);

            scheduler.scheduleJob(job, trigger);

            scheduler.start();
        }
        catch(SchedulerException ex) {
            LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
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
                    .withSchedule(simpleSchedule()
                                    .withIntervalInSeconds(this.INDEXTIME)
                                    .repeatForever()
                    )
                    .build();

            job.getJobDataMap().put("REPOLOCATIONS", this.REPOLOCATION);
            job.getJobDataMap().put("LOWMEMORY", this.LOWMEMORY);

            scheduler.scheduleJob(job, trigger);

            scheduler.start();
        }
        catch(SchedulerException ex) {
            LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
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
                    .withSchedule(simpleSchedule()
                                    .withIntervalInSeconds(this.INDEXTIME)
                                    .repeatForever()
                    )
                    .build();

            job.getJobDataMap().put("REPOLOCATIONS", this.REPOLOCATION);
            job.getJobDataMap().put("LOWMEMORY", this.LOWMEMORY);

            scheduler.scheduleJob(job, trigger);

            scheduler.start();
        }
        catch(SchedulerException ex) {
            LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
    }

    /**
     * Starts a background job which pulls all repositories from the database and adds them to the
     * queue to be indexed
     */
    public void startEnqueueJob() {
        try {
            Scheduler scheduler = Singleton.getScheduler();

            // Setup the indexer which runs forever adding documents to be indexed
            JobDetail job = newJob(EnqueueRepositoryJob.class)
                    .withIdentity("enqueuejob")
                    .build();

            SimpleTrigger trigger = newTrigger()
                    .withIdentity("enqueuejob")
                    .withSchedule(simpleSchedule()
                                    .withIntervalInSeconds(this.UPDATETIME)
                                    .repeatForever()
                    )
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
                    .withSchedule(simpleSchedule()
                                    .withIntervalInSeconds(this.FILEINDEXUPDATETIME)
                                    .repeatForever()
                    )
                    .build();

            scheduler2.scheduleJob(job2, trigger2);
            scheduler2.start();
        }  catch(SchedulerException ex) {
            LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
    }

    /**
     * Starts a background job which deletes repositories from the database, index and checked out disk
     */
    public void startDeleteJob() {
        try {
            Scheduler scheduler = Singleton.getScheduler();

            // Setup the indexer which runs forever adding documents to be indexed
            JobDetail job = newJob(DeleteRepositoryJob.class)
                    .withIdentity("deletejob")
                    .build();

            SimpleTrigger trigger = newTrigger()
                    .withIdentity("deletejob")
                    .withSchedule(simpleSchedule()
                                    .withIntervalInSeconds(1)
                                    .repeatForever()
                    )
                    .build();

            scheduler.scheduleJob(job, trigger);
            scheduler.start();
        }  catch(SchedulerException ex) {
            LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
    }

    /**
     * Starts all of the above jobs as per their unique requirements
     * TODO fix so this can only run once
     * TODO move the indexer job start into method like the above ones
     */
    @Override
    public void initialJobs() {
        try {
            Scheduler scheduler = Singleton.getScheduler();

            List<RepoResult> repoResults = this.repo.getAllRepo();

            // Create a pool of crawlers which read from the queue
            for(int i=0; i< this.NUMBERPROCESSORS; i++) {
                this.startIndexGitRepoJobs("" + i);
                if (SVNENABLED) {
                    this.startIndexSvnRepoJobs("" + i);
                }
            }

            // Single file index job
            this.startIndexFileRepoJobs("1");

            if(repoResults.size() == 0) {
                LOGGER.info("      \n///////////////////////////////////////////////////////////////////////////\n      // You have no repositories set to index. Add some using the admin page. //\n      // Browse to the admin page and manually add some repositories to index. //\n      ///////////////////////////////////////////////////////////////////////////");
            }

            // Setup the job which queues things to be downloaded and then indexed
            startEnqueueJob();
            // Setup the job which deletes repositories
            startDeleteJob();

            // Setup the indexer which runs forever indexing
            JobDetail job = newJob(IndexDocumentsJob.class)
                    .withIdentity("indexerjob")
                    .build();

            SimpleTrigger trigger = newTrigger()
                    .withIdentity("indexerjob")
                    .withSchedule(simpleSchedule()
                                    .withIntervalInSeconds(this.INDEXTIME)
                                    .repeatForever()
                    )
                    .build();

            scheduler.scheduleJob(job, trigger);
            scheduler.start();
        } catch(SchedulerException ex) {
            LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
    }

    @Override
    public boolean rebuildAll() {
        // Turn off everything
        Singleton.getLogger().info("Recrawl and rebuild of index starting");
        Singleton.setBackgroundJobsEnabled(false);
        try { Thread.sleep(2000); } catch (InterruptedException e) {} // TODO Ruzzz Fix this hack. Check flag ex. allJobsStopped
        int attempt = 0;
        boolean successful = false;

        String repoLocation = Properties.getProperties().getProperty(Values.REPOSITORYLOCATION, Values.DEFAULTREPOSITORYLOCATION);
        String indexLocation = Properties.getProperties().getProperty(Values.INDEXLOCATION, Values.DEFAULTINDEXLOCATION);

        while (attempt < 3) {
            try {
                FileUtils.deleteDirectory(new File(repoLocation));
                FileUtils.deleteDirectory(new File(indexLocation)); // Maybe use index.deleteAll?
                successful = true;
                Singleton.setBackgroundJobsEnabled(true);
                Singleton.getLogger().info("Recrawl and rebuild of index sucessful");
                break;
            } catch (IOException ex) {
                Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  "\n with message: " + ex.getMessage());
            }

            try { Thread.sleep(2000); } catch (InterruptedException e) {}
        }

        return successful;
    }

    @Override
    public boolean forceEnqueue() {
        if (Singleton.getBackgroundJobsEnabled() == false) {
            return false;
        }

        UniqueRepoQueue repoGitQueue = Singleton.getUniqueGitRepoQueue();
        UniqueRepoQueue repoSvnQueue = Singleton.getUniqueSvnRepoQueue();
        UniqueRepoQueue repoFileQueue = Singleton.getUniqueFileRepoQueue();

        // Get all of the repositories and enqueue them
        List<RepoResult> repoResultList = Singleton.getRepo().getAllRepo();
        Singleton.getLogger().info("Adding repositories to be indexed. " + repoResultList.size());
        for(RepoResult rr: repoResultList) {
            switch (rr.getScm().toLowerCase()) {
                case "git":
                    Singleton.getLogger().info("Adding to GIT queue " + rr.getName() + " " + rr.getScm());
                    repoGitQueue.add(rr);
                    break;
                case "svn":
                    Singleton.getLogger().info("Adding to SVN queue " + rr.getName() + " " + rr.getScm());
                    repoSvnQueue.add(rr);
                    break;
                case "file":
                    Singleton.getLogger().info("Adding to FILE queue " + rr.getName() + " " + rr.getScm());
                    repoFileQueue.add(rr);
                    break;
            }
        }

        return true;
    }
}
