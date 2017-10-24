package com.searchcode.app.service;

import com.searchcode.app.config.SphinxSearchConfig;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.LanguageType;
import com.searchcode.app.dao.SourceCode;
import com.searchcode.app.dto.*;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.SearchcodeLib;
import org.apache.lucene.document.Document;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class SphinxIndexService implements IIndexService {

    private final Helpers helpers;
    private final SphinxSearchConfig sphinxSearchConfig;
    private final SourceCode sourceCode;
    private final SearchcodeLib searchcodeLib;
    private final LanguageType languageType;

    private final int PAGE_LIMIT;
    private final int NO_PAGES_LIMIT;

    public SphinxIndexService() {
        this.helpers = Singleton.getHelpers();
        this.sphinxSearchConfig = new SphinxSearchConfig();
        this.sourceCode = Singleton.getSourceCode();
        this.languageType = Singleton.getLanguageType();
        this.searchcodeLib = Singleton.getSearchCodeLib();

        this.PAGE_LIMIT = 20;
        this.NO_PAGES_LIMIT = 20;
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

                    // TODO needs to know what sphinx servers exist, and the number of shards per index and update each

                    SourceCodeDTO sourceCodeDTO = sourceCode.saveCode(codeResult);
                    System.out.println("INDEX_DOCUMENT:" + sourceCodeDTO.getId());

                    stmt = connection.prepareStatement("REPLACE INTO codesearchrt1 VALUES(?,?,?,?,?,?,?,?,?,?)");

                    String indexContents = this.searchcodeLib.codeCleanPipeline(sourceCodeDTO.getFilename()) + " " +
                            this.searchcodeLib.splitKeywords(sourceCodeDTO.getFilename(), true) + " " +
                            sourceCodeDTO.getLocation() + " " +
                            this.searchcodeLib.splitKeywords(sourceCodeDTO.getContent(), true) + " " +
                            this.searchcodeLib.codeCleanPipeline(sourceCodeDTO.getContent()) + " " +
                            this.searchcodeLib.findInterestingKeywords(sourceCodeDTO.getContent()) + " " +
                            this.searchcodeLib.findInterestingCharacters(sourceCodeDTO.getContent()).toLowerCase();

                    stmt.setInt(1, sourceCodeDTO.getId());
                    stmt.setString(2, indexContents);
                    stmt.setString(3, sourceCodeDTO.getFilename());
                    stmt.setString(4, this.helpers.replaceForIndex(sourceCodeDTO.getLocation()));
                    stmt.setInt(5, sourceCodeDTO.getRepoid()); // repoid
                    stmt.setInt(6, sourceCodeDTO.getLanguageid()); // languageid
                    stmt.setInt(7, sourceCodeDTO.getSourceid()); // sourceid
                    stmt.setInt(8, sourceCodeDTO.getOwnerid()); // ownerid
                    stmt.setInt(9, sourceCodeDTO.getLicenseid()); // licenseid
                    stmt.setInt(10, sourceCodeDTO.getLinescount()); // linescount

                    ResultSet resultSet = stmt.executeQuery();
                    System.out.println(resultSet);
                } catch (SQLException ex) {
                    Singleton.getLogger().warning(ex.toString());
                }
            }
        }
        finally {
            this.helpers.closeQuietly(stmt);
//            this.helpers.closeQuietly(connection);
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
    public SearchResult search(String queryString, HashMap<String, String[]> facets, int page, boolean isLiteral) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        List<CodeResult> codeResultList = new ArrayList<>();
        List<CodeFacetLanguage> codeFacetLanguages = new ArrayList<>();
        List<CodeFacetRepo> codeFacetRepository = new ArrayList<>();
        int numTotalHits = 0;

        int start = this.PAGE_LIMIT * page;


        try {
            connection = this.sphinxSearchConfig.getConnection();

            String searchQuery = "SELECT id FROM codesearchrealtime WHERE MATCH(?) " +
                                 "LIMIT ?, 20 " +
                                 "FACET repoid ORDER BY COUNT(*) DESC " +
                                 "FACET languageid ORDER BY COUNT(*) DESC; " +
//                                 "FACET sourceid ORDER BY COUNT(*) DESC " +
//                                 "FACET ownerid ORDER BY COUNT(*) DESC " +
//                                 "FACET licenseid ORDER BY COUNT(*) DESC; " +
                                 "SHOW META;";

            // SELECT *, WEIGHT() FROM codesearchrealtime WHERE match('import test java') AND languageid IN (77) FACET languageid ORDER BY COUNT(*) DESC FACET sourceid ORDER BY COUNT(*) DESC; SHOW META;

            stmt = connection.prepareStatement(searchQuery);
            stmt.setString(1, queryString);
            stmt.setInt(2, start);

            boolean isResultSet = stmt.execute();

            if (isResultSet) {
                resultSet = stmt.getResultSet();

                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    System.out.println("id: " + id);
                    Optional<SourceCodeDTO> sourceCodeDTO = this.sourceCode.getById(id);

                    sourceCodeDTO.ifPresent(sourceCodeDTO1 -> codeResultList.add(this.sourceCodeDTOtoCodeResult(sourceCodeDTO1)));
                }

                isResultSet = stmt.getMoreResults();
            }

            // Repository Facets
            if (isResultSet) {
                resultSet = stmt.getResultSet();

                while (resultSet.next()) {
                    String tmp1 = resultSet.getString("repoid");
                    String tmp2 = resultSet.getString("count(*)");

                    codeFacetRepository.add(new CodeFacetRepo(tmp1, this.helpers.tryParseInt(tmp2, "0")));
                }

                isResultSet = stmt.getMoreResults();
            }

            // Language Facets
            if (isResultSet) {
                resultSet = stmt.getResultSet();

                while (resultSet.next()) {
                    String tmp1 = resultSet.getString("languageid");
                    String tmp2 = resultSet.getString("count(*)");

                    codeFacetLanguages.add(new CodeFacetLanguage(tmp1, this.helpers.tryParseInt(tmp2, "0")));
                }

                isResultSet = stmt.getMoreResults();
            }

            // META
            if (isResultSet) {
                resultSet = stmt.getResultSet();

                while (resultSet.next()) {
                    String tmp1 = resultSet.getString("Variable_name");
                    String tmp2 = resultSet.getString("Value");

                    if ("total".equals(tmp1)) {
                        numTotalHits = this.helpers.tryParseInt(tmp2, "0");
                    }
                }
            }

        } catch (SQLException ex) {
            //return results;
        }
        finally {
            this.helpers.closeQuietly(resultSet);
            this.helpers.closeQuietly(stmt);
        }


        int noPages = numTotalHits / this.PAGE_LIMIT;
        List<Integer> pages = this.calculatePages(numTotalHits, noPages);

        codeFacetLanguages = this.transformLanguageType(codeFacetLanguages);

        return new SearchResult(numTotalHits, page, queryString, codeResultList, pages, codeFacetLanguages, codeFacetRepository, new ArrayList<>());
    }

    public List<CodeFacetLanguage> transformLanguageType(List<CodeFacetLanguage> codeFacetLanguages) {
        List<CodeFacetLanguage> properCodeFacetLanguages = new ArrayList<>();

        List<LanguageTypeDTO> languageNamesByIds = this.languageType.getLanguageNamesByIds(codeFacetLanguages.stream().map(x -> x.languageName).collect(Collectors.toList()));

        for (CodeFacetLanguage codeFacetLanguage: codeFacetLanguages) {
            languageNamesByIds.stream()
                    .filter(languageType -> (Integer.toString(languageType.getId())).equals(codeFacetLanguage.languageName))
                    .findFirst()
                    .ifPresent(x -> {
                        codeFacetLanguage.languageName = x.getType();
                        properCodeFacetLanguages.add(new CodeFacetLanguage(x.getType(), codeFacetLanguage.count));
                    });
        }

        return properCodeFacetLanguages;
    }

    public CodeResult sourceCodeDTOtoCodeResult(SourceCodeDTO sourceCodeDTO) {
        CodeResult codeResult = new CodeResult(Arrays.asList(sourceCodeDTO.getContent().split(Values.ALL_NEWLINE)), null);
        codeResult.setFilePath(sourceCodeDTO.getLocation());

        codeResult.setCodePath(sourceCodeDTO.getLocation());
        codeResult.setFileName(sourceCodeDTO.getFilename());
        codeResult.setLanguageName(sourceCodeDTO.getLanguageid() + "");
        codeResult.setMd5hash(sourceCodeDTO.getHash());
        codeResult.setCodeLines(sourceCodeDTO.getLinescount() + "");
        codeResult.setDocumentId(sourceCodeDTO.getId());
        codeResult.setRepoName(sourceCodeDTO.getRepoid() + "");
        codeResult.setRepoLocation(sourceCodeDTO.getLocation() + "");
        codeResult.setCodeOwner(sourceCodeDTO.getOwnerid() + "");
        codeResult.setCodeId(sourceCodeDTO.getId() + "");
        codeResult.setDisplayLocation(sourceCodeDTO.getLocation());

        return codeResult;
    }

    /**
     * Calculate the number of pages which can be searched through
     * TODO taken from indexservice should be centralised
     */
    private List<Integer> calculatePages(int numTotalHits, int noPages) {
        List<Integer> pages = new ArrayList<>();
        if (numTotalHits != 0) {

            // Account for off by 1 errors
            if (numTotalHits % 10 == 0) {
                noPages -= 1;
            }

            for (int i = 0; i <= noPages; i++) {
                pages.add(i);
            }
        }
        return pages;
    }
}
