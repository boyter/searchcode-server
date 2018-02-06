package com.searchcode.app.jobs;

import com.searchcode.app.dto.RepositoryChanged;
import com.searchcode.app.jobs.repository.IndexBaseRepoJob;
import com.searchcode.app.jobs.repository.IndexGitRepoJob;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.IIndexService;
import com.searchcode.app.service.IndexService;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.UniqueRepoQueue;
import junit.framework.TestCase;
import org.apache.commons.lang3.RandomStringUtils;
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
    private JobDetail jobDetailMock;
    private JobDataMap jobDataMapMock;
    private IndexService indexServiceMock;

    public void setUp() {
        this.mockContext = mock(JobExecutionContext.class);
        this.jobDetailMock = mock(JobDetail.class);
        this.jobDataMapMock = mock(JobDataMap.class);
        this.indexServiceMock = mock(IndexService.class);

        when(jobDataMapMock.get("REPOLOCATIONS")).thenReturn("");
        when(jobDataMapMock.get("LOWMEMORY")).thenReturn("true");
        when(jobDetailMock.getJobDataMap()).thenReturn(jobDataMapMock);
        when(mockContext.getJobDetail()).thenReturn(jobDetailMock);
        when(indexServiceMock.shouldPause(IIndexService.JobType.REPO_ADDER)).thenReturn(false);
        when(indexServiceMock.shouldPause(IIndexService.JobType.REPO_PARSER)).thenReturn(false);
    }


    public void testExecuteNothingInQueue() throws JobExecutionException {
        IndexGitRepoJob indexGitRepoJob = new IndexGitRepoJob();
        IndexGitRepoJob spy = spy(indexGitRepoJob);
        spy.haveRepoResult = false;

        when(spy.getNextQueuedRepo()).thenReturn(new UniqueRepoQueue());
        spy.indexService = indexServiceMock;

        spy.execute(this.mockContext);
        assertThat(spy.haveRepoResult).isFalse();
    }

//    TODO investigate why this works in IntelliJ but not mvn test
//    public void testExecuteHasMethodInQueueNewRepository() throws JobExecutionException {
//        IndexGitRepoJob indexGitRepoJob = new IndexGitRepoJob(indexServiceMock);
//        IndexGitRepoJob spy = spy(indexGitRepoJob);
//        spy.haveRepoResult = false;
//
//        String randomName = RandomStringUtils.randomAscii(20);
//
//        UniqueRepoQueue uniqueRepoQueue = new UniqueRepoQueue();
//        uniqueRepoQueue.add(new RepoResult(1, randomName, "scm", "url", "username", "password", "source", "branch", "{}"));
//
//        when(spy.getNextQueuedRepo()).thenReturn(uniqueRepoQueue);
//        when(spy.isEnabled()).thenReturn(true);
//        when(spy.getNewRepository(new RepoResult(1, randomName, "scm", "url", "username", "password", "source", "branch", "{}"), "", true))
//                .thenReturn(new RepositoryChanged(false, null, null));
//
//        when(indexServiceMock.shouldPause(IIndexService.JobType.REPO_PARSER)).thenReturn(false);
//        spy.indexService = indexServiceMock;
//
//        spy.execute(this.mockContext);
//
//        assertThat(spy.haveRepoResult).isTrue();
//        assertThat(spy.LOWMEMORY).isTrue();
//        verify(spy).getNextQueuedRepo();
//        verify(spy, times(2)).getNewRepository(anyObject(), anyString(), anyBoolean());
//    }

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

    public void testGetMinified() {
        IndexGitRepoJob indexGitRepoJob = new IndexGitRepoJob();
        indexGitRepoJob.LOGINDEXED = true;
        List<String[]> reportList = new ArrayList<>();

        IndexBaseRepoJob.IsMinifiedReturn isMinified = indexGitRepoJob.getIsMinified(new ArrayList<>(), "", reportList);

        assertThat(isMinified.isMinified()).isFalse();
        assertThat(isMinified.getReportList().isEmpty()).isTrue();
    }
}
