package com.searchcode.app.integration;

import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class EndToEndTest extends TestCase{
    public void testEndToEndFile() throws IOException {
        // Create directory tree with files to index
        // Index directory tree
        // Search index for files
        // Delete single file using the path
        // Verify that file was deleted
        // Delete whole directory from index
        // Verify all is deleted

        createDirectoryWithFiles();

    }

    private String createDirectoryWithFiles() throws IOException {
        File tempPath = this.createTempPath();

        // Create files inside temp path

        return tempPath.toString();
    }

    private File createTempPath() throws IOException {
        String baseName = org.apache.commons.codec.digest.DigestUtils.md5Hex("EndToEndFileTest");
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        File tempDir = new File(baseDir, baseName);

        if(!tempDir.exists()) {
            FileUtils.deleteDirectory(tempDir);
        }

        tempDir.mkdir();

        return tempDir;
    }
}
