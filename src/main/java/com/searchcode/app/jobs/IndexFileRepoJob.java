/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.3
 */


package com.searchcode.app.jobs;

import com.searchcode.app.config.Values;
import com.searchcode.app.dto.CodeOwner;
import com.searchcode.app.dto.RepositoryChanged;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.CodeIndexer;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Properties;
import com.searchcode.app.util.SearchcodeLib;
import com.searchcode.app.util.UniqueRepoQueue;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.quartz.*;

import java.io.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This job is responsible for pulling and indexing file repositories which are kept upto date by some external
 * job such as cron or the like
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class IndexFileRepoJob extends IndexBaseRepoJob {

    private String repoName;

    /**
     * The main method used for finding jobs to index and actually doing the work
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (isEnabled() == false) {
            return;
        }

        if (Singleton.getBackgroundJobsEnabled() == false) {
            return;
        }

        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        while(CodeIndexer.shouldPauseAdding()) {
            Singleton.getLogger().info("Pausing parser.");
            return;
        }

        // Pull the next repo to index from the queue
        UniqueRepoQueue repoQueue = this.getNextQueuedRepo();

        RepoResult repoResult = repoQueue.poll();
        AbstractMap<String, Integer> runningIndexRepoJobs = Singleton.getRunningIndexRepoJobs();

        if (repoResult != null && !runningIndexRepoJobs.containsKey(repoResult.getName())) {
            Singleton.getLogger().info("File Indexer Indexing " + repoResult.getName());
            try {
                runningIndexRepoJobs.put(repoResult.getName(), (int) (System.currentTimeMillis() / 1000));

                JobDataMap data = context.getJobDetail().getJobDataMap();

                String repoName = repoResult.getName();
                this.repoName = repoName;
                String repoRemoteLocation = repoResult.getUrl();

                String repoLocations = data.get("REPOLOCATIONS").toString();
                this.LOWMEMORY = Boolean.parseBoolean(data.get("LOWMEMORY").toString());

                Path docDir = Paths.get(repoRemoteLocation);

                this.indexDocsByPath(docDir, repoName, repoLocations, repoRemoteLocation, true);
            }
            finally {
                // Clean up the job
                runningIndexRepoJobs.remove(repoResult.getName());
            }
        }
    }

    @Override
    public String getFileLocationFilename(String fileToString, String fileRepoLocations) {
        return this.repoName + fileToString.replace(fileRepoLocations, Values.EMPTYSTRING);
    }

    @Override
    public UniqueRepoQueue getNextQueuedRepo() {
        return Singleton.getUniqueFileRepoQueue();
    }

    @Override
    public String getCodeOwner(List<String> codeLines, String newString, String repoName, String fileRepoLocations, SearchcodeLib scl) {
        return "File System";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean ignoreFile(String fileParent) {
        if (fileParent.endsWith("/.git") || fileParent.contains("/.git/")) {
            return true;
        }

        if (fileParent.endsWith("/.svn") || fileParent.contains("/.svn/")) {
            return true;
        }

        return false;
    }
}
