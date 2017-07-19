package com.searchcode.app.service;

import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.dto.CodeResult;
import com.searchcode.app.dto.ProjectStats;
import com.searchcode.app.dto.SearchResult;
import com.searchcode.app.model.RepoResult;
import org.apache.lucene.document.Document;

import java.io.IOException;
import java.util.List;
import java.util.Queue;

public interface IIndexService {

    enum JobType {
        REPO_ADDER,
        REPO_PARSER,
    }

    void indexDocument(CodeIndexDocument codeIndexDocument) throws IOException;
    void indexDocument(Queue<CodeIndexDocument> documentQueue) throws IOException;

    void deleteByCodeId(String codeId) throws IOException;
    void deleteByRepo(RepoResult repo) throws IOException;
    void deleteAll() throws IOException;

    void reindexByRepo(RepoResult repo);
    void reindexAll();

    void flipIndex();

    boolean getRepoAdderPause();
    void setRepoAdderPause(boolean repoAdderPause);

    boolean shouldPause(JobType jobType);
    boolean shouldExit(JobType jobType);

    void incrementCodeIndexLinesCount(int incrementBy);
    void decrementCodeIndexLinesCount(int decrementBy);
    void setCodeIndexLinesCount(int value);
    int getCodeIndexLinesCount();

    Document buildDocument(CodeIndexDocument codeIndexDocument);
    int getIndexedDocumentCount();
    ProjectStats getProjectStats(String repoName);
    List<String> getRepoDocuments(String repoName, int page);
    CodeResult getCodeResultByCodeId(String codeId);
    SearchResult search(String queryString, int page);
}
