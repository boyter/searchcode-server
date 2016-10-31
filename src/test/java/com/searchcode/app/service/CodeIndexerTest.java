package com.searchcode.app.service;

import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dto.CodeIndexDocument;
import junit.framework.TestCase;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.jetty.util.ConcurrentArrayQueue;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Queue;
import java.util.Random;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

public class CodeIndexerTest extends TestCase {
    public CodeIndexDocument codeIndexDocument = new CodeIndexDocument("repoLocationRepoNameLocationFilename", "repoName", "fileName", "fileLocation", "fileLocationFilename", "md5hash", "languageName", 100, "contents", "repoRemoteLocation", "codeOwner");

    public void testIndexDocument() throws IOException {
        CodeIndexer.indexDocument(codeIndexDocument);
    }

    public void testShouldPauseAddingExpectTrue() {
        Singleton.setPauseBackgroundJobs(true);
        assertThat(CodeIndexer.shouldPauseAdding()).isTrue();
    }

    public void testShouldPauseAddingExpectFalse() {
        Singleton.setPauseBackgroundJobs(false);
        assertThat(CodeIndexer.shouldPauseAdding()).isFalse();
    }

    // TODO actually assert something in here
    public void testIndexDocuments() throws IOException {
        Queue<CodeIndexDocument> queue = new ConcurrentArrayQueue<>();
        queue.add(codeIndexDocument);
        CodeIndexer.indexDocuments(queue);
    }

    // TODO actually assert something in here
    public void testDeleteByRepoName() throws IOException {
        CodeIndexer.deleteByReponame("repoName");
    }

    // TODO actually assert something in here
    public void testDeleteByFileLocationFilename() throws IOException {
        CodeIndexer.deleteByFileLocationFilename("fileLocationFilename");
    }

    // TODO fix the assert rather then programming by exception
    public void testIndexDocumentsEmptyIssue() {
        try {
            CodeIndexDocument cid = new CodeIndexDocument("repoLocationRepoNameLocationFilename", "", "fileName", "fileLocation", "fileLocationFilename", "md5hash", "languageName", 0, null, "repoRemoteLocation", "codeOwner");

            Queue queue = new ConcurrentArrayQueue<CodeIndexDocument>();
            queue.add(cid);

            CodeIndexer.indexDocuments(queue);
        }
        catch(Exception ex) {
            assertTrue(false);
        }
    }

    public void testShouldBackOffWhenLoadVeryHigh() {
        Data dataMock = Mockito.mock(Data.class);
        StatsService statsServiceMock = Mockito.mock(StatsService.class);

        when(statsServiceMock.getLoadAverage()).thenReturn("10000000.0");
        when(dataMock.getDataByName(Values.BACKOFFVALUE, Values.DEFAULTBACKOFFVALUE)).thenReturn("1");
        Singleton.setStatsService(statsServiceMock);
        Singleton.setData(dataMock);

        assertThat(CodeIndexer.shouldBackOff()).isTrue();
    }

    public void testShouldBackOffWhenLoadZero() {
        StatsService statsServiceMock = Mockito.mock(StatsService.class);
        when(statsServiceMock.getLoadAverage()).thenReturn("0.0");
        Singleton.setStatsService(statsServiceMock);

        assertThat(CodeIndexer.shouldBackOff()).isFalse();
    }

    // TODO expand on these tests
//    public void testIndexTimeDocuments() {
//        try {
//            Random rand = new Random();
//
//            String contents = "this is some code that should be found";
//
//            for (int j=0; j < 1000; j++) {
//                contents += " " + RandomStringUtils.randomAlphabetic(rand.nextInt(10) + 1);
//            }
//
//            CodeIndexDocument cid = new CodeIndexDocument("repoLocationRepoNameLocationFilename", "", "fileName", "fileLocation", "fileLocationFilename", "md5hash", "languageName", 0, contents, "repoRemoteLocation", "codeOwner");
//            cid.setRevision("99a5a271063def87b2473be79ce6f840d42d1f95");
//            cid.setYearMonthDay("20160101");
//
//            Queue queue = new ConcurrentArrayQueue<CodeIndexDocument>();
//            queue.add(cid);
//
//            CodeIndexer.indexTimeDocuments(queue);
//        }
//        catch(Exception ex) {
//            assertTrue(false);
//        }
//    }
}
