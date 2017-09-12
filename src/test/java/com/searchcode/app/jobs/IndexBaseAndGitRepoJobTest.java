package com.searchcode.app.jobs;

import com.searchcode.app.TestHelpers;
import com.searchcode.app.jobs.repository.IndexGitRepoJob;
import com.searchcode.app.service.IndexService;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.service.StatsService;
import com.searchcode.app.util.Timer;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

public class IndexBaseAndGitRepoJobTest extends TestCase {
    public void testGetBlameFilePath() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
        String actual = gitRepoJob.getBlameFilePath("./repo/something/test");
        assertEquals("repo/something/test", actual);
    }

    // No such thing as a windows path in the index, so should return empty
    public void testGetBlameFilePathWindows() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
        String actual = gitRepoJob.getBlameFilePath("\\repo\\something\\test");
        assertEquals("", actual);
    }

    public void testIndexSucess() throws IOException {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();

        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        File tempDir = new File(baseDir, "testIndexSucess");

        if (tempDir.exists()) {
            FileUtils.deleteDirectory(tempDir);
        }
        tempDir.mkdir();
        String tempDirString = tempDir.toString();

        assertFalse(gitRepoJob.checkIndexSucess(tempDirString));
        gitRepoJob.createIndexSuccess(tempDirString);
        assertTrue(gitRepoJob.checkIndexSucess(tempDirString));
        gitRepoJob.deleteIndexSuccess(tempDirString);
        assertFalse(gitRepoJob.checkIndexSucess(tempDirString));
    }

    public void testCloneSucess() throws IOException {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();

        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        File tempDir = new File(baseDir, "testIndexSucess");

        if (tempDir.exists()) {
            FileUtils.deleteDirectory(tempDir);
        }
        tempDir.mkdir();
        String tempDirString = tempDir.toString();


        assertFalse(gitRepoJob.checkCloneUpdateSucess(tempDirString));
        gitRepoJob.createCloneUpdateSuccess(tempDirString);
        assertTrue(gitRepoJob.checkCloneUpdateSucess(tempDirString));
        gitRepoJob.deleteCloneUpdateSuccess(tempDirString);
        assertFalse(gitRepoJob.checkCloneUpdateSucess(tempDirString));
    }

    public void testDeleteNoFile() throws IOException {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();

        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        File tempDir = new File(baseDir, "testIndexSucess");

        if (tempDir.exists()) {
            FileUtils.deleteDirectory(tempDir);
        }
        tempDir.mkdir();
        String tempDirString = tempDir.toString();

        for(int i = 0; i < 100; i++) {
            gitRepoJob.deleteIndexSuccess(tempDirString);
            gitRepoJob.deleteCloneUpdateSuccess(tempDirString);
        }
    }

