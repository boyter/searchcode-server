/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 */

package com.searchcode.app.jobs.repository;

import com.google.common.collect.Lists;
import com.searchcode.app.config.Values;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.service.GitService;
import com.searchcode.app.service.Singleton;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Iterates through every revision of a git repository indexing the file differences between them.
 * This allows searching through time to find when some code was added to a repository or when
 * it was removed
 */
public class IndexGitHistoryJob implements Job {

    public int MAXFILELINEDEPTH = Singleton.getHelpers().tryParseInt(com.searchcode.app.util.Properties.getProperties().getProperty(Values.MAXFILELINEDEPTH, Values.DEFAULTMAXFILELINEDEPTH), Values.DEFAULTMAXFILELINEDEPTH);

    public void execute(JobExecutionContext context) throws JobExecutionException {
//        if (!Singleton.getSharedService().getBackgroundJobsEnabled()) {
//            return;
//        }
//
//        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
//
//        JobDataMap data = context.getJobDetail().getJobDataMap();
//        String repoLocations = data.get("REPOLOCATIONS").toString();
//
//        // Get all the current change sets for the project
//        // loop through each and get the changes and index

    }

//    public void getGitChangeSets() throws IOException, GitAPIException {
//        //Repository localRepository = new FileRepository(new File("./repo/server/.git"));
//        Repository localRepository = new FileRepository(new File("./repo/thumbor/.git"));
//
//        Git git = new Git(localRepository);
//        Iterable<RevCommit> logs = git.log().call();
//
//        List<GitChangeSet> gitChangeSets = new ArrayList<>();
//        for(RevCommit rev: logs) {
//            String message = rev.getFullMessage();
//            String author = rev.getAuthorIdent().getName();
//
//            Date expiry = new Date(Long.valueOf(rev.getCommitTime()) * 1000);
//            System.out.println(expiry.toString() + " " + rev.getCommitTime() + " " + rev.getName());
//
//            gitChangeSets.add(new GitChangeSet(message, author, rev.getName(), expiry));
//        }
//
//        gitChangeSets = Lists.reverse(gitChangeSets);
//
//        // TODO currently this is ignoring the very first commit changes need to include those
//        for (int i = 1; i < gitChangeSets.size(); i++) {
//            System.out.println("///////////////////////////////////////////////");
//            this.getRevisionChanges(localRepository, git, gitChangeSets.get(i - 1), gitChangeSets.get(i));
//        }
//
//    }
//
//    public void getRevisionChanges(Repository localRepository, Git git, GitChangeSet oldRevison, GitChangeSet newRevision) throws IOException, GitAPIException {
//        ObjectId oldHead = localRepository.resolve(oldRevison.getRevision() + "^{tree}");
//        ObjectId newHead = localRepository.resolve(newRevision.getRevision() + "^{tree}");
//
//        ObjectReader reader = localRepository.newObjectReader();
//
//        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
//        oldTreeIter.reset(reader, oldHead);
//
//        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
//        newTreeIter.reset(reader, newHead);
//
//
//        List<DiffEntry> entries = git.diff()
//                .setNewTree(newTreeIter)
//                .setOldTree(oldTreeIter)
//                .call();
//
//        GitService gs = new GitService();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//
//        for( DiffEntry entry : entries ) {
//            if ("DELETE".equals(entry.getChangeType().name())) {
//                System.out.println("DEL " + entry.getOldPath());
//
//                String contents = gs.fetchFileRevision(localRepository.getWorkTree().toString() + "/.git", oldRevison.getRevision(), entry.getOldPath());
//
//                CodeIndexDocument cd = new CodeIndexDocument(entry.getNewPath(), "thumbor", entry.getOldPath(), entry.getOldPath(), entry.getOldPath(), "md5hash", "Java", contents.split("\\r?\\n").length, contents, "", oldRevison.getAuthor());
//                cd.setRevision(oldRevison.getRevision());
//                cd.setYearMonthDay(sdf.format(oldRevison.getExpiry()));
//                cd.setYearMonth(cd.getYearMonthDay().substring(0, 6));
//                cd.setYear(cd.getYearMonthDay().substring(0, 4));
//                cd.setMessage(oldRevison.getMessage());
//                cd.setDeleted("TRUE");
//                Singleton.getCodeIndexer().indexTimeDocument(cd);
//            }
//            else {
//                System.out.println("ADD " + entry.getNewPath());
//                String contents = gs.fetchFileRevision(localRepository.getWorkTree().toString() + "/.git", newRevision.getRevision(), entry.getNewPath());
//
//                CodeIndexDocument cd = new CodeIndexDocument(entry.getNewPath(), "thumbor", entry.getNewPath(), entry.getNewPath(), entry.getNewPath(), "md5hash", "Java", contents.split("\\r?\\n").length, contents, "", newRevision.getAuthor());
//                cd.setRevision(newRevision.getRevision());
//                cd.setYearMonthDay(sdf.format(oldRevison.getExpiry()));
//                cd.setYearMonth(cd.getYearMonthDay().substring(0, 6));
//                cd.setYear(cd.getYearMonthDay().substring(0, 4));
//                cd.setMessage(newRevision.getMessage());
//                cd.setDeleted("FALSE");
//                Singleton.getCodeIndexer().indexTimeDocument(cd);
//            }
//        }
//    }
//
//    public class GitChangeSet {
//        private String message;
//        private String author;
//        private Date expiry;
//        private String revision;
//
//        public GitChangeSet(String message, String author, String revision, Date expiry) {
//            this.setMessage(message);
//            this.setAuthor(author);
//            this.setRevision(revision);
//            this.setExpiry(expiry);
//        }
//
//        public String getMessage() {
//            return message;
//        }
//
//        public void setMessage(String message) {
//            this.message = message;
//        }
//
//        public String getAuthor() {
//            return author;
//        }
//
//        public void setAuthor(String author) {
//            this.author = author;
//        }
//
//        public Date getExpiry() {
//            return expiry;
//        }
//
//        public void setExpiry(Date expiry) {
//            this.expiry = expiry;
//        }
//
//        public String getRevision() {
//            return revision;
//        }
//
//        public void setRevision(String revision) {
//            this.revision = revision;
//        }
//    }
}
