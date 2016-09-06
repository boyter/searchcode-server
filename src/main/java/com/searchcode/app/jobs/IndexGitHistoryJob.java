/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 */

package com.searchcode.app.jobs;

import com.searchcode.app.config.Values;
import com.searchcode.app.dto.RepositoryChanged;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.List;

/**
 * Iterates through every revision of a git repository indexing the file differences between them.
 * This allows searching through time to find when some code was added to a repository or when
 * it was removed
 */
public class IndexGitHistoryJob implements Job {

    public int MAXFILELINEDEPTH = Helpers.tryParseInt(com.searchcode.app.util.Properties.getProperties().getProperty(Values.MAXFILELINEDEPTH, Values.DEFAULTMAXFILELINEDEPTH), Values.DEFAULTMAXFILELINEDEPTH);

    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (Singleton.getBackgroundJobsEnabled() == false) {
            return;
        }

        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        JobDataMap data = context.getJobDetail().getJobDataMap();
        String repoLocations = data.get("REPOLOCATIONS").toString();

        // Clone the repository then
    }


    public RepositoryChanged cloneGitRepository(String repoName, String repoRemoteLocation, String repoUserName, String repoPassword, String repoLocations, String branch, boolean useCredentials) {
        boolean successful = false;
        Singleton.getLogger().info("Attempting to clone " + repoRemoteLocation);

        try {
            CloneCommand cloneCommand = Git.cloneRepository();
            cloneCommand.setURI(repoRemoteLocation);
            cloneCommand.setDirectory(new File(repoLocations + "/" + repoName + "/"));
            cloneCommand.setCloneAllBranches(true);
            cloneCommand.setBranch(branch);

            if(useCredentials) {
                cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(repoUserName, repoPassword));
            }

            cloneCommand.call();

            successful = true;

        } catch (GitAPIException | InvalidPathException ex) {
            successful = false;
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  "\n with message: " + ex.getMessage());
        }

        RepositoryChanged repositoryChanged = new RepositoryChanged(successful);
        repositoryChanged.setClone(true);

        return repositoryChanged;
    }


    public void getGitChangeSets() throws IOException, GitAPIException {
        Repository localRepository = new FileRepository(new File("./repo/.timelord/test/.git"));

        Git git = new Git(localRepository);
        Iterable<RevCommit> logs = git.log().call();

        for(RevCommit rev: logs) {
            System.out.println(rev.getName());
            git.checkout().setName(rev.getName()).call();
        }
    }

    /**
     * Update a git repository and return if it has changed and the differences
     */
    public RepositoryChanged updateGitRepository(String repoName, String repoRemoteLocation, String repoUserName, String repoPassword, String repoLocations, String branch, boolean useCredentials) {
        boolean changed = false;
        List<String> changedFiles = new ArrayList<>();
        List<String> deletedFiles = new ArrayList<>();
        Singleton.getLogger().info("Attempting to pull latest from " + repoRemoteLocation + " for " + repoName);

        try {
            Repository localRepository = new FileRepository(new File(repoLocations + "/" + repoName + "/.git"));

            Ref head = localRepository.getRef("HEAD");

            Git git = new Git(localRepository);
            git.reset();
            git.clean();

            PullCommand pullCmd = git.pull();

            if(useCredentials) {
                pullCmd.setCredentialsProvider(new UsernamePasswordCredentialsProvider(repoUserName, repoPassword));
            }

            pullCmd.call();
            Ref newHEAD = localRepository.getRef("HEAD");

            if(!head.toString().equals(newHEAD.toString())) {
                changed = true;

                // Get the differences between the the heads which we updated at
                // and use these to just update the differences between them
                ObjectId oldHead = localRepository.resolve(head.getObjectId().getName() + "^{tree}");
                ObjectId newHead = localRepository.resolve(newHEAD.getObjectId().getName() + "^{tree}");

                ObjectReader reader = localRepository.newObjectReader();

                CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                oldTreeIter.reset(reader, oldHead);

                CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                newTreeIter.reset(reader, newHead);


                List<DiffEntry> entries = git.diff()
                        .setNewTree(newTreeIter)
                        .setOldTree(oldTreeIter)
                        .call();


                for( DiffEntry entry : entries ) {
                    if ("DELETE".equals(entry.getChangeType().name())) {
                        deletedFiles.add(FilenameUtils.separatorsToUnix(entry.getOldPath()));
                    }
                    else {
                        changedFiles.add(FilenameUtils.separatorsToUnix(entry.getNewPath()));
                    }
                }
            }

        } catch (IOException | GitAPIException | InvalidPathException ex) {
            changed = false;
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  "\n with message: " + ex.getMessage());
        }

        return new RepositoryChanged(changed, changedFiles, deletedFiles);
    }
}
