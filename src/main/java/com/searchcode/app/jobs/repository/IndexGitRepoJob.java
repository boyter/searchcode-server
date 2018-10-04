/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.jobs.repository;

// Useful for the future
// http://stackoverflow.com/questions/1685228/how-to-cat-a-file-in-jgit

import com.searchcode.app.config.Values;
import com.searchcode.app.dto.CodeOwner;
import com.searchcode.app.dto.RepositoryChanged;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.IIndexService;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Properties;
import com.searchcode.app.util.SearchCodeLib;
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
import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;

import java.io.*;
import java.nio.file.InvalidPathException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This job is responsible for pulling and indexing git repositories
 *
 * TODO add more tests as they are lacking
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class IndexGitRepoJob extends IndexBaseRepoJob {

    private final String GIT_BINARY_PATH;
    private boolean USE_SYSTEM_GIT;

    public IndexGitRepoJob() {
        this(Singleton.getIndexService());
    }

    public IndexGitRepoJob(IIndexService indexService) {
        this.GIT_BINARY_PATH = Properties.getProperties().getProperty(Values.GITBINARYPATH, Values.DEFAULTGITBINARYPATH);
        this.USE_SYSTEM_GIT = Boolean.parseBoolean(Properties.getProperties().getProperty(Values.USESYSTEMGIT, Values.DEFAULTUSESYSTEMGIT));

        File f = new File(this.GIT_BINARY_PATH);
        if (this.USE_SYSTEM_GIT && !f.exists()) {
            this.logger.severe(String.format("309560e3::property %s for git_binary_path in properties file appears to be incorrect, falling back to internal git implementation", this.GIT_BINARY_PATH));
            this.USE_SYSTEM_GIT = false;
        }

        this.indexService = indexService;
    }

    @Override
    public RepositoryChanged updateExistingRepository(RepoResult repoResult, String repoLocations, boolean useCredentials) {
        return this.updateGitRepository(repoResult, repoLocations, useCredentials);
    }

    @Override
    public RepositoryChanged getNewRepository(RepoResult repoResult,String repoLocations, boolean useCredentials) {
        return this.cloneGitRepository(repoResult, repoLocations, useCredentials);
    }

    @Override
    public UniqueRepoQueue getNextQueuedRepo() {
        return Singleton.getUniqueGitRepoQueue();
    }

    @Override
    public String getCodeOwner(List<String> codeLines, String fileName, String repoDirectory, String fileRepoLocations, SearchCodeLib scl) {
        List<CodeOwner> owners;

        if (this.USE_SYSTEM_GIT) {
            owners = this.getBlameInfoExternal(codeLines.size(), repoDirectory, fileRepoLocations, fileName);
        } else {
            owners = this.getBlameInfo(codeLines.size(), repoDirectory, fileRepoLocations, fileName);
        }

        return scl.codeOwner(owners);
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

        return false;
    }

    /**
     * Only works if we have path to GIT
     * Should change over to git log -n 1 --pretty=format:%an -- README.md
     */
    public List<CodeOwner> getBlameInfoExternal(int codeLinesSize, String repoName, String repoLocations, String fileName) {
        List<CodeOwner> codeOwners = new ArrayList<>(codeLinesSize);

        // -w is to ignore whitespace bug
        ProcessBuilder processBuilder = new ProcessBuilder(this.GIT_BINARY_PATH, "blame", "-c", "-w", fileName);
        // The / part is required due to centos bug for version 1.1.1
        processBuilder.directory(new File(repoLocations + "/" + repoName));

        Process process = null;
        BufferedReader bufferedReader = null;

        try {
            process = processBuilder.start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, Values.CHARSET_UTF8);
            bufferedReader = new BufferedReader(isr);
            String line;
            DateFormat df = new SimpleDateFormat("yyyy-mm-dd kk:mm:ss");

            HashMap<String, CodeOwner> owners = new HashMap<>();

            boolean foundSomething = false;

            while ((line = bufferedReader.readLine()) != null) {
                this.logger.info(String.format("c7448564::blame line reponame %s filename %s", repoName, fileName));
                String[] split = line.split("\t");

                if (split.length > 2 && split[1].length() != 0) {
                    foundSomething = true;
                    String author = split[1].substring(1);
                    int commitTime = (int) (System.currentTimeMillis() / 1000);
                    try {
                        commitTime = (int) (df.parse(split[2]).getTime() / 1000);
                    }
                    catch (ParseException ex) {
                        this.logger.severe(String.format("05aa777b::error in class %s exception %s for time parse", ex.getClass(), ex.getMessage()));
                    }

                    if (owners.containsKey(author)) {
                        CodeOwner codeOwner = owners.get(author);
                        codeOwner.incrementLines();

                        int timestamp = codeOwner.getMostRecentUnixCommitTimestamp();

                        if (commitTime > timestamp) {
                            codeOwner.setMostRecentUnixCommitTimestamp(commitTime);
                        }
                        owners.put(author, codeOwner);
                    } else {
                        owners.put(author, new CodeOwner(author, 1, commitTime));
                    }
                }
            }

            if (foundSomething == false) {
                // External call for CentOS issue
                String[] split = fileName.split("/");

                if ( split.length != 1) {
                    codeOwners = this.getBlameInfoExternal(codeLinesSize, repoName, repoLocations, String.join("/", Arrays.asList(split).subList(1, split.length)));
                }

            } else {
                codeOwners = new ArrayList<>(owners.values());
            }

        } catch (IOException | StringIndexOutOfBoundsException ex) {
            this.logger.severe(String.format("1cf371a5::error in class %s exception %s for repository %s", ex.getClass(), ex.getMessage(), repoName));
        }
        finally {
            Singleton.getHelpers().closeQuietly(process);
            Singleton.getHelpers().closeQuietly(bufferedReader);
        }

        return codeOwners;
    }

    /**
     * Uses the inbuilt git
     * TODO lots of hairy bits in here need tests to capture issues
     */
    public List<CodeOwner> getBlameInfo(int codeLinesSize, String repoName, String repoLocations, String fileName) {
        List<CodeOwner> codeOwners = new ArrayList<>(codeLinesSize);
        try {
            // The / part is required due to centos bug for version 1.1.1
            // This appears to be correct
            String repoLoc = repoLocations + "/" + repoName + "/.git";

            Repository localRepository = new FileRepository(new File(repoLoc));
            BlameCommand blamer = new BlameCommand(localRepository);

            ObjectId commitID = localRepository.resolve("HEAD");

            if (commitID == null) {
                this.logger.severe(String.format("caca9ca8::getblameinfo commitId is null for repository %s filename %s", repoLoc, fileName));
                return codeOwners;
            }

            BlameResult blame;

            // Somewhere in here appears to be wrong...
            blamer.setStartCommit(commitID);
            blamer.setFilePath(fileName);
            blame = blamer.call();

            // Hail mary attempt to solve issue on CentOS Attempt to set at all costs
            if (blame == null) { // This one appears to solve the issue so don't remove it
                String[] split = fileName.split("/");
                blamer.setStartCommit(commitID);
                if ( split.length != 1) {
                    blamer.setFilePath(String.join("/", Arrays.asList(split).subList(1, split.length)));
                }
                blame = blamer.call();
            }
            if (blame == null) {
                String[] split = fileName.split("/");
                blamer.setStartCommit(commitID);
                if ( split.length != 1) {
                    blamer.setFilePath("/" + String.join("/", Arrays.asList(split).subList(1, split.length)));
                }
                blame = blamer.call();
            }

            if (blame == null) {
                this.logger.info(String.format("273e0a9e::getblameinfo is null for repository %s filename %s", repoLoc, fileName));
            }

            if (blame != null) {
                // Get all the owners their number of commits and most recent commit
                HashMap<String, CodeOwner> owners = new HashMap<>();
                RevCommit commit;
                PersonIdent authorIdent;

                try {
                    for (int i = 0; i < codeLinesSize; i++) {
                        commit = blame.getSourceCommit(i);
                        authorIdent = commit.getAuthorIdent();

                        if (owners.containsKey(authorIdent.getName())) {
                            CodeOwner codeOwner = owners.get(authorIdent.getName());
                            codeOwner.incrementLines();

                            int timestamp = codeOwner.getMostRecentUnixCommitTimestamp();

                            if (commit.getCommitTime() > timestamp) {
                                codeOwner.setMostRecentUnixCommitTimestamp(commit.getCommitTime());
                            }
                            owners.put(authorIdent.getName(), codeOwner);
                        } else {
                            owners.put(authorIdent.getName(), new CodeOwner(authorIdent.getName(), 1, commit.getCommitTime()));
                        }
                    }
                }
                catch (IndexOutOfBoundsException ex) {
                    // blame.getSourceCommit(i) will throw this exception and there is no way I can see to correctly
                    // identify when it will occur. Its not something that is severe so we log it under info
                    this.logger.info(String.format("4cf371a5::error in class %s exception %s for repository %s index out of bounds, this can safely be ignored but is something to fix in the future", ex.getClass(), ex.getMessage(), repoName));
                }

                codeOwners = new ArrayList<>(owners.values());
            }

        } catch (IOException ex) {
            this.logger.severe(String.format("85cd8d0c::error in class %s exception %s for repository %s", ex.getClass(), ex.getMessage(), repoName));
        } catch (GitAPIException ex) {
            this.logger.severe(String.format("91029067::error in class %s exception %s for repository %s", ex.getClass(), ex.getMessage(), repoName));
        } catch (IllegalArgumentException ex) {
            this.logger.severe(String.format("8b6da512::error in class %s exception %s for repository %s", ex.getClass(), ex.getMessage(), repoName));
        }

        return codeOwners;
    }

    /**
     * Update a git repository and return if it has changed and the differences
     */
    public RepositoryChanged updateGitRepository(RepoResult repoResult, String repoLocations, boolean useCredentials) {
        boolean changed = false;
        List<String> changedFiles = new ArrayList<>();
        List<String> deletedFiles = new ArrayList<>();
        this.logger.info(String.format("6cffea0f::attempting to pull latest from %s for %s", repoLocations, repoResult.getName()));

        Repository localRepository = null;
        Git git = null;

        try {
            localRepository = new FileRepository(new File(repoLocations + "/" + repoResult.getDirectoryName() + "/.git"));

            Ref head = localRepository.getRef("HEAD");
            git = new Git(localRepository);

            git.reset();
            git.clean();

            PullCommand pullCmd = git.pull();

            if (useCredentials) {
                pullCmd.setCredentialsProvider(new UsernamePasswordCredentialsProvider(repoResult.getUsername(), repoResult.getPassword()));
            }

            pullCmd.call();
            Ref newHEAD = localRepository.getRef("HEAD");

            if (!head.toString().equals(newHEAD.toString())) {
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
            String error = String.format("c6646806::error in class %s exception %s repository %s", ex.getClass(), ex.getMessage(), repoResult.getName());
            this.logger.severe(error);
            repoResult.getData().indexError = error;
            Singleton.getRepo().saveRepo(repoResult);
        }
        finally {
            Singleton.getHelpers().closeQuietly(localRepository);
            Singleton.getHelpers().closeQuietly(git);
        }

        return new RepositoryChanged(changed, changedFiles, deletedFiles);
    }

    /**
     * Clones the repository from scratch
     */
    public RepositoryChanged cloneGitRepository(RepoResult repoResult, String repoLocations, boolean useCredentials) {
        boolean successful;
        this.logger.info(String.format("664f20c7::attempting to clone %s", repoResult.getUrl()));

        Git call = null;

        try {
            CloneCommand cloneCommand = Git.cloneRepository();
            cloneCommand.setURI(repoResult.getUrl());
            cloneCommand.setDirectory(new File(repoLocations + "/" + repoResult.getDirectoryName() + "/"));
            cloneCommand.setCloneAllBranches(true);
            cloneCommand.setBranch(repoResult.getBranch());

            if (useCredentials) {
                cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(repoResult.getUsername(), repoResult.getPassword()));
            }

            call = cloneCommand.call();
            successful = true;
        } catch (GitAPIException | InvalidPathException ex) {
            successful = false;
            String error = String.format("6e56fa26::error in class %s exception %s repository %s", ex.getClass(), ex.getMessage(), repoResult.getName());
            this.logger.severe(error);
            repoResult.getData().indexError = error;
            Singleton.getRepo().saveRepo(repoResult);
        }
        finally {
            Singleton.getHelpers().closeQuietly(call);
        }

        RepositoryChanged repositoryChanged = new RepositoryChanged(successful);
        repositoryChanged.setClone(true);

        return repositoryChanged;
    }
}
