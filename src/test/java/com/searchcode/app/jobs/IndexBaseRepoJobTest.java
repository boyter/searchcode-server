package com.searchcode.app.jobs;

import com.searchcode.app.dto.RepositoryChanged;
import com.searchcode.app.jobs.repository.IndexBaseRepoJob;
import com.searchcode.app.jobs.repository.IndexGitRepoJob;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.CodeIndexer;
import com.searchcode.app.service.SharedService;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.UniqueRepoQueue;
import junit.framework.TestCase;
import org.mockito.Mockito;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class IndexBaseRepoJobTest extends TestCase {

    private JobExecutionContext mockContext;
    private JobDetail mockDetail;
    private JobDataMap mockJobDataMap;
    private CodeIndexer mockCodeIndexer;

    public void setUp() {
        this.mockContext = Mockito.mock(JobExecutionContext.class);
        this.mockDetail = Mockito.mock(JobDetail.class);
        this.mockJobDataMap = Mockito.mock(JobDataMap.class);
        this.mockCodeIndexer = Mockito.mock(CodeIndexer.class);

        when(mockJobDataMap.get("REPOLOCATIONS")).thenReturn("");
        when(mockJobDataMap.get("LOWMEMORY")).thenReturn("true");
        when(mockDetail.getJobDataMap()).thenReturn(mockJobDataMap);
        when(mockContext.getJobDetail()).thenReturn(mockDetail);
        when(mockCodeIndexer.shouldPauseAdding()).thenReturn(false);
    }


    public void testExecuteNothingInQueue() throws JobExecutionException {
        IndexGitRepoJob indexGitRepoJob = new IndexGitRepoJob();
        IndexGitRepoJob spy = spy(indexGitRepoJob);
        spy.haveRepoResult = false;

        when(spy.getNextQueuedRepo()).thenReturn(new UniqueRepoQueue());
        spy.codeIndexer = mockCodeIndexer;

        spy.execute(this.mockContext);
        assertThat(spy.haveRepoResult).isFalse();
    }

    public void testExecuteHasMethodInQueueNewRepository() throws JobExecutionException {
        SharedService sharedServiceMock = mock(SharedService.class);
        when(sharedServiceMock.getPauseBackgroundJobs()).thenReturn(false);
        when(sharedServiceMock.getBackgroundJobsEnabled()).thenReturn(true);

        IndexGitRepoJob indexGitRepoJob = new IndexGitRepoJob(sharedServiceMock);
        IndexGitRepoJob spy = spy(indexGitRepoJob);
        spy.haveRepoResult = false;

        UniqueRepoQueue uniqueRepoQueue = new UniqueRepoQueue();
        uniqueRepoQueue.add(new RepoResult(1, "name", "scm", "url", "username", "password", "source", "branch", "{}"));

        when(spy.getNextQueuedRepo()).thenReturn(uniqueRepoQueue);
        when(spy.isEnabled()).thenReturn(true);
        when(spy.getNewRepository(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(new RepositoryChanged(false, null, null));

        when(mockCodeIndexer.shouldPauseAdding()).thenReturn(false);
        spy.codeIndexer = mockCodeIndexer;

        spy.execute(this.mockContext);

        assertThat(spy.haveRepoResult).isTrue();
        assertThat(spy.LOWMEMORY).isTrue();
        verify(spy).getNextQueuedRepo();
        verify(spy, times(2)).getNewRepository(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }

    public void testGetCodeLinesLogIndexedInvalid() {
        IndexGitRepoJob indexGitRepoJob = new IndexGitRepoJob();
        indexGitRepoJob.LOGINDEXED = true;
        List<String[]> reportList = new ArrayList<>();

        IndexBaseRepoJob.CodeLinesReturn codeLines = indexGitRepoJob.getCodeLines("", reportList);

        assertThat(codeLines.isError()).isTrue();
        assertThat(codeLines.getReportList().get(0)[1]).isEqualTo("excluded");
        assertThat(codeLines.getReportList().get(0)[2]).isEqualTo("unable to guess guess file encoding");
    }

    public void testGetCodeLinesLogNotIndexedInvalid() {
        IndexGitRepoJob indexGitRepoJob = new IndexGitRepoJob();
        indexGitRepoJob.LOGINDEXED = false;
        List<String[]> reportList = new ArrayList<>();

        IndexBaseRepoJob.CodeLinesReturn codeLines = indexGitRepoJob.getCodeLines("", reportList);

        assertThat(codeLines.isError()).isTrue();
        assertThat(codeLines.getReportList().isEmpty()).isTrue();
    }

    public void testGetMinifiedd() {
        IndexGitRepoJob indexGitRepoJob = new IndexGitRepoJob();
        indexGitRepoJob.LOGINDEXED = true;
        List<String[]> reportList = new ArrayList<>();

        IndexBaseRepoJob.IsMinifiedReturn isMinified = indexGitRepoJob.getIsMinified(new ArrayList<>(), "", reportList);

        assertThat(isMinified.isMinified()).isFalse();
        assertThat(isMinified.getReportList().isEmpty()).isTrue();
    }
}
