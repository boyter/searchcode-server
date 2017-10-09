package com.searchcode.app.service;

import com.searchcode.app.config.SphinxSearchConfig;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.SourceCode;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.dto.CodeResult;
import com.searchcode.app.dto.ProjectStats;
import com.searchcode.app.dto.SearchResult;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.SearchcodeLib;
import org.apache.lucene.document.Document;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class SphinxIndexService implements IIndexService {

    private final Helpers helpers;
    private final SphinxSearchConfig sphinxSearchConfig;
    private final SourceCode sourceCode;
    private final SearchcodeLib searchcodeLib;

    public SphinxIndexService() {
        this.helpers = Singleton.getHelpers();
        this.sphinxSearchConfig = new SphinxSearchConfig();
        this.sourceCode = new SourceCode();
        this.searchcodeLib = Singleton.getSearchCodeLib();
    }

    @Override
    public void indexDocument(CodeIndexDocument codeIndexDocument) throws IOException {
    }

    @Override
    public void indexDocument(Queue<CodeIndexDocument> documentQueue) throws IOException {
        // Need to connect to each sphinx config eventually
        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = this.sphinxSearchConfig.getConnection();
        } catch (SQLException ignored) {
            System.out.println(ignored);
        }


        CodeIndexDocument codeIndexDocument = documentQueue.poll();
        List<CodeIndexDocument> codeIndexDocumentList = new ArrayList<>();

        while (codeIndexDocument != null) {
            codeIndexDocumentList.add(codeIndexDocument);
            codeIndexDocument = documentQueue.poll();
        }

        try {

            // TODO should batch these
            for (CodeIndexDocument codeResult: codeIndexDocumentList) {
                try {
                    // Check if language in database
                    // Upsert value into database
                    // Upsert the index

                    int id = sourceCode.saveCode(codeResult);
                    System.out.println("INDEX_DOCUMENT:" + id);

                    stmt = connection.prepareStatement("REPLACE INTO codesearchrt1 VALUES(?,?,?,?,?,?,?,?,?)");
                    String indexContents = this.searchcodeLib.codeCleanPipeline(codeIndexDocument.getFileName()) + " " +
                            this.searchcodeLib.splitKeywords(codeIndexDocument.getFileName(), true) + " " +
                            codeIndexDocument.getFileLocationFilename() + " " +
                            codeIndexDocument.getFileLocation() +
                            this.searchcodeLib.splitKeywords(codeIndexDocument.getContents(), true) +
                            this.searchcodeLib.codeCleanPipeline(codeIndexDocument.getContents()) +
                            this.searchcodeLib.findInterestingKeywords(codeIndexDocument.getContents()) +
                            this.searchcodeLib.findInterestingCharacters(codeIndexDocument.getContents()).toLowerCase();

                    stmt.setInt(1, id);
                    stmt.setString(2, indexContents);
                    stmt.setString(3, codeResult.getFileName());
                    stmt.setInt(4, 99); // repoid
                    stmt.setInt(5, 77); // languageid
                    stmt.setInt(6, 2); // sourceid
                    stmt.setInt(7, 1337); // ownerid
                    stmt.setInt(8, 1); // licenseid
                    stmt.setInt(9, codeResult.getCodeLines()); // licenseid

                    ResultSet resultSet = stmt.executeQuery();
                    System.out.println(resultSet);
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
    public String getProperty(String propertyValue) {
        return Values.EMPTYSTRING;
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
        Connection connection;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            connection = this.sphinxSearchConfig.getConnection();

            String searchQuery = "SELECT * FROM codesearchrt1 WHERE MATCH(?) " +
                                 "FACET languageid ORDER BY COUNT(*) DESC " +
                                 "FACET sourceid ORDER BY COUNT(*) DESC; " +
                                 "SHOW META;";

            // SELECT *, WEIGHT() FROM codesearchrealtime WHERE match('import test java') AND languageid IN (77) FACET languageid ORDER BY COUNT(*) DESC FACET sourceid ORDER BY COUNT(*) DESC; SHOW META;

            stmt = connection.prepareStatement(searchQuery);
            stmt.setString(1, queryString);

            boolean isResultSet = stmt.execute();

            if (isResultSet) {
                resultSet = stmt.getResultSet();

                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    System.out.println("id: " + id);
                }

                isResultSet = stmt.getMoreResults();
            }

            if (isResultSet) {
                resultSet = stmt.getResultSet();

                while (resultSet.next()) {
                    int id = resultSet.getInt("languageid");
                    System.out.println("languageid: " + id);
                }
            }


        } catch (SQLException ex) {
            //return results;
        }
        finally {
            this.helpers.closeQuietly(resultSet);
            this.helpers.closeQuietly(stmt);
        }

        //return results;

        return null;
    }
}
