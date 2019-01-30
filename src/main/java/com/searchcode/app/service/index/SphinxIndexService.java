package com.searchcode.app.service.index;

import com.searchcode.app.config.SphinxSearchConfig;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.LanguageType;
import com.searchcode.app.dao.SourceCode;
import com.searchcode.app.dto.*;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.Properties;
import com.searchcode.app.util.SearchCodeLib;
import org.apache.lucene.document.Document;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class SphinxIndexService extends IndexBaseService {

    private final Helpers helpers;
    private final SphinxSearchConfig sphinxSearchConfig;
    private final SourceCode sourceCode;
    private final SearchCodeLib searchcodeLib;
    private final LanguageType languageType;

    private final int SHARD_COUNT;
    private final String SPHINX_SERVERS_SHARDS;

    public SphinxIndexService() {

        this(Singleton.getLanguageType());
    }

    public SphinxIndexService(LanguageType languageType) {
        super();
        this.languageType = languageType;

        this.helpers = Singleton.getHelpers();
        this.sphinxSearchConfig = new SphinxSearchConfig();
        this.sourceCode = Singleton.getSourceCode();
        this.searchcodeLib = Singleton.getSearchCodeLib();

        this.SPHINX_SERVERS_SHARDS = Properties.getProperties().getProperty(Values.SPHINX_SERVERS_SHARDS, Values.DEFAULT_SPHINX_SERVERS_SHARDS);
        this.SHARD_COUNT = this.getShardCount(this.SPHINX_SERVERS_SHARDS);
    }

    @Override
    public void indexDocument(CodeIndexDocument codeIndexDocument) throws IOException {
    }

    @Override
    public void indexDocument(Queue<CodeIndexDocument> documentQueue) throws IOException {
        // TODO Need to connect to each sphinx config eventually
        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            var connectionOptional = this.sphinxSearchConfig.getConnection("localhost");
            connection = connectionOptional.orElseThrow(() -> new IOException("Unable to connect to sphinx"));
        } catch (SQLException ex) {
            this.logger.severe(String.format("90cf00ef::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }


        var codeIndexDocument = documentQueue.poll();
        var codeIndexDocumentList = new ArrayList<CodeIndexDocument>();

        while (codeIndexDocument != null) {
            codeIndexDocumentList.add(codeIndexDocument);
            codeIndexDocument = documentQueue.poll();
        }

        try {

            // TODO should batch these
            for (var codeResult : codeIndexDocumentList) {
                try {

                    // TODO this is wrong. It assumes that we want to save to the database as well IE its doing too much
                    // TODO refactor so it ONLY does the indexing. It should already be in the database at this point.


                    // Check if language in database
                    // Upsert value into database
                    // Upsert the index

                    // TODO needs to know what sphinx servers exist, and the number of shards per index and update each
//                    var sourceCodeDTO = sourceCode.saveCode(codeResult);

                    // TODO consider using consistent hashing IE like memcached so we can drop in more indexes at will
                    var shard = (codeResult.getId() % this.SHARD_COUNT) + 1;
                    stmt = connection.prepareStatement(String.format("REPLACE INTO codesearchrt%s VALUES(?,?,?,?,?,?)", shard));

                    var indexContents = this.indexContentPipeline(codeResult);

                    stmt.setInt(1, codeResult.getId());
                    stmt.setString(2, indexContents);
                    stmt.setString(3, codeResult.getFileName());
                    stmt.setString(4, this.helpers.replaceForIndex(codeResult.getDisplayLocation()));
                    stmt.setInt(5, 1);
                    stmt.setInt(6, 1);
//                    stmt.setInt(7, sourceCodeDTO.getSourceid()); // TODO get the below values
//                    stmt.setInt(8, sourceCodeDTO.getOwnerid());
//                    stmt.setInt(9, sourceCodeDTO.getLicenseid());
//                    stmt.setInt(10, sourceCodeDTO.getLinescount());

                    stmt.execute();
                } catch (SQLException ex) {
                    this.logger.severe(String.format("893321b2::error in class %s exception %s", ex.getClass(), ex.getMessage()));
                }
            }
        } finally {
            this.helpers.closeQuietly(stmt);
            // this.helpers.closeQuietly(connection);
        }
    }

    @Override
    public void deleteByCodeId(String codeId) throws IOException {
    }

    @Override
    public void deleteByRepo(RepoResult repo) throws IOException {
    }

    @Override
    public void deleteAll() throws IOException {
    }

    @Override
    public void reindexAll() {
    }

    @Override
    public void flipIndex() {

    }

    @Override
    public void flipReadIndex() {

    }

    @Override
    public void flipWriteIndex() {

    }

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
        var byId = this.sourceCode.getById(this.helpers.tryParseInt(codeId, "-1"));
        return byId.map(x -> {
            return new CodeResult(Arrays.asList(x.content.split("\n")), null)
                    .setFileName(x.filename)
                    .setLines(Integer.toString(x.linesCount))
                    .setCodeLines(Integer.toString(x.code))
                    .setCommentLines(Integer.toString(x.comment));
        }).orElse(null);
    }

    @Override
    public SearchResult search(String queryString, HashMap<String, String[]> facets, int page, boolean isLiteral) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        var codeResultList = new ArrayList<CodeResult>();
        var codeFacetLanguages = new ArrayList<CodeFacetLanguage>();
        var codeFacetRepository = new ArrayList<CodeFacetRepo>();
        var numTotalHits = 0;

        var start = this.PAGE_LIMIT * page;

        try {
            var connectionOptional = this.sphinxSearchConfig.getConnection("localhost");
            // TODO handle this better
            connection = connectionOptional.get();

            var searchQuery = " SELECT id FROM codesearchrealtime WHERE MATCH(?) " +
                    this.getLanguageFacets(facets) +
                    " LIMIT ?, 20 " +
                    " FACET repoid ORDER BY COUNT(*) DESC " +
                    " FACET languageid ORDER BY COUNT(*) DESC; " +
                    // "FACET sourceid ORDER BY COUNT(*) DESC " +
                    // "FACET ownerid ORDER BY COUNT(*) DESC " +
                    // "FACET licenseid ORDER BY COUNT(*) DESC; " +
                    "SHOW META;";

            // SELECT *, WEIGHT() FROM codesearchrealtime WHERE match('import test java') AND languageid IN (77) FACET languageid ORDER BY COUNT(*) DESC FACET sourceid ORDER BY COUNT(*) DESC; SHOW META;
            stmt = connection.prepareStatement(searchQuery);
            stmt.setString(1, queryString);
            stmt.setInt(2, start);

            var isResultSet = stmt.execute();

            // TODO sourcecode needs to hold the connection till we are done
            if (isResultSet) {
                resultSet = stmt.getResultSet();

                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    var sourceCodeDTO = this.sourceCode.getById(id);
                    sourceCodeDTO.ifPresent(sourceCodeDTO1 -> codeResultList.add(this.sourceCodeDTOtoCodeResult(sourceCodeDTO1)));
                }

                isResultSet = stmt.getMoreResults();
            }

            // Repository Facets
            if (isResultSet) {
                resultSet = stmt.getResultSet();

                while (resultSet.next()) {
                    codeFacetRepository.add(new CodeFacetRepo(resultSet.getString("repoid"), this.helpers.tryParseInt(resultSet.getString("count(*)"), "0")));
                }

                isResultSet = stmt.getMoreResults();
            }

            // Language Facets
            if (isResultSet) {
                resultSet = stmt.getResultSet();

                while (resultSet.next()) {
                    codeFacetLanguages.add(new CodeFacetLanguage(resultSet.getString("languageid"), this.helpers.tryParseInt(resultSet.getString("count(*)"), "0")));
                }

                isResultSet = stmt.getMoreResults();
            }

            // Meta results produce all sorts of interesting information about the search but the main one is the total number of results
            if (isResultSet) {
                resultSet = stmt.getResultSet();

                while (resultSet.next()) {
                    if ("total".equals(resultSet.getString("Variable_name"))) {
                        numTotalHits = this.helpers.tryParseInt(resultSet.getString("Value"), "0");
                    }
                }
            }

        } catch (SQLException ex) {
            this.logger.severe(String.format("c0ed0920::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        } finally {
            this.helpers.closeQuietly(resultSet);
            this.helpers.closeQuietly(stmt);
        }

        int noPages = numTotalHits / this.PAGE_LIMIT;
        List<Integer> pages = this.calculatePages(numTotalHits, noPages);

        codeFacetLanguages = this.transformLanguageType(codeFacetLanguages);

        return new SearchResult(numTotalHits, page, queryString, codeResultList, pages, codeFacetLanguages, codeFacetRepository, new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Given the list of language facet id's from Sphinx convert them into a string
     * which matches the name EG 23 -> Ruby
     */
    public ArrayList<CodeFacetLanguage> transformLanguageType(List<CodeFacetLanguage> codeFacetLanguages) {
        var properCodeFacetLanguages = new ArrayList<CodeFacetLanguage>();

        var languageNamesByIds = this.languageType.getLanguageNamesByIds(
                codeFacetLanguages.stream().map(x -> x.languageName).collect(Collectors.toList())
        );

        for (var codeFacetLanguage : codeFacetLanguages) {
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
        var codeResult = new CodeResult(Arrays.asList(sourceCodeDTO.content.split(Values.ALL_NEWLINE)), null);
        codeResult.setFilePath(sourceCodeDTO.location);

        codeResult.setCodePath(sourceCodeDTO.location);
        codeResult.setFileName(sourceCodeDTO.filename);
        codeResult.setLanguageName(sourceCodeDTO.languageName + Values.EMPTYSTRING);
        codeResult.setMd5hash(sourceCodeDTO.hash);
//        codeResult.setCodeLines(sourceCodeDTO.getLinescount() + Values.EMPTYSTRING);
        codeResult.setDocumentId(sourceCodeDTO.id);
        codeResult.setRepoName(sourceCodeDTO.repoId + Values.EMPTYSTRING);
        codeResult.setRepoLocation(sourceCodeDTO.location + Values.EMPTYSTRING);
//        codeResult.setCodeOwner(sourceCodeDTO.getOwnerid() + Values.EMPTYSTRING);
        codeResult.setCodeId(sourceCodeDTO.id + Values.EMPTYSTRING);
        codeResult.setDisplayLocation(sourceCodeDTO.location);

        return codeResult;
    }

    /**
     * Takes in the facts map which contains types to ids and for languages (lan)
     * looks up the id that is in the database so that we can tell sphinx which facets to apply
     */
    public String getLanguageFacets(HashMap<String, String[]> facets) {
        if (facets == null) {
            return Values.EMPTYSTRING;
        }

        // TODO replace this with single get query for all because it would be much faster
        // TODO or cache the results in the language type? Probably not a bad idea...
        var languageFacets = Arrays.stream(facets.getOrDefault("lan", new String[0]))
                .map((s) -> this.languageType.getByType(s).orElse(null))
                .filter(Objects::nonNull)
                .map(LanguageTypeDTO::getId)
                .map(x -> Integer.toString(x))
                .collect(Collectors.toList());

        var languageFacetsString = Values.EMPTYSTRING;

        if (!languageFacets.isEmpty()) {
            languageFacetsString = "AND languageid IN (" + String.join(",", languageFacets) + ")";
        }

        return languageFacetsString;
    }

    /**
     * Using sphinxrt index means we need to work out which shards on which
     * host we need to add documents to
     */
    public int getShardCount(String sphinxShards) {
        var count = 0;
        var serverShards = sphinxShards.split(";");

        for (var shard : serverShards) {
            var servers = shard.split(":");

            if (servers.length == 2) {
                var shards = servers[1].split(",");
                count += shards.length;
            }
        }

        return count;
    }
}
