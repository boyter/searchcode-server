package com.searchcode.app.jobs;

import com.searchcode.app.config.Values;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.LoggerWrapper;
import com.searchcode.app.util.Properties;
import org.apache.commons.io.FilenameUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class PopulateSpellingCorrectorJob implements Job {

    private final LoggerWrapper logger;
    public int MAXFILELINEDEPTH = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.MAXFILELINEDEPTH, Values.DEFAULTMAXFILELINEDEPTH), Values.DEFAULTMAXFILELINEDEPTH);

    public PopulateSpellingCorrectorJob() {
        this.logger = Singleton.getLogger();
    }

    public void execute(JobExecutionContext context) {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        Path path = Paths.get(Properties.getProperties().getProperty(Values.REPOSITORYLOCATION, Values.DEFAULTREPOSITORYLOCATION));
        this.logger.info(String.format("4f5b6cb6::starting populatespellingcorrector in path %s", path.toString()));

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
                    Singleton.getLogger().severe(String.format("a173f0e6::error in class %s exception %s", ex.getClass(), ex.getMessage()));
                }

                // Continue at all costs
                return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            this.logger.severe(String.format("55d4cf9a::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }
    }
}