//    TODO This broke with the change over to the new index format should look into resolving or removing
//    public void testShouldJobTerminate() {
//        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
//        StatsService statsServiceMock = mock(StatsService.class);
//
//        when(statsServiceMock.getLoadAverage()).thenReturn("0.0");
//        Singleton.setStatsService(statsServiceMock);
//
//        assertThat(gitRepoJob.shouldJobPauseOrTerminate()).isFalse();
//
//        Singleton.getIndexService().setRepoAdderPause(true);
//        assertThat(gitRepoJob.shouldJobPauseOrTerminate()).isTrue();
//
//        Singleton.getIndexService().setRepoAdderPause(false);
//        assertThat(gitRepoJob.shouldJobPauseOrTerminate()).isFalse();
//
//        Singleton.getIndexService().setRepoAdderPause(true);
//        assertThat(gitRepoJob.shouldJobPauseOrTerminate()).isTrue();
//
//    }

    public void testGetFileMd5() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
        gitRepoJob.getFileMd5("filedoesnotexist");
    }

    public void testDetermineBinary() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
        gitRepoJob.LOGINDEXED = true;
        List<String[]> reportList = new ArrayList<>();

        boolean result = gitRepoJob.determineBinary("", "", new ArrayList<>(), reportList);

        assertThat(result).isTrue();
        assertThat(reportList.size()).isEqualTo(1);
    }

    // TODO actually do something with this information
    public void testSomething() {
        String[] split = "myrepo/path/to/myfile.txt".split("/");
        String temp = String.join("/", Arrays.asList(split).subList(1, split.length));
        assertEquals("path/to/myfile.txt", temp);
    }

    public void testGetRelativeToProjectPath() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
        String relativeToProjectPath = gitRepoJob.getRelativeToProjectPath("/Users/boyter/test5", "/Users/boyter/test5/u/something/sources/small/c3p0.csv");
        assertThat(relativeToProjectPath).isEqualTo("/u/something/sources/small/c3p0.csv");

        relativeToProjectPath = gitRepoJob.getRelativeToProjectPath("/Users/boyter/test5/", "/Users/boyter/test5/u/something/sources/small/c3p0.csv");
        assertThat(relativeToProjectPath).isEqualTo("/u/something/sources/small/c3p0.csv");

        relativeToProjectPath = gitRepoJob.getRelativeToProjectPath("./repo/test", "./repo/test/chinese.php");
        assertThat(relativeToProjectPath).isEqualTo("/chinese.php");

        relativeToProjectPath = gitRepoJob.getRelativeToProjectPath("./repo/test/", "./repo/test/chinese.php");
        assertThat(relativeToProjectPath).isEqualTo("/chinese.php");

        relativeToProjectPath = gitRepoJob.getRelativeToProjectPath("./repo/test", "./repo//test/chinese.php");
        assertThat(relativeToProjectPath).isEqualTo("/chinese.php");
    }

    public void testGetFileLocationFilename() {
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob();
        String fileLocationFilename = gitRepoJob.getFileLocationFilename(".git/filename", "./repo/");
        assertThat(fileLocationFilename).isEqualTo(".git/filename");

        fileLocationFilename = gitRepoJob.getFileLocationFilename("./repo/.git/filename", "./repo/");
        assertThat(fileLocationFilename).isEqualTo(".git/filename");
    }

    public void testMissingPathFilesNoLocations() {
        IndexService indexServiceMock = mock(IndexService.class);
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob(indexServiceMock);

        when(indexServiceMock.getRepoDocuments("testRepoName", 0)).thenReturn(new ArrayList<>());
        gitRepoJob.cleanMissingPathFiles("testRepoName", new HashMap<>());
        verify(indexServiceMock, times(1)).getRepoDocuments("testRepoName", 0);
    }

    public void testMissingPathFilesShouldPage() {
        IndexService indexServiceMock = mock(IndexService.class);
        IndexGitRepoJob gitRepoJob = new IndexGitRepoJob(indexServiceMock);

        List<String> repoReturn = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            repoReturn.add("string"+i);
        }

        when(indexServiceMock.getRepoDocuments("testRepoName", 0)).thenReturn(repoReturn);
        when(indexServiceMock.getRepoDocuments("testRepoName", 1)).thenReturn(repoReturn);
        when(indexServiceMock.getRepoDocuments("testRepoName", 2)).thenReturn(new ArrayList<>());

        gitRepoJob.cleanMissingPathFiles("testRepoName", new HashMap<>());

        verify(indexServiceMock, times(1)).getRepoDocuments("testRepoName", 0);
        verify(indexServiceMock, times(1)).getRepoDocuments("testRepoName", 1);
        verify(indexServiceMock, times(1)).getRepoDocuments("testRepoName", 2);
    }

    public void testCheckCloneSuccessEmptyReturnsFalse() {
        IndexGitRepoJob indexGitRepoJob = new IndexGitRepoJob();
        boolean actual = indexGitRepoJob.checkCloneSuccess("", "");
        assertThat(actual).isFalse();
    }

    public void testCheckCloneSuccessEmptyReturnsTrue() throws IOException {
        File location = TestHelpers.clearAndCreateTempPath("testCheckCloneSuccessEmptyReturnsTrue");
        File projectLocation = TestHelpers.createDirectory(location, "myawesomeproject");
        TestHelpers.createFile(projectLocation, "myfile.java", "some file content");

        IndexGitRepoJob indexGitRepoJob = new IndexGitRepoJob();
        boolean actual = indexGitRepoJob.checkCloneSuccess("myawesomeproject", location.getAbsolutePath());
        assertThat(actual).isTrue();

        File toCheck = new File(projectLocation.getAbsolutePath());
        assertThat(toCheck.exists()).isFalse();
    }

    // TODO implement this to speed up the Owner portion
//    public void testTesty() throws IOException, GitAPIException {
//        //public List<CodeOwner> getBlameInfo(int codeLinesSize, String repoName, String repoLocations, String fileName) {
//
//        // The / part is required due to centos bug for version 1.1.1
//        // This appears to be correct
//        String repoLoc = "/Users/boyter/Documents/Projects/searchcode-server/.git";
////
//        Timer timer = Singleton.getNewTimer();
//        Repository localRepository = new FileRepository(new File(repoLoc));
//        System.out.println("" + timer.toc());
////
////        Git git = new Git(localRepository);
////        Iterable<RevCommit> logs = git.log().call();
////
////        for(RevCommit rev: logs) {
////            String message = rev.getFullMessage();
////            String author = rev.getAuthorIdent().getName();
////            System.out.println(author);
////        }
//
//
//        timer.tic();
//        Git git = new Git(localRepository);
//        System.out.println("" + timer.toc());
//
//        LogCommand logCommand = git.log()
//                .add(git.getRepository().resolve(Constants.HEAD))
//                .addPath("README.md");
//
//        timer.tic();
//        for (RevCommit revCommit: logCommand.call()) {
//            System.out.println(revCommit.getAuthorIdent().getName());
//            break;
//        }
//        System.out.println("" + timer.toc());
//
//
//    }
}
