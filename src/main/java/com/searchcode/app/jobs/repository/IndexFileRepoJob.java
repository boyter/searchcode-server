/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */


package com.searchcode.app.jobs.repository;

import com.searchcode.app.config.Values;
import com.searchcode.app.dto.RunningIndexJob;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.IIndexService;
import com.searchcode.app.service.IndexService;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.SearchCodeLib;
import com.searchcode.app.util.UniqueRepoQueue;
import org.quartz.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

/**
 * This job is responsible for pulling and indexing file repositories which are kept upto date by some external
 * job such as cron or the like
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class IndexFileRepoJob extends IndexBaseRepoJob {

    private final IIndexService indexService;
    public String repoName;

    public IndexFileRepoJob() {
        this(Singleton.getIndexService());
    }

    public IndexFileRepoJob(IIndexService indexService) {
        this.indexService = indexService;
    }

    /**
     * The main method used for finding jobs to index and actually doing the work
     */
    @Override
    public void execute(JobExecutionContext context) {
        if (!isEnabled()) {
            return;
        }

        if (this.indexService.shouldPause(IIndexService.JobType.REPO_PARSER)) {
            this.logger.info("8fe60701::pausing parser");
            return;
        }

        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        // Pull the next repo to index from the queue
        UniqueRepoQueue repoQueue = this.getNextQueuedRepo();

        RepoResult repoResult = repoQueue.poll();

        if (repoResult != null && !Singleton.getRunningIndexRepoJobs().containsKey(repoResult.getName())) {

            this.logger.info(String.format("7aec9dd0::file indexer indexing repository %s", repoResult.getName()));
            repoResult.getData().indexStatus = "indexing";
            Singleton.getRepo().saveRepo(repoResult);

            try {
                Singleton.getRunningIndexRepoJobs().put(repoResult.getName(),
                        new RunningIndexJob("Indexing", Singleton.getHelpers().getCurrentTimeSeconds()));

                JobDataMap data = context.getJobDetail().getJobDataMap();

                this.repoName = repoResult.getName();
                String repoRemoteLocation = repoResult.getUrl();

                String repoLocations = data.get("REPOLOCATIONS").toString();
                this.LOWMEMORY = Boolean.parseBoolean(data.get("LOWMEMORY").toString());

                Path docDir = Paths.get(repoRemoteLocation);

                this.indexDocsByPath(docDir, repoResult, repoLocations, repoRemoteLocation, true);

                int runningTime = Singleton.getHelpers().getCurrentTimeSeconds() - Singleton.getRunningIndexRepoJobs().get(repoResult.getName()).startTime;
                repoResult.getData().averageIndexTimeSeconds = (repoResult.getData().averageIndexTimeSeconds + runningTime) / 2;
                repoResult.getData().indexStatus = "success";
                repoResult.getData().jobRunTime = Instant.now();
                Singleton.getRepo().saveRepo(repoResult);
            }
            catch (Exception ex) {
                this.logger.severe(String.format("05aa777b::error in class %s exception %s repository %s", ex.getClass(), ex.getMessage(), repoResult.getName()));
            }
            finally {
                // Clean up the job
                // Mark that this job is finished
                // TODO ensure that this line is covered by tests
                this.indexService.decrementRepoJobsCount();
                Singleton.getRunningIndexRepoJobs().remove(repoResult.getName());
            }
        }
    }

    @Override
    public String getFileLocationFilename(String fileToString, String fileRepoLocations) {
        if (this.repoName == null) {
            return fileToString.replace(fileRepoLocations, Values.EMPTYSTRING);
        }
        return this.repoName + fileToString.replace(fileRepoLocations, Values.EMPTYSTRING);
    }

    @Override
    public UniqueRepoQueue getNextQueuedRepo() {
        return Singleton.getUniqueFileRepoQueue();
    }

    @Override
    public String getCodeOwner(List<String> codeLines, String newString, String repoName, String fileRepoLocations, SearchCodeLib scl) {
        return "File System";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean ignoreFile(String fileParent) {
        return Singleton.getHelpers().ignoreFiles(fileParent);
    }
}
