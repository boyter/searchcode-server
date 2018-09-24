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
    private CodeIndexDocument codeIndexDocument = new CodeIndexDocument()
            .setRepoLocationRepoNameLocationFilename("repoLocationRepoNameLocationFilename")
            .setRepoName(this.repoName)
            .setFileName("fileName")
            .setFileLocation("fileLocation")
            .setFileLocationFilename("fileLocationFilename")
            .setMd5hash("md5hash")
            .setLanguageName(this.languageName)
            .setCodeLines(100)
            .setLines(99)
            .setContents(this.contents)
            .setRepoRemoteLocation("repoRemoteLocation")
            .setCodeOwner(this.codeOwner)
            .setDisplayLocation("mydisplaylocation")
            .setSource("source");

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

        CodeResult codeResult = this.indexService.getCodeResultByCodeId(this.codeIndexDocument.getHash());
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
        Document indexFields = this.indexService.buildDocument(new CodeIndexDocument()
            .setRepoLocationRepoNameLocationFilename("repoLocationRepoNameLocationFilename")
            .setRepoName("repo Name")
            .setFileName("fileName")
            .setFileLocation("fileLocation")
            .setFileLocationFilename("fileLocationFilename")
            .setMd5hash("md5hash")
            .setLanguageName("language Name")
            .setLines(10)
            .setCodeLines(10)
            .setBlankLines(5)
            .setCommentLines(5)
            .setComplexity(20)
            .setContents("contents")
            .setRepoRemoteLocation("repoRemoteLocation")
            .setCodeOwner("code Owner")
            .setDisplayLocation("displayLocation")
            .setSource("code source"));

        AssertionsForClassTypes.assertThat(indexFields.getFields().size()).isEqualTo(29);

        IndexableField[] fields = indexFields.getFields(Values.REPONAME);
        AssertionsForClassTypes.assertThat(fields[0].stringValue()).isEqualTo("repo_Name");

        fields = indexFields.getFields(Values.LANGUAGENAME);
        AssertionsForClassTypes.assertThat(fields[0].stringValue()).isEqualTo("language_Name");

        fields = indexFields.getFields(Values.CODEOWNER);
        AssertionsForClassTypes.assertThat(fields[0].stringValue()).isEqualTo("code_Owner");

        fields = indexFields.getFields(Values.SOURCE);
        AssertionsForClassTypes.assertThat(fields[0].stringValue()).isEqualTo("code_source");

        // Verifies that we ran through the pipeline
        fields = indexFields.getFields(Values.CONTENTS);
        AssertionsForClassTypes.assertThat(fields[0].stringValue()).isEqualTo(" filename filename filename filename filename filename filename emanelif  file name file filename filelocationfilename filelocation   contents contents contents contents contents contents contents  ");
    }

    public void testSearch() throws IOException {
        this.indexService = new IndexService();

        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(this.codeIndexDocument);
        this.indexService.indexDocument(queue);

        SearchResult contents = this.indexService.search(this.contents, null, 0, false);
        assertThat(contents.getTotalHits()).isNotZero();
        assertThat(contents.getLanguageFacetResults().size()).isNotZero();
        assertThat(contents.getRepoFacetResults().size()).isNotZero();
        assertThat(contents.getOwnerFacetResults().size()).isNotZero();

        assertThat(this.indexService.getIndexedDocumentCount()).isNotZero();

        this.indexService.deleteByCodeId(this.codeId);
    }

    public void testSearchLiteral() throws IOException {
        this.indexService = new IndexService();

        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(this.codeIndexDocument);
        this.indexService.indexDocument(queue);

        SearchResult contents = this.indexService.search(this.contents + " AND rn:" + this.repoName + " AND fn:fileName*", null, 0, true);
        assertThat(contents.getTotalHits()).isNotZero();
        assertThat(contents.getLanguageFacetResults().size()).isNotZero();
        assertThat(contents.getRepoFacetResults().size()).isNotZero();
        assertThat(contents.getOwnerFacetResults().size()).isNotZero();

        assertThat(contents.getCodeResultList().get(0).getDisplayLocation()).isEqualTo("mydisplaylocation");

        assertThat(this.indexService.getIndexedDocumentCount()).isNotZero();

        this.indexService.deleteByCodeId(this.codeId);
    }

    public void testSearchRepo() throws IOException {
        this.indexService = new IndexService();

        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(this.codeIndexDocument);
        this.indexService.indexDocument(queue);

        Helpers helpers = new Helpers();
        SearchResult contents = this.indexService.search("rn:" + helpers.replaceForIndex(this.repoName), null, 0, true);

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
        SearchResult contents = this.indexService.search(this.contents, null, 0, false);
        assertThat(contents.getTotalHits()).isNotZero();
        String read = data.getDataByName(Values.INDEX_READ, Values.INDEX_A);
        String write = data.getDataByName(Values.INDEX_WRITE, Values.INDEX_A);

        // Check on flipped index
        this.indexService.flipIndex();
        assertThat(data.getDataByName(Values.INDEX_READ)).isNotEqualTo(read);
        assertThat(data.getDataByName(Values.INDEX_WRITE)).isNotEqualTo(write);
        queue.add(this.codeIndexDocument);
        this.indexService.indexDocument(queue);
        contents = this.indexService.search(this.contents, null, 0, false);
        assertThat(contents.getTotalHits()).isNotZero();
        this.indexService.deleteByCodeId(this.codeId);
        contents = this.indexService.search(this.contents, null, 0, false);
        assertThat(contents.getTotalHits()).isZero();

        // Flip and check on first index
        this.indexService.flipIndex();
        assertThat(data.getDataByName(Values.INDEX_READ)).isEqualTo(read);
        assertThat(data.getDataByName(Values.INDEX_WRITE)).isEqualTo(write);
        contents = this.indexService.search(this.contents, null, 0, false);
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
        assertThat(projectStats.getTotalCodeLines()).isEqualTo(99);
    }

    public void testGetProjectFileTree() throws IOException {
        this.indexService = new IndexService();
        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(this.codeIndexDocument);
        this.indexService.indexDocument(queue);

        SearchResult projectFileTree = this.indexService.getProjectFileTree(this.repoName);
        assertThat(projectFileTree.getCodeResultList().size()).isEqualTo(1);
    }

    public void testReindexAllSetsIndexingStatus() throws IOException {
        JobService jobServiceMock = mock(JobService.class);
        when(jobServiceMock.forceEnqueueWithCount()).thenReturn(2);

        this.indexService = new IndexService(Singleton.getData(),
                Singleton.getStatsService(),
                Singleton.getSearchCodeLib(),
                Singleton.getLogger(),
                Singleton.getHelpers(),
                Singleton.getCodeIndexQueue(),
                jobServiceMock);

        assertThat(this.indexService.shouldExit(IIndexService.JobType.REPO_PARSER)).isFalse();
        assertThat(this.indexService.getReindexingAll()).isFalse();

        this.indexService.reindexAll();

        assertThat(this.indexService.getRepoAdderPause()).isFalse();
        assertThat(this.indexService.getReindexingAll()).isTrue();
        assertThat(this.indexService.shouldExit(IIndexService.JobType.REPO_PARSER)).isFalse();

        this.indexService.decrementRepoJobsCount();
        assertThat(this.indexService.getReindexingAll()).isTrue();
        this.indexService.decrementRepoJobsCount();
        assertThat(this.indexService.getReindexingAll()).isFalse();
    }

    public void testReindexAllNoReturnSetsIndexingStatus() throws IOException {
        JobService jobServiceMock = mock(JobService.class);
        when(jobServiceMock.forceEnqueueWithCount()).thenReturn(0);

        this.indexService = new IndexService(Singleton.getData(),
                Singleton.getStatsService(),
                Singleton.getSearchCodeLib(),
                Singleton.getLogger(),
                Singleton.getHelpers(),
                Singleton.getCodeIndexQueue(),
                jobServiceMock);

        assertThat(this.indexService.shouldExit(IIndexService.JobType.REPO_PARSER)).isFalse();
        assertThat(this.indexService.getReindexingAll()).isFalse();

        this.indexService.reindexAll();

        assertThat(this.indexService.getRepoAdderPause()).isFalse();
        assertThat(this.indexService.getReindexingAll()).isFalse();
        assertThat(this.indexService.shouldExit(IIndexService.JobType.REPO_PARSER)).isFalse();
    }

    public void testBuildFacets() {
        this.indexService = new IndexService();
        assertThat(this.indexService.buildFacets(null)).isEmpty();
        assertThat(this.indexService.buildFacets(new HashMap<String, String[]>(){{
            put("nomatch", new String[0]);
        }})).isEmpty();
        assertThat(this.indexService.buildFacets(new HashMap<String, String[]>(){{
            put("lan", new String[0]);
        }})).isEmpty();
        assertThat(this.indexService.buildFacets(new HashMap<String, String[]>(){{
            put("repo", new String[0]);
        }})).isEmpty();
        assertThat(this.indexService.buildFacets(new HashMap<String, String[]>(){{
            put("own", new String[0]);
        }})).isEmpty();

        assertThat(this.indexService.buildFacets(new HashMap<String, String[]>(){{
            put("lan", new String[]{"java"});
        }})).isEqualTo(" && (ln:java)");

        assertThat(this.indexService.buildFacets(new HashMap<String, String[]>(){{
            put("lan", new String[]{"java", "python"});
        }})).isEqualTo(" && (ln:java || ln:python)");

        assertThat(this.indexService.buildFacets(new HashMap<String, String[]>(){{
            put("lan", new String[]{"java", "python", "c++"});
        }})).isEqualTo(" && (ln:java || ln:python || ln:c__)");

        assertThat(this.indexService.buildFacets(new HashMap<String, String[]>(){{
            put("repo", new String[]{"java", "python", "c++"});
        }})).isEqualTo(" && (rn:java || rn:python || rn:c__)");

        assertThat(this.indexService.buildFacets(new HashMap<String, String[]>(){{
            put("own", new String[]{"java", "python", "c++"});
        }})).isEqualTo(" && (on:java || on:python || on:c__)");

        assertThat(this.indexService.buildFacets(new HashMap<String, String[]>(){{
            put("fl", new String[]{"temp/something", "another", "thing"});
        }})).isEqualTo(" && (fl:temp_something*)");

        assertThat(this.indexService.buildFacets(new HashMap<String, String[]>(){{
            put("src", new String[]{"something"});
        }})).isEqualTo(" && (source:something)");
    }

    public void testIndexContentPipeline() {
        this.indexService = new IndexService();
        String result = this.indexService.indexContentPipeline(new CodeIndexDocument()
            .setRepoName(this.repoName)
            .setFileName("fileName")
            .setFileLocation("fileLocation")
            .setFileLocationFilename("fileLocationFilename")
            .setMd5hash("md5hash")
            .setLanguageName(this.languageName)
            .setCodeLines(199)
            .setContents("PhysicsServer::get_singleton()->area_set_monitorable(get_rid(), monitorable);")
            .setRepoRemoteLocation("repoRemoteLocation")
            .setCodeOwner(this.codeOwner)
            .setDisplayLocation("mydisplaylocation")
            .setSource("source"));

        assertThat(result).isNotEmpty();
    }

    ///////////////////////////////////////////////////
    // Regression and search odd cases
    ///////////////////////////////////////////////////

    public void testIndexEndToEndDots() throws IOException {
        this.indexService = new IndexService();

        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(new CodeIndexDocument()
                .setRepoLocationRepoNameLocationFilename("something")
                .setRepoName(this.repoName)
                .setFileName("fileName")
                .setFileLocation("fileLocation")
                .setFileLocationFilename("fileLocationFilename")
                .setMd5hash("md5hash")
                .setLanguageName(this.languageName)
                .setCodeLines(199)
                .setContents("actual.contains")
                .setRepoRemoteLocation("repoRemoteLocation")
                .setCodeOwner(this.codeOwner)
                .setDisplayLocation("mydisplaylocation")
                .setSource("source"));
        this.indexService.indexDocument(queue);

        SearchResult search = this.indexService.search("actual.contains", null, 0, false);
        assertThat(search.getTotalHits()).isGreaterThanOrEqualTo(1);
    }

    public void testIndexReversedFilename() throws IOException {
        this.indexService = new IndexService();

        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(new CodeIndexDocument()
                .setRepoLocationRepoNameLocationFilename("something")
                .setRepoName(this.repoName)
                .setFileName("fileName")
                .setFileLocation("fileLocation")
                .setFileLocationFilename("fileLocationFilename")
                .setMd5hash("md5hash")
                .setLanguageName(this.languageName)
                .setCodeLines(199)
                .setContents("PhysicsServer::get_singleton()->area_set_monitorable(get_rid(), monitorable);")
                .setRepoRemoteLocation("repoRemoteLocation")
                .setCodeOwner(this.codeOwner)
                .setDisplayLocation("mydisplaylocation")
                .setSource("source"));
        this.indexService.indexDocument(queue);

        SearchResult search = this.indexService.search("emaN*", null, 0, false);
        assertThat(search.getTotalHits()).isGreaterThanOrEqualTo(1);
    }

    public void testIndexIssue188() throws IOException {
        this.indexService = new IndexService();

        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(new CodeIndexDocument()
                .setRepoLocationRepoNameLocationFilename("something")
                .setRepoName(this.repoName)
                .setFileName("fileName")
                .setFileLocation("fileLocation")
                .setFileLocationFilename("fileLocationFilename")
                .setMd5hash("md5hash")
                .setLanguageName(this.languageName)
                .setCodeLines(199)
                .setContents("PhysicsServer::get_singleton()->area_set_monitorable(get_rid(), monitorable); std::cout << \\\"A fixed-size array:\\\\n\\\"; void RegisterVector(const std::string V_AS,")
                .setRepoRemoteLocation("repoRemoteLocation")
                .setCodeOwner(this.codeOwner)
                .setDisplayLocation("mydisplaylocation")
                .setSource("source"));
        this.indexService.indexDocument(queue);

        SearchResult search = this.indexService.search("PhysicsServer::get_singleton", null, 0, false);
        assertThat(search.getTotalHits()).isGreaterThanOrEqualTo(1);
        search = this.indexService.search("std::cout", null, 0, false);
        assertThat(search.getTotalHits()).isGreaterThanOrEqualTo(1);
        search = this.indexService.search("std::string", null, 0, false);
        assertThat(search.getTotalHits()).isGreaterThanOrEqualTo(1);
    }

    ///////////////////////////////////////////////////
    // Concurrency checks below
    ///////////////////////////////////////////////////

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
        methodList.add(arg -> {
            try {
                this.indexService.deleteByRepo(new RepoResult()
                        .setRowId(0)
                        .setName(RandomStringUtils.randomAscii(rand.nextInt(20) + 1))
                        .setScm("scm")
                        .setUrl("url")
                        .setUsername("username")
                        .setPassword("password")
                        .setSource("source")
                        .setBranch("branch")
                        .setData("{}"));
            } catch (IOException ex) {
                assertThat(true).isFalse();
            }
        });
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
        methodList.add(arg -> this.indexService.search(RandomStringUtils.randomAscii(rand.nextInt(20) + 1), null, rand.nextInt(40), false));
        methodList.add(arg -> this.indexService.shouldPause(IIndexService.JobType.REPO_ADDER));
        methodList.add(arg -> this.indexService.shouldPause(IIndexService.JobType.REPO_PARSER));
        methodList.add(arg -> this.indexService.shouldExit(IIndexService.JobType.REPO_ADDER));
        methodList.add(arg -> this.indexService.shouldExit(IIndexService.JobType.REPO_PARSER));
        methodList.add(arg -> this.indexService.setRepoAdderPause(false));
        methodList.add(arg -> this.indexService.setRepoAdderPause(true));
        methodList.add(arg -> this.indexService.getRepoAdderPause());
        methodList.add(arg -> this.indexService.decrementRepoJobsCount());
        methodList.add(arg -> this.indexService.getProjectFileTree(RandomStringUtils.randomAscii(rand.nextInt(20) + 1)));

        List<Thread> threadList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
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
