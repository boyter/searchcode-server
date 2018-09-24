package com.searchcode.app.integration;

import com.searchcode.app.TestHelpers;
import com.searchcode.app.config.Values;
import com.searchcode.app.dto.CodeResult;
import com.searchcode.app.dto.RepositoryChanged;
import com.searchcode.app.dto.SearchResult;
import com.searchcode.app.jobs.repository.IndexFileRepoJob;
import com.searchcode.app.jobs.repository.IndexGitRepoJob;
import com.searchcode.app.jobs.repository.IndexSvnRepoJob;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.IndexService;
import com.searchcode.app.util.Properties;
import junit.framework.TestCase;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class EndToEndITCase extends TestCase{

    public String GITPATH = Properties.getProperties().getProperty(Values.GITBINARYPATH, Values.DEFAULTGITBINARYPATH);

    public void testEndToEndFilePath() throws IOException {
        IndexService indexService = new IndexService();
        File directoryWithFiles = TestHelpers.createDirectoryWithFiles("EndToEndFileTest");
        IndexFileRepoJob indexFileRepoJob = new IndexFileRepoJob();

        // Index created files

        indexFileRepoJob.indexDocsByPath(Paths.get(directoryWithFiles.toString()),
                new RepoResult()
                        .setRowId(0)
                        .setName("ENDTOENDTEST")
                        .setScm("scm")
                        .setUrl("url")
                        .setUsername("username")
                        .setPassword("password")
                        .setSource("source")
                        .setBranch("branch")
                        .setData("{}"),
                "", directoryWithFiles.toString(), false);
        SearchResult searchResult = indexService.search("endtoendtestfile", null, 0, false);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(3);

        CodeResult codeResult1 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile1.php")).findFirst().get();
        CodeResult codeResult2 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile2.py")).findFirst().get();
        CodeResult codeResult3 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile3.java")).findFirst().get();
        assertThat(codeResult1.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile1");
        assertThat(codeResult2.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile2");
        assertThat(codeResult3.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile3");

        // Delete a single file
        String codeId = searchResult.getCodeResultList().get(0).getCodeId();
        indexService.deleteByCodeId(codeId);
        searchResult = indexService.search("endtoendtestfile".toLowerCase(), null, 0, false);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(2);

        codeResult1 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile2.py")).findFirst().get();
        codeResult2 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile3.java")).findFirst().get();
        assertThat(codeResult1.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile2");
        assertThat(codeResult2.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile3");

        // Delete file from disk then index to ensure it is removed from the index
        File toDelete = new File(directoryWithFiles.toString() + "/EndToEndTestFile2.py");
        toDelete.delete();
        indexFileRepoJob.indexDocsByPath(Paths.get(directoryWithFiles.toString()), new RepoResult()
                .setRowId(0)
                .setName("ENDTOENDTEST")
                .setScm("scm")
                .setUrl("url")
                .setUsername("username")
                .setPassword("password")
                .setSource("source")
                .setBranch("branch")
                .setData("{}"), "", directoryWithFiles.toString(), true);
        searchResult = indexService.search("endtoendtestfile", null, 0, false);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(2);

        codeResult1 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile1.php")).findFirst().get();
        codeResult2 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile3.java")).findFirst().get();
        assertThat(codeResult1.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile1");
        assertThat(codeResult2.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile3");

        // Delete everything
        indexService.deleteByRepo(new RepoResult()
                .setRowId(0)
                .setName("ENDTOENDTEST")
                .setScm("scm")
                .setUrl("url")
                .setUsername("username")
                .setPassword("password")
                .setSource("source")
                .setBranch("branch")
                .setData("{}"));
        searchResult = indexService.search("endtoendtestfile".toLowerCase(), null, 0, false);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(0);
    }

    public void testEndToEndGitPath() throws IOException {
        IndexService indexService = new IndexService();
        File directoryWithFiles = TestHelpers.createDirectoryWithFiles("EndToEndGitTest");

        String result = this.runCommand(directoryWithFiles.toString(), this.GITPATH, "init", ".");
        result = this.runCommand(directoryWithFiles.toString(), this.GITPATH, "add", ".");
        result = this.runCommand(directoryWithFiles.toString(), this.GITPATH, "commit", "-m", "\"First commit\"");

        IndexGitRepoJob indexGitRepoJob = new IndexGitRepoJob();
        indexGitRepoJob.indexDocsByPath(Paths.get(directoryWithFiles.toString()), new RepoResult()
                .setRowId(0)
                .setName("ENDTOENDTEST")
                .setScm("scm")
                .setUrl("url")
                .setUsername("username")
                .setPassword("password")
                .setSource("source")
                .setBranch("branch")
                .setData("{}"), "", directoryWithFiles.toString(), false);

        SearchResult searchResult = indexService.search("endtoendtestfile", null, 0, false);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(3);

        CodeResult codeResult1 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile1.php")).findFirst().get();
        CodeResult codeResult2 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile2.py")).findFirst().get();
        CodeResult codeResult3 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile3.java")).findFirst().get();
        assertThat(codeResult1.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile1");
        assertThat(codeResult2.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile2");
        assertThat(codeResult3.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile3");

        // Delete a single file
        String codeId = searchResult.getCodeResultList().get(0).getCodeId();
        indexService.deleteByCodeId(codeId);
        searchResult = indexService.search("endtoendtestfile".toLowerCase(), null, 0, false);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(2);

        // Delete file from disk then index to ensure it is removed from the index
        File toDelete = new File(directoryWithFiles.toString() + "/EndToEndTestFile2.py");
        toDelete.delete();
        indexGitRepoJob.indexDocsByPath(Paths.get(directoryWithFiles.toString()), new RepoResult()
                .setRowId(0)
                .setName("ENDTOENDTEST")
                .setScm("scm")
                .setUrl("url")
                .setUsername("username")
                .setPassword("password")
                .setSource("source")
                .setBranch("branch")
                .setData("{}"), "", directoryWithFiles.toString(), true);
        searchResult = indexService.search("endtoendtestfile", null, 0, false);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(2);

        codeResult1 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile1.php")).findFirst().get();
        codeResult2 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile3.java")).findFirst().get();
        assertThat(codeResult1.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile1");
        assertThat(codeResult2.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile3");

        indexService.deleteByRepo(new RepoResult()
                .setRowId(0)
                .setName("ENDTOENDTEST")
                .setScm("scm")
                .setUrl("url")
                .setUsername("username")
                .setPassword("password")
                .setSource("source")
                .setBranch("branch")
                .setData("{}"));
        searchResult = indexService.search("endtoendtestfile".toLowerCase(), null, 0, false);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(0);
    }

    public void testEndToEndSvnPath() throws IOException {
        IndexService indexService = new IndexService();
        File directoryWithFiles = TestHelpers.createDirectoryWithFiles("EndToEndSvnTest");

        IndexSvnRepoJob indexSvnRepoJob = new IndexSvnRepoJob();
        indexSvnRepoJob.indexDocsByPath(Paths.get(directoryWithFiles.toString()), new RepoResult()
                .setRowId(0)
                .setName("ENDTOENDTEST")
                .setScm("scm")
                .setUrl("url")
                .setUsername("username")
                .setPassword("password")
                .setSource("source")
                .setBranch("branch")
                .setData("{}"), "", directoryWithFiles.toString(), false);

        SearchResult searchResult = indexService.search("endtoendtestfile", null, 0, false);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(3);

        CodeResult codeResult1 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile1.php")).findFirst().get();
        CodeResult codeResult2 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile2.py")).findFirst().get();
        CodeResult codeResult3 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile3.java")).findFirst().get();
        assertThat(codeResult1.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile1");
        assertThat(codeResult2.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile2");
        assertThat(codeResult3.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile3");

        // Delete a single file
        String codeId = searchResult.getCodeResultList().get(0).getCodeId();
        indexService.deleteByCodeId(codeId);
        searchResult = indexService.search("endtoendtestfile".toLowerCase(), null, 0, false);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(2);

        // Delete file from disk then index to ensure it is removed from the index
        File toDelete = new File(directoryWithFiles.toString() + "/EndToEndTestFile2.py");
        toDelete.delete();
        indexSvnRepoJob.indexDocsByPath(Paths.get(directoryWithFiles.toString()), new RepoResult()
                .setRowId(0)
                .setName("ENDTOENDTEST")
                .setScm("scm")
                .setUrl("url")
                .setUsername("username")
                .setPassword("password")
                .setSource("source")
                .setBranch("branch")
                .setData("{}"), "", directoryWithFiles.toString(), true);
        searchResult = indexService.search("endtoendtestfile", null, 0, false);
        assertThat(searchResult.getCodeResultList().size()).isEqualTo(2);

        codeResult1 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile1.php")).findFirst().get();
        codeResult2 = searchResult.getCodeResultList().stream().filter(x -> x.getFileName().equals("EndToEndTestFile3.java")).findFirst().get();
        assertThat(codeResult1.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile1");
        assertThat(codeResult2.getCode().get(0)).isEqualTo("EndToEndTestFile EndToEndTestFile3");

        indexService.deleteByRepo(new RepoResult()
                .setRowId(0)
                .setName("ENDTOENDTEST")
                .setScm("scm")
                .setUrl("url")
                .setUsername("username")
                .setPassword("password")
                .setSource("source")
                .setBranch("branch")
                .setData("{}"));
        searchResult = indexService.search("endtoendtestfile".toLowerCase(), null, 0, false);
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
