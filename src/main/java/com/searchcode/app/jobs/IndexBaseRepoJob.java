package com.searchcode.app.jobs;


import com.searchcode.app.dto.RepositoryChanged;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

public abstract class IndexBaseRepoJob implements Job {
    public void execute(JobExecutionContext context) throws JobExecutionException {
    }

    public void updateIndex(String repoName, String repoLocations, String repoRemoteLocation, boolean existingRepo, RepositoryChanged repositoryChanged) {
    }

    public void indexDocsByDelta(Path path, String repoName, String repoLocations, String repoRemoteLocation, RepositoryChanged repositoryChanged) {
    }

    public void indexDocsByPath(Path path, String repoName, String repoLocations, String repoRemoteLocation, boolean existingRepo) {
    }

    public String getBlameFilePath(String fileLocationFilename) {
        String[] split = fileLocationFilename.split("/");
        String newString = String.join("/", Arrays.asList(split).subList(1, split.length));
        return newString;
    }

    public RepositoryChanged cloneGitRepository(String repoName, String repoRemoteLocation, String repoUserName, String repoPassword, String repoLocations, String branch, boolean useCredentials) {
        return null;
    }

    // The below are used to create temp files indicating everything worked

    public void createCloneUpdateSuccess(String repoLocation) {
        createFile(repoLocation, "cloneupdate");
    }

    public void deleteCloneUpdateSuccess(String repoLocation) {
        deleteFile(repoLocation, "cloneupdate");
    }

    public boolean checkCloneUpdateSucess(String repoLocation) {
        return checkFile(repoLocation, "cloneupdate");
    }

    public void createIndexSuccess(String repoLocation) {
        createFile(repoLocation, "index");
    }

    public void deleteIndexSuccess(String repoLocation) {
        deleteFile(repoLocation, "index");
    }

    public boolean checkIndexSucess(String repoLocation) {
        return checkFile(repoLocation, "index");
    }

    private boolean checkFile(String repoLocation, String type) {
        File f = new File(repoLocation + "/searchcode." + type + ".success");
        return f.exists();
    }

    private void createFile(String repoLocation, String type) {
        File file = new File(repoLocation, "/searchcode." + type + ".success");
        file.mkdir();
    }

    private void deleteFile(String repoLocation, String type) {
        File file = new File(repoLocation, "/searchcode." + type + ".success");

        file.delete();
    }
}
