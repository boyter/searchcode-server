/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.0
 */

package com.searchcode.app.service;

import com.searchcode.app.config.Values;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.util.CodeAnalyzer;
import com.searchcode.app.util.Helpers;
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
import java.nio.file.Paths;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Responsible for dealing with any updates to the index be they inserts updates or deletes.
 * The main reason for this single entry point is that all methods are synchronized and static
 */
public class CodeIndexer {

    private static int MAXINDEXSIZE = Helpers.tryParseInt(Properties.getProperties().getProperty(Values.MAXDOCUMENTQUEUESIZE, Values.DEFAULTMAXDOCUMENTQUEUESIZE), Values.DEFAULTMAXDOCUMENTQUEUESIZE);
    private static int MAXLINESINDEXSIZE = Helpers.tryParseInt(Properties.getProperties().getProperty(Values.MAXDOCUMENTQUEUELINESIZE, Values.DEFAULTMAXDOCUMENTQUEUELINESIZE), Values.DEFAULTMAXDOCUMENTQUEUELINESIZE);

    /**
     * Returns true if indexing should be paused, false otherwise
     * used by the parsers to know if they should continue processing or not
     */
    public static synchronized boolean shouldPauseAdding() {
        int indexQueueSize = Singleton.getCodeIndexQueue().size();
        int codeIndexLinesCount = Singleton.getCodeIndexLinesCount();

        if (indexQueueSize > MAXINDEXSIZE) {
            Singleton.getLogger().info("indexQueueSize " + indexQueueSize + " larger then " + MAXINDEXSIZE);
            return true;
        }

        if (codeIndexLinesCount > MAXLINESINDEXSIZE) {
            Singleton.getLogger().info("codeIndexLinesCount " + codeIndexLinesCount + " larger then " + MAXLINESINDEXSIZE);
            return true;
        }

        return false;
    }

    /**
     * Deletes all files that belong to a repository.
     * TODO I don't think this clears anything from the facets, which it should
     */
    public static synchronized void deleteByReponame(String repoName) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(Properties.getProperties().getProperty(Values.INDEXLOCATION, Values.DEFAULTINDEXLOCATION)));

        Analyzer analyzer = new CodeAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        IndexWriter writer = new IndexWriter(dir, iwc);

        writer.deleteDocuments(new Term(Values.REPONAME, repoName));
        writer.close();
    }

    /**
     * Deletes a file from the index given its location
     * TODO I don't think this clears anything from the facets, which it should
     */
    public static synchronized void deleteByFileLocationFilename(String fileLocationFilename) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(Properties.getProperties().getProperty(Values.INDEXLOCATION, Values.DEFAULTINDEXLOCATION)));

        Analyzer analyzer = new CodeAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        IndexWriter writer = new IndexWriter(dir, iwc);

        try {
            QueryParser parser = new QueryParser("contents", analyzer);
            Query query = parser.parse("filelocationfilename:" + QueryParser.escape(fileLocationFilename));
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
     * Given a queue of documents to index, index them by popping the queue limited to 1000 items.
     * This method must be synchronized as we have not added any logic to deal with multiple threads writing to the
     * index.
     * TODO investigate how Lucene deals with multiple writes
     * TODO make the 1000 limit configurable
     * TODO there appears to be something in here causing some serious slowdowns
     */
    public static synchronized void indexDocuments(Queue<CodeIndexDocument> codeIndexDocumentQueue) throws IOException {
        // Index all documents and commit at the end for performance gains
        Directory dir = FSDirectory.open(Paths.get(Properties.getProperties().getProperty(Values.INDEXLOCATION, Values.DEFAULTINDEXLOCATION)));
        Directory facetsdir = FSDirectory.open(Paths.get(Properties.getProperties().getProperty(Values.FACETSLOCATION, Values.DEFAULTFACETSLOCATION)));

        Analyzer analyzer = new CodeAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        FacetsConfig facetsConfig = new FacetsConfig();
        SearchcodeLib scl = new SearchcodeLib();

        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        IndexWriter writer = new IndexWriter(dir, iwc);
        TaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(facetsdir);


        try {
            CodeIndexDocument codeIndexDocument = codeIndexDocumentQueue.poll();
            int count = 0;

            while (codeIndexDocument != null) {
                Singleton.getLogger().info("Indexing file " + codeIndexDocument.getRepoLocationRepoNameLocationFilename());
                Singleton.decrementCodeIndexLinesCount(codeIndexDocument.getCodeLines());

                Document doc = new Document();
                // Path is the primary key for documents
                // needs to include repo location, project name and then filepath including file
                Field pathField = new StringField("path", codeIndexDocument.getRepoLocationRepoNameLocationFilename(), Field.Store.YES);
                doc.add(pathField);

                // Add in facets
                facetsConfig = new FacetsConfig();
                facetsConfig.setIndexFieldName(Values.LANGUAGENAME, Values.LANGUAGENAME);
                facetsConfig.setIndexFieldName(Values.REPONAME, Values.REPONAME);
                facetsConfig.setIndexFieldName(Values.CODEOWNER, Values.CODEOWNER);

                if (Helpers.isNullEmptyOrWhitespace(codeIndexDocument.getLanguageName()) == false) {
                    doc.add(new SortedSetDocValuesFacetField(Values.LANGUAGENAME, codeIndexDocument.getLanguageName()));
                }
                if (Helpers.isNullEmptyOrWhitespace(codeIndexDocument.getRepoName()) == false) {
                    doc.add(new SortedSetDocValuesFacetField(Values.REPONAME, codeIndexDocument.getRepoName()));
                }
                if (Helpers.isNullEmptyOrWhitespace(codeIndexDocument.getCodeOwner()) == false) {
                    doc.add(new SortedSetDocValuesFacetField(Values.CODEOWNER, codeIndexDocument.getCodeOwner()));
                }

                String indexContents = Values.EMPTYSTRING;

                indexContents += scl.splitKeywords(codeIndexDocument.getContents());
                indexContents += scl.codeCleanPipeline(codeIndexDocument.getContents());
                scl.addToSpellingCorrector(codeIndexDocument.getContents()); // Store in spelling corrector

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
                doc.add(new TextField(Values.CODEID, codeIndexDocument.getHash(), Field.Store.YES));

                // Extra metadata in this case when it was last indexed
                doc.add(new LongField(Values.MODIFIED, new Date().getTime(), Field.Store.YES));

                writer.updateDocument(new Term(Values.PATH, codeIndexDocument.getRepoLocationRepoNameLocationFilename()), facetsConfig.build(taxoWriter, doc));

                count++;
                if (count >= 1000) { // Only index 1000 documents at most each time
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
    public static synchronized void indexDocument(CodeIndexDocument codeIndexDocument) throws IOException {
        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<CodeIndexDocument>();
        queue.add(codeIndexDocument);
        indexDocuments(queue);
        queue = null;
    }
}
