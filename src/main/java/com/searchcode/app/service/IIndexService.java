package com.searchcode.app.service;

import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.dto.CodeResult;
import com.searchcode.app.model.RepoResult;

import java.io.IOException;
import java.util.Queue;

public interface IIndexService {
    void indexDocument(Queue<CodeIndexDocument> documentQueue) throws IOException;

    void deleteByCodeId(String codeId) throws IOException;
    void deleteByRepo(RepoResult repo) throws IOException;
    void deleteAll() throws IOException;

    void reindexByRepo(RepoResult repo);
    void reindexAll();

    boolean shouldRepoAdderPause();
    boolean shouldRepoJobPause();
    boolean shouldRepoJobExit();

    int getIndexedDocumentCount();

    CodeResult getCodeResultByCodeId(String codeId);
//    List<String> getRepoDocuments(String repoName, int page);
//    List<Integer> calculatePages(int numTotalHits, int noPages);
//
//    SearchResult search(String queryString, int page);
//    SearchResult doPagingSearch(IndexReader reader, IndexSearcher searcher, Query query, int page) throws IOException;
}
