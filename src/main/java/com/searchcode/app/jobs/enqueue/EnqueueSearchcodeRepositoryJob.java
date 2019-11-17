package com.searchcode.app.jobs.enqueue;

import com.searchcode.app.dao.IRepo;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.service.index.IIndexService;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;

/**
 * Responsible for adding all of the repositories inside the database into the queues. There will be a queue
 * for GIT and SVN or any other repository added.
 * TODO add logic to test that the right queue has things added to it
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class EnqueueSearchcodeRepositoryJob implements Job {
    private final IIndexService indexService;
    private final LoggerWrapper logger;
    private final IRepo repo;
    private final Helpers helpers;

    public EnqueueSearchcodeRepositoryJob() {
        this.indexService = Singleton.getIndexService();
        this.repo = Singleton.getRepo();
        this.logger = Singleton.getLogger();
        this.helpers = Singleton.getHelpers();
    }

    public void execute(JobExecutionContext context) {
        this.logger.info("fe39b962::starting enqueue job");
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        // TODO this never returns false, should probably implement back-pressure
        if (this.indexService.shouldPause(IIndexService.JobType.REPO_ADDER)) {
            return;
        }

        try {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

            var repoGitQueue = Singleton.getUniqueGitRepoQueue();
            var repoSvnQueue = Singleton.getUniqueSvnRepoQueue();

            // Determine where we have started from a repo collection
            // determine where that ends
            // update the point
            // Get the batch of repositories from the database
            // add them to the queue
        } catch (Exception ex) {
            this.logger.severe(String.format("8675db36::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }
}
