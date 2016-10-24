package com.searchcode.app.jobs;

import com.searchcode.app.service.Singleton;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class IndexGitRepoJobTest extends TestCase {
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

        for(int i=0; i< 100; i++) {
            gitRepoJob.deleteIndexSuccess("/tmp/");
            gitRepoJob.deleteCloneUpdateSuccess("/tmp/");
        }
    }

    public void testShouldJobTerminate() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();

        assertThat(gitRepoJob.shouldJobPauseOrTerminate()).isFalse();
        Singleton.setBackgroundJobsEnabled(false);
        assertThat(gitRepoJob.shouldJobPauseOrTerminate()).isTrue();
        Singleton.setBackgroundJobsEnabled(true);
        assertThat(gitRepoJob.shouldJobPauseOrTerminate()).isFalse();
        Singleton.setPauseBackgroundJobs(true);
        Singleton.setBackgroundJobsEnabled(false);
        assertThat(gitRepoJob.shouldJobPauseOrTerminate()).isTrue();
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
}
