/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.11
 */

package com.searchcode.app.service;


import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dto.*;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.util.CodeAnalyzer;
import com.searchcode.app.util.LoggerWrapper;
import com.searchcode.app.util.Properties;
import com.searchcode.app.util.SearchcodeLib;
import org.apache.commons.io.FileUtils;
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
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;

/**
 * Service to deal with any tasks that involve talking to the index
 * specifically
 */
public class IndexService implements IIndexService {

    private final StatsService statsService;
    private final Data data;
    private final SharedService sharedService;
    private final SearchcodeLib searchcodeLib;
    private final LoggerWrapper logger;

    private final int MAX_INDEX_SIZE;
    private final int MAX_LINES_INDEX_SIZE;
    private final int INDEX_QUEUE_BATCH_SIZE;

    private final Path INDEX_READ_LOCATION;
    private final Path FACET_READ_LOCATION;
    private final Path INDEX_WRITE_LOCATION;
    private final Path FACET_WRITE_LOCATION;

    private int PAGE_LIMIT;
    private int CHILD_FACET_LIMIT;


    public IndexService() {
        this(Singleton.getData(), Singleton.getStatsService(), Singleton.getSearchCodeLib(), Singleton.getSharedService(), Singleton.getLogger());
    }

    public IndexService(Data data, StatsService statsService, SearchcodeLib searchcodeLib, SharedService sharedService, LoggerWrapper logger) {
        this.data = data;
        this.statsService = statsService;
        this.searchcodeLib = searchcodeLib;
        this.sharedService = sharedService;
        this.logger = logger;
        this.MAX_INDEX_SIZE = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.MAXDOCUMENTQUEUESIZE, Values.DEFAULTMAXDOCUMENTQUEUESIZE), Values.DEFAULTMAXDOCUMENTQUEUESIZE);
        this.MAX_LINES_INDEX_SIZE = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.MAXDOCUMENTQUEUELINESIZE, Values.DEFAULTMAXDOCUMENTQUEUELINESIZE), Values.DEFAULTMAXDOCUMENTQUEUELINESIZE);
        this.INDEX_QUEUE_BATCH_SIZE = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.INDEX_QUEUE_BATCH_SIZE, Values.DEFAULT_INDEX_QUEUE_BATCH_SIZE), Values.DEFAULT_INDEX_QUEUE_BATCH_SIZE);

        this.INDEX_READ_LOCATION = Paths.get(Properties.getProperties().getProperty(Values.INDEXLOCATION, Values.DEFAULTINDEXLOCATION));
        this.FACET_READ_LOCATION = Paths.get(Properties.getProperties().getProperty(Values.FACETSLOCATION, Values.DEFAULTFACETSLOCATION));
        this.INDEX_WRITE_LOCATION = Paths.get(Properties.getProperties().getProperty(Values.INDEXLOCATION, Values.DEFAULTINDEXLOCATION));
        this.FACET_WRITE_LOCATION = Paths.get(Properties.getProperties().getProperty(Values.FACETSLOCATION, Values.DEFAULTFACETSLOCATION));
        this.PAGE_LIMIT = 20;
        this.CHILD_FACET_LIMIT = 200;
    }

    //////////////////////////////////////////////////////////////
    // Methods for controlling the index
    //////////////////////////////////////////////////////////////

    /**
     * Given a queue of documents to index, index them by popping the queue supplied.
     * This method must be synchronized as we have not added any logic to deal with multiple threads writing to the
     * index.
     */
    @Override
    public synchronized void indexDocument(Queue<CodeIndexDocument> codeIndexDocumentQueue) throws IOException {
        Directory indexDirectory = FSDirectory.open(this.INDEX_READ_LOCATION);
        Directory facetDirectory = FSDirectory.open(this.FACET_READ_LOCATION);

        Analyzer analyzer = new CodeAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        FacetsConfig facetsConfig;

        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        IndexWriter writer = new IndexWriter(indexDirectory, indexWriterConfig);
        TaxonomyWriter taxonomyWriter = new DirectoryTaxonomyWriter(facetDirectory);

        try {
            CodeIndexDocument codeIndexDocument = codeIndexDocumentQueue.poll();

            while (codeIndexDocument != null) {
                this.logger.info("Indexing file " + codeIndexDocument.getRepoLocationRepoNameLocationFilename());
                this.sharedService.decrementCodeIndexLinesCount(codeIndexDocument.getCodeLines());

                facetsConfig = new FacetsConfig();
                facetsConfig.setIndexFieldName(Values.LANGUAGENAME, Values.LANGUAGENAME);
                facetsConfig.setIndexFieldName(Values.REPONAME, Values.REPONAME);
                facetsConfig.setIndexFieldName(Values.CODEOWNER, Values.CODEOWNER);

                Document doc = this.buildDocument(codeIndexDocument);

                writer.updateDocument(new Term(Values.PATH, codeIndexDocument.getRepoLocationRepoNameLocationFilename()), facetsConfig.build(taxonomyWriter, doc));
                codeIndexDocument = codeIndexDocumentQueue.poll();
            }
        }
        finally {
            try {
                writer.close();
            }
            finally {
                taxonomyWriter.close();
            }
            this.logger.info("Closing writers");
        }
    }

    /**
     * Builds a document ready to be indexed by lucene
     */
    public Document buildDocument(CodeIndexDocument codeIndexDocument) {
        Document document = new Document();
        // Path is the primary key for documents
        // needs to include repo location, project name and then filepath including file
        Field pathField = new StringField("path", codeIndexDocument.getRepoLocationRepoNameLocationFilename(), Field.Store.YES);
        document.add(pathField);

        if (!Singleton.getHelpers().isNullEmptyOrWhitespace(codeIndexDocument.getLanguageName())) {
            document.add(new SortedSetDocValuesFacetField(Values.LANGUAGENAME, codeIndexDocument.getLanguageName()));
        }
        if (!Singleton.getHelpers().isNullEmptyOrWhitespace(codeIndexDocument.getRepoName())) {
            document.add(new SortedSetDocValuesFacetField(Values.REPONAME, codeIndexDocument.getRepoName()));
        }
        if (!Singleton.getHelpers().isNullEmptyOrWhitespace(codeIndexDocument.getCodeOwner())) {
            document.add(new SortedSetDocValuesFacetField(Values.CODEOWNER, codeIndexDocument.getCodeOwner()));
        }

        this.searchcodeLib.addToSpellingCorrector(codeIndexDocument.getContents());

        String indexContents = this.searchcodeLib.codeCleanPipeline(codeIndexDocument.getFileName()) + " " +
                this.searchcodeLib.splitKeywords(codeIndexDocument.getFileName()) + " " +
                codeIndexDocument.getFileLocationFilename() + " " +
                codeIndexDocument.getFileLocation() +
                this.searchcodeLib.splitKeywords(codeIndexDocument.getContents()) +
                this.searchcodeLib.codeCleanPipeline(codeIndexDocument.getContents()) +
                this.searchcodeLib.findInterestingKeywords(codeIndexDocument.getContents()) +
                this.searchcodeLib.findInterestingCharacters(codeIndexDocument.getContents());

        document.add(new TextField(Values.REPONAME,             codeIndexDocument.getRepoName().replace(" ", "_"), Field.Store.YES));
        document.add(new TextField(Values.FILENAME,             codeIndexDocument.getFileName(), Field.Store.YES));
        document.add(new TextField(Values.FILELOCATION,         codeIndexDocument.getFileLocation(), Field.Store.YES));
        document.add(new TextField(Values.FILELOCATIONFILENAME, codeIndexDocument.getFileLocationFilename(), Field.Store.YES));
        document.add(new TextField(Values.MD5HASH,              codeIndexDocument.getMd5hash(), Field.Store.YES));
        document.add(new TextField(Values.LANGUAGENAME,         codeIndexDocument.getLanguageName().replace(" ", "_"), Field.Store.YES));
        document.add(new IntField(Values.CODELINES,             codeIndexDocument.getCodeLines(), Field.Store.YES));
        document.add(new TextField(Values.CONTENTS,             indexContents.toLowerCase(), Field.Store.NO));
        document.add(new TextField(Values.REPOLOCATION,         codeIndexDocument.getRepoRemoteLocation(), Field.Store.YES));
        document.add(new TextField(Values.CODEOWNER,            codeIndexDocument.getCodeOwner().replace(" ", "_"), Field.Store.YES));
        document.add(new TextField(Values.CODEID,               codeIndexDocument.getHash(), Field.Store.YES));

        // Extra metadata in this case when it was last indexed
        document.add(new LongField(Values.MODIFIED, new Date().getTime(), Field.Store.YES));
        return document;
    }

    /**
     * Deletes a file from the index using the code id which seems to be
     * the most reliable way of doing it. Code id being a hash of the file
     * name and location.
     * TODO Update the record and set the facets to a value we can ignore
     */
    @Override
    public synchronized void deleteByCodeId(String codeId) throws IOException {
        Directory dir = FSDirectory.open(this.INDEX_READ_LOCATION);

        Analyzer analyzer = new CodeAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        try (IndexWriter writer = new IndexWriter(dir, iwc)) {
            QueryParser parser = new QueryParser(Values.CONTENTS, analyzer);
            Query query = parser.parse(Values.CODEID + ":" + QueryParser.escape(codeId));
            writer.deleteDocuments(query);
        } catch (Exception ex) {
            this.logger.warning("ERROR - caught a " + ex.getClass() + " in CodeIndexer\n with message: " + ex.getMessage());
        }
    }

    /**
     * Deletes all files that belong to a repository.
     * NB does not clean up from the facets
     */
    @Override
    public synchronized void deleteByRepo(RepoResult repo) throws IOException {
        Directory dir = FSDirectory.open(this.INDEX_READ_LOCATION);

        Analyzer analyzer = new CodeAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        IndexWriter writer = new IndexWriter(dir, iwc);

        writer.deleteDocuments(new Term(Values.REPONAME, repo.getName()));
        writer.close();
    }

    @Override
    public void deleteAll() throws IOException {
        FileUtils.deleteDirectory(this.INDEX_READ_LOCATION.toFile());
        FileUtils.deleteDirectory(this.INDEX_WRITE_LOCATION.toFile());
    }

    @Override
    public void reindexByRepo(RepoResult repo) {

    }

    @Override
    public void reindexAll() {}

    @Override
    public void flipReadLocation() {

    }

    @Override
    public void flipWriteLocation() {

    }

    @Override
    public boolean shouldRepoAdderPause() {
        return false;
    }

    @Override
    public boolean shouldRepoJobPause() {
        return false;
    }

    @Override
    public boolean shouldRepoJobExit() {
        return false;
    }

    @Override
    public int getIndexedDocumentCount() {
        return 0;
    }

    @Override
    public CodeResult getCodeResultByCodeId(String codeId) {
        CodeResult codeResult = null;

        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(this.INDEX_READ_LOCATION));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new CodeAnalyzer();
            QueryParser parser = new QueryParser(Values.CONTENTS, analyzer);

            Query query = parser.parse(Values.CODEID + ":" + QueryParser.escape(codeId));
            logger.info("Query to get by " + Values.CODEID + ":" + QueryParser.escape(codeId));

            TopDocs results = searcher.search(query, 1);
            ScoreDoc[] hits = results.scoreDocs;

            if (hits.length != 0) {
                Document doc = searcher.doc(hits[0].doc);

                String filepath = doc.get(Values.PATH);

                List<String> code = new ArrayList<>();
                try {
                    code = Singleton.getHelpers().readFileLinesGuessEncoding(filepath, Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.MAXFILELINEDEPTH, Values.DEFAULTMAXFILELINEDEPTH), Values.DEFAULTMAXFILELINEDEPTH));
                } catch (Exception ex) {
                    logger.info("Indexed file appears to binary: " + filepath);
                }

                codeResult = new CodeResult(code, null);
                codeResult.setFilePath(filepath);
                codeResult.setCodePath(doc.get(Values.FILELOCATIONFILENAME));
                codeResult.setFileName(doc.get(Values.FILENAME));
                codeResult.setLanguageName(doc.get(Values.LANGUAGENAME));
                codeResult.setMd5hash(doc.get(Values.MD5HASH));
                codeResult.setCodeLines(doc.get(Values.CODELINES));
                codeResult.setDocumentId(hits[0].doc);
                codeResult.setRepoName(doc.get(Values.REPONAME));
                codeResult.setRepoLocation(doc.get(Values.REPOLOCATION));
                codeResult.setCodeOwner(doc.get(Values.CODEOWNER));
                codeResult.setCodeId(doc.get(Values.CODEID));
            }

            reader.close();
        }
        catch (Exception ex) {
            this.logger.warning("ERROR - caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        return codeResult;
    }

    /**
     * Given a query and what page of results we are on return the matching results for that search
     */
    @Override
    public SearchResult search(String queryString, int page) {
        SearchResult searchResult = new SearchResult();
        statsService.incrementSearchCount();


        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(this.INDEX_READ_LOCATION));
            IndexSearcher searcher = new IndexSearcher(reader);

            Analyzer analyzer = new CodeAnalyzer();

            QueryParser parser = new QueryParser(Values.CONTENTS, analyzer);

            Query query = parser.parse(queryString);
            this.logger.info("Searching for: " + query.toString(Values.CONTENTS));
            this.logger.searchLog(query.toString(Values.CONTENTS) + " " + page);

            searchResult = this.doPagingSearch(reader, searcher, query, page);
            reader.close();
        }
        catch (Exception ex) {
            this.logger.warning("ERROR -  caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        return searchResult;
    }

    /**
     * Only really used internally but does the heavy lifting of actually converting the index document on disk to the
     * format used internally including reading the file from disk.
     */
    public SearchResult doPagingSearch(IndexReader reader, IndexSearcher searcher, Query query, int page) throws IOException {
        TopDocs results = searcher.search(query, 20 * this.PAGE_LIMIT); // 20 pages worth of documents
        ScoreDoc[] hits = results.scoreDocs;

        int numTotalHits = results.totalHits;
        int start = this.PAGE_LIMIT * page;
        int end = Math.min(numTotalHits, (this.PAGE_LIMIT * (page + 1)));
        int noPages = numTotalHits / this.PAGE_LIMIT;

        if (noPages > 20) {
            noPages = 19;
        }

        List<Integer> pages = this.calculatePages(numTotalHits, noPages);

        List<CodeResult> codeResults = new ArrayList<>();

        for (int i = start; i < end; i++) {
            Document doc = searcher.doc(hits[i].doc);

            String filepath = doc.get(Values.PATH);

            if (filepath != null) {
                // This line is occasionally useful for debugging ranking, but not useful enough to have as log info
                //System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score);

                List<String> code = new ArrayList<>();
                try {
                    // This should probably be limited by however deep we are meant to look into the file
                    // or the value we use here whichever is less
                    code = Singleton.getHelpers().readFileLinesGuessEncoding(filepath, Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.MAXFILELINEDEPTH, Values.DEFAULTMAXFILELINEDEPTH), Values.DEFAULTMAXFILELINEDEPTH));
                }
                catch(Exception ex) {
                    this.logger.warning("Indexed file appears to binary or missing: " + filepath);
                }

                CodeResult cr = new CodeResult(code, null);
                cr.setCodePath(doc.get(Values.FILELOCATIONFILENAME));
                cr.setFileName(doc.get(Values.FILENAME));
                cr.setLanguageName(doc.get(Values.LANGUAGENAME));
                cr.setMd5hash(doc.get(Values.MD5HASH));
                cr.setCodeLines(doc.get(Values.CODELINES));
                cr.setDocumentId(hits[i].doc);
                cr.setRepoLocation(doc.get(Values.REPOLOCATION));
                cr.setRepoName(doc.get(Values.REPONAME));
                cr.setCodeOwner(doc.get(Values.CODEOWNER));
                cr.setCodeId(doc.get(Values.CODEID));

                codeResults.add(cr);
            } else {
                this.logger.warning((i + 1) + ". " + "No path for this document");
            }
        }

        List<CodeFacetLanguage> codeFacetLanguages = this.getLanguageFacetResults(searcher, reader, query);
        List<CodeFacetRepo> repoFacetLanguages = this.getRepoFacetResults(searcher, reader, query);
        List<CodeFacetOwner> repoFacetOwner= this.getOwnerFacetResults(searcher, reader, query);

        return new SearchResult(numTotalHits, page, query.toString(), codeResults, pages, codeFacetLanguages, repoFacetLanguages, repoFacetOwner);
    }

    public List<Integer> calculatePages(int numTotalHits, int noPages) {
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
        }
        catch(IOException ex) {}
        catch(Exception ex) {}

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
        }
        catch(IOException ex) {}
        catch(Exception ex) {}

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
                int stepThru = result.childCount > this.CHILD_FACET_LIMIT ? this.CHILD_FACET_LIMIT : result.childCount;

                for (int i = 0; i < stepThru; i++) {
                    LabelAndValue lv = result.labelValues[i];

                    if (lv != null && lv.value != null) {
                        codeFacetRepo.add(new CodeFacetOwner(lv.label, lv.value.intValue()));
                    }
                }
            }
        }
        catch (IOException ex) {}
        catch (Exception ex) {}

        return codeFacetRepo;
    }
}
