package com.searchcode.app.jobs.repository;

import com.searchcode.app.config.Values;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.service.Singleton;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;


public class SearchcodeFileVisitor<Path> extends SimpleFileVisitor<Path> {

    private final IndexBaseRepoJob indexBaseRepoJob;
    private final String repoName;
    private final String fileRepoLocations;
    private final String repoRemoteLocation;

    // Used to hold the reports of what was indexed and what needs to be removed
    public List<String[]> reportList = new ArrayList<>();
    public Map<String, String> fileLocationsMap = new HashMap<>();

    public SearchcodeFileVisitor(IndexBaseRepoJob indexBaseRepoJob, String repoName, String fileRepoLocations, String repoRemoteLocation) {
        this.indexBaseRepoJob = indexBaseRepoJob;
        this.repoName = repoName;
        this.fileRepoLocations = fileRepoLocations;
        this.repoRemoteLocation = repoRemoteLocation;
    }

    @Override
    public FileVisitResult preVisitDirectory(Object dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Object file, BasicFileAttributes attrs) throws IOException {

        try {
            java.nio.file.Path filePath = (java.nio.file.Path)file;

            if (this.indexBaseRepoJob.shouldJobPauseOrTerminate()) {
                return FileVisitResult.TERMINATE;
            }

            // Convert Path file to unix style that way everything is easier to reason about
            String fileParent = FilenameUtils.separatorsToUnix(filePath.getParent().toString());
            String fileToString = FilenameUtils.separatorsToUnix(filePath.toString());
            String fileName = filePath.getFileName().toString();
            String repoLocationRepoNameLocationFilename = fileToString;

            if (this.indexBaseRepoJob.ignoreFile(fileParent)) {
                return FileVisitResult.CONTINUE;
            }

            // This needs to be the primary key of the file
            fileLocationsMap.put(repoLocationRepoNameLocationFilename, null);

            List<String> codeLines;
            try {
                codeLines = Singleton.getHelpers().readFileLinesGuessEncoding(fileToString, this.indexBaseRepoJob.MAXFILELINEDEPTH);
            } catch (IOException ex) {
                Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() + " indexDocsByPath walkFileTree\n with message: " + ex.getMessage() + " for file " + file.toString() + " in path " + filePath +" in repo " + repoName);
                if (this.indexBaseRepoJob.LOGINDEXED) {
                    reportList.add(new String[]{fileToString, "excluded", "unable to guess guess file encoding"});
                }
                fileLocationsMap.remove(repoLocationRepoNameLocationFilename);
                return FileVisitResult.CONTINUE;
            }

            if (Singleton.getSearchCodeLib().isMinified(codeLines, fileName)) {
                Singleton.getLogger().info("Appears to be minified will not index " + fileToString);
                if (this.indexBaseRepoJob.LOGINDEXED) {
                    reportList.add(new String[]{fileToString, "excluded", "appears to be minified"});
                }
                fileLocationsMap.remove(repoLocationRepoNameLocationFilename);
                return FileVisitResult.CONTINUE;
            }

            if (codeLines.isEmpty()) {
                Singleton.getLogger().info("Unable to guess encoding type or file is empty " + fileToString);
                if (this.indexBaseRepoJob.LOGINDEXED) {
                    reportList.add(new String[]{fileToString, "excluded", "empty file"});
                }
                fileLocationsMap.remove(repoLocationRepoNameLocationFilename);
                return FileVisitResult.CONTINUE;
            }

            if (this.indexBaseRepoJob.determineBinary(fileToString, fileName, codeLines, reportList)) {
                fileLocationsMap.remove(repoLocationRepoNameLocationFilename);
                return FileVisitResult.CONTINUE;
            }

            String md5Hash = this.indexBaseRepoJob.getFileMd5(fileToString);
            String languageName = Singleton.getFileClassifier().languageGuesser(fileName, codeLines);


            String fileLocation = this.indexBaseRepoJob.getRelativeToProjectPath(file.toString(), fileToString);
            String fileLocationFilename = this.indexBaseRepoJob.getFileLocationFilename(fileToString, fileRepoLocations);

            String newString = this.indexBaseRepoJob.getBlameFilePath(fileLocationFilename);
            String codeOwner = this.indexBaseRepoJob.getCodeOwner(codeLines, newString, this.repoName, fileRepoLocations, Singleton.getSearchCodeLib());

            if (this.indexBaseRepoJob.LOWMEMORY) {
                Singleton.getCodeIndexer().indexDocument(new CodeIndexDocument(repoLocationRepoNameLocationFilename, this.repoName, fileName, fileLocation, fileLocationFilename, md5Hash, languageName, codeLines.size(), StringUtils.join(codeLines, " "), repoRemoteLocation, codeOwner));
            } else {
                Singleton.incrementCodeIndexLinesCount(codeLines.size());
                Singleton.getCodeIndexQueue().add(new CodeIndexDocument(repoLocationRepoNameLocationFilename, this.repoName, fileName, fileLocation, fileLocationFilename, md5Hash, languageName, codeLines.size(), StringUtils.join(codeLines, " "), repoRemoteLocation, codeOwner));
            }

            if (this.indexBaseRepoJob.LOGINDEXED) {
                reportList.add(new String[]{fileToString, "included", Values.EMPTYSTRING});
            }
        }
        catch(Exception ex) {
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() + " indexDocsByPath walkFileTree\n with message: " + ex.getMessage() + " for file " + file.toString() + " in path " + file + " in repo " + this.repoName);
        }

        // Continue at all costs
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Object file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Object dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
}
