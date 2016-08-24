package com.searchcode.app.jobs;


import com.searchcode.app.dto.RepositoryChanged;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.CodeIndexer;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.UniqueRepoQueue;
import org.apache.commons.io.FileUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Arrays;

public abstract class IndexBaseRepoJob implements Job {
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        while(CodeIndexer.shouldPauseAdding()) {
            Singleton.getLogger().info("Pausing parser.");
            return;
        }

        // Pull the next repo to index from the queue
        UniqueRepoQueue repoQueue = Singleton.getUniqueGitRepoQueue();

        RepoResult repoResult = repoQueue.poll();
        AbstractMap<String, Integer> runningIndexGitRepoJobs = Singleton.getRunningIndexRepoJobs();

        if (repoResult != null && !runningIndexGitRepoJobs.containsKey(repoResult.getName())) {
            Singleton.getLogger().info("Indexing " + repoResult.getName());
            try {
                runningIndexGitRepoJobs.put(repoResult.getName(), (int) (System.currentTimeMillis() / 1000));

                JobDataMap data = context.getJobDetail().getJobDataMap();

                String repoName = repoResult.getName();
                String repoRemoteLocation = repoResult.getUrl();
                String repoUserName = repoResult.getUsername();
                String repoPassword = repoResult.getPassword();
                String repoBranch = repoResult.getBranch();

                String repoLocations = data.get("REPOLOCATIONS").toString();
                //this.LOWMEMORY = Boolean.parseBoolean(data.get("LOWMEMORY").toString());

                // Check if sucessfully cloned, and if not delete and restart
                boolean cloneSucess = checkCloneUpdateSucess(repoLocations + repoName);
                if (cloneSucess == false) {
                    // Delete the folder and delete from the index
                    try {
                        FileUtils.deleteDirectory(new File(repoLocations + "/" + repoName + "/"));
                        CodeIndexer.deleteByReponame(repoName);
                    } catch (IOException ex) {
                        Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() + "\n with message: " + ex.getMessage());
                    }
                }
                deleteCloneUpdateSuccess(repoLocations + "/" + repoName);

                String repoGitLocation = repoLocations + "/" + repoName + "/.git/";

                File f = new File(repoGitLocation);
                boolean existingRepo = f.exists();
                boolean useCredentials = repoUserName != null && !repoUserName.isEmpty();
                RepositoryChanged repositoryChanged = null;

                if (existingRepo) {
                    repositoryChanged = this.updateExistingRepository(repoName, repoRemoteLocation, repoUserName, repoPassword, repoLocations, repoBranch, useCredentials);
                } else {
                    repositoryChanged = this.getNewRepository(repoName, repoRemoteLocation, repoUserName, repoPassword, repoLocations, repoBranch, useCredentials);
                }

                // Write file indicating we have sucessfully cloned
                createCloneUpdateSuccess(repoLocations + "/" + repoName);
                // If the last index was not sucessful, then trigger full index
                boolean indexsuccess = checkIndexSucess(repoGitLocation);

                if (repositoryChanged.isChanged() || indexsuccess == false) {
                    Singleton.getLogger().info("Update found indexing " + repoRemoteLocation);
                    this.updateIndex(repoName, repoLocations, repoRemoteLocation, existingRepo, repositoryChanged);
                }
            }
            finally {
                // Clean up the job
                runningIndexGitRepoJobs.remove(repoResult.getName());
            }
        }
    }

    public RepositoryChanged updateExistingRepository(String repoName, String repoRemoteLocation, String repoUserName, String repoPassword, String repoLocations, String repoBranch, boolean useCredentials) {
        return null;
    }

    public RepositoryChanged getNewRepository(String repoName, String repoRemoteLocation, String repoUserName, String repoPassword, String repoLocations, String repoBranch, boolean useCredentials) {
        return null;
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
