package com.searchcode.app.service;

import com.searchcode.app.config.SQLiteMemoryDatabaseConfig;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.dto.CodeResult;
import com.searchcode.app.dto.ProjectStats;
import com.searchcode.app.dto.SearchResult;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.util.Helpers;
import junit.framework.TestCase;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.assertj.core.api.AssertionsForClassTypes;
import org.eclipse.jetty.util.ConcurrentArrayQueue;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IndexServiceTest extends TestCase {

    private IndexService indexService = null;
    private String codeId = "b9cc3f33794cad323047b4e982e8b3849b7422a8";
    private String contents = "06e3e59f51894adea03c343910c26282";
    private String repoName = "b89bb20026ff426dae30ab92e1e59b19";
    private String languageName = "languageName";
    private String codeOwner = "codeOwner";
    private CodeIndexDocument codeIndexDocument = new CodeIndexDocument("repoLocationRepoNameLocationFilename",
            this.repoName,
            "fileName",
            "fileLocation",
            "fileLocationFilename",
            "md5hash",
            this.languageName,
            100,
            this.contents,
            "repoRemoteLocation",
            this.codeOwner);

    public void testIndexDocumentEndToEnd() throws IOException {
        this.indexService = new IndexService();

        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(this.codeIndexDocument);
        this.indexService.indexDocument(queue);

        CodeResult codeResult = this.indexService.getCodeResultByCodeId(this.codeId);
        assertThat(codeResult.getCodeId()).isEqualTo(this.codeId);

        this.indexService.deleteByCodeId(this.codeId);
        codeResult = this.indexService.getCodeResultByCodeId(this.codeId);
        assertThat(codeResult).isNull();
    }

    public void testIndexDocumentNoQueueEndToEnd() throws IOException {
        this.indexService = new IndexService();
        this.indexService.indexDocument(this.codeIndexDocument);

        CodeResult codeResult = this.indexService.getCodeResultByCodeId(this.codeId);
        assertThat(codeResult.getCodeId()).isEqualTo(this.codeId);

        this.indexService.deleteByCodeId(this.codeId);
        codeResult = this.indexService.getCodeResultByCodeId(this.codeId);
        assertThat(codeResult).isNull();
    }


    public void testDeleteByCodeId() throws IOException {
        this.indexService = new IndexService();
        this.indexService.deleteByCodeId("this should not do anything but not blow up either");
    }

    public void testDeleteByRepoName() throws IOException {
        this.indexService = new IndexService();

        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(this.codeIndexDocument);
        this.indexService.indexDocument(queue);

        CodeResult codeResult = this.indexService.getCodeResultByCodeId(this.codeId);
        assertThat(codeResult.getCodeId()).isEqualTo(this.codeId);

        RepoResult repoResult = new RepoResult();
        repoResult.setName(this.repoName);

        this.indexService.deleteByRepo(repoResult);

        codeResult = this.indexService.getCodeResultByCodeId(this.codeId);
        assertThat(codeResult).isNull();
    }

    public void testDeleteByRepoNameNull() throws IOException {
        this.indexService = new IndexService();
        this.indexService.deleteByRepo(null);
    }

    public void testDeleteAll() throws IOException {
        this.indexService = new IndexService();

        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(this.codeIndexDocument);
        this.indexService.indexDocument(queue);

        CodeResult codeResult = this.indexService.getCodeResultByCodeId(this.codeId);
        assertThat(codeResult.getCodeId()).isEqualTo(this.codeId);

        this.indexService.deleteAll();

        codeResult = this.indexService.getCodeResultByCodeId(this.codeId);
        assertThat(codeResult).isNull();
    }


    public void testBuildDocument() {
        this.indexService = new IndexService();
        Document indexFields = this.indexService.buildDocument(new CodeIndexDocument(
                "repoLocationRepoNameLocationFilename",
                "repo Name",
                "fileName",
                "fileLocation",
                "fileLocationFilename",
                "md5hash",
                "language Name",
                10,
                "contents",
                "repoRemoteLocation",
                "code Owner"
        ));

        AssertionsForClassTypes.assertThat(indexFields.getFields().size()).isEqualTo(17);

        IndexableField[] fields = indexFields.getFields(Values.REPONAME);
        AssertionsForClassTypes.assertThat(fields[0].stringValue()).isEqualTo("repo_Name");

        fields = indexFields.getFields(Values.LANGUAGENAME);
        AssertionsForClassTypes.assertThat(fields[0].stringValue()).isEqualTo("language_Name");

        fields = indexFields.getFields(Values.CODEOWNER);
        AssertionsForClassTypes.assertThat(fields[0].stringValue()).isEqualTo("code_Owner");

        // Verifies that we ran through the pipeline
        fields = indexFields.getFields(Values.CONTENTS);
        AssertionsForClassTypes.assertThat(fields[0].stringValue()).isEqualTo(" filename filename filename filename filename filename  file name filelocationfilename filelocation contents contents contents contents contents contents");
    }

    public void testSearch() throws IOException {
        this.indexService = new IndexService();

        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(this.codeIndexDocument);
        this.indexService.indexDocument(queue);

        SearchResult contents = this.indexService.search(this.contents, 0);
        assertThat(contents.getTotalHits()).isNotZero();
        assertThat(contents.getLanguageFacetResults().size()).isNotZero();
        assertThat(contents.getRepoFacetResults().size()).isNotZero();
        assertThat(contents.getOwnerFacetResults().size()).isNotZero();

        assertThat(this.indexService.getIndexedDocumentCount()).isNotZero();

        this.indexService.deleteByCodeId(this.codeId);
    }

    public void testSearchRepo() throws IOException {
        this.indexService = new IndexService();

        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(this.codeIndexDocument);
        this.indexService.indexDocument(queue);

        SearchResult contents = this.indexService.search("reponame:" + this.repoName, 0);

        assertThat(contents.getTotalHits()).isNotZero();
        assertThat(contents.getLanguageFacetResults().size()).isNotZero();
        assertThat(contents.getRepoFacetResults().size()).isNotZero();
        assertThat(contents.getOwnerFacetResults().size()).isNotZero();

        assertThat(contents.getCodeResultList().get(0).codeId).isEqualTo(this.codeId);

        assertThat(contents.getLanguageFacetResults().get(0).getLanguageName()).isEqualTo(this.languageName);
        assertThat(contents.getLanguageFacetResults().get(0).getCount()).isEqualTo(1);
        assertThat(contents.getRepoFacetResults().get(0).getRepoName()).isEqualTo(this.repoName);
        assertThat(contents.getRepoFacetResults().get(0).getCount()).isEqualTo(1);
        assertThat(contents.getOwnerFacetResults().get(0).getOwner()).isEqualTo(this.codeOwner);
        assertThat(contents.getOwnerFacetResults().get(0).getCount()).isEqualTo(1);

        this.indexService.deleteByCodeId(this.codeId);
    }

    public void testSearchWithFlip() throws IOException {
        Data data = new Data(new SQLiteMemoryDatabaseConfig(), new Helpers());

        this.indexService = new IndexService(data,
                Singleton.getStatsService(),
                Singleton.getSearchCodeLib(),
                Singleton.getLogger(),
                Singleton.getHelpers(),
                Singleton.getCodeIndexQueue(),
                Singleton.getJobService());

        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(this.codeIndexDocument);
        this.indexService.indexDocument(queue);

        // Check on first index
        SearchResult contents = this.indexService.search(this.contents, 0);
        assertThat(contents.getTotalHits()).isNotZero();
        String read = data.getDataByName(Values.INDEX_READ, Values.INDEX_A);
        String write = data.getDataByName(Values.INDEX_WRITE, Values.INDEX_A);

        // Check on flipped index
        this.indexService.flipIndex();
        assertThat(data.getDataByName(Values.INDEX_READ)).isNotEqualTo(read);
        assertThat(data.getDataByName(Values.INDEX_WRITE)).isNotEqualTo(write);
        queue.add(this.codeIndexDocument);
        this.indexService.indexDocument(queue);
        contents = this.indexService.search(this.contents, 0);
        assertThat(contents.getTotalHits()).isNotZero();
        this.indexService.deleteByCodeId(this.codeId);
        contents = this.indexService.search(this.contents, 0);
        assertThat(contents.getTotalHits()).isZero();

        // Flip and check on first index
        this.indexService.flipIndex();
        assertThat(data.getDataByName(Values.INDEX_READ)).isEqualTo(read);
        assertThat(data.getDataByName(Values.INDEX_WRITE)).isEqualTo(write);
        contents = this.indexService.search(this.contents, 0);
        assertThat(contents.getTotalHits()).isNotZero();

        this.indexService.deleteByCodeId(this.codeId);
    }

    public void testChangeCodeIndexLinesCount() {
        this.indexService = new IndexService();

        this.indexService.setCodeIndexLinesCount(0);
        assertThat(this.indexService.getCodeIndexLinesCount()).isZero();
        this.indexService.incrementCodeIndexLinesCount(1000);
        assertThat(this.indexService.getCodeIndexLinesCount()).isEqualTo(1000);
        this.indexService.decrementCodeIndexLinesCount(999);
        assertThat(this.indexService.getCodeIndexLinesCount()).isEqualTo(1);
        this.indexService.decrementCodeIndexLinesCount(1000);
        assertThat(this.indexService.getCodeIndexLinesCount()).isEqualTo(0);
    }

    public void testShouldRepoParserJobPause() {
        this.indexService = new IndexService();
        boolean shouldPause = this.indexService.shouldPause(IIndexService.JobType.REPO_PARSER);
        assertThat(shouldPause).isFalse();
    }

    public void testShouldRepoAdderJobPause() {
        this.indexService = new IndexService();
        boolean shouldPause = this.indexService.shouldPause(IIndexService.JobType.REPO_ADDER);
        assertThat(shouldPause).isFalse();
    }

    public void testShouldRepoAdderJobPauseWhenSet() {
        this.indexService = new IndexService();
        this.indexService.setRepoAdderPause(true);
        boolean shouldPause = this.indexService.shouldPause(IIndexService.JobType.REPO_ADDER);
        assertThat(shouldPause).isTrue();
        assertThat(this.indexService.getRepoAdderPause()).isTrue();

        this.indexService.setRepoAdderPause(false);
        shouldPause = this.indexService.shouldPause(IIndexService.JobType.REPO_ADDER);
        assertThat(shouldPause).isFalse();
        assertThat(this.indexService.getRepoAdderPause()).isFalse();
    }

    public void testShouldRepoParserJobPauseWhenIndexLinesSizeLarge() {
        this.indexService = new IndexService();
        this.indexService.setCodeIndexLinesCount(1000000000);
        boolean shouldPause = this.indexService.shouldPause(IIndexService.JobType.REPO_PARSER);
        assertThat(shouldPause).isTrue();
    }

    public void testShouldRepoParserJobPauseWhenIndexQueueSizeLarge() {
        Queue<CodeIndexDocument> queue = new ConcurrentArrayQueue<>();

        for (int i=0; i<10000; i++) {
            queue.add(new CodeIndexDocument());
        }

        this.indexService = new IndexService(Singleton.getData(),
                Singleton.getStatsService(),
                Singleton.getSearchCodeLib(),
                Singleton.getLogger(),
                Singleton.getHelpers(),
                queue,
                Singleton.getJobService());

        boolean shouldPause = this.indexService.shouldPause(IIndexService.JobType.REPO_PARSER);
        assertThat(shouldPause).isTrue();
    }

    public void testShouldBackOffWhenLoadVeryHigh() {
        Data dataMock = mock(Data.class);
        StatsService statsServiceMock = mock(StatsService.class);

        when(statsServiceMock.getLoadAverage()).thenReturn("10000000.0");
        when(dataMock.getDataByName(Values.BACKOFFVALUE, Values.DEFAULTBACKOFFVALUE)).thenReturn("1");

        this.indexService = new IndexService(dataMock, statsServiceMock, null, Singleton.getLogger(), Singleton.getHelpers(), null, null);

        assertThat(this.indexService.shouldPause(IIndexService.JobType.REPO_PARSER)).isTrue();
    }

    public void testShouldBackOffWhenLoadHigherThanValue() {
        Data dataMock = mock(Data.class);
        StatsService statsServiceMock = mock(StatsService.class);

        when(statsServiceMock.getLoadAverage()).thenReturn("0.21");
        when(dataMock.getDataByName(Values.BACKOFFVALUE, Values.DEFAULTBACKOFFVALUE)).thenReturn("0.2");

        this.indexService = new IndexService(dataMock, statsServiceMock, null, Singleton.getLogger(), Singleton.getHelpers(), null, null);

        assertThat(this.indexService.shouldPause(IIndexService.JobType.REPO_PARSER)).isTrue();
    }

    public void testShouldNotBackOffWhenLoadZero() {
        Data dataMock = mock(Data.class);
        StatsService statsServiceMock = mock(StatsService.class);

        when(statsServiceMock.getLoadAverage()).thenReturn("0.0");
        when(dataMock.getDataByName(Values.BACKOFFVALUE, Values.DEFAULTBACKOFFVALUE)).thenReturn("1");

        this.indexService = new IndexService(dataMock, statsServiceMock, null, Singleton.getLogger(), Singleton.getHelpers(), new ConcurrentArrayQueue<>(), null);

        assertThat(this.indexService.shouldPause(IIndexService.JobType.REPO_PARSER)).isFalse();
    }

    public void testGetRepoDocuments() throws IOException {
        this.indexService = new IndexService();
        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(this.codeIndexDocument);
        this.indexService.indexDocument(queue);

        List<String> test = this.indexService.getRepoDocuments(this.repoName, 0);
        assertThat(test.size()).isEqualTo(1);
        assertThat(test.get(0)).isEqualTo("repoLocationRepoNameLocationFilename");
    }

    public void testGetProjectStats() throws IOException {
        this.indexService = new IndexService();
        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(this.codeIndexDocument);
        this.indexService.indexDocument(queue);

        ProjectStats projectStats = this.indexService.getProjectStats(this.repoName);
        assertThat(projectStats.getTotalFiles()).isEqualTo(1);
        assertThat(projectStats.getTotalCodeLines()).isEqualTo(100);
    }

    public void testReindexAllSetsIndexingStatus() throws IOException {
        this.indexService = new IndexService();

        assertThat(this.indexService.getReindexingAll()).isFalse();
        this.indexService.reindexAll();
        assertThat(this.indexService.getReindexingAll()).isTrue();
    }

    public void testIndexerLock() throws InterruptedException {
        // You can only prove the presence of concurrent bugs, not their absence.
        // Although that's true of any code. Anyway let's see if we can identify any...
        this.indexService = new IndexService();

        List<MethodRunner> methodList = new ArrayList<>();
        methodList.add(arg -> this.indexService.setCodeIndexLinesCount(100));
        methodList.add(arg -> this.indexService.incrementCodeIndexLinesCount(1));
        methodList.add(arg -> this.indexService.decrementCodeIndexLinesCount(1));
        methodList.add(arg -> this.indexService.getCodeIndexLinesCount());

        List<Thread> threadList = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            Thread thread = new Thread(() -> {
                Collections.shuffle(methodList);
                for (MethodRunner runner: methodList) {
                    runner.run(new Object());
                }
            });
            thread.start();
            threadList.add(thread);
        }

        for (Thread thread: threadList) {
            thread.join();
        }
        int codeIndexLinesCount = this.indexService.getCodeIndexLinesCount();
        assertThat(codeIndexLinesCount).isBetween(98, 102);
    }

    public void testIndexerWithThreads() throws InterruptedException {
        // You can only prove the presence of concurrent bugs, not their absence.
        // Although that's true of any code. Anyway let's see if we can identify any...
        Random rand = new Random();
        this.indexService = new IndexService();

        List<MethodRunner> methodList = new ArrayList<>();
        methodList.add(arg -> { try { this.indexService.deleteByCodeId(RandomStringUtils.randomAscii(rand.nextInt(20) + 1)); } catch (IOException e) { assertThat(true).isFalse(); }});
        methodList.add(arg -> this.indexService.setCodeIndexLinesCount(rand.nextInt(2000)));
        methodList.add(arg -> this.indexService.incrementCodeIndexLinesCount(rand.nextInt(2000)));
        methodList.add(arg -> this.indexService.decrementCodeIndexLinesCount(rand.nextInt(2000)));
        methodList.add(arg -> this.indexService.getCodeIndexLinesCount());
        methodList.add(arg -> { try { this.indexService.deleteByRepo(new RepoResult(0, RandomStringUtils.randomAscii(rand.nextInt(20) + 1), "", "", "", "", "", "", "{}")); } catch (IOException e) { assertThat(true).isFalse(); }});
        methodList.add(arg -> { try { this.indexService.deleteAll(); } catch (IOException e) { assertThat(true).isFalse(); }});
        methodList.add(arg -> this.indexService.flipIndex());
        methodList.add(arg -> this.indexService.getCodeResultByCodeId(RandomStringUtils.randomAscii(rand.nextInt(20) + 1)));
        methodList.add(arg -> this.indexService.getIndexedDocumentCount());
        methodList.add(arg -> this.indexService.getProjectStats(RandomStringUtils.randomAscii(rand.nextInt(20) + 1)));
        methodList.add(arg -> { try {
            Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
            for (int j = 0; j < rand.nextInt(100) + 1; j++) {
                queue.add(this.codeIndexDocument);
            }
            this.indexService.indexDocument(queue);
        } catch (IOException e) { assertThat(true).isFalse(); }});
        methodList.add(arg -> { try { this.indexService.indexDocument(this.codeIndexDocument); } catch (IOException e) { assertThat(true).isFalse(); }});
        methodList.add(arg -> this.indexService.reindexAll());
        methodList.add(arg -> this.indexService.reindexByRepo(new RepoResult()));
        methodList.add(arg -> this.indexService.search(RandomStringUtils.randomAscii(rand.nextInt(20) + 1), rand.nextInt(40)));
        methodList.add(arg -> this.indexService.shouldPause(IIndexService.JobType.REPO_ADDER));
        methodList.add(arg -> this.indexService.shouldPause(IIndexService.JobType.REPO_PARSER));
        methodList.add(arg -> this.indexService.shouldExit(IIndexService.JobType.REPO_ADDER));
        methodList.add(arg -> this.indexService.shouldExit(IIndexService.JobType.REPO_PARSER));
        methodList.add(arg -> this.indexService.setRepoAdderPause(false));
        methodList.add(arg -> this.indexService.setRepoAdderPause(true));
        methodList.add(arg -> this.indexService.getRepoAdderPause());
        methodList.add(arg -> this.indexService.decrementRepoJobsCount());

        List<Thread> threadList = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            Thread thread = new Thread(() -> {
                Collections.shuffle(methodList);
                for (MethodRunner runner: methodList) {
                    runner.run(new Object());
                }
            });
            thread.start();
           threadList.add(thread);
        }

        for (Thread thread: threadList) {
            thread.join();
        }
    }

    public interface MethodRunner {
        void run(Object arg);
    }
}
