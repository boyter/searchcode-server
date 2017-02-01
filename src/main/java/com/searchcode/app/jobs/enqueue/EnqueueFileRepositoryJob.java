/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.8
 */

package com.searchcode.app.jobs.enqueue;

import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.UniqueRepoQueue;
import org.quartz.*;

import java.util.List;

/**
 * Responsible for adding all of the repositories inside the database into the queues. This one runs slower
 * and deals with file operation jobs which should run less frequently
 * TODO add logic to test that the right queue has things added to it
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class EnqueueFileRepositoryJob implements Job {
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (Singleton.getBackgroundJobsEnabled() == false) {
            return;
        }

        try {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

            UniqueRepoQueue repoQueue = Singleton.getUniqueFileRepoQueue();

            // Get all of the repositories and enqueue them
            List<RepoResult> repoResultList = Singleton.getRepo().getAllRepo();
            Singleton.getLogger().info("Adding repositories to be indexed. " + repoResultList.size());
            for(RepoResult rr: repoResultList) {
                switch (rr.getScm().toLowerCase()) {
                    case "file":
                        Singleton.getLogger().info("Adding to FILE queue " + rr.getName() + " " + rr.getScm());
                        repoQueue.add(rr);
                        break;
                }
            }
        }
        catch (Exception ex) {}
    }
}
