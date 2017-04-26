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
import java.util.List;


public class SearchcodeFileVisitor<Path> extends SimpleFileVisitor<Path> {
    @Override
    public FileVisitResult preVisitDirectory(Object dir, BasicFileAttributes attrs) throws IOException {
        return null;
    }

    @Override
    public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs) throws IOException {

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
                codeLines = Singleton.getHelpers().readFileLinesGuessEncoding(fileToString, MAXFILELINEDEPTH);
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
            String languageName = Singleton.getFileClassifier().languageGuesser(fileName, codeLines);


            String fileLocation = getRelativeToProjectPath(path.toString(), fileToString);
            String fileLocationFilename = getFileLocationFilename(fileToString, fileRepoLocations);

            String newString = getBlameFilePath(fileLocationFilename);
            String codeOwner = getCodeOwner(codeLines, newString, repoName, fileRepoLocations, scl);

            if (lowMemory) { // TODO this should be inside the indexer class not in here
                Singleton.getCodeIndexer().indexDocument(new CodeIndexDocument(repoLocationRepoNameLocationFilename, repoName, fileName, fileLocation, fileLocationFilename, md5Hash, languageName, codeLines.size(), StringUtils.join(codeLines, " "), repoRemoteLocation, codeOwner));
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

    @Override
    public FileVisitResult visitFileFailed(Object file, IOException exc) throws IOException {
        return null;
    }

    @Override
    public FileVisitResult postVisitDirectory(Object dir, IOException exc) throws IOException {
        return null;
    }
}
