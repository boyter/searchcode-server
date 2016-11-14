package com.searchcode.app.jobs;


import junit.framework.TestCase;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class IndexFileRepoJobTest extends TestCase {

    public void testGetFileLocationFilename() {
        IndexFileRepoJob fileRepoJob = new IndexFileRepoJob();
        fileRepoJob.repoName = "repo";
        String fileLocationFilename = fileRepoJob.getFileLocationFilename(".git/filename", "./repo/");
        assertThat(fileLocationFilename).isEqualTo("repo.git/filename");

        fileLocationFilename = fileRepoJob.getFileLocationFilename("./repo/.git/filename", "./repo/");
        assertThat(fileLocationFilename).isEqualTo("repo.git/filename");
    }
}
