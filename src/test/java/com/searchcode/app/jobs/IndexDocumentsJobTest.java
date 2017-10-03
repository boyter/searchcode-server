package com.searchcode.app.jobs;

import com.searchcode.app.jobs.repository.IndexDocumentsJob;
import com.searchcode.app.service.IndexService;
import com.searchcode.app.service.StatsService;
import com.searchcode.app.util.LoggerWrapper;
import junit.framework.TestCase;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.in;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;

public class IndexDocumentsJobTest extends TestCase {

    public void testExecute() throws JobExecutionException {
        JobExecutionContext jobExecutionContext = mock(JobExecutionContext.class);
        IndexService indexService = mock(IndexService.class);
        StatsService statsService = mock(StatsService.class);
        LoggerWrapper loggerWrapper = mock(LoggerWrapper.class);

//        IndexDocumentsJob indexDocumentsJob = new IndexDocumentsJob(indexService, statsService, loggerWrapper);
//
//        indexDocumentsJob.execute(jobExecutionContext);
    }
}
