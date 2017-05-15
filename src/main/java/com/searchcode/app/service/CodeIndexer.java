/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.10
 */

package com.searchcode.app.service;

import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.util.CodeAnalyzer;
import com.searchcode.app.util.Properties;
import com.searchcode.app.util.SearchcodeLib;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Responsible for dealing with any updates to the index be they inserts updates or deletes.
 * The main reason for this single entry point is that all methods are synchronized
 */
public class CodeIndexer {

    private final int MAX_INDEX_SIZE;
    private final int MAX_LINES_INDEX_SIZE;
    private final int INDEX_QUEUE_BATCH_SIZE;
    private final SearchcodeLib searchcodeLib;
    private final Path INDEX_LOCATION;
    private final Path FACET_LOCATION;
    private final StatsService statsService;
    private final Data data;
    private final SharedService sharedService;

    public CodeIndexer() {
        this(Singleton.getData(), Singleton.getStatsService(), Singleton.getSearchCodeLib(), Singleton.getSharedService());
    }

    public CodeIndexer(Data data, StatsService statsService, SearchcodeLib searchcodeLib, SharedService sharedService) {
        this.data = data;
        this.statsService = statsService;
        this.searchcodeLib = searchcodeLib;
        this.sharedService = sharedService;
        this.MAX_INDEX_SIZE = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.MAXDOCUMENTQUEUESIZE, Values.DEFAULTMAXDOCUMENTQUEUESIZE), Values.DEFAULTMAXDOCUMENTQUEUESIZE);
        this.MAX_LINES_INDEX_SIZE = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.MAXDOCUMENTQUEUELINESIZE, Values.DEFAULTMAXDOCUMENTQUEUELINESIZE), Values.DEFAULTMAXDOCUMENTQUEUELINESIZE);
        this.INDEX_QUEUE_BATCH_SIZE = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.INDEX_QUEUE_BATCH_SIZE, Values.DEFAULT_INDEX_QUEUE_BATCH_SIZE), Values.DEFAULT_INDEX_QUEUE_BATCH_SIZE);
        this.INDEX_LOCATION = Paths.get(Properties.getProperties().getProperty(Values.INDEXLOCATION, Values.DEFAULTINDEXLOCATION));
        this.FACET_LOCATION = Paths.get(Properties.getProperties().getProperty(Values.FACETSLOCATION, Values.DEFAULTFACETSLOCATION));
    }

    /**
     * Returns true if indexing should be paused, false otherwise
     * used by the parsers to know if they should continue processing or not
     */
    public synchronized boolean shouldPauseAdding() {

        if (this.sharedService.getPauseBackgroundJobs()) {
            return true;
        }

        if (shouldBackOff()) {
            return true;
        }

        int indexQueueSize = Singleton.getCodeIndexQueue().size();
        int codeIndexLinesCount = this.sharedService.getCodeIndexLinesCount();

        if (indexQueueSize > MAX_INDEX_SIZE) {
            Singleton.getLogger().info("indexQueueSize " + indexQueueSize + " larger than " + MAX_INDEX_SIZE);
            return true;
        }

        if (codeIndexLinesCount > MAX_LINES_INDEX_SIZE) {
            Singleton.getLogger().info("codeIndexLinesCount " + codeIndexLinesCount + " larger than " + MAX_LINES_INDEX_SIZE);
            return true;
        }

        return false;
    }

    /**
     * Checks to see how much CPU we are using and if its higher then the limit set
     * inside the settings page mute the index for a while
     */
    public synchronized boolean shouldBackOff() {
        Double loadValue = Double.parseDouble(this.data.getDataByName(Values.BACKOFFVALUE, Values.DEFAULTBACKOFFVALUE));
        Double loadAverage = Double.parseDouble(this.statsService.getLoadAverage());

        if (loadValue <= 0) {
            return false;
        }

        if (loadAverage >= loadValue) {
            Singleton.getLogger().info("Load Average higher than set value. Pausing indexing.");
            return true;
        }

        return false;
    }

    /**
     * Deletes all files that belong to a repository.
     * TODO I don't think this clears anything from the facets, which it should
     */
    public synchronized void deleteByReponame(String repoName) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(Properties.getProperties().getProperty(Values.INDEXLOCATION, Values.DEFAULTINDEXLOCATION)));

        Analyzer analyzer = new CodeAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        IndexWriter writer = new IndexWriter(dir, iwc);

        writer.deleteDocuments(new Term(Values.REPONAME, repoName));
        writer.close();
    }

    /**
     * Deletes a file from the index using the code id which seems to be
     * the most reliable way of doing it
     * TODO Update the record and set the facets to a value we can ignore
     */
    public synchronized void deleteByCodeId(String codeId) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(Properties.getProperties().getProperty(Values.INDEXLOCATION, Values.DEFAULTINDEXLOCATION)));

        Analyzer analyzer = new CodeAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        IndexWriter writer = new IndexWriter(dir, iwc);

        try {
            QueryParser parser = new QueryParser(Values.CONTENTS, analyzer);
            Query query = parser.parse(Values.CODEID + ":" + QueryParser.escape(codeId));
            writer.deleteDocuments(query);
        }
        catch(Exception ex) {
            Singleton.getLogger().warning("ERROR - caught a " + ex.getClass() + " in CodeIndexer\n with message: " + ex.getMessage());
        }
        finally {
            writer.close();
        }
    }

    /**
     * Given a queue of documents to index, index them by popping the queue limited to default of 1000 items.
     * This method must be synchronized as we have not added any logic to deal with multiple threads writing to the
     * index.
     * TODO investigate how Lucene deals with multiple writes
     */
    public synchronized void indexDocuments(Queue<CodeIndexDocument> codeIndexDocumentQueue) throws IOException {
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
            int count = 0;

            while (codeIndexDocument != null) {
                Singleton.getLogger().info("Indexing file " + codeIndexDocument.getRepoLocationRepoNameLocationFilename());
                this.sharedService.decrementCodeIndexLinesCount(codeIndexDocument.getCodeLines());

                facetsConfig = new FacetsConfig();
                facetsConfig.setIndexFieldName(Values.LANGUAGENAME, Values.LANGUAGENAME);
                facetsConfig.setIndexFieldName(Values.REPONAME, Values.REPONAME);
                facetsConfig.setIndexFieldName(Values.CODEOWNER, Values.CODEOWNER);

                Document doc = this.buildDocument(codeIndexDocument);

                writer.updateDocument(new Term(Values.PATH, codeIndexDocument.getRepoLocationRepoNameLocationFilename()), facetsConfig.build(taxonomyWriter, doc));

                count++;
                if (count >= INDEX_QUEUE_BATCH_SIZE) {
                    codeIndexDocument = null;
                }
                else {
                    codeIndexDocument = codeIndexDocumentQueue.poll();
                }
            }
        }
        finally {
            try {
                writer.close();
            }
            finally {
                taxonomyWriter.close();
            }
            Singleton.getLogger().info("Closing writers");
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
     * Given a queue of documents to index, index them by popping the queue limited to 1000 items.
     * This method must be synchronized as we have not added any logic to deal with multiple threads writing to the
     * index.
     * TODO investigate how Lucene deals with multiple writes
     */
    public synchronized void indexTimeDocuments(Queue<CodeIndexDocument> codeIndexDocumentQueue) throws IOException {
        // Index all documents and commit at the end for performance gains
        Directory dir = FSDirectory.open(Paths.get(Properties.getProperties().getProperty(Values.TIMEINDEXLOCATION, Values.DEFAULTTIMEINDEXLOCATION)));
        Directory facetsdir = FSDirectory.open(Paths.get(Properties.getProperties().getProperty(Values.TIMEINDEXFACETLOCATION, Values.DEFAULTTIMEINDEXFACETLOCATION)));

        Analyzer analyzer = new CodeAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        FacetsConfig facetsConfig;

        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        IndexWriter writer = new IndexWriter(dir, iwc);
        TaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(facetsdir);


        try {
            CodeIndexDocument codeIndexDocument = codeIndexDocumentQueue.poll();
            int count = 0;

            while (codeIndexDocument != null) {
                Singleton.getLogger().info("Indexing time file " + codeIndexDocument.getRepoLocationRepoNameLocationFilename());
                this.sharedService.decrementCodeIndexLinesCount(codeIndexDocument.getCodeLines());

                Document doc = new Document();
                // Path is the primary key for documents
                // needs to include repo location, project name and then filepath including file and revision
                Field pathField = new StringField("path", codeIndexDocument.getRepoLocationRepoNameLocationFilename() + ":" + codeIndexDocument.getRevision(), Field.Store.YES);
                doc.add(pathField);

                // Add in facets
                facetsConfig = new FacetsConfig();
                facetsConfig.setIndexFieldName(Values.LANGUAGENAME, Values.LANGUAGENAME);
                facetsConfig.setIndexFieldName(Values.REPONAME, Values.REPONAME);
                facetsConfig.setIndexFieldName(Values.CODEOWNER, Values.CODEOWNER);
                facetsConfig.setIndexFieldName(Values.DATEYEARMONTHDAY, Values.DATEYEARMONTHDAY);
                facetsConfig.setIndexFieldName(Values.DATEYEARMONTH, Values.DATEYEARMONTH);
                facetsConfig.setIndexFieldName(Values.DATEYEAR, Values.DATEYEAR);
                facetsConfig.setIndexFieldName(Values.REVISION, Values.REVISION);
                facetsConfig.setIndexFieldName(Values.DELETED, Values.DELETED);

                if (Singleton.getHelpers().isNullEmptyOrWhitespace(codeIndexDocument.getLanguageName()) == false) {
                    doc.add(new SortedSetDocValuesFacetField(Values.LANGUAGENAME, codeIndexDocument.getLanguageName()));
                }
                if (Singleton.getHelpers().isNullEmptyOrWhitespace(codeIndexDocument.getRepoName()) == false) {
                    doc.add(new SortedSetDocValuesFacetField(Values.REPONAME, codeIndexDocument.getRepoName()));
                }
                if (Singleton.getHelpers().isNullEmptyOrWhitespace(codeIndexDocument.getCodeOwner()) == false) {
                    doc.add(new SortedSetDocValuesFacetField(Values.CODEOWNER, codeIndexDocument.getCodeOwner()));
                }
                if (Singleton.getHelpers().isNullEmptyOrWhitespace(codeIndexDocument.getYearMonthDay()) == false) {
                    doc.add(new SortedSetDocValuesFacetField(Values.DATEYEARMONTHDAY, codeIndexDocument.getYearMonthDay()));
                }
                if (Singleton.getHelpers().isNullEmptyOrWhitespace(codeIndexDocument.getYearMonthDay()) == false) {
                    doc.add(new SortedSetDocValuesFacetField(Values.DATEYEARMONTH, codeIndexDocument.getYearMonthDay().substring(0, 6)));
                }
                if (Singleton.getHelpers().isNullEmptyOrWhitespace(codeIndexDocument.getYearMonthDay()) == false) {
                    doc.add(new SortedSetDocValuesFacetField(Values.DATEYEAR, codeIndexDocument.getYearMonthDay().substring(0, 4)));
                }
                if (Singleton.getHelpers().isNullEmptyOrWhitespace(codeIndexDocument.getRevision()) == false) {
                    doc.add(new SortedSetDocValuesFacetField(Values.REVISION, codeIndexDocument.getRevision()));
                }
                if (Singleton.getHelpers().isNullEmptyOrWhitespace(codeIndexDocument.isDeleted()) == false) {
                    doc.add(new SortedSetDocValuesFacetField(Values.DELETED, codeIndexDocument.isDeleted()));
                }

                String indexContents = Values.EMPTYSTRING;

                indexContents += this.searchcodeLib.splitKeywords(codeIndexDocument.getContents());
                indexContents += this.searchcodeLib.codeCleanPipeline(codeIndexDocument.getContents());
                this.searchcodeLib.addToSpellingCorrector(codeIndexDocument.getContents()); // Store in spelling corrector

                indexContents = indexContents.toLowerCase();

                doc.add(new TextField(Values.REPONAME, codeIndexDocument.getRepoName(), Field.Store.YES));
                doc.add(new TextField(Values.FILENAME, codeIndexDocument.getFileName(), Field.Store.YES));
                doc.add(new TextField(Values.FILELOCATION, codeIndexDocument.getFileLocation(), Field.Store.YES));
                doc.add(new TextField(Values.FILELOCATIONFILENAME, codeIndexDocument.getFileLocationFilename(), Field.Store.YES));
                doc.add(new TextField(Values.MD5HASH, codeIndexDocument.getMd5hash(), Field.Store.YES));
                doc.add(new TextField(Values.LANGUAGENAME, codeIndexDocument.getLanguageName(), Field.Store.YES));
                doc.add(new IntField(Values.CODELINES, codeIndexDocument.getCodeLines(), Field.Store.YES));
                doc.add(new TextField(Values.CONTENTS, indexContents, Field.Store.NO));
                doc.add(new TextField(Values.REPOLOCATION, codeIndexDocument.getRepoRemoteLocation(), Field.Store.YES));
                doc.add(new TextField(Values.CODEOWNER, codeIndexDocument.getCodeOwner(), Field.Store.YES));
                doc.add(new TextField(Values.REVISION, codeIndexDocument.getRevision(), Field.Store.YES));
                doc.add(new TextField(Values.DATEYEARMONTHDAY, codeIndexDocument.getYearMonthDay(), Field.Store.YES));
                doc.add(new TextField(Values.DATEYEARMONTH, codeIndexDocument.getYearMonth(), Field.Store.YES));
                doc.add(new TextField(Values.DATEYEAR, codeIndexDocument.getYear(), Field.Store.YES));
                doc.add(new TextField(Values.MESSAGE, codeIndexDocument.getMessage(), Field.Store.YES));
                doc.add(new TextField(Values.DELETED, codeIndexDocument.isDeleted(), Field.Store.YES));

                // Extra metadata in this case when it was last indexed
                doc.add(new LongField(Values.MODIFIED, new Date().getTime(), Field.Store.YES));

                writer.updateDocument(new Term(Values.PATH, codeIndexDocument.getRepoLocationRepoNameLocationFilename()), facetsConfig.build(taxoWriter, doc));

                count++;
                if (count >= INDEX_QUEUE_BATCH_SIZE) {
                    codeIndexDocument = null;
                }
                else {
                    codeIndexDocument = codeIndexDocumentQueue.poll();
                }
            }
        }
        finally {
            Singleton.getLogger().info("Closing writers");
            writer.close();
            taxoWriter.close();
        }
    }

    /**
     * Possibly better in ultra low memory environments? Reuses the above method by creating a queue with one
     * element and passes it in.
     */
    public synchronized void indexDocument(CodeIndexDocument codeIndexDocument) throws IOException {
        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(codeIndexDocument);
        indexDocuments(queue);
    }

    /**
     * Possibly better in ultra low memory environments? Reuses the above method by creating a queue with one
     * element and passes it in.
     */
    public synchronized void indexTimeDocument(CodeIndexDocument codeIndexDocument) throws IOException {
        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(codeIndexDocument);
        indexTimeDocuments(queue);
    }
}
