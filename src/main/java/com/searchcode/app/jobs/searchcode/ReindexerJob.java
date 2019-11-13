package com.searchcode.app.jobs.searchcode;

import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dao.SourceCode;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.model.searchcode.SearchcodeCodeResult;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;
import com.searchcode.app.util.Properties;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;

import java.util.Queue;
import java.util.stream.Collectors;

/**
 * This job is specific to searchcode.com and pulls files that exist in the database and adds them to the queue
 * to reindex.
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ReindexerJob implements Job {

    private final int INDEXTIME;
    private final LoggerWrapper logger;
    private final Queue<CodeIndexDocument> indexQueue;
    private final SourceCode sourcecode;
    private final Data data;
    private final Helpers helpers;

    public ReindexerJob() {
        this(Singleton.getSourceCode(), Singleton.getCodeIndexQueue(), Singleton.getData(), Singleton.getHelpers(), Singleton.getLogger());
    }

    public ReindexerJob(SourceCode sourcecode, Queue<CodeIndexDocument> indexQueue, Data data, Helpers helpers, LoggerWrapper logger) {
        this.sourcecode = sourcecode;
        this.indexQueue = indexQueue;
        this.logger = logger;
        this.data = data;
        this.helpers = helpers;
        this.INDEXTIME = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.INDEXTIME, Values.DEFAULTINDEXTIME), Values.DEFAULTINDEXTIME);
    }

    public void execute(JobExecutionContext context) {
        this.logger.info("c987ff40::starting reindexerjob");
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        // This should run forever, and only restart if it has a hard crash
        try {
            while (true) {
                // Sleep for a jitter time to avoid many indexers converging on the same schedule
                Thread.sleep(this.INDEXTIME * this.helpers.getRandomJitterSleepTimeMilliseconds());

                var codeIndexQueueSize = this.indexQueue.size();

                // Check if the indexQueue size is large, and if so skip processing for a while
                // TODO also check system load and pause if there is a lot of activity
                while (codeIndexQueueSize > 10000) {
                    this.logger.info(String.format("feddddbd::index queue size %d is large so pausing reindexer", codeIndexQueueSize));
                    Thread.sleep(this.INDEXTIME * this.helpers.getRandomJitterSleepTimeMilliseconds());
                }

                // Do it in batches of 5000
                // TODO make this configurable
                var startIndex = this.helpers.tryParseInt(this.data.getDataByName("reIndexerStart", "0"), "0");
                var endIndex = startIndex + 5000;

                if (endIndex > this.sourcecode.getMaxId()) {
                    endIndex = 0;
                }

                // Update where we are processing so if another processor is running it does not step on this one
                // there is still a race condition here but hopefully the jitter takes care of redundant work
                this.data.saveData("reIndexerStart", String.valueOf(endIndex));

                // Fetch documents from SQL and add them to the index
                var codeBetween = this.sourcecode.getCodeBetween(startIndex, endIndex);
                var indexDocuments = codeBetween.stream().map(this::convert).collect(Collectors.toList());
                this.indexQueue.addAll(indexDocuments);
            }
        } catch (Exception ex) {
            this.logger.severe(String.format("32639901::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }

    /**
     * Convert between searchcode result from DB to index result we can process
     * TODO this needs to be shared between this job and the indexer jobs possibly
     */
    public CodeIndexDocument convert(SearchcodeCodeResult codeResult) {
        return new CodeIndexDocument()
                .setFileName(codeResult.getFilename())
                .setContents(codeResult.getContent())
                .setLanguageNameId(codeResult.getLangugeid())
                .setLines(codeResult.getLinescount())
                .setId(codeResult.getId())
                .setRepoNameId(codeResult.getRepoid())
                .setSourceId(codeResult.getSourceid());
    }
}