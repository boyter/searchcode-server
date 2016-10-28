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
    CodeResult getByRepoFileName(String repo, String fileName);
    CodeResult getByCodeId(String codeId);

    CodeResult getById(int documentId);
    List<String> getRepoDocuments(String repoName);

    SearchResult doPagingSearch(IndexReader reader, IndexSearcher searcher, Query query, int page) throws IOException;
    List<Integer> calculatePages(int numTotalHits, int noPages);
}
