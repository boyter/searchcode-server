/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.service;


import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dto.*;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.util.*;
import com.searchcode.app.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.facet.*;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.jetty.util.ArrayQueue;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Service to deal with any tasks that involve talking to the index
 * specifically
 */
public class IndexService implements IIndexService {


    private final StatsService statsService;
    private final Data data;
    private final SearchCodeLib searchcodeLib;
    private final LoggerWrapper logger;
    private final Helpers helpers;
    private final JobService jobService;

    private final int MAX_INDEX_SIZE;
    private final int MAX_LINES_INDEX_SIZE;

    private final Path INDEX_A_LOCATION;
    private final Path INDEX_B_LOCATION;
    private final Path FACET_A_LOCATION;
    private final Path FACET_B_LOCATION;

    private Path INDEX_READ_LOCATION;
    private Path INDEX_WRITE_LOCATION;
    private Path FACET_WRITE_LOCATION;

    private int PAGE_LIMIT;
    private int NO_PAGES_LIMIT;
    private int CHILD_FACET_LIMIT;

    private final Queue<CodeIndexDocument> codeIndexDocumentQueue;
    private final UniqueRepoQueue uniqueGitRepoQueue;
    private final UniqueRepoQueue uniqueFileRepoQueue;
    private final UniqueRepoQueue uniqueSvnRepoQueue;

    private boolean repoAdderPause = false;     // Controls if repo add job should pause, controlled through the UI
    private boolean repoJobExit = false;        // Controls if repo indexing jobs should exit instantly
    private int codeIndexLinesCount = 0;

    private List<String> indexAllFields; // Contains the fields that should be added to the all portion of the index

    ///////////////////////////////////////////////////////////////////////
    // The below store state for when the reindexing + flip should occur
    //////////////////////////////////////////////////////////////////////
    private boolean reindexingAll = false;      // Are we in reindexing state waiting to flip over
    private int repoJobsCount = 0;              // If we are reindexing then how many jobs need to finish

    private ReentrantLock codeIndexLinesCountLock = new ReentrantLock();

    public IndexService() {
        this(Singleton.getData(),
                Singleton.getStatsService(),
                Singleton.getSearchCodeLib(),
                Singleton.getLogger(),
                Singleton.getHelpers(),
                Singleton.getCodeIndexQueue(),
                Singleton.getJobService());
    }

    public IndexService(Data data, StatsService statsService, SearchCodeLib searchcodeLib, LoggerWrapper logger, Helpers helpers, Queue<CodeIndexDocument> codeIndexDocumentQueue, JobService jobService) {
        this.data = data;
        this.statsService = statsService;
        this.searchcodeLib = searchcodeLib;
        this.logger = logger;
        this.helpers = helpers;
        this.jobService = jobService;

        this.MAX_INDEX_SIZE = this.helpers.tryParseInt(Properties.getProperties().getProperty(Values.MAXDOCUMENTQUEUESIZE, Values.DEFAULTMAXDOCUMENTQUEUESIZE), Values.DEFAULTMAXDOCUMENTQUEUESIZE);
        this.MAX_LINES_INDEX_SIZE = this.helpers.tryParseInt(Properties.getProperties().getProperty(Values.MAXDOCUMENTQUEUELINESIZE, Values.DEFAULTMAXDOCUMENTQUEUELINESIZE), Values.DEFAULTMAXDOCUMENTQUEUELINESIZE);

        this.indexAllFields = Arrays.asList(Properties.getProperties().getProperty(Values.INDEX_ALL_FIELDS, Values.DEFAULT_INDEX_ALL_FIELDS).split(","));

        // Locations that should never change once class created
        this.INDEX_A_LOCATION = Paths.get(Properties.getProperties().getProperty(Values.INDEXLOCATION, Values.DEFAULTINDEXLOCATION) + "/" + Values.INDEX_A);
        this.INDEX_B_LOCATION = Paths.get(Properties.getProperties().getProperty(Values.INDEXLOCATION, Values.DEFAULTINDEXLOCATION) + "/" + Values.INDEX_B);
        this.FACET_A_LOCATION = Paths.get(Properties.getProperties().getProperty(Values.FACETSLOCATION, Values.DEFAULTFACETSLOCATION) + "/" + Values.INDEX_A);
        this.FACET_B_LOCATION = Paths.get(Properties.getProperties().getProperty(Values.FACETSLOCATION, Values.DEFAULTFACETSLOCATION) + "/" + Values.INDEX_B);

        // Where do we think we should be looking
        String indexRead = this.data.getDataByName(Values.INDEX_READ, Values.INDEX_A);
        String indexWrite = this.data.getDataByName(Values.INDEX_WRITE, Values.INDEX_A);

        if (indexRead == null || indexWrite == null) {
            indexRead = Values.INDEX_A;
            indexWrite = Values.INDEX_A;
        }

        // If read != write then assume that write was in the process of building
        // and fall back to the read since it should be more accurate
        if (!indexRead.equals(indexWrite)) {
            indexWrite = indexRead;
            this.data.saveData(Values.INDEX_WRITE, indexRead);
        }

        this.INDEX_READ_LOCATION = Values.INDEX_A.equals(indexRead) ? this.INDEX_A_LOCATION : this.INDEX_B_LOCATION;
        this.INDEX_WRITE_LOCATION = Values.INDEX_A.equals(indexWrite) ? this.INDEX_A_LOCATION : this.INDEX_B_LOCATION;

        // Facet locations are keyed off the index so only needs write location
        this.FACET_WRITE_LOCATION = Values.INDEX_A.equals(indexRead) ? this.FACET_A_LOCATION : this.FACET_B_LOCATION;

        this.PAGE_LIMIT = 20;
        this.NO_PAGES_LIMIT = 20;
        this.CHILD_FACET_LIMIT = 200;

        this.codeIndexDocumentQueue = codeIndexDocumentQueue;
        this.uniqueGitRepoQueue = Singleton.getUniqueGitRepoQueue();
        this.uniqueSvnRepoQueue = Singleton.getUniqueSvnRepoQueue();
        this.uniqueFileRepoQueue = Singleton.getUniqueFileRepoQueue();
    }

