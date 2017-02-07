/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.8
 */

package com.searchcode.app.jobs.repository;


import au.com.bytecode.opencsv.CSVWriter;
import com.searchcode.app.config.Values;
import com.searchcode.app.dto.BinaryFinding;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.dto.RepositoryChanged;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.CodeIndexer;
import com.searchcode.app.service.CodeSearcher;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.*;
import com.searchcode.app.util.Properties;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public abstract class IndexBaseRepoJob implements Job {

    protected boolean LOWMEMORY = true;
    protected int SLEEPTIME = 5000;
    public int MAXFILELINEDEPTH = Helpers.tryParseInt(Properties.getProperties().getProperty(Values.MAXFILELINEDEPTH, Values.DEFAULTMAXFILELINEDEPTH), Values.DEFAULTMAXFILELINEDEPTH);
    public boolean LOGINDEXED = Boolean.parseBoolean(Properties.getProperties().getProperty(Values.LOG_INDEXED, "false")); // TODO make this configurable

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

    public boolean ignoreFile(String fileParent) {
        return true;
    }

    /**
     * This method can be implemented by extending class but is not required
     * as it requires project name at the front
     */
    public String getFileLocationFilename(String fileToString, String fileRepoLocations) {
        return fileToString.replace(fileRepoLocations, Values.EMPTYSTRING);
    }

    /**
     * The main method used for finding jobs to index and actually doing the work
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (isEnabled() == false) {
            return;
        }

        if (Singleton.getBackgroundJobsEnabled() == false) {
            return;
        }

        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        while (CodeIndexer.shouldPauseAdding()) {
            Singleton.getLogger().info("Pausing parser.");
            return;
        }

        // Pull the next repo to index from the queue
        UniqueRepoQueue repoQueue = this.getNextQueuedRepo();

        RepoResult repoResult = repoQueue.poll();
        AbstractMap<String, Integer> runningIndexRepoJobs = Singleton.getRunningIndexRepoJobs();

        if (repoResult != null && !runningIndexRepoJobs.containsKey(repoResult.getName())) {

            String repoName = repoResult.getName();
            String repoRemoteLocation = repoResult.getUrl();
            String repoUserName = repoResult.getUsername();
            String repoPassword = repoResult.getPassword();
            String repoBranch = repoResult.getBranch();
            Singleton.getLogger().info("Indexing " + repoName);

            try {
                runningIndexRepoJobs.put(repoResult.getName(), (int) (System.currentTimeMillis() / 1000));

                JobDataMap data = context.getJobDetail().getJobDataMap();

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

                File file = new File(repoGitLocation);
                boolean existingRepo = file.exists();
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

        // Used to hold the reports of what was indexed
        List<String[]> reportList = new ArrayList<>();

        for(String changedFile: repositoryChanged.getChangedFiles()) {
            if (this.shouldJobPauseOrTerminate() == true) {
                return;
            }

            String[] split = changedFile.split("/");
            String fileName = split[split.length - 1];
            changedFile = fileRepoLocations + "/" + repoName + "/" + changedFile;
            changedFile = changedFile.replace("//", "/");

            String md5Hash = Values.EMPTYSTRING;
            List<String> codeLines = null;

            try {
                codeLines = Helpers.readFileLinesGuessEncoding(changedFile, this.MAXFILELINEDEPTH);
            } catch (IOException ex) {
                Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  "\n with message: " + ex.getMessage());
                reportList.add(new String[]{changedFile, "excluded", "unable to guess guess file encoding"});
                break;
            }

            if (scl.isMinified(codeLines, fileName)) {
                Singleton.getLogger().info("Appears to be minified will not index  " + changedFile);
                reportList.add(new String[]{changedFile, "excluded", "appears to be minified"});
                break;
            }

            if (codeLines.isEmpty()) {
                Singleton.getLogger().info("Unable to guess encoding type or file is empty " + changedFile);
                reportList.add(new String[]{changedFile, "excluded", "empty file"});
                break;
            }

            if (this.determineBinary(changedFile, fileName, codeLines, reportList)) {
                break;
            }

            md5Hash = this.getFileMd5(changedFile);

            String languageName = scl.languageGuesser(changedFile, codeLines);

            String fileLocation = getRelativeToProjectPath(path.toString(), changedFile);
            String fileLocationFilename = changedFile.replace(fileRepoLocations, Values.EMPTYSTRING);
            String repoLocationRepoNameLocationFilename = changedFile;

            String newString = this.getBlameFilePath(fileLocationFilename);
            String codeOwner = getCodeOwner(codeLines, newString, repoName, fileRepoLocations, scl);

            reportList.add(new String[]{changedFile, "included", ""});

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

        if (this.LOGINDEXED && reportList.isEmpty() == false) {
            this.logIndexed(repoName + "_delta", reportList);
        }

        for(String deletedFile: repositoryChanged.getDeletedFiles()) {
            deletedFile = fileRepoLocations + "/" + repoName + "/" + deletedFile;
            deletedFile = deletedFile.replace("//", "/");
            Singleton.getLogger().info("Missing from disk, removing from index " + deletedFile);
            try {
                CodeIndexer.deleteByCodeId(DigestUtils.sha1Hex(deletedFile));
            } catch (IOException ex) {
                Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  " indexDocsByDelta deleteByFileLocationFilename for " + repoName + " " + deletedFile + "\n with message: " + ex.getMessage());
            }
        }
    }

    /**
     * Indexes all the documents in the path provided. Will also remove anything from the index if not on disk
     * Generally this is a slow update used only for the initial clone of a repository
     * NB this can be used for updates but it will be much slower as it needs to to walk the contents of the disk
     */
    public void indexDocsByPath(Path path, String repoName, String repoLocations, String repoRemoteLocation, boolean existingRepo) {
        SearchcodeLib scl = Singleton.getSearchCodeLib();
        CodeSearcher codeSearcher = new CodeSearcher();
        
        Map<String, String> fileLocationsMap = new HashMap<>();

        Queue<CodeIndexDocument> codeIndexDocumentQueue = Singleton.getCodeIndexQueue();

        // Convert once outside the main loop
        String fileRepoLocations = FilenameUtils.separatorsToUnix(repoLocations);
        boolean lowMemory = this.LOWMEMORY;

        // Used to hold the reports of what was indexed
        List<String[]> reportList = new ArrayList<>();

        try {
            Files.walkFileTree(path, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        if (shouldJobPauseOrTerminate()) {
                            return FileVisitResult.TERMINATE;
                        }

                        // Convert Path file to unix style that way everything is easier to reason about
                        String fileParent = FilenameUtils.separatorsToUnix(file.getParent().toString());
                        String fileToString = FilenameUtils.separatorsToUnix(file.toString());
                        String fileName = file.getFileName().toString();
                        String repoLocationRepoNameLocationFilename = fileToString;

                        if (ignoreFile(fileParent)) {
                            return FileVisitResult.CONTINUE;
                        }

                        // This needs to be the primary key of the file
                        fileLocationsMap.put(repoLocationRepoNameLocationFilename, null);

                        List<String> codeLines;
                        try {
                            codeLines = Helpers.readFileLinesGuessEncoding(fileToString, MAXFILELINEDEPTH);
                        } catch (IOException ex) {
                            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() + " indexDocsByPath walkFileTree\n with message: " + ex.getMessage() + " for file " + file.toString() + " in path " + path +" in repo " + repoName);
                            if (LOGINDEXED) {
                                reportList.add(new String[]{fileToString, "excluded", "unable to guess guess file encoding"});
                            }
                            fileLocationsMap.remove(repoLocationRepoNameLocationFilename);
                            return FileVisitResult.CONTINUE;
                        }

                        if (scl.isMinified(codeLines, fileName)) {
                            Singleton.getLogger().info("Appears to be minified will not index " + fileToString);
                            if (LOGINDEXED) {
                                reportList.add(new String[]{fileToString, "excluded", "appears to be minified"});
                            }
                            fileLocationsMap.remove(repoLocationRepoNameLocationFilename);
                            return FileVisitResult.CONTINUE;
                        }

                        if (codeLines.isEmpty()) {
                            Singleton.getLogger().info("Unable to guess encoding type or file is empty " + fileToString);
                            if (LOGINDEXED) {
                                reportList.add(new String[]{fileToString, "excluded", "empty file"});
                            }
                            fileLocationsMap.remove(repoLocationRepoNameLocationFilename);
                            return FileVisitResult.CONTINUE;
                        }

                        if (determineBinary(fileToString, fileName, codeLines, reportList)) {
                            fileLocationsMap.remove(repoLocationRepoNameLocationFilename);
                            return FileVisitResult.CONTINUE;
                        }

                        String md5Hash = getFileMd5(fileToString);
                        String languageName = scl.languageGuesser(fileName, codeLines);


                        String fileLocation = getRelativeToProjectPath(path.toString(), fileToString);
                        String fileLocationFilename = getFileLocationFilename(fileToString, fileRepoLocations);

                        String newString = getBlameFilePath(fileLocationFilename);
                        String codeOwner = getCodeOwner(codeLines, newString, repoName, fileRepoLocations, scl);

                        if (lowMemory) { // TODO this should be inside the indexer class not in here
                            CodeIndexer.indexDocument(new CodeIndexDocument(repoLocationRepoNameLocationFilename, repoName, fileName, fileLocation, fileLocationFilename, md5Hash, languageName, codeLines.size(), StringUtils.join(codeLines, " "), repoRemoteLocation, codeOwner));
                        } else {
                            Singleton.incrementCodeIndexLinesCount(codeLines.size());
                            codeIndexDocumentQueue.add(new CodeIndexDocument(repoLocationRepoNameLocationFilename, repoName, fileName, fileLocation, fileLocationFilename, md5Hash, languageName, codeLines.size(), StringUtils.join(codeLines, " "), repoRemoteLocation, codeOwner));
                        }

                        if (LOGINDEXED) {
                            reportList.add(new String[]{fileToString, "included", Values.EMPTYSTRING});
                        }
                    }
                    catch(Exception ex) {
                        Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() + " indexDocsByPath walkFileTree\n with message: " + ex.getMessage() + " for file " + file.toString() + " in path " + path +" in repo " + repoName);
                    }

                    // Continue at all costs
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  " indexDocsByPath walkFileTree\n with message: " + ex.getMessage());
        }

        if (this.LOGINDEXED) {
            logIndexed(repoName, reportList);
        }

        if (existingRepo) {
            this.cleanMissingPathFiles(codeSearcher, repoName, fileLocationsMap);
        }
    }

    /**
     * Method to remove from the index files that are no longer required
     */
    public void cleanMissingPathFiles(CodeSearcher codeSearcher, String repoName, Map<String, String> fileLocations) {
        int page = 0;
        boolean doClean = true;

        while (doClean) {
            List<String> indexLocations = codeSearcher.getRepoDocuments(repoName, page);
            Singleton.getLogger().info("cleanMissingPathFiles doClean " + page + " " + indexLocations.size());

            if (indexLocations.isEmpty()) {
                doClean = false;
            }

            for (String file: indexLocations) {
                if (!fileLocations.containsKey(file)) {
                    Singleton.getLogger().info("Missing from disk, removing from index " + file);
                    try {
                        CodeIndexer.deleteByCodeId(DigestUtils.sha1Hex(file));
                    } catch (IOException ex) {
                        Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() + " indexDocsByPath deleteByFileLocationFilename for " + repoName + " " + file + "\n with message: " + ex.getMessage());
                    }
                }
            }

            page++;
        }
    }


    /**
     * Returns a string the represents where the file lives inside the repository
     */
    public String getRelativeToProjectPath(String projectPath, String filePath) {
        if (projectPath.charAt(projectPath.length() - 1) == '/') {
            projectPath = projectPath.substring(0, projectPath.length() - 1);
        }
        projectPath = projectPath.replace("//", "/");
        filePath = filePath.replace("//", "/");

        return filePath.replace(projectPath, Values.EMPTYSTRING);
    }

    /**
     * Shared method which performs all logic for determining and doing if
     * the file is believed to be binary
     */
    public boolean determineBinary(String fileLocation, String fileName, List<String> codeLines, List<String[]> reportList) {
        SearchcodeLib scl = new SearchcodeLib();
        BinaryFinding binaryFinding = scl.isBinary(codeLines, fileName);

        if (binaryFinding.isBinary()) {
            Singleton.getLogger().info("Appears to be binary will not index " + binaryFinding.getReason() + " " + fileLocation);
            reportList.add(new String[]{ fileLocation, "excluded", binaryFinding.getReason() });
            return true;
        }

        return false;
    }

    /**
     * Attempts to get MD5 for file on disk
     */
    public String getFileMd5(String fileName) {
        String md5Hash = Values.EMPTYSTRING;
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(new File(fileName));
            md5Hash = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
        } catch (IOException ex) {
            Singleton.getLogger().warning("Unable to generate MD5 for " + fileName);
        }
        finally {
            IOUtils.closeQuietly(fis);
        }

        return md5Hash;
    }

    /**
     * Checks if the job should pause and if so loop endlessly sleeping. Returns
     * true if the job should be terminated and false if it should continue to run
     */
    public boolean shouldJobPauseOrTerminate() {
        if (Singleton.getBackgroundJobsEnabled() == false) {
            return true;
        }

        while (CodeIndexer.shouldPauseAdding()) {
            if (Singleton.getBackgroundJobsEnabled() == false) {
                return true;
            }

            Singleton.getLogger().info("Pausing parser.");
            try {
                Thread.sleep(SLEEPTIME);
            } catch (InterruptedException ex) {}
        }

        return false;
    }

    /**
     * Logs to the logs directory a formatted CSV of the supplied list strings
     */
    private void logIndexed(String repoName, List<String[]> reportList) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(Helpers.getLogPath() + repoName + ".csv.tmp"));
            writer.writeAll(reportList);
            writer.flush();
            writer.close();

            Path source = Paths.get(Helpers.getLogPath() + repoName + ".csv.tmp");
            Files.move(source, source.resolveSibling(repoName + ".csv"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() + " logIndexed for " + repoName + "\n with message: " + ex.getMessage());
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
