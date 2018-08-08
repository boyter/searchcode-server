package com.searchcode.app.jobs;

import com.searchcode.app.config.Values;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.Properties;
import org.apache.commons.io.FilenameUtils;
import org.quartz.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class PopulateSpellingCorrectorJob implements Job {

    public int MAXFILELINEDEPTH = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.MAXFILELINEDEPTH, Values.DEFAULTMAXFILELINEDEPTH), Values.DEFAULTMAXFILELINEDEPTH);

    public void execute(JobExecutionContext context) throws JobExecutionException {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        Path path = Paths.get(Properties.getProperties().getProperty(Values.REPOSITORYLOCATION, Values.DEFAULTREPOSITORYLOCATION));
        Singleton.getLogger().info("Starting PopulateSpellingCorrectorJob in path " + path.toString());

        try {
            Files.walkFileTree(path, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                    // Convert Path file to unix style that way everything is easier to reason about
                    String fileParent = FilenameUtils.separatorsToUnix(file.getParent().toString());
                    String fileToString = FilenameUtils.separatorsToUnix(file.toString());
                    String fileName = file.getFileName().toString();

                    if (Singleton.getHelpers().ignoreFiles(fileParent)) {
                        return FileVisitResult.CONTINUE;
                    }

                    List<String> codeLines;
                    try {
                        codeLines = Singleton.getHelpers().readFileLinesGuessEncoding(fileToString, MAXFILELINEDEPTH);
                    } catch (IOException ex) {
                        return FileVisitResult.CONTINUE;
                    }

                    if (Singleton.getSearchCodeLib().isMinified(codeLines, fileName)) {
                        return FileVisitResult.CONTINUE;
                    }

                    if (codeLines.isEmpty()) {
                        return FileVisitResult.CONTINUE;
                    }

                    if (Singleton.getSearchCodeLib().isBinary(codeLines, fileName).isBinary()) {
                        return FileVisitResult.CONTINUE;
                    }

                    Singleton.getSearchCodeLib().addToSpellingCorrector(String.join(" ", codeLines));
                } catch (Exception ex) {
                    Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() + " PopulateSpellingCorrectorJob\n with message: " + ex.getMessage() + " for file " + file.toString() + " in path " + path);
                }

                // Continue at all costs
                return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in " + this.getClass() +  " PopulateSpellingCorrectorJob\n with message: " + ex.getMessage());
        }
    }
}