    //////////////////////////////////////////////////////////////
    // Methods for controlling the index
    //////////////////////////////////////////////////////////////

    @Override
    public boolean getRepoAdderPause() {
        return this.repoAdderPause;
    }

    @Override
    public void setRepoAdderPause(boolean repoAdderPause) {
        this.repoAdderPause = repoAdderPause;
    }

    @Override
    public void toggleRepoAdderPause() {
        this.repoAdderPause = !this.repoAdderPause;
    }

    @Override
    public boolean getReindexingAll() {
        return this.reindexingAll;
    }

    @Override
    public void resetReindexingAll() {
        this.reindexingAll = false;
    }

    @Override
    public synchronized void indexDocument(CodeIndexDocument codeIndexDocument) throws IOException {
        Queue<CodeIndexDocument> queue = new ArrayQueue<>();
        queue.add(codeIndexDocument);
        this.indexDocument(queue);
    }

    /**
     * Given a queue of documents to index, index them by popping the queue supplied.
     * This method must be synchronized as we have not added any logic to deal with multiple threads writing to the
     * index.
     */
    @Override
    public synchronized void indexDocument(Queue<CodeIndexDocument> codeIndexDocumentQueue) throws IOException {
        Directory indexDirectory = FSDirectory.open(this.INDEX_WRITE_LOCATION);
        Directory facetDirectory = FSDirectory.open(this.FACET_WRITE_LOCATION);

        Analyzer analyzer = new CodeAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);

        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        IndexWriter writer = new IndexWriter(indexDirectory, indexWriterConfig);
        TaxonomyWriter taxonomyWriter = new DirectoryTaxonomyWriter(facetDirectory);

        CodeIndexDocument codeIndexDocument = codeIndexDocumentQueue.poll();
        List<CodeIndexDocument> codeIndexDocumentList = new ArrayList<>();

        while (codeIndexDocument != null) {
            codeIndexDocumentList.add(codeIndexDocument);
            codeIndexDocument = codeIndexDocumentQueue.poll();
        }

