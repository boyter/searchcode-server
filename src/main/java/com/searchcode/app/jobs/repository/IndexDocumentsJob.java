/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.14
 */

package com.searchcode.app.jobs.repository;

import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.service.IndexService;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.service.StatsService;
import com.searchcode.app.util.LoggerWrapper;
import org.quartz.*;

import java.util.Queue;

/**
 * This job is responsible for passing the queue of documents along to be indexed. It does not do much and only exists
 * as a job to allow this to be scheduled in a background thread. It also never pauses. To stop it doing anything
 * the jobs feeding the index queue should be paused.
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class IndexDocumentsJob implements Job {

    private final IndexService indexService;
    private final LoggerWrapper logger;
    private final StatsService statsService;
    private final Queue<CodeIndexDocument> indexQueue;

    public IndexDocumentsJob() {
        this(Singleton.getIndexService(), Singleton.getStatsService(), Singleton.getCodeIndexQueue(), Singleton.getLogger());
    }

    public IndexDocumentsJob(IndexService indexService, StatsService statsService, Queue<CodeIndexDocument> indexQueue, LoggerWrapper logger) {
        this.indexService = indexService;
        this.statsService = statsService;
        this.indexQueue = indexQueue;
        this.logger = logger;
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.logger.info("Starting IndexDocumentsJob");
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        // This should run forever, and only restart if it has a hard crash
        try {
            while (true) {
                int codeIndexQueueSize = this.indexQueue.size();

                if (codeIndexQueueSize != 0) {
                    this.logger.info("Documents to index: " + codeIndexQueueSize);
                    this.logger.info("Lines to index: " + this.indexService.getCodeIndexLinesCount());
                    this.logger.info("Memory Usage: " + this.statsService.getMemoryUsage(", "));
                    this.indexService.indexDocument(this.indexQueue);
                }

                Thread.sleep(100);
            }
        } catch (Exception ex) {
            // Continue at all costs
            this.logger.warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() + "\n with message: " + ex.getMessage());
        }
    }
}
