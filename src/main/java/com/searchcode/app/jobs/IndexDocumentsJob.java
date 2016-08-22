/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 */

package com.searchcode.app.jobs;

import com.searchcode.app.service.CodeIndexer;
import com.searchcode.app.service.Singleton;
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
        try {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY + 1);
            int codeIndexQueueSize = Singleton.getCodeIndexQueue().size();
            Singleton.getLogger().info("Documents to index: " + codeIndexQueueSize);
            Singleton.getLogger().info("Lines to index: " + Singleton.getCodeIndexLinesCount());
            if (codeIndexQueueSize != 0) {
                CodeIndexer.indexDocuments(Singleton.getCodeIndexQueue());
            }
        } catch (Exception ex) {
            // Continue at all costs
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  "\n with message: " + ex.getMessage());
        }
    }
}