        try {
            codeIndexDocumentList.parallelStream()
                    .forEach(x -> {
                        this.logger.info("Indexing file " + x.getRepoLocationRepoNameLocationFilename());
                        this.decrementCodeIndexLinesCount(x.getLines());

                        FacetsConfig facetsConfig = new FacetsConfig();
                        facetsConfig.setIndexFieldName(Values.LANGUAGENAME, Values.LANGUAGENAME);
                        facetsConfig.setIndexFieldName(Values.REPONAME, Values.REPONAME);
                        facetsConfig.setIndexFieldName(Values.CODEOWNER, Values.CODEOWNER);
                        facetsConfig.setIndexFieldName(Values.SOURCE, Values.SOURCE);

                        Document document = this.buildDocument(x);

                        try {
                            writer.updateDocument(new Term(Values.PATH, x.getRepoLocationRepoNameLocationFilename()), facetsConfig.build(taxonomyWriter, document));
                        } catch (Exception ignored) {}
                    });
        }
        finally {
            this.helpers.closeQuietly(writer);
            this.helpers.closeQuietly(taxonomyWriter);
            this.logger.info("Closing writers");
        }
    }
    
    /**
     * Builds a document ready to be indexed by lucene
     */
    @Override
    public Document buildDocument(CodeIndexDocument codeIndexDocument) {
        Document document = new Document();

        // Path is the primary key for documents
        // needs to include repo location, project name and then filepath including file
        Field pathField = new StringField(Values.PATH, codeIndexDocument.getRepoLocationRepoNameLocationFilename(), Field.Store.YES);
        document.add(pathField);

        if (!this.helpers.isNullEmptyOrWhitespace(codeIndexDocument.getLanguageName())) {
            document.add(new SortedSetDocValuesFacetField(Values.LANGUAGENAME, codeIndexDocument.getLanguageName()));
        }
        if (!this.helpers.isNullEmptyOrWhitespace(codeIndexDocument.getRepoName())) {
            document.add(new SortedSetDocValuesFacetField(Values.REPONAME, codeIndexDocument.getRepoName()));
        }
        if (!this.helpers.isNullEmptyOrWhitespace(codeIndexDocument.getCodeOwner())) {
            document.add(new SortedSetDocValuesFacetField(Values.CODEOWNER, codeIndexDocument.getCodeOwner()));
        }
        if (!this.helpers.isNullEmptyOrWhitespace(codeIndexDocument.getSource())) {
            document.add(new SortedSetDocValuesFacetField(Values.SOURCE, codeIndexDocument.getSource()));
        }

        this.searchcodeLib.addToSpellingCorrector(codeIndexDocument.getContents());
        String indexContents = indexContentPipeline(codeIndexDocument);

        document.add(new TextField(Values.REPONAME,                 codeIndexDocument.getRepoName().replace(" ", "_"), Field.Store.YES));
        document.add(new TextField(Values.REPO_NAME_LITERAL,        this.helpers.replaceForIndex(codeIndexDocument.getRepoName()).toLowerCase(), Field.Store.NO));
        document.add(new TextField(Values.FILENAME,                 codeIndexDocument.getFileName(), Field.Store.YES));
        document.add(new TextField(Values.FILE_NAME_LITERAL,        this.helpers.replaceForIndex(codeIndexDocument.getFileName()).toLowerCase(), Field.Store.NO));
        document.add(new TextField(Values.FILELOCATION,             codeIndexDocument.getFileLocation(), Field.Store.YES));
        document.add(new TextField(Values.FILELOCATIONFILENAME,     codeIndexDocument.getFileLocationFilename(), Field.Store.YES));
        document.add(new TextField(Values.DISPLAY_LOCATION,         codeIndexDocument.getDisplayLocation(), Field.Store.YES));
        document.add(new TextField(Values.DISPLAY_LOCATION_LITERAL, this.helpers.replaceForIndex(codeIndexDocument.getDisplayLocation()).toLowerCase(), Field.Store.NO));
        document.add(new TextField(Values.MD5HASH,                  codeIndexDocument.getMd5hash(), Field.Store.YES));
        document.add(new TextField(Values.LANGUAGENAME,             codeIndexDocument.getLanguageName().replace(" ", "_"), Field.Store.YES));
        document.add(new TextField(Values.LANGUAGE_NAME_LITERAL,    this.helpers.replaceForIndex(codeIndexDocument.getLanguageName()).toLowerCase(), Field.Store.NO));
        document.add(new IntField(Values.LINES,                     codeIndexDocument.getLines(), Field.Store.YES));
        document.add(new IntField(Values.CODELINES,                 codeIndexDocument.getCodeLines(), Field.Store.YES));
        document.add(new IntField(Values.BLANKLINES,                codeIndexDocument.getBlankLines(), Field.Store.YES));
        document.add(new IntField(Values.COMMENTLINES,              codeIndexDocument.getCommentLines(), Field.Store.YES));
        document.add(new IntField(Values.COMPLEXITY,                codeIndexDocument.getComplexity(), Field.Store.YES));
        document.add(new TextField(Values.CONTENTS,                 indexContents.toLowerCase(), Field.Store.NO));
        document.add(new TextField(Values.REPOLOCATION,             codeIndexDocument.getRepoRemoteLocation(), Field.Store.YES));
        document.add(new TextField(Values.CODEOWNER,                codeIndexDocument.getCodeOwner().replace(" ", "_"), Field.Store.YES));
        document.add(new TextField(Values.OWNER_NAME_LITERAL,       this.helpers.replaceForIndex(codeIndexDocument.getCodeOwner()).toLowerCase(), Field.Store.NO));
        document.add(new TextField(Values.CODEID,                   codeIndexDocument.getHash(), Field.Store.YES));
        document.add(new TextField(Values.SCHASH,                   codeIndexDocument.getSchash(), Field.Store.YES));
        document.add(new TextField(Values.SOURCE,                   this.helpers.replaceForIndex(codeIndexDocument.getSource()), Field.Store.YES));

        // Extra metadata in this case when it was last indexed
        document.add(new LongField(Values.MODIFIED, new Date().getTime(), Field.Store.YES));
        return document;
    }

    public String indexContentPipeline(CodeIndexDocument codeIndexDocument) {
        // This is the main pipeline for making code searchable and probably the most important
        // part of the indexer codebase
        StringBuilder indexBuilder = new StringBuilder();

        if (this.indexAllFields.contains("filename")) {
            indexBuilder.append(this.searchcodeLib.codeCleanPipeline(codeIndexDocument.getFileName())).append(" ");
        }
        if (this.indexAllFields.contains("filenamereverse")) {
            indexBuilder.append(new StringBuilder(codeIndexDocument.getFileName()).reverse().toString()).append(" ");
        }
        if (this.indexAllFields.contains("path")) {
            indexBuilder.append(this.searchcodeLib.splitKeywords(codeIndexDocument.getFileName(), true)).append(" ");
            indexBuilder.append(codeIndexDocument.getFileLocationFilename()).append(" ");
            indexBuilder.append(codeIndexDocument.getFileLocation()).append(" ");
        }
        if (this.indexAllFields.contains("content")) {
            indexBuilder.append(this.searchcodeLib.splitKeywords(codeIndexDocument.getContents(), true)).append(" ");
            indexBuilder.append( this.searchcodeLib.codeCleanPipeline(codeIndexDocument.getContents())).append(" ");
        }
        if (this.indexAllFields.contains("interesting")) {
            indexBuilder.append(this.searchcodeLib.findInterestingKeywords(codeIndexDocument.getContents())).append(" ");
            indexBuilder.append(this.searchcodeLib.findInterestingCharacters(codeIndexDocument.getContents()));
        }

        return indexBuilder.toString();
    }

    /**
     * Deletes a file from the index using the code id which seems to be
     * the most reliable way of doing it. Code id being a hash of the file
     * name and location.
     * TODO Update the record and set the facets to a value we can ignore
     */
    @Override
    public synchronized void deleteByCodeId(String codeId) throws IOException {
        Directory dir = FSDirectory.open(this.INDEX_WRITE_LOCATION);

        Analyzer analyzer = new CodeAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        IndexWriter writer = null;
        Query query;

        try {
            writer = new IndexWriter(dir, iwc);
            QueryParser parser = new QueryParser(Values.CONTENTS, analyzer);

            query = parser.parse(Values.CODEID + ":" + QueryParser.escape(codeId));
            writer.deleteDocuments(query);
        } catch (ParseException | NoSuchFileException ex) {
            this.logger.warning("ERROR - caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        this.helpers.closeQuietly(writer);
    }

    /**
     * Deletes all files that belong to a repository.
     * NB does not clean up from the facets
     */
    @Override
    public synchronized void deleteByRepo(RepoResult repo) throws IOException {
        if (repo == null) {
            return;
        }

        Directory dir = FSDirectory.open(this.INDEX_READ_LOCATION);

        Analyzer analyzer = new CodeAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        IndexWriter writer = new IndexWriter(dir, iwc);

        writer.deleteDocuments(new Term(Values.REPONAME, repo.getName()));
        this.helpers.closeQuietly(writer);
    }

    @Override
    public synchronized void deleteAll() throws IOException {
        FileUtils.deleteDirectory(this.INDEX_READ_LOCATION.toFile());
        FileUtils.deleteDirectory(this.INDEX_WRITE_LOCATION.toFile());
    }

    @Override
    public synchronized void reindexAll() {
        if (this.reindexingAll) {
            return;
        }

        // Stop adding to queue
        this.reindexingAll = true;
        this.repoJobExit = true;

        // This should be moved somewhere else I think...
        this.codeIndexDocumentQueue.clear();
        this.uniqueGitRepoQueue.clear();
        this.uniqueFileRepoQueue.clear();
        this.uniqueSvnRepoQueue.clear();

        // flip write index
        this.flipWriteIndex();

        // Delete the new write index locations
        try {
            FileUtils.deleteDirectory(this.INDEX_WRITE_LOCATION.toFile());
            FileUtils.deleteDirectory(this.FACET_WRITE_LOCATION.toFile());
        } catch (IOException ex) {
            this.logger.warning("Unable to delete index locations: " + this.INDEX_WRITE_LOCATION.toString());
            this.logger.warning("Unable to delete index locations: " + this.FACET_WRITE_LOCATION.toString());
        }

        // queue all repos to be parsed
        this.repoJobExit = false; // TODO add check to see if they have all exited needs to be fast!!!!!
        this.repoJobsCount = this.jobService.forceEnqueueWithCount();

        if (this.repoJobsCount == 0) {
            this.reindexingAll = false;
            this.flipReadIndex();
            this.logger.info("Finished reindex flipping index");
        }
    }

    @Override
    public String getProperty(String propertyValue) {

        switch (propertyValue.toLowerCase()) {
            case "index_read_location":
                return this.INDEX_READ_LOCATION.toString();
            case "index_write_location":
                return this.INDEX_WRITE_LOCATION.toString();
            case "facet_write_location":
                return this.FACET_WRITE_LOCATION.toString();
        }

        return Values.EMPTYSTRING;
    }

    /**
     * Flips the internal state such that reads and writes point at the other index
     * and updates the saved state so relaunch works as expected
     */
    @Override
    public synchronized void flipIndex() {
        this.flipReadIndex();
        this.flipWriteIndex();
    }


    @Override
    public boolean shouldPause(JobType jobType) {
        switch (jobType) {
            case REPO_ADDER:
                return this.shouldRepoAdderPause();
            case REPO_PARSER:
                return this.shouldBackOff() || this.shouldRepoJobPause();
        }

        return false;
    }

    @Override
    public synchronized boolean shouldExit(JobType jobType) {
        switch(jobType) {
            case REPO_PARSER:
                return this.repoJobExit;
        }

        return false;
    }

    @Override
    public synchronized void decrementRepoJobsCount() {
        if (this.repoJobsCount > 0) {
            this.repoJobsCount--;
        }

        if (this.repoJobsCount < 0) {
            this.repoJobsCount = 0;
        }

        if (this.repoJobsCount == 0 && this.reindexingAll) {
            this.reindexingAll = false;
            this.flipReadIndex();
            this.logger.info("Finished reindex flipping index");
        }
    }


    @Override
    public void incrementCodeIndexLinesCount(int incrementBy) {
        codeIndexLinesCountLock.lock();

        try {
            this.codeIndexLinesCount = this.codeIndexLinesCount + incrementBy;
        }
        finally {
            codeIndexLinesCountLock.unlock();
        }
    }

    @Override
    public void decrementCodeIndexLinesCount(int decrementBy) {
        codeIndexLinesCountLock.lock();

        try {
            this.codeIndexLinesCount = this.codeIndexLinesCount - decrementBy;

            if (this.codeIndexLinesCount < 0) {
                this.codeIndexLinesCount = 0;
            }
        }
        finally {
            codeIndexLinesCountLock.unlock();
        }
    }

    @Override
    public void setCodeIndexLinesCount(int value) {
        codeIndexLinesCountLock.lock();

        try {
            this.codeIndexLinesCount = value;
        }
        finally {
            codeIndexLinesCountLock.unlock();
        }
    }

    @Override
    public int getCodeIndexLinesCount() {
        codeIndexLinesCountLock.lock();

        try {
            return this.codeIndexLinesCount;
        }
        finally {
            codeIndexLinesCountLock.unlock();
        }
    }

    @Override
    public int getIndexedDocumentCount() {
        int numDocs = 0;
        IndexReader reader = null;

        try {
            reader = DirectoryReader.open(FSDirectory.open(this.INDEX_READ_LOCATION));
            numDocs = reader.numDocs();
        }
        catch (Exception ex) {
            this.logger.info("ERROR - caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            this.helpers.closeQuietly(reader);
        }

        return numDocs;
    }

    @Override
    public CodeResult getCodeResultByCodeId(String codeId) {
        CodeResult codeResult = null;
        IndexReader reader = null;

        try {
            reader = DirectoryReader.open(FSDirectory.open(this.INDEX_READ_LOCATION));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new CodeAnalyzer();
            QueryParser parser = new QueryParser(Values.CONTENTS, analyzer);

            Query query = parser.parse(Values.CODEID + ":" + QueryParser.escape(codeId));
            logger.info("Query to get by " + Values.CODEID + ":" + QueryParser.escape(codeId));

            TopDocs results = searcher.search(query, 1);
            ScoreDoc[] hits = results.scoreDocs;

            if (hits.length != 0) {
                Document doc = searcher.doc(hits[0].doc);

                String filePath = doc.get(Values.PATH);

                List<String> code = new ArrayList<>();
                try {
                    code = this.helpers.readFileLinesGuessEncoding(filePath, this.helpers.tryParseInt(Properties.getProperties().getProperty(Values.MAXFILELINEDEPTH, Values.DEFAULTMAXFILELINEDEPTH), Values.DEFAULTMAXFILELINEDEPTH));
                } catch (Exception ex) {
                    logger.info("Indexed file appears to binary: " + filePath);
                }

                codeResult = this.createCodeResult(code, filePath, doc, hits[0].doc);
            }
        }
        catch (Exception ex) {
            this.logger.warning("ERROR - caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            this.helpers.closeQuietly(reader);
        }

        return codeResult;
    }

    /**
     * Due to very large repositories (500,000 files) this needs to support
     * paging. Also need to consider the fact that is a list of strings
     * TODO maybe convert to hash so lookups are faster
     */
    @Override
    public List<String> getRepoDocuments(String repoName, int page) {
        int REPOPAGELIMIT = 1000;
        List<String> fileLocations = new ArrayList<>(REPOPAGELIMIT);
        int start = REPOPAGELIMIT * page;

        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(this.INDEX_READ_LOCATION));
            IndexSearcher searcher = new IndexSearcher(reader);

            Analyzer analyzer = new CodeAnalyzer();
            QueryParser parser = new QueryParser(Values.CONTENTS, analyzer);
            Query query = parser.parse(Values.REPO_NAME_LITERAL + ":" + this.helpers.replaceForIndex(repoName));

            TopDocs results = searcher.search(query, Integer.MAX_VALUE);
            int end = Math.min(results.totalHits, (REPOPAGELIMIT * (page + 1)));
            ScoreDoc[] hits = results.scoreDocs;

            for (int i = start; i < end; i++) {
                Document doc = searcher.doc(hits[i].doc);
                fileLocations.add(doc.get(Values.PATH));
            }

            reader.close();
        }
        catch (Exception ex) {
            this.logger.warning("IndexService getRepoDocuments caught a " + ex.getClass() + " on page " + page + "\n with message: " + ex.getMessage());
        }

        return fileLocations;
    }

    /**
     * Collects project stats for a repo given its name
     */
    @Override
    public ProjectStats getProjectStats(String repoName) {
        if (this.helpers.isNullEmptyOrWhitespace(repoName)) {
            return new ProjectStats(0, 0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        int totalCodeLines = 0;
        int totalFiles = 0;
        List<CodeFacetLanguage> codeFacetLanguages = new ArrayList<>();
        List<CodeFacetOwner> repoFacetOwners = new ArrayList<>();
        List<CodeFacetLanguage> codeByLines = new ArrayList<>();

        IndexReader reader = null;

        try {
            reader = DirectoryReader.open(FSDirectory.open(this.INDEX_READ_LOCATION));
            IndexSearcher searcher = new IndexSearcher(reader);

            Analyzer analyzer = new CodeAnalyzer();
            QueryParser parser = new QueryParser(Values.CONTENTS, analyzer);
            Query query = parser.parse(Values.REPO_NAME_LITERAL + ":" + this.helpers.replaceForIndex(repoName));

            TopDocs results = searcher.search(query, Integer.MAX_VALUE);
            ScoreDoc[] hits = results.scoreDocs;

            Map<String, Integer> linesCount = new HashMap<>();

            for (int i = 0; i < results.totalHits; i++) {
                Document doc = searcher.doc(hits[i].doc);

                String languageName = doc.get(Values.LANGUAGENAME).replace("_", " ");
                int lines = Singleton.getHelpers().tryParseInt(doc.get(Values.LINES), "0");
                totalCodeLines += lines;

                if (linesCount.containsKey(languageName)) {
                    linesCount.put(languageName, linesCount.get(languageName) + lines);
                }
                else {
                    linesCount.put(languageName, lines);
                }
            }

            for (String key: linesCount.keySet()) {
                codeByLines.add(new CodeFacetLanguage(key, linesCount.get(key)));
            }
            codeByLines.sort((a, b) -> b.getCount() - a.getCount());

            totalFiles = results.totalHits;
            codeFacetLanguages = this.getLanguageFacetResults(searcher, reader, query);
            repoFacetOwners = this.getOwnerFacetResults(searcher, reader, query);
        }
        catch (Exception ex) {
            this.logger.warning("IndexSearch getProjectStats caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            this.helpers.closeQuietly(reader);
        }

        return new ProjectStats(totalCodeLines, totalFiles, codeFacetLanguages, codeByLines, repoFacetOwners);
    }

    /**
     * Collects project stats for a repo given its name
     */
    @Override
    public SearchResult getProjectFileTree(String repoName) {

        List<CodeResult> codeResults = new ArrayList<>();

        if (this.helpers.isNullEmptyOrWhitespace(repoName)) {
            return new SearchResult(0, 0, Values.EMPTYSTRING, codeResults, null, null, null, null, null);
        }


        IndexReader reader = null;

        try {
            reader = DirectoryReader.open(FSDirectory.open(this.INDEX_READ_LOCATION));
            IndexSearcher searcher = new IndexSearcher(reader);

            Analyzer analyzer = new CodeAnalyzer();
            QueryParser parser = new QueryParser(Values.CONTENTS, analyzer);
            Query query = parser.parse(Values.REPO_NAME_LITERAL + ":" + this.helpers.replaceForIndex(repoName));

            TopDocs results = searcher.search(query, Integer.MAX_VALUE);
            ScoreDoc[] hits = results.scoreDocs;

            for (int i = 0; i < results.totalHits; i++) {
                Document doc = searcher.doc(hits[i].doc);

                CodeResult codeResult = this.createCodeResult(null, Values.EMPTYSTRING, doc, hits[i].doc);
                codeResults.add(codeResult);

                if (i == 1000) {
                    break;
                }
            }
        }
        catch (Exception ex) {
            this.logger.warning("IndexSearch getProjectStats caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            this.helpers.closeQuietly(reader);
        }

        codeResults.sort(Comparator.comparing(x -> x.displayLocation));

        return new SearchResult(0, 0, Values.EMPTYSTRING, codeResults, null, null, null, null, null);
    }

    /**
     * Given a query and what page of results we are on return the matching results for that search.
     * Does not escape the query so it allows syntax such as fileName:some*
     * TODO consider escaping if not a lucene query QueryParserBase.escape(queryString)
     * TODO document the extended syntax to allow raw queries
     */
    @Override
    public SearchResult search(String queryString, HashMap<String, String[]> facets, int page, boolean isLiteral) {
        SearchResult searchResult = new SearchResult();
        this.statsService.incrementSearchCount();
        IndexReader reader = null;

        if (!isLiteral) {
            queryString = this.searchcodeLib.formatQueryString(queryString);
        }

        queryString += this.buildFacets(facets);

        try {
            reader = DirectoryReader.open(FSDirectory.open(this.INDEX_READ_LOCATION));
            IndexSearcher searcher = new IndexSearcher(reader);

            Analyzer analyzer = new CodeAnalyzer();

            // Values.CONTENTS is the field that QueryParser will search if you don't
            // prefix it with a field EG. fileName:something* or other raw lucene search
            QueryParser parser = new QueryParser(Values.CONTENTS, analyzer);
            Query query = parser.parse(queryString);

            this.logger.info("Searching for: " + query.toString(Values.CONTENTS));
            this.logger.searchLog(query.toString(Values.CONTENTS) + " " + page);

            searchResult = this.doPagingSearch(reader, searcher, query, page);
        }
        catch (Exception ex) {
            this.logger.warning("ERROR - caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            this.helpers.closeQuietly(reader);
        }

        return searchResult;
    }

    public String buildFacets(HashMap<String, String[]> facets) {
        if (facets == null) {
            return Values.EMPTYSTRING;
        }

        StringBuilder filters = new StringBuilder(Values.EMPTYSTRING);

        for (String key: facets.keySet()) {
            switch (key) {
                case "repo":
                    List<String> reposList = Arrays.stream(facets.get(key))
                            .map((s) -> Values.REPO_NAME_LITERAL + ":" + QueryParser.escape(Singleton.getHelpers().replaceForIndex(s)))
                            .collect(Collectors.toList());

                    if (!reposList.isEmpty()) {
                        filters.append(" && (").append(StringUtils.join(reposList, " || ")).append(")");
                    }
                    break;
                case "lan":
                    List<String> langsList = Arrays.stream(facets.get(key))
                            .map((s) -> Values.LANGUAGE_NAME_LITERAL + ":" + QueryParser.escape(Singleton.getHelpers().replaceForIndex(s)))
                            .collect(Collectors.toList());

                    if (!langsList.isEmpty()) {
                        filters.append(" && (").append(StringUtils.join(langsList, " || ")).append(")");
                    }
                    break;
                case "own":
                    List<String> ownersList = Arrays.stream(facets.get(key))
                            .map((s) -> Values.OWNER_NAME_LITERAL + ":" + QueryParser.escape(Singleton.getHelpers().replaceForIndex(s)))
                            .collect(Collectors.toList());

                    if (!ownersList.isEmpty()) {
                        filters.append(" && (").append(StringUtils.join(ownersList, " || ")).append(")");
                    }
                    break;
                case "fl":
                    filters.append(" && (fl:").append(this.helpers.replaceForIndex(facets.get(key)[0])).append("*)");
                    break;
                case "src":
                    List<String> srcList = Arrays.stream(facets.get(key))
                            .map((s) -> Values.SOURCE + ":" + QueryParser.escape(Singleton.getHelpers().replaceForIndex(s)))
                            .collect(Collectors.toList());

                    if (!srcList.isEmpty()) {
                        filters.append(" && (").append(StringUtils.join(srcList, " || ")).append(")");
                    }
                    break;
                default:
                    break;
            }
        }

        return filters.toString();
    }

    @Override
    public synchronized void flipReadIndex() {
        this.INDEX_READ_LOCATION = this.INDEX_READ_LOCATION.equals(this.INDEX_A_LOCATION) ? this.INDEX_B_LOCATION : this.INDEX_A_LOCATION;

        if (this.INDEX_READ_LOCATION.equals(this.INDEX_A_LOCATION)) {
            this.data.saveData(Values.INDEX_READ, Values.INDEX_A);
        } else {
            this.data.saveData(Values.INDEX_READ, Values.INDEX_B);
        }
    }

    @Override
    public synchronized void flipWriteIndex() {
        this.INDEX_WRITE_LOCATION = this.INDEX_WRITE_LOCATION.equals(this.INDEX_A_LOCATION) ? this.INDEX_B_LOCATION : this.INDEX_A_LOCATION;
        this.FACET_WRITE_LOCATION = this.FACET_WRITE_LOCATION.equals(this.FACET_A_LOCATION) ? this.FACET_B_LOCATION : this.FACET_A_LOCATION;

        if (this.INDEX_WRITE_LOCATION.equals(this.INDEX_A_LOCATION)) {
            this.data.saveData(Values.INDEX_WRITE, Values.INDEX_A);
        } else {
            this.data.saveData(Values.INDEX_WRITE, Values.INDEX_B);
        }
    }

    /**
     * Checks to see how much CPU we are using and if its higher then the limit set
     * inside the settings page mute the index for a while
     */
    private boolean shouldBackOff() {
        Double loadValue = this.helpers.tryParseDouble(this.data.getDataByName(Values.BACKOFFVALUE, Values.DEFAULTBACKOFFVALUE), Values.DEFAULTBACKOFFVALUE);
        Double loadAverage = this.helpers.tryParseDouble(this.statsService.getLoadAverage(), "0");

        if (loadValue <= 0) {
            return false;
        }

        if (loadAverage >= loadValue) {
            this.logger.info("Load Average higher than set value. Pausing indexing.");
            return true;
        }

        return false;
    }

    /**
     * Only really used internally but does the heavy lifting of actually converting the index document on disk to the
     * format used internally including reading the file from disk.
     */
    private SearchResult doPagingSearch(IndexReader reader, IndexSearcher searcher, Query query, int page) throws IOException {
        TopDocs results = searcher.search(query, this.NO_PAGES_LIMIT * this.PAGE_LIMIT); // 20 pages worth of documents
        ScoreDoc[] hits = results.scoreDocs;

        int numTotalHits = results.totalHits;
        int start = this.PAGE_LIMIT * page;
        int end = Math.min(numTotalHits, (this.PAGE_LIMIT * (page + 1)));
        int noPages = numTotalHits / this.PAGE_LIMIT;

        if (noPages > this.NO_PAGES_LIMIT) {
            noPages = this.NO_PAGES_LIMIT - 1;
        }

        List<Integer> pages = this.calculatePages(numTotalHits, noPages);
        List<CodeResult> codeResults = new ArrayList<>();

        for (int i = start; i < end; i++) {
            Document doc = searcher.doc(hits[i].doc);

            String filePath = doc.get(Values.PATH);

            if (filePath != null) {
                // This line is occasionally useful for debugging ranking
                this.logger.fine("doc=" + hits[i].doc + " score=" + hits[i].score);

                List<String> code = new ArrayList<>();
                try {
                    code = this.helpers.readFileLinesGuessEncoding(filePath, this.helpers.tryParseInt(Properties.getProperties().getProperty(Values.MAXFILELINEDEPTH, Values.DEFAULTMAXFILELINEDEPTH), Values.DEFAULTMAXFILELINEDEPTH));
                }
                catch (Exception ex) {
                    this.logger.warning("Indexed file appears to binary or missing: " + filePath);
                }

                CodeResult codeResult = this.createCodeResult(code, Values.EMPTYSTRING, doc, hits[i].doc);
                codeResults.add(codeResult);
            } else {
                this.logger.warning((i + 1) + ". " + "No path for this document");
            }
        }

        List<CodeFacetLanguage> codeFacetLanguages = this.getLanguageFacetResults(searcher, reader, query);
        List<CodeFacetRepo> repoFacetLanguages = this.getRepoFacetResults(searcher, reader, query);
        List<CodeFacetOwner> repoFacetOwner= this.getOwnerFacetResults(searcher, reader, query);
        List<CodeFacetSource> repoFacetSource= this.getSourceFacetResults(searcher, reader, query);

        return new SearchResult(numTotalHits, page, query.toString(), codeResults, pages, codeFacetLanguages, repoFacetLanguages, repoFacetOwner, repoFacetSource);
    }

    private CodeResult createCodeResult(List<String> code, String filePath, Document doc, int docId) {
        CodeResult codeResult = new CodeResult()
                .setCode(code)
                .setMatchingResults(null)
                .setFilePath(filePath)
                .setCodePath(doc.get(Values.FILELOCATIONFILENAME))
                .setFileName(doc.get(Values.FILENAME))
                .setLanguageName(doc.get(Values.LANGUAGENAME))
                .setMd5hash(doc.get(Values.MD5HASH))
                .setLines(doc.get(Values.LINES))
                .setCodeLines(doc.get(Values.CODELINES))
                .setCommentLines(doc.get(Values.COMMENTLINES))
                .setBlankLines(doc.get(Values.BLANKLINES))
                .setComplexity(doc.get(Values.COMPLEXITY))
                .setDocumentId(docId)
                .setRepoName(doc.get(Values.REPONAME))
                .setRepoLocation(doc.get(Values.REPOLOCATION))
                .setCodeOwner(doc.get(Values.CODEOWNER))
                .setCodeId(doc.get(Values.CODEID))
                .setDisplayLocation(doc.get(Values.DISPLAY_LOCATION))
                .setSource(doc.get(Values.SOURCE));

        return codeResult;
    }

    /**
     * Calculate the number of pages which can be searched through
     */
    private List<Integer> calculatePages(int numTotalHits, int noPages) {
        List<Integer> pages = new ArrayList<>();
        if (numTotalHits != 0) {

            // Account for off by 1 errors
            if (numTotalHits % 10 == 0) {
                noPages -= 1;
            }

            for (int i = 0; i <= noPages; i++) {
                pages.add(i);
            }
        }
        return pages;
    }

    /**
     * Returns the matching language facets for a given query
     */
    private List<CodeFacetLanguage> getLanguageFacetResults(IndexSearcher searcher, IndexReader reader, Query query) {
        List<CodeFacetLanguage> codeFacetLanguages = new ArrayList<>();

        try {
            SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(reader, Values.LANGUAGENAME);
            FacetsCollector fc = new FacetsCollector();
            FacetsCollector.search(searcher, query, 10, fc);
            Facets facets = new SortedSetDocValuesFacetCounts(state, fc);
            FacetResult result = facets.getTopChildren(this.CHILD_FACET_LIMIT, Values.LANGUAGENAME);

            if (result != null) {
                int stepThru = result.childCount > this.CHILD_FACET_LIMIT ? this.CHILD_FACET_LIMIT : result.childCount;

                for (int i = 0; i < stepThru; i++) {
                    LabelAndValue lv = result.labelValues[i];

                    if (lv != null && lv.value != null) {
                        codeFacetLanguages.add(new CodeFacetLanguage(lv.label, lv.value.intValue()));
                    }
                }
            }
        } catch (Exception ignore) {}

        return codeFacetLanguages;
    }

    /**
     * Returns the matching repository facets for a given query
     */
    private List<CodeFacetRepo> getRepoFacetResults(IndexSearcher searcher, IndexReader reader, Query query) {
        List<CodeFacetRepo> codeFacetRepo = new ArrayList<>();

        try {
            SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(reader, Values.REPONAME);
            FacetsCollector fc = new FacetsCollector();
            FacetsCollector.search(searcher, query, 10, fc);
            Facets facets = new SortedSetDocValuesFacetCounts(state, fc);
            FacetResult result = facets.getTopChildren(this.CHILD_FACET_LIMIT, Values.REPONAME);

            if (result != null) {
                int stepThru = result.childCount > this.CHILD_FACET_LIMIT ? this.CHILD_FACET_LIMIT : result.childCount;

                for (int i = 0; i < stepThru; i++) {
                    LabelAndValue lv = result.labelValues[i];

                    if (lv != null && lv.value != null) {
                        codeFacetRepo.add(new CodeFacetRepo(lv.label, lv.value.intValue()));
                    }
                }
            }
        } catch (Exception ignore) {}

        return codeFacetRepo;
    }

    /**
     * Returns the matching owner facets for a given query
     */
    private List<CodeFacetOwner> getOwnerFacetResults(IndexSearcher searcher, IndexReader reader, Query query) {
        List<CodeFacetOwner> codeFacetRepo = new ArrayList<>();

        try {
            SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(reader, Values.CODEOWNER);
            FacetsCollector fc = new FacetsCollector();
            FacetsCollector.search(searcher, query, 10, fc);
            Facets facets = new SortedSetDocValuesFacetCounts(state, fc);
            FacetResult result = facets.getTopChildren(this.CHILD_FACET_LIMIT, Values.CODEOWNER);

            if (result != null) {
                int stepThrough = result.childCount > this.CHILD_FACET_LIMIT ? this.CHILD_FACET_LIMIT : result.childCount;

                for (int i = 0; i < stepThrough; i++) {
                    LabelAndValue lv = result.labelValues[i];

                    if (lv != null && lv.value != null) {
                        codeFacetRepo.add(new CodeFacetOwner(lv.label, lv.value.intValue()));
                    }
                }
            }
        } catch (Exception ignore) {}

        return codeFacetRepo;
    }

    /**
     * Returns the matching source facets for a given query
     */
    private List<CodeFacetSource> getSourceFacetResults(IndexSearcher searcher, IndexReader reader, Query query) {
        List<CodeFacetSource> codeFacetSource = new ArrayList<>();

        try {
            SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(reader, Values.SOURCE);
            FacetsCollector fc = new FacetsCollector();
            FacetsCollector.search(searcher, query, 10, fc);
            Facets facets = new SortedSetDocValuesFacetCounts(state, fc);
            FacetResult result = facets.getTopChildren(this.CHILD_FACET_LIMIT, Values.SOURCE);

            if (result != null) {
                int stepThrough = result.childCount > this.CHILD_FACET_LIMIT ? this.CHILD_FACET_LIMIT : result.childCount;

                for (int i = 0; i < stepThrough; i++) {
                    LabelAndValue lv = result.labelValues[i];

                    if (lv != null && lv.value != null) {
                        codeFacetSource.add(new CodeFacetSource(lv.label, lv.value.intValue()));
                    }
                }
            }
        } catch (Exception ignore) {}

        return codeFacetSource;
    }

    /**
     * Should the repo parsers terminate from adding documents
     * into the queue.
     */
    private boolean shouldRepoJobExit() {
        return this.repoJobExit;
    }

    /**
     * Should the job which adds repositories to the queue to
     * be processed pause.
     */
    private boolean shouldRepoAdderPause() {
        return this.reindexingAll || this.repoAdderPause;
    }

    /**
     * Should the repo parsers pause from adding documents into
     * the index queue.
     */
    private boolean shouldRepoJobPause() {
        int indexQueueSize = this.codeIndexDocumentQueue.size();

        if (indexQueueSize > MAX_INDEX_SIZE) {
            this.logger.info("indexQueueSize " + indexQueueSize + " larger than " + MAX_INDEX_SIZE);
            return true;
        }

        if (this.codeIndexLinesCount > MAX_LINES_INDEX_SIZE) {
            this.logger.info("codeIndexLinesCount " + codeIndexLinesCount + " larger than " + MAX_LINES_INDEX_SIZE);
            return true;
        }

        return false;
    }
}