/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 */


package com.searchcode.app.jobs;

// Useful for the future
// http://stackoverflow.com/questions/1685228/how-to-cat-a-file-in-jgit

import com.searchcode.app.config.Values;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.dto.CodeOwner;
import com.searchcode.app.dto.RepositoryChanged;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.CodeIndexer;
import com.searchcode.app.service.CodeSearcher;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.*;
import com.searchcode.app.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This job is responsible for pulling and indexing git repositories
 *
 * TODO add more tests as they are lacking
 * TODO use inheritance/template methods to combine the common stuff between this and SVN job then subclass
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class IndexGitRepoJob extends IndexBaseRepoJob {

    private String GITBINARYPATH;
    private boolean USESYSTEMGIT;

    public IndexGitRepoJob() {
        this.GITBINARYPATH = Properties.getProperties().getProperty(Values.GITBINARYPATH, Values.DEFAULTGITBINARYPATH);
        this.USESYSTEMGIT = Boolean.parseBoolean(Properties.getProperties().getProperty(Values.USESYSTEMGIT, Values.DEFAULTUSESYSTEMGIT));

        File f = new File(this.GITBINARYPATH);
        if (USESYSTEMGIT && !f.exists()) {
            Singleton.getLogger().warning("\n///////////////////////////////////////////////////////////////////////////\n// Property git_binary_path in properties file appears to be incorrect.  //\n// Please check the path. Falling back to internal git implementation.   //\n///////////////////////////////////////////////////////////////////////////");

            this.USESYSTEMGIT = false;
        }
    }

    public RepositoryChanged updateExistingRepository(String repoName, String repoRemoteLocation, String repoUserName, String repoPassword, String repoLocations, String repoBranch, boolean useCredentials) {
        return this.updateGitRepository(repoName, repoRemoteLocation, repoUserName, repoPassword, repoLocations, repoBranch, useCredentials);
    }

    public RepositoryChanged getNewRepository(String repoName, String repoRemoteLocation, String repoUserName, String repoPassword, String repoLocations, String repoBranch, boolean useCredentials) {
        return this.cloneGitRepository(repoName, repoRemoteLocation, repoUserName, repoPassword, repoLocations, repoBranch, useCredentials);
    }


    /**
     * Indexes all the documents in the repository changed file effectively performing a delta update
     * Should only be called when there is a genuine update IE something was indexed previously and
     * has has a new commit.
     */
    public void indexDocsByDelta(Path path, String repoName, String repoLocations, String repoRemoteLocation, RepositoryChanged repositoryChanged) {
        SearchcodeLib scl = Singleton.getSearchCodeLib(); // Should have data object by this point
        Queue<CodeIndexDocument> codeIndexDocumentQueue = Singleton.getCodeIndexQueue();
        String fileRepoLocations = FilenameUtils.separatorsToUnix(repoLocations);

        for(String changedFile: repositoryChanged.getChangedFiles()) {

            while(CodeIndexer.shouldPauseAdding()) {
                Singleton.getLogger().info("Pausing parser.");
                try {
                    Thread.sleep(super.SLEEPTIME);
                } catch (InterruptedException ex) {}
            }

            String[] split = changedFile.split("/");
            String fileName = split[split.length - 1];
            changedFile = fileRepoLocations + "/" + repoName + "/" + changedFile;

            String md5Hash = Values.EMPTYSTRING;
            List<String> codeLines = null;

            try {
                codeLines = Helpers.readFileLines(changedFile, this.MAXFILELINEDEPTH);
            } catch (IOException ex) {
                Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  "\n with message: " + ex.getMessage());
                break;
            }

            try {
                FileInputStream fis = new FileInputStream(new File(changedFile));
                md5Hash = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
                fis.close();
            } catch (IOException ex) {
                Singleton.getLogger().warning("Unable to generate MD5 for " + changedFile);
            }

            if(scl.isMinified(codeLines)) {
                Singleton.getLogger().info("Appears to be minified will not index  " + changedFile);
                break;
            }

            String languageName = scl.languageGuesser(changedFile, codeLines);
            String fileLocation = changedFile.replace(fileRepoLocations, Values.EMPTYSTRING).replace(fileName, Values.EMPTYSTRING);
            String fileLocationFilename = changedFile.replace(fileRepoLocations, Values.EMPTYSTRING);
            String repoLocationRepoNameLocationFilename = changedFile;


            String newString = getBlameFilePath(fileLocationFilename);
            List<CodeOwner> owners;
            if (this.USESYSTEMGIT) {
                owners = getBlameInfoExternal(codeLines.size(), repoName, fileRepoLocations, newString);
            }
            else {
                owners = getBlameInfo(codeLines.size(), repoName, fileRepoLocations, newString);
            }
            String codeOwner = scl.codeOwner(owners);


            if (codeLines != null) {
                if (super.LOWMEMORY) {
                    try {
                        CodeIndexer.indexDocument(new CodeIndexDocument(repoLocationRepoNameLocationFilename, repoName, fileName, fileLocation, fileLocationFilename, md5Hash, languageName, codeLines.size(), StringUtils.join(codeLines, " "), repoRemoteLocation, codeOwner));
                    } catch (IOException ex) {
                        Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  "\n with message: " + ex.getMessage());
                    }
                } else {
                    Singleton.incrementCodeIndexLinesCount(codeLines.size());
                    codeIndexDocumentQueue.add(new CodeIndexDocument(repoLocationRepoNameLocationFilename, repoName, fileName, fileLocation, fileLocationFilename, md5Hash, languageName, codeLines.size(), StringUtils.join(codeLines, " "), repoRemoteLocation, codeOwner));
                }
            }
        }

        for(String deletedFile: repositoryChanged.getDeletedFiles()) {
            Singleton.getLogger().info("Missing from disk, removing from index " + deletedFile);
            try {
                CodeIndexer.deleteByFileLocationFilename(deletedFile);
            } catch (IOException ex) {
                Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  "\n with message: " + ex.getMessage());
            }
        }
    }




    /**
     * Only works if we have path to GIT
     */
    private List<CodeOwner> getBlameInfoExternal(int codeLinesSize, String repoName, String repoLocations, String fileName) {
        List<CodeOwner> codeOwners = new ArrayList<>(codeLinesSize);

        try {
            // -w is to ignore whitespace bug
            ProcessBuilder processBuilder = new ProcessBuilder(this.GITBINARYPATH, "blame", "-c", "-w", fileName);
            // The / part is required due to centos bug for version 1.1.1
            processBuilder.directory(new File(repoLocations + "/" + repoName));

            Process process = processBuilder.start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            DateFormat df = new SimpleDateFormat("yyyy-mm-dd kk:mm:ss");

            HashMap<String, CodeOwner> owners = new HashMap<>();

            boolean foundSomething = false;

            while ((line = br.readLine()) != null) {
                Singleton.getLogger().info("Blame line " + repoName + fileName + ": " + line);
                String[] split = line.split("\t");

                if (split.length > 2 && split[1].length() != 0) {
                    foundSomething = true;
                    String author = split[1].substring(1);
                    int commitTime = (int) (System.currentTimeMillis() / 1000);
                    try {
                        commitTime = (int) (df.parse(split[2]).getTime() / 1000);
                    }
                    catch(ParseException ex) {
                        Singleton.getLogger().info("time parse expection for " + repoName + fileName);
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
                    codeOwners = getBlameInfoExternal(codeLinesSize, repoName, repoLocations, String.join("/", Arrays.asList(split).subList(1, split.length)));
                }

            } else {
                codeOwners = new ArrayList<>(owners.values());
            }

        } catch (IOException | StringIndexOutOfBoundsException ex) {
            Singleton.getLogger().info("getBlameInfoExternal repoloc: " + repoLocations + "/" + repoName);
            Singleton.getLogger().info("getBlameInfoExternal fileName: " + fileName);
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() + "\n with message: " + ex.getMessage());
        }

        return codeOwners;
    }

    // TODO this method appears to leak memory like crazy... need to resolve this
    private List<CodeOwner> getBlameInfo(int codeLinesSize, String repoName, String repoLocations, String fileName) {
        List<CodeOwner> codeOwners = new ArrayList<>(codeLinesSize);
        try {
            // The / part is required due to centos bug for version 1.1.1
            // This appears to be correct
            String repoLoc = repoLocations + "/" + repoName + "/.git";

            Repository localRepository = new FileRepository(new File(repoLoc));
            BlameCommand blamer = new BlameCommand(localRepository);

            ObjectId commitID = localRepository.resolve("HEAD");

            if (commitID == null) {
                Singleton.getLogger().info("getBlameInfo commitID is null for " + repoLoc + " " + fileName);
                return codeOwners;
            }

            BlameResult blame;

            // Somewhere in here appears to be wrong...
            blamer.setStartCommit(commitID);
            blamer.setFilePath(fileName);
            blame = blamer.call();

            // Hail mary attempt to solve issue on CentOS Attempt to set at all costs
            if(blame == null) { // This one appears to solve the issue so don't remove it
                String[] split = fileName.split("/");
                blamer.setStartCommit(commitID);
                if ( split.length != 1) {
                    blamer.setFilePath(String.join("/", Arrays.asList(split).subList(1, split.length)));
                }
                blame = blamer.call();
            }
            if(blame == null) {
                String[] split = fileName.split("/");
                blamer.setStartCommit(commitID);
                if ( split.length != 1) {
                    blamer.setFilePath("/" + String.join("/", Arrays.asList(split).subList(1, split.length)));
                }
                blame = blamer.call();
            }

            if (blame == null) {
                Singleton.getLogger().info("getBlameInfo blame is null for " + repoLoc + " " + fileName);
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
                catch(IndexOutOfBoundsException ex) {
                    // Ignore this as its not really a problem or is it?
                    Singleton.getLogger().info("IndexOutOfBoundsException when trying to get blame for " + repoName + fileName);
                }

                codeOwners = new ArrayList<>(owners.values());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        System.gc(); // Try to clean up
        return codeOwners;
    }


    private RepositoryChanged updateGitRepository(String repoName, String repoRemoteLocation, String repoUserName, String repoPassword, String repoLocations, String branch, boolean useCredentials) {
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
}
