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


            IndexBaseRepoJob.CodeLinesReturn codeLinesReturn = this.indexBaseRepoJob.getCodeLines(fileToString, reportList);
            if (codeLinesReturn.isError()) {
                fileLocationsMap.remove(repoLocationRepoNameLocationFilename);
                return FileVisitResult.CONTINUE;
            }


            IndexBaseRepoJob.IsMinifiedReturn isMinified = this.indexBaseRepoJob.getIsMinified(codeLinesReturn.getCodeLines(), fileName, reportList);
            if (isMinified.isMinified()) { return FileVisitResult.CONTINUE; }


            if (codeLinesReturn.getCodeLines().isEmpty()) {
                Singleton.getLogger().info("Unable to guess encoding type or file is empty " + fileToString);
                if (this.indexBaseRepoJob.LOGINDEXED) {
                    reportList.add(new String[]{fileToString, "excluded", "empty file"});
                }
                fileLocationsMap.remove(repoLocationRepoNameLocationFilename);
                return FileVisitResult.CONTINUE;
            }

            if (this.indexBaseRepoJob.determineBinary(fileToString, fileName, codeLinesReturn.getCodeLines(), reportList)) {
                fileLocationsMap.remove(repoLocationRepoNameLocationFilename);
                return FileVisitResult.CONTINUE;
            }

            String md5Hash = this.indexBaseRepoJob.getFileMd5(fileToString);
            String languageName = Singleton.getFileClassifier().languageGuesser(fileName, codeLinesReturn.getCodeLines());


            String fileLocation = this.indexBaseRepoJob.getRelativeToProjectPath(file.toString(), fileToString);
            String fileLocationFilename = this.indexBaseRepoJob.getFileLocationFilename(fileToString, fileRepoLocations);

            String newString = this.indexBaseRepoJob.getBlameFilePath(fileLocationFilename);
            String codeOwner = this.indexBaseRepoJob.getCodeOwner(codeLinesReturn.getCodeLines(), newString, this.repoName, fileRepoLocations, Singleton.getSearchCodeLib());

            if (this.indexBaseRepoJob.LOWMEMORY) {
                Singleton.getCodeIndexer().indexDocument(new CodeIndexDocument(repoLocationRepoNameLocationFilename, this.repoName, fileName, fileLocation, fileLocationFilename, md5Hash, languageName, codeLinesReturn.getCodeLines().size(), StringUtils.join(codeLinesReturn.getCodeLines(), " "), repoRemoteLocation, codeOwner));
            } else {
                Singleton.incrementCodeIndexLinesCount(codeLinesReturn.getCodeLines().size());
                Singleton.getCodeIndexQueue().add(new CodeIndexDocument(repoLocationRepoNameLocationFilename, this.repoName, fileName, fileLocation, fileLocationFilename, md5Hash, languageName, codeLinesReturn.getCodeLines().size(), StringUtils.join(codeLinesReturn.getCodeLines(), " "), repoRemoteLocation, codeOwner));
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
