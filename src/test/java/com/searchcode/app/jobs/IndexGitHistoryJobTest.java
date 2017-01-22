package com.searchcode.app.jobs;

import com.searchcode.app.jobs.repository.IndexGitHistoryJob;
import junit.framework.TestCase;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;

public class IndexGitHistoryJobTest extends TestCase {
    public void testGetBlameFilePath() throws IOException, GitAPIException {
        IndexGitHistoryJob gitRepoJob = new IndexGitHistoryJob();

        //String repoName, String repoRemoteLocation, String repoUserName, String repoPassword, String repoLocations, String branch, boolean useCredentials
        //gitRepoJob.cloneGitRepository("test", "/Users/boyter/Desktop/searchcode-server", "", "", "./repo/.timelord/", "master", false);
        //gitRepoJob.getGitChangeSets(); // does the indexing currently
        //gitRepoJob.getRevisionChanges();
    }
}
