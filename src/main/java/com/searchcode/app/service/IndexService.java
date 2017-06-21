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


import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.dto.CodeResult;
import com.searchcode.app.dto.SearchResult;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.List;
import java.util.Queue;

/**
 * Service to deal with any tasks that involve talking to the index
 */
public class IndexService implements IIndexService {
    public synchronized void indexDocuments(Queue<CodeIndexDocument> codeIndexDocumentQueue) throws IOException {}
    public synchronized void indexDocument(CodeIndexDocument codeIndexDocument) throws IOException {}
    public synchronized void deleteByCodeId(String codeId) throws IOException {}
    public synchronized void deleteByReponame(String repoName) throws IOException {}

    public int getTotalNumberDocumentsIndexed() { return 0; };
    public SearchResult search(String queryString, int page) { return null; }
    public CodeResult getByCodeId(String codeId) { return null; }
    public List<String> getRepoDocuments(String repoName, int page) { return null; }
    public SearchResult doPagingSearch(IndexReader reader, IndexSearcher searcher, Query query, int page) throws IOException { return null; }
    public List<Integer> calculatePages(int numTotalHits, int noPages) { return null; }
}
