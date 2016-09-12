/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 */

package com.searchcode.app.jobs;

import com.google.common.collect.Lists;
import com.searchcode.app.config.Values;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.dto.RepositoryChanged;
import com.searchcode.app.service.CodeIndexer;
import com.searchcode.app.service.GitService;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
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

        // Get all the current change sets for the project
        // loop through each and get the changes and index

    }

    public void getGitChangeSets() throws IOException, GitAPIException {
        Repository localRepository = new FileRepository(new File("./repo/server/.git"));

        Git git = new Git(localRepository);
        Iterable<RevCommit> logs = git.log().call();

        List<String> revisions = new ArrayList<>();
        for(RevCommit rev: logs) {
            System.out.println(rev.getCommitTime() + " " + rev.getName());
            revisions.add(rev.getName());
        }
        revisions = Lists.reverse(revisions);

        // TODO currently this is ignoring the very first commit changes need to include those
        for (int i = 1; i < revisions.size(); i++) {
            System.out.println("///////////////////////////////////////////////");
            this.getRevisionChanges(localRepository, git, revisions.get(i - 1), revisions.get(i));
        }

    }

    public void getRevisionChanges(Repository localRepository, Git git, String oldRevison, String newRevision) throws IOException, GitAPIException {
        ObjectId oldHead = localRepository.resolve(oldRevison + "^{tree}");
        ObjectId newHead = localRepository.resolve(newRevision + "^{tree}");

        ObjectReader reader = localRepository.newObjectReader();

        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        oldTreeIter.reset(reader, oldHead);

        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        newTreeIter.reset(reader, newHead);


        List<DiffEntry> entries = git.diff()
                .setNewTree(newTreeIter)
                .setOldTree(oldTreeIter)
                .call();

        GitService gs = new GitService();


        for( DiffEntry entry : entries ) {
            if ("DELETE".equals(entry.getChangeType().name())) {
                System.out.println("DEL " + entry.getOldPath());
            }
            else {
                System.out.println("ADD " + entry.getNewPath());
                String contents = gs.fetchFileRevision(localRepository.getWorkTree().toString() + "/.git", newRevision, entry.getNewPath());

                CodeIndexDocument cd = new CodeIndexDocument(entry.getNewPath(), "server", entry.getNewPath(), entry.getNewPath(), entry.getNewPath(), "md5hash", "Java", contents.split("\\r?\\n").length, contents, "", "someone");
                cd.setRevision(newRevision);
                cd.setYearMonthDay("20160101");
                CodeIndexer.indexTimeDocument(cd);
            }
        }
    }

}
