package com.searchcode.app.jobs;

import com.searchcode.app.TestHelpers;
import com.searchcode.app.dto.RepositoryChanged;
import com.searchcode.app.jobs.repository.IndexGitRepoJob;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.CodeIndexer;
import com.searchcode.app.service.CodeSearcher;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.service.StatsService;
import com.searchcode.app.util.Properties;
import com.searchcode.app.util.UniqueRepoQueue;
import junit.framework.TestCase;
import org.mockito.Mockito;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static org.mockito.Mockito.*;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class IndexBaseAndGitRepoJobTest extends TestCase {
    public void testGetBlameFilePath() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
        String actual = gitRepoJob.getBlameFilePath("./repo/something/test");
        assertEquals("repo/something/test", actual);
    }

    // No such thing as a windows path in the index, so should return empty
    public void testGetBlameFilePathWindows() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
        String actual = gitRepoJob.getBlameFilePath("\\repo\\something\\test");
        assertEquals("", actual);
    }

    public void testIndexSucess() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
        assertFalse(gitRepoJob.checkIndexSucess("/tmp/"));
        gitRepoJob.createIndexSuccess("/tmp/");
        assertTrue(gitRepoJob.checkIndexSucess("/tmp/"));
        gitRepoJob.deleteIndexSuccess("/tmp/");
        assertFalse(gitRepoJob.checkIndexSucess("/tmp/"));
    }

    public void testCloneSucess() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
        assertFalse(gitRepoJob.checkCloneUpdateSucess("/tmp/"));
        gitRepoJob.createCloneUpdateSuccess("/tmp/");
        assertTrue(gitRepoJob.checkCloneUpdateSucess("/tmp/"));
        gitRepoJob.deleteCloneUpdateSuccess("/tmp/");
        assertFalse(gitRepoJob.checkCloneUpdateSucess("/tmp/"));
    }

    public void testDeleteNoFile() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();

        for(int i = 0; i < 100; i++) {
            gitRepoJob.deleteIndexSuccess("/tmp/");
            gitRepoJob.deleteCloneUpdateSuccess("/tmp/");
        }
    }

    public void testShouldJobTerminate() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
        StatsService statsServiceMock = Mockito.mock(StatsService.class);
        when(statsServiceMock.getLoadAverage()).thenReturn("0.0");
        Singleton.setStatsService(statsServiceMock);

        assertThat(gitRepoJob.shouldJobPauseOrTerminate()).isFalse();
        Singleton.setBackgroundJobsEnabled(false);
        assertThat(gitRepoJob.shouldJobPauseOrTerminate()).isTrue();
        Singleton.setBackgroundJobsEnabled(true);
        assertThat(gitRepoJob.shouldJobPauseOrTerminate()).isFalse();
        Singleton.setPauseBackgroundJobs(true);
        Singleton.setBackgroundJobsEnabled(false);
        assertThat(gitRepoJob.shouldJobPauseOrTerminate()).isTrue();

        // Reset
        Singleton.setStatsService(new StatsService());
    }

    public void testGetFileMd5() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
        gitRepoJob.getFileMd5("filedoesnotexist");
    }

    public void testDetermineBinary() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
        List<String[]> reportList = new ArrayList<>();

        boolean result = gitRepoJob.determineBinary("", "", new ArrayList<>(), reportList);

        assertThat(result).isTrue();
        assertThat(reportList.size()).isEqualTo(1);
    }

    // TODO actually do something with this information
    public void testSomething() {
        String[] split = "myrepo/path/to/myfile.txt".split("/");
        String temp = String.join("/", Arrays.asList(split).subList(1, split.length));
        assertEquals("path/to/myfile.txt", temp);
    }

    public void testGetRelativeToProjectPath() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
        String relativeToProjectPath = gitRepoJob.getRelativeToProjectPath("/Users/boyter/test5", "/Users/boyter/test5/u/something/sources/small/c3p0.csv");
        assertThat(relativeToProjectPath).isEqualTo("/u/something/sources/small/c3p0.csv");

        relativeToProjectPath = gitRepoJob.getRelativeToProjectPath("/Users/boyter/test5/", "/Users/boyter/test5/u/something/sources/small/c3p0.csv");
        assertThat(relativeToProjectPath).isEqualTo("/u/something/sources/small/c3p0.csv");

        relativeToProjectPath = gitRepoJob.getRelativeToProjectPath("./repo/test", "./repo/test/chinese.php");
        assertThat(relativeToProjectPath).isEqualTo("/chinese.php");

        relativeToProjectPath = gitRepoJob.getRelativeToProjectPath("./repo/test/", "./repo/test/chinese.php");
        assertThat(relativeToProjectPath).isEqualTo("/chinese.php");

        relativeToProjectPath = gitRepoJob.getRelativeToProjectPath("./repo/test", "./repo//test/chinese.php");
        assertThat(relativeToProjectPath).isEqualTo("/chinese.php");
    }

    public void testGetFileLocationFilename() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
        String fileLocationFilename = gitRepoJob.getFileLocationFilename(".git/filename", "./repo/");
        assertThat(fileLocationFilename).isEqualTo(".git/filename");

        fileLocationFilename = gitRepoJob.getFileLocationFilename("./repo/.git/filename", "./repo/");
        assertThat(fileLocationFilename).isEqualTo(".git/filename");
    }

    public void testMissingPathFilesNoLocations() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
        CodeSearcher codeSearcherMock = Mockito.mock(CodeSearcher.class);

        when(codeSearcherMock.getRepoDocuments("testRepoName", 0)).thenReturn(new ArrayList<>());
        gitRepoJob.cleanMissingPathFiles(codeSearcherMock, "testRepoName", new HashMap<String, String>());
        verify(codeSearcherMock, times(1)).getRepoDocuments("testRepoName", 0);
    }

    public void testMissingPathFilesShouldPage() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
        CodeSearcher codeSearcherMock = Mockito.mock(CodeSearcher.class);

        List<String> repoReturn = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            repoReturn.add("string"+i);
        }

        when(codeSearcherMock.getRepoDocuments("testRepoName", 0)).thenReturn(repoReturn);
        when(codeSearcherMock.getRepoDocuments("testRepoName", 1)).thenReturn(repoReturn);
        when(codeSearcherMock.getRepoDocuments("testRepoName", 2)).thenReturn(new ArrayList<>());

        gitRepoJob.cleanMissingPathFiles(codeSearcherMock, "testRepoName", new HashMap<String, String>());

        verify(codeSearcherMock, times(1)).getRepoDocuments("testRepoName", 0);
        verify(codeSearcherMock, times(1)).getRepoDocuments("testRepoName", 1);
        verify(codeSearcherMock, times(1)).getRepoDocuments("testRepoName", 2);
    }

    public void testCheckCloneSuccessEmptyReturnsFalse() {
        IndexGitRepoJob indexGitRepoJob = new IndexGitRepoJob();
        boolean actual = indexGitRepoJob.checkCloneSuccess("", "");
        assertThat(actual).isFalse();
    }

    public void testCheckCloneSuccessEmptyReturnsTrue() throws IOException {
        File location = TestHelpers.clearAndCreateTempPath("testCheckCloneSuccessEmptyReturnsTrue");
        File projectLocation = TestHelpers.createDirectory(location, "myawesomeproject");
        TestHelpers.createFile(projectLocation, "myfile.java", "some file content");

        IndexGitRepoJob indexGitRepoJob = new IndexGitRepoJob();
        boolean actual = indexGitRepoJob.checkCloneSuccess("myawesomeproject", location.getAbsolutePath());
        assertThat(actual).isTrue();

        File toCheck = new File(projectLocation.getAbsolutePath());
        assertThat(toCheck.exists()).isFalse();
    }

    public void testExecuteNothingInQueue() throws JobExecutionException {
        IndexGitRepoJob indexGitRepoJob = new IndexGitRepoJob();
        IndexGitRepoJob spy = spy(indexGitRepoJob);

        when(spy.getNextQueuedRepo()).thenReturn(new UniqueRepoQueue());
        spy.execute(null);
        assertThat(spy.haveRepoResult).isFalse();
    }

    public void testExecuteHasMethodInQueueNewRepository() throws JobExecutionException {
        IndexGitRepoJob indexGitRepoJob = new IndexGitRepoJob();
        IndexGitRepoJob spy = spy(indexGitRepoJob);
        spy.haveRepoResult = false;

        UniqueRepoQueue uniqueRepoQueue = new UniqueRepoQueue();
        uniqueRepoQueue.add(new RepoResult(1, "name", "scm", "url", "username", "password", "source", "branch", "{}"));

        when(spy.getNextQueuedRepo()).thenReturn(uniqueRepoQueue);
        when(spy.isEnabled()).thenReturn(true);
        when(spy.getNewRepository(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(new RepositoryChanged(false, null, null));
        Singleton.setBackgroundJobsEnabled(true);

        CodeIndexer mockCodeIndexer = Mockito.mock(CodeIndexer.class);
        JobExecutionContext mockContext = Mockito.mock(JobExecutionContext.class);
        JobDetail mockDetail = Mockito.mock(JobDetail.class);
        JobDataMap mockJobDataMap = Mockito.mock(JobDataMap.class);

        when(mockJobDataMap.get("REPOLOCATIONS")).thenReturn("");
        when(mockJobDataMap.get("LOWMEMORY")).thenReturn("true");
        when(mockDetail.getJobDataMap()).thenReturn(mockJobDataMap);
        when(mockContext.getJobDetail()).thenReturn(mockDetail);
        when(mockCodeIndexer.shouldPauseAdding()).thenReturn(false);
        spy.codeIndexer = mockCodeIndexer;

        spy.execute(mockContext);

        assertThat(spy.haveRepoResult).isTrue();
        assertThat(spy.LOWMEMORY).isTrue();
        verify(spy).getNextQueuedRepo();
        //verify(spy).getNewRepository(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }
}
