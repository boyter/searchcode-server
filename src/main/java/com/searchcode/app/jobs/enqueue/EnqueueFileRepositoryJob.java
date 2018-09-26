/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.jobs.enqueue;

import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Repo;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.IIndexService;
import com.searchcode.app.service.IndexService;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;
import com.searchcode.app.util.UniqueRepoQueue;
import org.quartz.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Responsible for adding all of the repositories inside the database into the queues. This one runs slower
 * and deals with file operation jobs which should run less frequently
 * TODO add logic to test that the right queue has things added to it
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class EnqueueFileRepositoryJob implements Job {

    private final IIndexService indexService;
    private final LoggerWrapper logger;
    private final Repo repo;
    private final Helpers helpers;

    public EnqueueFileRepositoryJob() {
        this.indexService = Singleton.getIndexService();
        this.repo = Singleton.getRepo();
        this.logger = Singleton.getLogger();
        this.helpers = Singleton.getHelpers();
    }

    public void execute(JobExecutionContext context) {
        if (this.indexService.shouldPause(IIndexService.JobType.REPO_ADDER)) {
            return;
        }

        try {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

            UniqueRepoQueue repoQueue = Singleton.getUniqueFileRepoQueue();

            // Filter out those queued to be deleted and not file repositories
            List<RepoResult> repoResultList = this.helpers.filterRunningAndDeletedRepoJobs(Singleton.getRepo().getAllRepo())
                .stream()
                .filter(x -> x.getScm().equals(Values.FILE))
                .collect(Collectors.toList());

            this.logger.info(String.format("4f37a297::adding %d file repositories to be indexed", repoResultList.size()));

            for (RepoResult rr: repoResultList) {
                if (Singleton.getEnqueueFileRepositoryJobFirstRun()) {
                    rr.getData().jobRunTime = Instant.parse("1800-01-01T00:00:00.000Z");
                    this.repo.saveRepo(rr);
                    this.logger.info(String.format("40cd7595::resetting job run time due to firstRun %b repoName %s", Singleton.getEnqueueFileRepositoryJobFirstRun(), rr.getName()));
                }

                switch (rr.getScm().toLowerCase()) {
                    case Values.FILE:
                        this.logger.info(String.format("18310da9::adding to file queue reponame %s", rr.getName()));
                        repoQueue.add(rr);
                        break;
                    default:
                        break;
                }
            }

            Singleton.setEnqueueFileRepositoryJob(false);
        }
        catch (Exception ex) {
            this.logger.severe(String.format("056c9f75::error in class %s exception %s", ex.getClass(), ex.getMessage()));
            Singleton.setEnqueueFileRepositoryJob(false);
        }
    }
}
