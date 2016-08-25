package com.searchcode.app.jobs;


import com.searchcode.app.config.Values;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.dto.RepositoryChanged;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.CodeIndexer;
import com.searchcode.app.service.CodeSearcher;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.SearchcodeLib;
import com.searchcode.app.util.UniqueRepoQueue;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public abstract class IndexBaseRepoJob implements Job {

    protected boolean LOWMEMORY = true;
    protected int SLEEPTIME = 5000;
    public int MAXFILELINEDEPTH = Helpers.tryParseInt(com.searchcode.app.util.Properties.getProperties().getProperty(Values.MAXFILELINEDEPTH, Values.DEFAULTMAXFILELINEDEPTH), Values.DEFAULTMAXFILELINEDEPTH);

    /**
     * This method to be implemented by the extending class
     */
    public RepositoryChanged updateExistingRepository(String repoName, String repoRemoteLocation, String repoUserName, String repoPassword, String repoLocations, String repoBranch, boolean useCredentials) {
        return null;
    }

    /**
     * This method to be implemented by the extending class
     */
    public RepositoryChanged getNewRepository(String repoName, String repoRemoteLocation, String repoUserName, String repoPassword, String repoLocations, String repoBranch, boolean useCredentials) {
        return null;
    }

    /**
     * This method to be implemented by the extending class
     */
    public UniqueRepoQueue getNextQueuedRepo() {
        return null;
    }

    /**
     * This method to be implemented by the extending class
     */
    public String getCodeOwner(List<String> codeLines, String newString, String repoName, String fileRepoLocations, SearchcodeLib scl) {
        return null;
    }

    /**
     * This method to be implemented by the extending class
     */
    public boolean isEnabled() {
        return false;
    }

    /**
     * The main method used for finding jobs to index and actually doing the work
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (isEnabled() == false) {
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
            Singleton.getLogger().info("Indexing " + repoResult.getName());
            try {
                runningIndexRepoJobs.put(repoResult.getName(), (int) (System.currentTimeMillis() / 1000));

                JobDataMap data = context.getJobDetail().getJobDataMap();

                String repoName = repoResult.getName();
                String repoRemoteLocation = repoResult.getUrl();
                String repoUserName = repoResult.getUsername();
                String repoPassword = repoResult.getPassword();
                String repoBranch = repoResult.getBranch();

                String repoLocations = data.get("REPOLOCATIONS").toString();
                this.LOWMEMORY = Boolean.parseBoolean(data.get("LOWMEMORY").toString());

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
                runningIndexRepoJobs.remove(repoResult.getName());
            }
        }
    }

    public void updateIndex(String repoName, String repoLocations, String repoRemoteLocation, boolean existingRepo, RepositoryChanged repositoryChanged) {
        String repoGitLocation = repoLocations + "/" + repoName;
        Path docDir = Paths.get(repoGitLocation);

        // Was the previous index sucessful? if not then index by path
        boolean indexsucess = checkIndexSucess(repoGitLocation);
        deleteIndexSuccess(repoGitLocation);

        if (!repositoryChanged.isClone() && indexsucess == false) {
            Singleton.getLogger().info("Failed to index " + repoName + " fully, performing a full index.");
        }

        if (repositoryChanged.isClone() || indexsucess == false) {
            Singleton.getLogger().info("Doing full index of files for " + repoName);
            this.indexDocsByPath(docDir, repoName, repoLocations, repoRemoteLocation, existingRepo);
        }
        else {
            Singleton.getLogger().info("Doing delta index of files " + repoName);
            this.indexDocsByDelta(docDir, repoName, repoLocations, repoRemoteLocation, repositoryChanged);
        }

        // Write file indicating that the index was sucessful
        Singleton.getLogger().info("Sucessfully processed writing index success for " + repoName);
        createIndexSuccess(repoGitLocation);
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
                    Thread.sleep(this.SLEEPTIME);
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


            String newString = this.getBlameFilePath(fileLocationFilename);
            String codeOwner = getCodeOwner(codeLines, newString, repoName, fileRepoLocations, scl);


            if (codeLines != null) {
                if (this.LOWMEMORY) {
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
     * Indexes all the documents in the path provided. Will also remove anything from the index if not on disk
     * Generally this is a slow update used only for the inital clone of a repository
     * NB this can be used for updates but it will be much slower as it needs to to walk the contents of the disk
     */
    public void indexDocsByPath(Path path, String repoName, String repoLocations, String repoRemoteLocation, boolean existingRepo) {
        SearchcodeLib scl = Singleton.getSearchCodeLib(); // Should have data object by this point
        List<String> fileLocations = new ArrayList<>();
        Queue<CodeIndexDocument> codeIndexDocumentQueue = Singleton.getCodeIndexQueue();

        // Convert once outside the main loop
        String fileRepoLocations = FilenameUtils.separatorsToUnix(repoLocations);
        boolean lowMemory = this.LOWMEMORY;

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                    while (CodeIndexer.shouldPauseAdding()) {
                        Singleton.getLogger().info("Pausing parser.");
                        try {
                            Thread.sleep(SLEEPTIME);
                        } catch (InterruptedException ex) {
                        }
                    }

                    // Convert Path file to unix style that way everything is easier to reason about
                    String fileParent = FilenameUtils.separatorsToUnix(file.getParent().toString());
                    String fileToString = FilenameUtils.separatorsToUnix(file.toString());
                    String fileName = file.getFileName().toString();
                    String md5Hash = Values.EMPTYSTRING;

                    // TODO check for ignored types here .get .svn .hg should be good to start with
                    // TODO template method this sucker
                    if (fileParent.endsWith("/.git") || fileParent.contains("/.git/")) {
                        return FileVisitResult.CONTINUE;
                    }


                    List<String> codeLines;
                    try {
                        codeLines = Helpers.readFileLines(fileToString, MAXFILELINEDEPTH);
                    } catch (IOException ex) {
                        return FileVisitResult.CONTINUE;
                    }

                    try {
                        FileInputStream fis = new FileInputStream(new File(fileToString));
                        md5Hash = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
                        fis.close();
                    } catch (IOException ex) {
                        Singleton.getLogger().warning("Unable to generate MD5 for " + fileToString);
                    }

                    // is the file minified?
                    if (scl.isMinified(codeLines)) {
                        Singleton.getLogger().info("Appears to be minified will not index  " + fileToString);
                        return FileVisitResult.CONTINUE;
                    }

                    String languageName = scl.languageGuesser(fileName, codeLines);
                    String fileLocation = fileToString.replace(fileRepoLocations, Values.EMPTYSTRING).replace(fileName, Values.EMPTYSTRING);
                    String fileLocationFilename = fileToString.replace(fileRepoLocations, Values.EMPTYSTRING);
                    String repoLocationRepoNameLocationFilename = fileToString;


                    String newString = getBlameFilePath(fileLocationFilename);
                    String codeOwner = getCodeOwner(codeLines, newString, repoName, fileRepoLocations, scl);

                    // If low memory don't add to the queue, just index it directly
                    if (lowMemory) {
                        CodeIndexer.indexDocument(new CodeIndexDocument(repoLocationRepoNameLocationFilename, repoName, fileName, fileLocation, fileLocationFilename, md5Hash, languageName, codeLines.size(), StringUtils.join(codeLines, " "), repoRemoteLocation, codeOwner));
                    } else {
                        Singleton.incrementCodeIndexLinesCount(codeLines.size());
                        codeIndexDocumentQueue.add(new CodeIndexDocument(repoLocationRepoNameLocationFilename, repoName, fileName, fileLocation, fileLocationFilename, md5Hash, languageName, codeLines.size(), StringUtils.join(codeLines, " "), repoRemoteLocation, codeOwner));
                    }

                    fileLocations.add(fileLocationFilename);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  "\n with message: " + ex.getMessage());
        }

        if (existingRepo) {
            CodeSearcher cs = new CodeSearcher();
            List<String> indexLocations = cs.getRepoDocuments(repoName);

            for (String file : indexLocations) {
                if (!fileLocations.contains(file)) {
                    Singleton.getLogger().info("Missing from disk, removing from index " + file);
                    try {
                        CodeIndexer.deleteByFileLocationFilename(file);
                    } catch (IOException ex) {
                        Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  "\n with message: " + ex.getMessage());
                    }
                }
            }
        }
    }


    /*
     * The below are are shared among all extending classes
     */
    public String getBlameFilePath(String fileLocationFilename) {
        String[] split = fileLocationFilename.split("/");
        String newString = String.join("/", Arrays.asList(split).subList(1, split.length));
        return newString;
    }

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
