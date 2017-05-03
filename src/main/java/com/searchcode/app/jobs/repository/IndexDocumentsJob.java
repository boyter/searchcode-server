/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.10
 */

package com.searchcode.app.jobs.repository;

import com.searchcode.app.service.CodeIndexer;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.service.StatsService;
import org.quartz.*;

import java.io.IOException;

/**
 * This job is responsible for passing the queue of documents along to be indexed. It does not do much and only exists
 * as a job to allow this to be scheduled in a background thread.
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class IndexDocumentsJob implements Job {

    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (!Singleton.getBackgroundJobsEnabled()) {
            return;
        }
        
        try {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY + 1);
            int codeIndexQueueSize = Singleton.getCodeIndexQueue().size();

            if (codeIndexQueueSize != 0) {
                StatsService statsService = new StatsService();
                Singleton.getLogger().info("Documents to index: " + codeIndexQueueSize);
                Singleton.getLogger().info("Lines to index: " + Singleton.getCodeIndexLinesCount());
                Singleton.getLogger().info("Memory Usage: " + statsService.getMemoryUsage(", "));
                Singleton.getCodeIndexer().indexDocuments(Singleton.getCodeIndexQueue());
            }
        } catch (Exception ex) {
            // Continue at all costs
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  "\n with message: " + ex.getMessage());
        }
    }
}
