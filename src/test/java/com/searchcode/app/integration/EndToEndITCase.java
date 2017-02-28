package com.searchcode.app.integration;

import com.searchcode.app.TestHelpers;
import com.searchcode.app.config.Values;
import com.searchcode.app.dto.CodeResult;
import com.searchcode.app.dto.RepositoryChanged;
import com.searchcode.app.dto.SearchResult;
import com.searchcode.app.jobs.repository.IndexFileRepoJob;
import com.searchcode.app.jobs.repository.IndexGitRepoJob;
import com.searchcode.app.jobs.repository.IndexSvnRepoJob;
import com.searchcode.app.service.CodeIndexer;
import com.searchcode.app.service.CodeSearcher;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Properties;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class EndToEndITCase extends TestCase{

    public String GITPATH = Properties.getProperties().getProperty(Values.GITBINARYPATH, Values.DEFAULTGITBINARYPATH);

    public void testEndToEndFilePath() throws IOException {
        CodeSearcher cs = new CodeSearcher();
        File directoryWithFiles = TestHelpers.createDirectoryWithFiles("EndToEndFileTest");
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
        Singleton.getCodeIndexer().deleteByCodeId(codeId);
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
        Singleton.getCodeIndexer().deleteByReponame("ENDTOENDTEST");
        searchResult = cs.search("endtoendtestfile".toLowerCase(), 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(0);
    }

    public void testEndToEndGitPath() throws IOException {
        CodeSearcher cs = new CodeSearcher();
        File directoryWithFiles = TestHelpers.createDirectoryWithFiles("EndToEndGitTest");

        String result = this.runCommand(directoryWithFiles.toString(), this.GITPATH, "init", ".");
        result = this.runCommand(directoryWithFiles.toString(), this.GITPATH, "add", ".");
        result = this.runCommand(directoryWithFiles.toString(), this.GITPATH, "commit", "-m", "\"First commit\"");

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
        Singleton.getCodeIndexer().deleteByCodeId(codeId);
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

        Singleton.getCodeIndexer().deleteByReponame("ENDTOENDTEST");
        searchResult = cs.search("endtoendtestfile".toLowerCase(), 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(0);
    }

    public void testEndToEndSvnPath() throws IOException {
        CodeSearcher cs = new CodeSearcher();
        File directoryWithFiles = TestHelpers.createDirectoryWithFiles("EndToEndSvnTest");

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
        Singleton.getCodeIndexer().deleteByCodeId(codeId);
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

        Singleton.getCodeIndexer().deleteByReponame("ENDTOENDTEST");
        searchResult = cs.search("endtoendtestfile".toLowerCase(), 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(0);
    }

    public void testEndToEndGitDelta() throws IOException {
        CodeSearcher cs = new CodeSearcher();
        IndexGitRepoJob indexGitRepoJob = new IndexGitRepoJob();
        File directoryWithFiles = TestHelpers.createDirectoryWithFiles("EndToEndGitTest");

        this.runCommand(directoryWithFiles.toString(), this.GITPATH, "init", ".");
        this.runCommand(directoryWithFiles.toString(), this.GITPATH, "add", ".");
        this.runCommand(directoryWithFiles.toString(), this.GITPATH, "commit", "-m", "\"First commit\"");

        // Clone from the above into a new directory
        File tempPath = TestHelpers.clearAndCreateTempPath("EndToEndGitCloneTest");
        this.runCommand(tempPath.toString(), this.GITPATH, "clone", directoryWithFiles.toString(), "EndToEndGitTest");

        // Index
        indexGitRepoJob.indexDocsByPath(Paths.get(tempPath.toString()), "EndToEndGitTest", "", tempPath.toString(), false);
        SearchResult searchResult = cs.search("endtoendtestfile", 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(3);

        // Update the source
        TestHelpers.createFile(directoryWithFiles, "EndToEndTestFile4.cpp", "EndToEndTestFile EndToEndTestFile4");
        this.runCommand(directoryWithFiles.toString(), this.GITPATH, "add", ".");
        this.runCommand(directoryWithFiles.toString(), this.GITPATH, "commit", "-m", "\"Add new\"");

        // Index and lets dance
        RepositoryChanged repositoryChanged = indexGitRepoJob.updateExistingRepository("EndToEndGitTest", "repoRemoteLocation", "", "", tempPath.toString(), "", false);
        String repoGitLocation = tempPath.toString() + "/" + "EndToEndGitTest";
        Path docDir = Paths.get(repoGitLocation);
        indexGitRepoJob.indexDocsByDelta(docDir, "EndToEndGitTest", tempPath.toString(), "", repositoryChanged);

        searchResult = cs.search("endtoendtestfile", 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(4);

        // Update the source
        this.runCommand(directoryWithFiles.toString(), this.GITPATH, "rm", "EndToEndTestFile4.cpp");
        this.runCommand(directoryWithFiles.toString(), this.GITPATH, "commit", "-m", "\"Baleted\"");

        // Index and lets dance
        repositoryChanged = indexGitRepoJob.updateExistingRepository("EndToEndGitTest", "repoRemoteLocation", "", "", tempPath.toString(), "", false);
        repoGitLocation = tempPath.toString() + "/" + "EndToEndGitTest";
        docDir = Paths.get(repoGitLocation);
        indexGitRepoJob.indexDocsByDelta(docDir, "EndToEndGitTest", tempPath.toString(), "", repositoryChanged);

        searchResult = cs.search("endtoendtestfile", 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(3);

        Singleton.getCodeIndexer().deleteByReponame("EndToEndGitTest");
        searchResult = cs.search("endtoendtestfile".toLowerCase(), 0);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(0);
    }

    private String runCommand(String directory, String... command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.directory(new File(directory));
        Process process = processBuilder.start();

        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is, Values.CHARSET_UTF8);
        BufferedReader br = new BufferedReader(isr);
        String line;
        StringBuilder sb = new StringBuilder();

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
    }
}
