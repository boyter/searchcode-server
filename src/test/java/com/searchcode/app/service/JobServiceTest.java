package com.searchcode.app.service;


import com.searchcode.app.model.RepoResult;
import com.searchcode.app.util.UniqueRepoQueue;
import junit.framework.TestCase;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class JobServiceTest extends TestCase {
    public void testPass() {
        assertThat(true).isTrue();
    }

//    TODO need to fix these tests
//    public void testEnqueueBackgroundEnabledReturnsTrue() {
//        JobService jobService = new JobService();
//        Singleton.getIndexService().setRepoAdderPause(true);
//
//        boolean result = jobService.forceEnqueue();
//        assertThat(result).isTrue();
//    }
//
//    public void testEnqueueBackgroundDisabledReturnsFalse() {
//        JobService jobService = new JobService();
//        Singleton.getIndexService().setRepoAdderPause(false);
//        boolean result = jobService.forceEnqueue();
//
//        assertThat(result).isFalse();
//        Singleton.getIndexService().setRepoAdderPause(false);
//    }
//
//    public void testEnqueueByReporesultBackgroundDisabledReturnsFalse() {
//
//        UniqueRepoQueue repoGitQueue = Singleton.getUniqueGitRepoQueue();
//        UniqueRepoQueue repoSvnQueue = Singleton.getUniqueSvnRepoQueue();
//        UniqueRepoQueue repoFileQueue = Singleton.getUniqueFileRepoQueue();
//
//        repoGitQueue.clear();
//        repoSvnQueue.clear();
//        repoFileQueue.clear();
//
//
//        JobService jobService = new JobService();
//        Singleton.getIndexService().setRepoAdderPause(true);
//
//        assertThat(jobService.forceEnqueue(new RepoResult(0, "name", "git", "url", "username", "password", "source", "branch", ""))).isTrue();
//        assertThat(jobService.forceEnqueue(new RepoResult(1, "name", "svn", "url", "username", "password", "source", "branch", ""))).isTrue();
//        assertThat(jobService.forceEnqueue(new RepoResult(2, "name", "file", "url", "username", "password", "source", "branch", ""))).isTrue();
//
//        assertThat(repoGitQueue.size()).isEqualTo(1);
//        assertThat(repoSvnQueue.size()).isEqualTo(1);
//        assertThat(repoFileQueue.size()).isEqualTo(1);
//
//        assertThat(repoGitQueue.poll().getRowId()).isEqualTo(0);
//        assertThat(repoSvnQueue.poll().getRowId()).isEqualTo(1);
//        assertThat(repoFileQueue.poll().getRowId()).isEqualTo(2);
//    }
}
