package com.searchcode.app.service;

import com.searchcode.app.dto.CodeResult;
import com.searchcode.app.dto.SearchResult;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.List;

public interface ICodeSearcher {
    int getTotalNumberDocumentsIndexed();
    SearchResult search(String queryString, int page);
    CodeResult getByCodeId(String codeId);
    List<String> getRepoDocuments(String repoName, int page);

    SearchResult doPagingSearch(IndexReader reader, IndexSearcher searcher, Query query, int page) throws IOException;
    List<Integer> calculatePages(int numTotalHits, int noPages);
}
