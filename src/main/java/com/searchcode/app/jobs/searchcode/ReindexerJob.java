package com.searchcode.app.jobs.searchcode;

import com.searchcode.app.config.Values;
import com.searchcode.app.dao.SourceCode;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.model.searchcode.SearchcodeCodeResult;
import com.searchcode.app.service.Singleton;
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

    public ReindexerJob() {
        this(Singleton.getSourceCode(), Singleton.getCodeIndexQueue(), Singleton.getLogger());
    }

    public ReindexerJob(SourceCode sourcecode, Queue<CodeIndexDocument> indexQueue, LoggerWrapper logger) {
        this.sourcecode = sourcecode;
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
                var codeIndexQueueSize = this.indexQueue.size();

                // Check if the indexQueue size is large, and if so skip processing for a while
                if (codeIndexQueueSize > 10000) {
                    this.logger.info(String.format("feddddbd::index queue size %d is large so pausing reindexer", codeIndexQueueSize));
                    Thread.sleep(this.INDEXTIME);
                    continue;
                }

                // Fetch documents from SQL and add them to the index
                var codeBetween = this.sourcecode.getCodeBetween(0, 10000);
                var indexDocuments = codeBetween.stream().map(this::convert).collect(Collectors.toList());
                this.indexQueue.addAll(indexDocuments);

                Thread.sleep(this.INDEXTIME);
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
                .setId(codeResult.getId());
    }
}