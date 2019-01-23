package com.searchcode.app.jobs.searchcode;

import com.searchcode.app.config.Values;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.service.IIndexService;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.service.StatsService;
import com.searchcode.app.util.LoggerWrapper;
import com.searchcode.app.util.Properties;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;

import java.util.Queue;

/**
 * This job is specific to searchcode.com and pulls files that exist in the database and adds them to the queue
 * to reindex.
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ReindexerJob implements Job {

    private final int INDEXTIME;
    private final IIndexService indexService;
    private final LoggerWrapper logger;
    private final StatsService statsService;
    private final Queue<CodeIndexDocument> indexQueue;

    public ReindexerJob() {
        this(Singleton.getIndexService(), Singleton.getStatsService(), Singleton.getCodeIndexQueue(), Singleton.getLogger());
    }

    public ReindexerJob(IIndexService indexService, StatsService statsService, Queue<CodeIndexDocument> indexQueue, LoggerWrapper logger) {
        this.indexService = indexService;
        this.statsService = statsService;
        this.indexQueue = indexQueue;
        this.logger = logger;
        this.INDEXTIME = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.INDEXTIME, Values.DEFAULTINDEXTIME), Values.DEFAULTINDEXTIME);
    }

    public void execute(JobExecutionContext context) {
        this.logger.info("c987ff40::starting reindexerjob");
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        // This should run forever, and only restart if it has a hard crash
        try {
            while (true) {
//                int codeIndexQueueSize = this.indexQueue.size();
//
//                if (codeIndexQueueSize != 0) {
//                    this.logger.info(String.format("19494c98::documents to index %d lines to index %d", codeIndexQueueSize, this.indexService.getCodeIndexLinesCount()));
//                    this.indexService.indexDocument(this.indexQueue);
//                }

                Thread.sleep(this.INDEXTIME);
            }
        } catch (Exception ex) {
            this.logger.severe(String.format("32639901::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }
}