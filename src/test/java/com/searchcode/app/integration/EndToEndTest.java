package com.searchcode.app.integration;

import com.searchcode.app.dto.SearchResult;
import com.searchcode.app.jobs.IndexFileRepoJob;
import com.searchcode.app.service.CodeIndexer;
import com.searchcode.app.service.CodeSearcher;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class EndToEndTest extends TestCase{
    public void testEndToEndFile() throws IOException {
        File directoryWithFiles = createDirectoryWithFiles();
        IndexFileRepoJob indexFileRepoJob = new IndexFileRepoJob();
        indexFileRepoJob.indexDocsByPath(Paths.get(directoryWithFiles.toString()), "ENDTOENDTEST", "", directoryWithFiles.toString(), false);

        CodeSearcher cs = new CodeSearcher();

        SearchResult endToEndTestFile = cs.search("EndToEndTestFile".toLowerCase(), 0);
        assertThat(endToEndTestFile.getCodeResultList().size()).isEqualTo(2);

        //String toDelete = directoryWithFiles.toString() + "/EndToEndTestFile1.php";
        String toDelete = endToEndTestFile.getCodeResultList().get(0).getCodePath();
        CodeIndexer.deleteByFilePath(toDelete);

        endToEndTestFile = cs.search("EndToEndTestFile".toLowerCase(), 0);
        assertThat(endToEndTestFile.getCodeResultList().size()).isEqualTo(1);

        CodeIndexer.deleteByReponame("ENDTOENDTEST");
        endToEndTestFile = cs.search("EndToEndTestFile".toLowerCase(), 0);
        assertThat(endToEndTestFile.getCodeResultList().size()).isEqualTo(0);
    }



    private File createDirectoryWithFiles() throws IOException {
        File tempPath = this.createTempPath();

        File file1 = createFile(tempPath, "EndToEndTestFile1.php", "EndToEndTestFile EndToEndTestFile1");
        File file2 = createFile(tempPath, "EndToEndTestFile2.py",  "EndToEndTestFile EndToEndTestFile2");

        return tempPath;
    }

    private File createFile(File tempDir, String filename, String contents) throws IOException {
        File file = new File(tempDir, filename);

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            pw.println(contents);
        }

        return file;
    }

    private File createTempPath() throws IOException {
        String baseName = org.apache.commons.codec.digest.DigestUtils.md5Hex("EndToEndFileTest");
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        File tempDir = new File(baseDir, baseName);

        if(tempDir.exists()) {
            FileUtils.deleteDirectory(tempDir);
        }

        tempDir.mkdir();

        return tempDir;
    }
}
