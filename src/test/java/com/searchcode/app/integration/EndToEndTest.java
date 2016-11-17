package com.searchcode.app.integration;

import com.searchcode.app.dto.CodeResult;
import com.searchcode.app.dto.RepositoryChanged;
import com.searchcode.app.dto.SearchResult;
import com.searchcode.app.jobs.IndexFileRepoJob;
import com.searchcode.app.jobs.IndexGitRepoJob;
import com.searchcode.app.jobs.IndexSvnRepoJob;
import com.searchcode.app.service.CodeIndexer;
import com.searchcode.app.service.CodeSearcher;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class EndToEndTest extends TestCase{
    public void testEndToEndFilePath() throws IOException {
        CodeSearcher cs = new CodeSearcher();
        File directoryWithFiles = createDirectoryWithFiles("EndToEndFileTest");
        IndexFileRepoJob indexFileRepoJob = new IndexFileRepoJob();

        // Index created files
        indexFileRepoJob.indexDocsByPath(Paths.get(directoryWithFiles.toString()), "ENDTOENDTEST", "", directoryWithFiles.toString(), false);
        SearchResult searchResult = cs.search("endtoendtestfile", 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(3);

        CodeResult codeResult1 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile1.php")).findFirst().get();
        CodeResult codeResult2 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile2.py")).findFirst().get();
        CodeResult codeResult3 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile3.java")).findFirst().get();
        assertThat(codeResult1.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile1");
        assertThat(codeResult2.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile2");
        assertThat(codeResult3.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile3");

        // Delete a single file
        String codeId = searchResult.getCodeResultList().get(0).getCodeId();
        CodeIndexer.deleteByCodeId(codeId);
        searchResult = cs.search("endtoendtestfile".toLowerCase(), 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(2);

        codeResult1 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile2.py")).findFirst().get();
        codeResult2 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile3.java")).findFirst().get();
        assertThat(codeResult1.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile2");
        assertThat(codeResult2.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile3");

        // Delete file from disk then index to ensure it is removed from the index
        File toDelete = new File(directoryWithFiles.toString() + "/EndToEndTestFile2.py");
        toDelete.delete();
        indexFileRepoJob.indexDocsByPath(Paths.get(directoryWithFiles.toString()), "ENDTOENDTEST", "", directoryWithFiles.toString(), true);
        searchResult = cs.search("endtoendtestfile", 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(2);

        codeResult1 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile1.php")).findFirst().get();
        codeResult2 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile3.java")).findFirst().get();
        assertThat(codeResult1.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile1");
        assertThat(codeResult2.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile3");

        // Delete everything
        CodeIndexer.deleteByReponame("ENDTOENDTEST");
        searchResult = cs.search("endtoendtestfile".toLowerCase(), 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(0);
    }

    public void testEndToEndGitPath() throws IOException {
        CodeSearcher cs = new CodeSearcher();
        File directoryWithFiles = createDirectoryWithFiles("EndToEndGitTest");

        String result = this.runCommand(directoryWithFiles.toString(), "/usr/bin/git", "init", ".");
        result = this.runCommand(directoryWithFiles.toString(), "/usr/bin/git", "add", ".");
        result = this.runCommand(directoryWithFiles.toString(), "/usr/bin/git", "commit", "-m", "\"First commit\"");

        IndexGitRepoJob indexGitRepoJob = new IndexGitRepoJob();
        indexGitRepoJob.indexDocsByPath(Paths.get(directoryWithFiles.toString()), "ENDTOENDTEST", "", directoryWithFiles.toString(), false);

        SearchResult searchResult = cs.search("endtoendtestfile", 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(3);

        CodeResult codeResult1 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile1.php")).findFirst().get();
        CodeResult codeResult2 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile2.py")).findFirst().get();
        CodeResult codeResult3 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile3.java")).findFirst().get();
        assertThat(codeResult1.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile1");
        assertThat(codeResult2.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile2");
        assertThat(codeResult3.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile3");

        // Delete a single file
        String codeId = searchResult.getCodeResultList().get(0).getCodeId();
        CodeIndexer.deleteByCodeId(codeId);
        searchResult = cs.search("endtoendtestfile".toLowerCase(), 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(2);

        // Delete file from disk then index to ensure it is removed from the index
        File toDelete = new File(directoryWithFiles.toString() + "/EndToEndTestFile2.py");
        toDelete.delete();
        indexGitRepoJob.indexDocsByPath(Paths.get(directoryWithFiles.toString()), "ENDTOENDTEST", "", directoryWithFiles.toString(), true);
        searchResult = cs.search("endtoendtestfile", 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(2);

        codeResult1 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile1.php")).findFirst().get();
        codeResult2 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile3.java")).findFirst().get();
        assertThat(codeResult1.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile1");
        assertThat(codeResult2.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile3");

        CodeIndexer.deleteByReponame("ENDTOENDTEST");
        searchResult = cs.search("endtoendtestfile".toLowerCase(), 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(0);
    }

    public void testEndToEndSvnPath() throws IOException {
        CodeSearcher cs = new CodeSearcher();
        File directoryWithFiles = createDirectoryWithFiles("EndToEndSvnTest");

        IndexSvnRepoJob indexSvnRepoJob = new IndexSvnRepoJob();
        indexSvnRepoJob.indexDocsByPath(Paths.get(directoryWithFiles.toString()), "ENDTOENDTEST", "", directoryWithFiles.toString(), false);

        SearchResult searchResult = cs.search("endtoendtestfile", 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(3);

        CodeResult codeResult1 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile1.php")).findFirst().get();
        CodeResult codeResult2 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile2.py")).findFirst().get();
        CodeResult codeResult3 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile3.java")).findFirst().get();
        assertThat(codeResult1.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile1");
        assertThat(codeResult2.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile2");
        assertThat(codeResult3.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile3");

        // Delete a single file
        String codeId = searchResult.getCodeResultList().get(0).getCodeId();
        CodeIndexer.deleteByCodeId(codeId);
        searchResult = cs.search("endtoendtestfile".toLowerCase(), 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(2);

        // Delete file from disk then index to ensure it is removed from the index
        File toDelete = new File(directoryWithFiles.toString() + "/EndToEndTestFile2.py");
        toDelete.delete();
        indexSvnRepoJob.indexDocsByPath(Paths.get(directoryWithFiles.toString()), "ENDTOENDTEST", "", directoryWithFiles.toString(), true);
        searchResult = cs.search("endtoendtestfile", 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(2);

        codeResult1 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile1.php")).findFirst().get();
        codeResult2 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile3.java")).findFirst().get();
        assertThat(codeResult1.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile1");
        assertThat(codeResult2.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile3");

        CodeIndexer.deleteByReponame("ENDTOENDTEST");
        searchResult = cs.search("endtoendtestfile".toLowerCase(), 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(0);
    }

    public void testEndToEndGitDelta() throws IOException {
        CodeSearcher cs = new CodeSearcher();
        IndexGitRepoJob indexGitRepoJob = new IndexGitRepoJob();
        File directoryWithFiles = createDirectoryWithFiles("EndToEndGitTest");

        this.runCommand(directoryWithFiles.toString(), "/usr/bin/git", "init", ".");
        this.runCommand(directoryWithFiles.toString(), "/usr/bin/git", "add", ".");
        this.runCommand(directoryWithFiles.toString(), "/usr/bin/git", "commit", "-m", "\"First commit\"");

        // Clone from the above into a new directory
        File tempPath = this.clearAndCreateTempPath("EndToEndGitCloneTest");
        this.runCommand(tempPath.toString(), "/usr/bin/git", "clone", directoryWithFiles.toString(), "EndToEndGitTest");

        // Index
        indexGitRepoJob.indexDocsByPath(Paths.get(tempPath.toString()), "EndToEndGitTest", "", tempPath.toString(), false);
        SearchResult searchResult = cs.search("endtoendtestfile", 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(3);

        // Update the source
        createFile(directoryWithFiles, "EndToEndTestFile4.cpp", "EndToEndTestFile EndToEndTestFile4");
        this.runCommand(directoryWithFiles.toString(), "/usr/bin/git", "add", ".");
        this.runCommand(directoryWithFiles.toString(), "/usr/bin/git", "commit", "-m", "\"Add new\"");

        // Index and lets dance
        RepositoryChanged repositoryChanged = indexGitRepoJob.updateExistingRepository("EndToEndGitTest", "repoRemoteLocation", "", "", tempPath.toString(), "", false);
        String repoGitLocation = tempPath.toString() + "/" + "EndToEndGitTest";
        Path docDir = Paths.get(repoGitLocation);
        indexGitRepoJob.indexDocsByDelta(docDir, "EndToEndGitTest", tempPath.toString(), "", repositoryChanged);

        searchResult = cs.search("endtoendtestfile", 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(4);

        // Update the source
        this.runCommand(directoryWithFiles.toString(), "/usr/bin/git", "rm", "EndToEndTestFile4.cpp");
        this.runCommand(directoryWithFiles.toString(), "/usr/bin/git", "commit", "-m", "\"Baleted\"");

        // Index and lets dance
        repositoryChanged = indexGitRepoJob.updateExistingRepository("EndToEndGitTest", "repoRemoteLocation", "", "", tempPath.toString(), "", false);
        repoGitLocation = tempPath.toString() + "/" + "EndToEndGitTest";
        docDir = Paths.get(repoGitLocation);
        indexGitRepoJob.indexDocsByDelta(docDir, "EndToEndGitTest", tempPath.toString(), "", repositoryChanged);

        searchResult = cs.search("endtoendtestfile", 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(3);

        CodeIndexer.deleteByReponame("EndToEndGitTest");
        searchResult = cs.search("endtoendtestfile".toLowerCase(), 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(0);
    }

    private String runCommand(String directory, String... command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.directory(new File(directory));
        Process process = processBuilder.start();

        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        StringBuilder sb = new StringBuilder();

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
    }

    private File createDirectoryWithFiles(String unique) throws IOException {
        File tempPath = this.clearAndCreateTempPath(unique);

        createFile(tempPath, "EndToEndTestFile1.php", "EndToEndTestFile EndToEndTestFile1");
        createFile(tempPath, "EndToEndTestFile2.py",  "EndToEndTestFile EndToEndTestFile2");
        createFile(tempPath, "EndToEndTestFile3.java",  "EndToEndTestFile EndToEndTestFile3");

        return tempPath;
    }

    private File createFile(File tempDir, String filename, String contents) throws IOException {
        File file = new File(tempDir, filename);

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            pw.println(contents);
        }

        return file;
    }

    private File clearAndCreateTempPath(String unique) throws IOException {
        String baseName = org.apache.commons.codec.digest.DigestUtils.md5Hex(unique);
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        File tempDir = new File(baseDir, baseName);

        if(tempDir.exists()) {
            FileUtils.deleteDirectory(tempDir);
        }

        tempDir.mkdir();

        return tempDir;
    }
}
