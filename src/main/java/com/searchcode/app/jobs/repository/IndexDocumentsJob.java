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
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.service.IIndexService;
import com.searchcode.app.service.IndexService;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.service.StatsService;
import com.searchcode.app.util.LoggerWrapper;
import com.searchcode.app.util.Properties;
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

    private final int INDEXTIME;
    private final IIndexService indexService;
    private final LoggerWrapper logger;
    private final StatsService statsService;
    private final Queue<CodeIndexDocument> indexQueue;

    public IndexDocumentsJob() {
        this(Singleton.getIndexService(), Singleton.getStatsService(), Singleton.getCodeIndexQueue(), Singleton.getLogger());
    }

    public IndexDocumentsJob(IIndexService indexService, StatsService statsService, Queue<CodeIndexDocument> indexQueue, LoggerWrapper logger) {
        this.indexService = indexService;
        this.statsService = statsService;
        this.indexQueue = indexQueue;
        this.logger = logger;
        this.INDEXTIME = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.INDEXTIME, Values.DEFAULTINDEXTIME), Values.DEFAULTINDEXTIME);
    }

    public void execute(JobExecutionContext context) {
        this.logger.info("2091e574::starting indexdocumentsjob");
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        // This should run forever, and only restart if it has a hard crash
        try {
            while (true) {
                int codeIndexQueueSize = this.indexQueue.size();

                if (codeIndexQueueSize != 0) {
                    this.logger.info(String.format("19494c98::documents to index %d lines to index %d", codeIndexQueueSize, this.indexService.getCodeIndexLinesCount()));
                    this.indexService.indexDocument(this.indexQueue);
                }

                Thread.sleep(this.INDEXTIME);
            }
        } catch (Exception ex) {
            this.logger.severe(String.format("aebb3b30::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }
}
