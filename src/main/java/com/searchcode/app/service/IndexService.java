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
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.dto.CodeResult;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.util.CodeAnalyzer;
import com.searchcode.app.util.LoggerWrapper;
import com.searchcode.app.util.Properties;
import com.searchcode.app.util.SearchcodeLib;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
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

    private final Path INDEX_LOCATION;
    private final Path FACET_LOCATION;


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

        this.INDEX_LOCATION = Paths.get(Properties.getProperties().getProperty(Values.INDEXLOCATION, Values.DEFAULTINDEXLOCATION));
        this.FACET_LOCATION = Paths.get(Properties.getProperties().getProperty(Values.FACETSLOCATION, Values.DEFAULTFACETSLOCATION));
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
        Directory indexDirectory = FSDirectory.open(this.INDEX_LOCATION);
        Directory facetDirectory = FSDirectory.open(this.FACET_LOCATION);

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

        StringBuilder indexContents = new StringBuilder();

        indexContents.append(this.searchcodeLib.codeCleanPipeline(codeIndexDocument.getFileName())).append(" ");
        indexContents.append(this.searchcodeLib.splitKeywords(codeIndexDocument.getFileName())).append(" ");
        indexContents.append(codeIndexDocument.getFileLocationFilename()).append(" ");
        indexContents.append(codeIndexDocument.getFileLocation());
        indexContents.append(this.searchcodeLib.splitKeywords(codeIndexDocument.getContents()));
        indexContents.append(this.searchcodeLib.codeCleanPipeline(codeIndexDocument.getContents()));
        indexContents.append(this.searchcodeLib.findInterestingKeywords(codeIndexDocument.getContents()));
        indexContents.append(this.searchcodeLib.findInterestingCharacters(codeIndexDocument.getContents()));

        document.add(new TextField(Values.REPONAME,             codeIndexDocument.getRepoName().replace(" ", "_"), Field.Store.YES));
        document.add(new TextField(Values.FILENAME,             codeIndexDocument.getFileName(), Field.Store.YES));
        document.add(new TextField(Values.FILELOCATION,         codeIndexDocument.getFileLocation(), Field.Store.YES));
        document.add(new TextField(Values.FILELOCATIONFILENAME, codeIndexDocument.getFileLocationFilename(), Field.Store.YES));
        document.add(new TextField(Values.MD5HASH,              codeIndexDocument.getMd5hash(), Field.Store.YES));
        document.add(new TextField(Values.LANGUAGENAME,         codeIndexDocument.getLanguageName().replace(" ", "_"), Field.Store.YES));
        document.add(new IntField(Values.CODELINES,             codeIndexDocument.getCodeLines(), Field.Store.YES));
        document.add(new TextField(Values.CONTENTS,             indexContents.toString().toLowerCase(), Field.Store.NO));
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
        Directory dir = FSDirectory.open(this.INDEX_LOCATION);

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
        Directory dir = FSDirectory.open(this.INDEX_LOCATION);

        Analyzer analyzer = new CodeAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        IndexWriter writer = new IndexWriter(dir, iwc);

        writer.deleteDocuments(new Term(Values.REPONAME, repo.getName()));
        writer.close();
    }

    @Override
    public void deleteAll() throws IOException {

    }

    @Override
    public void reindexByRepo(RepoResult repo) {

    }

    @Override
    public void reindexAll() {}

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
            IndexReader reader = DirectoryReader.open(FSDirectory.open(this.INDEX_LOCATION));
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
        catch(Exception ex) {
            this.logger.severe("ERROR - caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        return codeResult;
    }
}
