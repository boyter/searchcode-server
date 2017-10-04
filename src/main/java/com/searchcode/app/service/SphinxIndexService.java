package com.searchcode.app.service;

import com.searchcode.app.config.SphinxSearchConfig;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.dto.CodeResult;
import com.searchcode.app.dto.ProjectStats;
import com.searchcode.app.dto.SearchResult;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.util.Helpers;
import org.apache.lucene.document.Document;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class SphinxIndexService implements IIndexService {

    private final Helpers helpers;

    public SphinxIndexService() {
        this.helpers = Singleton.getHelpers();
    }

    @Override
    public void indexDocument(CodeIndexDocument codeIndexDocument) throws IOException {

    }

    @Override
    public void indexDocument(Queue<CodeIndexDocument> documentQueue) throws IOException {
        // Need to connect to each sphinx config eventually
        SphinxSearchConfig sphinxSearchConfig = new SphinxSearchConfig();
        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = sphinxSearchConfig.getConnection();
        } catch (SQLException ignored) { }


        CodeIndexDocument codeIndexDocument = documentQueue.poll();
        List<CodeIndexDocument> codeIndexDocumentList = new ArrayList<>();

        while (codeIndexDocument != null) {
            codeIndexDocumentList.add(codeIndexDocument);
            codeIndexDocument = documentQueue.poll();
        }


        try {
            for (CodeIndexDocument codeResult: codeIndexDocumentList) {
                try {
                    stmt = connection.prepareStatement("REPLACE INTO codesearchrt1 VALUES(?,?,?,?,?,?,?,?,?)");

//                    stmt.setInt(1, codeResult.getId());
//                    stmt.setString(2, CodeIndexer.runCodeIndexPipeline(Singleton.getSearchCodeLib(), codeResult.getContent()));
                    stmt.setString(3, codeResult.getFileName());
//                    stmt.setInt(4, codeResult.getRepoid());
//                    stmt.setInt(5, codeResult.getFiletypeid());
//                    stmt.setInt(6, codeResult.getLangugeid());
//                    stmt.setInt(7, codeResult.getSourceid());
//                    stmt.setInt(8, 0); //CCR
//                    stmt.setInt(9, codeResult.getLinescount());
                    stmt.execute();
                } catch (SQLException ex) {
                    Singleton.getLogger().warning(ex.toString());
                }
            }
        }
        finally {
            this.helpers.closeQuietly(stmt);
            this.helpers.closeQuietly(connection);
        }
    }

    @Override
    public void deleteByCodeId(String codeId) throws IOException {

    }

    @Override
    public void deleteByRepo(RepoResult repo) throws IOException {

    }

    @Override
    public void deleteAll() throws IOException { throw new NotImplementedException(); }

    @Override
    public void reindexAll() { throw new NotImplementedException(); }

    @Override
    public void flipIndex() { throw new NotImplementedException(); }

    @Override
    public void flipReadIndex()  { throw new NotImplementedException(); }

    @Override
    public void flipWriteIndex()  { throw new NotImplementedException(); }

    @Override
    public boolean getRepoAdderPause() {
        return false;
    }

    @Override
    public void setRepoAdderPause(boolean repoAdderPause) {

    }

    @Override
    public void toggleRepoAdderPause() {

    }

    @Override
    public boolean getReindexingAll() {
        return false;
    }

    @Override
    public void resetReindexingAll() {

    }

    @Override
    public boolean shouldPause(JobType jobType) {
        return false;
    }

    @Override
    public boolean shouldExit(JobType jobType) {
        return false;
    }

    @Override
    public void incrementCodeIndexLinesCount(int incrementBy) {

    }

    @Override
    public void decrementCodeIndexLinesCount(int decrementBy) {

    }

    @Override
    public void setCodeIndexLinesCount(int value) {

    }

    @Override
    public int getCodeIndexLinesCount() {
        return 0;
    }

    @Override
    public void decrementRepoJobsCount() {

    }

    @Override
    public Document buildDocument(CodeIndexDocument codeIndexDocument) {
        return null;
    }

    @Override
    public int getIndexedDocumentCount() {
        return 0;
    }

    @Override
    public ProjectStats getProjectStats(String repoName) {
        return null;
    }

    @Override
    public SearchResult getProjectFileTree(String repoName) {
        return null;
    }

    @Override
    public List<String> getRepoDocuments(String repoName, int page) {
        return null;
    }

    @Override
    public CodeResult getCodeResultByCodeId(String codeId) {
        return null;
    }

    @Override
    public SearchResult search(String queryString, int page) {


        SphinxSearchConfig sphinxSearchConfig = new SphinxSearchConfig();

        List<Integer> results = new ArrayList<>();

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            connection = sphinxSearchConfig.getConnection();

            stmt = connection.prepareStatement("SELECT * FROM codesearchrt1 WHERE MATCH(?);");
            stmt.setString(1, queryString);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                results.add(id);
            }

        } catch (SQLException ex) {
            //return results;
        }
        finally {
            this.helpers.closeQuietly(resultSet);
            this.helpers.closeQuietly(stmt);
            this.helpers.closeQuietly(connection);
        }

        //return results;

        return null;
    }
}
