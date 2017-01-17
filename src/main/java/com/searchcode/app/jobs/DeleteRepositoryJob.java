/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.6
 */

package com.searchcode.app.jobs;

import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Repo;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.CodeIndexer;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Properties;
import com.searchcode.app.util.UniqueRepoQueue;
import org.apache.commons.io.FileUtils;
import org.quartz.*;

import java.io.File;
import java.util.AbstractMap;

/**
 * The job which deletes repositories from the database index and disk where one exists in the deletion queue.
 * TODO fix race condition where it can start deleting while the repo has been re-added to be indexed
 * TODO add some tests for this to ensure everything such as the early return occurs correctly
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class DeleteRepositoryJob implements Job {
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (Singleton.getBackgroundJobsEnabled() == false) {
            return;
        }

        UniqueRepoQueue deleteRepoQueue = Singleton.getUniqueDeleteRepoQueue();
        RepoResult rr = null;

        try {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

            AbstractMap<String, Integer> runningProcesses = Singleton.getRunningIndexRepoJobs();
            Repo repo = Singleton.getRepo();

            rr = deleteRepoQueue.poll();
            if (rr == null) {
                return;
            }

            Singleton.getUniqueGitRepoQueue().delete(rr);

            if (runningProcesses.containsKey(rr.getName())) {
                // Put back into delete queue and quit
                deleteRepoQueue.add(rr);
                return;
            }

            Singleton.getLogger().info("Deleting repository. " + rr.getName());
            CodeIndexer.deleteByReponame(rr.getName());

            // remove the directory
            String repoLocations = Properties.getProperties().getProperty(Values.REPOSITORYLOCATION, Values.DEFAULTREPOSITORYLOCATION);
            FileUtils.deleteDirectory(new File(repoLocations + rr.getName() + "/"));

            // Remove from the database
            repo.deleteRepoByName(rr.getName());
        }
        catch (Exception ex) {
            if (rr != null) {
                deleteRepoQueue.add(rr);
            }
        }
    }
}
