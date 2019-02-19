package com.searchcode.app;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class TestHelpers {

    public static String getRandomAscii() {
        Random random = new Random();
        return RandomStringUtils.randomAscii(random.nextInt(20) + 20);
    }

    public static String getRandomAlphanumeric() {
        Random random = new Random();
        return RandomStringUtils.randomAlphanumeric(random.nextInt(20) + 20);
    }

    public static File createDirectoryWithFiles(String unique) throws IOException {
        File tempPath = clearAndCreateTempPath(unique);

        createFile(tempPath, "EndToEndTestFile1.php", "EndToEndTestFile EndToEndTestFile1");
        createFile(tempPath, "EndToEndTestFile2.py", "EndToEndTestFile EndToEndTestFile2");
        createFile(tempPath, "EndToEndTestFile3.java", "EndToEndTestFile EndToEndTestFile3");

        return tempPath;
    }

    public static File createFile(File tempDir, String filename, String contents) throws IOException {
        File file = new File(tempDir, filename);

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            pw.println(contents);
        }

        return file;
    }

    public static File createDirectory(File baseDir, String name) throws IOException {
        File directory = new File(baseDir, name);

        if (directory.exists()) {
            FileUtils.deleteDirectory(directory);
        }

        directory.mkdir();
        return directory;
    }

    public static File clearAndCreateTempPath(String unique) throws IOException {
        String baseName = org.apache.commons.codec.digest.DigestUtils.md5Hex(unique);
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        File tempDir = new File(baseDir, baseName);

        if (tempDir.exists()) {
            FileUtils.deleteDirectory(tempDir);
        }

        tempDir.mkdir();

        return tempDir;
    }
}
