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
import com.searchcode.app.dto.SearchResult;
import com.searchcode.app.util.LoggerWrapper;
import com.searchcode.app.util.Properties;
import com.searchcode.app.util.SearchcodeLib;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Queue;

/**
 * Service to deal with any tasks that involve talking to the index
 */
public class IndexService implements IIndexService {

    private final StatsService statsService;
    private final Data data;
    private final SharedService sharedService;
    private final SearchcodeLib searchcodeLib;
    private final LoggerWrapper loggerWrapper;

    private final int MAX_INDEX_SIZE;
    private final int MAX_LINES_INDEX_SIZE;
    private final int INDEX_QUEUE_BATCH_SIZE;

    private final Path INDEX_LOCATION;
    private final Path FACET_LOCATION;


    public IndexService() {
        this(Singleton.getData(), Singleton.getStatsService(), Singleton.getSearchCodeLib(), Singleton.getSharedService(), Singleton.getLogger());
    }

    public IndexService(Data data, StatsService statsService, SearchcodeLib searchcodeLib, SharedService sharedService, LoggerWrapper loggerWrapper) {
        this.data = data;
        this.statsService = statsService;
        this.searchcodeLib = searchcodeLib;
        this.sharedService = sharedService;
        this.loggerWrapper = loggerWrapper;
        this.MAX_INDEX_SIZE = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.MAXDOCUMENTQUEUESIZE, Values.DEFAULTMAXDOCUMENTQUEUESIZE), Values.DEFAULTMAXDOCUMENTQUEUESIZE);
        this.MAX_LINES_INDEX_SIZE = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.MAXDOCUMENTQUEUELINESIZE, Values.DEFAULTMAXDOCUMENTQUEUELINESIZE), Values.DEFAULTMAXDOCUMENTQUEUELINESIZE);
        this.INDEX_QUEUE_BATCH_SIZE = Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.INDEX_QUEUE_BATCH_SIZE, Values.DEFAULT_INDEX_QUEUE_BATCH_SIZE), Values.DEFAULT_INDEX_QUEUE_BATCH_SIZE);
        this.INDEX_LOCATION = Paths.get(Properties.getProperties().getProperty(Values.INDEXLOCATION, Values.DEFAULTINDEXLOCATION));
        this.FACET_LOCATION = Paths.get(Properties.getProperties().getProperty(Values.FACETSLOCATION, Values.DEFAULTFACETSLOCATION));
    }

    public synchronized void indexDocuments(Queue<CodeIndexDocument> codeIndexDocumentQueue) throws IOException {}
    public synchronized void indexDocument(CodeIndexDocument codeIndexDocument) throws IOException {}
    public synchronized void deleteByCodeId(String codeId) throws IOException {}
    public synchronized void deleteByReponame(String repoName) throws IOException {}


    //////////////////////////////////////////////////////////////
    // Methods fof querying the index go below here
    //////////////////////////////////////////////////////////////

    /**
     * Returns the total number of documents that are present in the index at this time
     */
    public int getTotalNumberDocumentsIndexed() {
        int numDocs = 0;
        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(this.INDEX_LOCATION));
            numDocs = reader.numDocs();
            reader.close();
        }
        catch(Exception ex) {
            this.loggerWrapper.info(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        return numDocs;
    }


    public SearchResult search(String queryString, int page) { return null; }
    public CodeResult getByCodeId(String codeId) { return null; }
    public List<String> getRepoDocuments(String repoName, int page) { return null; }
    public SearchResult doPagingSearch(IndexReader reader, IndexSearcher searcher, Query query, int page) throws IOException { return null; }
    public List<Integer> calculatePages(int numTotalHits, int noPages) { return null; }
}
