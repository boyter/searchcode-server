package com.searchcode.app.service.index;

import com.searchcode.app.config.SphinxSearchConfig;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.IRepo;
import com.searchcode.app.dao.LanguageType;
import com.searchcode.app.dao.SourceCode;
import com.searchcode.app.dto.*;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.CacheSingleton;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.SearchCodeLib;
import org.apache.lucene.document.Document;
import org.cache2k.Cache;

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
    private final IRepo repo;
    private final com.searchcode.app.dao.Source source;
    private final Cache<String, SearchResult> cache;

    public SphinxIndexService() {
        this(Singleton.getLanguageType(), Singleton.getRepo(), Singleton.getSource(), CacheSingleton.getSearchResultCache());
    }

    public SphinxIndexService(LanguageType languageType, IRepo repo, com.searchcode.app.dao.Source source, Cache<String, SearchResult> cache) {
        super();
        this.languageType = languageType;
        this.repo = repo;
        this.source = source;
        this.cache = cache;

        this.helpers = Singleton.getHelpers();
        this.sphinxSearchConfig = new SphinxSearchConfig();
        this.sourceCode = Singleton.getSourceCode();
        this.searchcodeLib = Singleton.getSearchCodeLib();
    }

    @Override
    public void indexDocument(CodeIndexDocument codeIndexDocument) throws IOException {
    }

    @Override
    public void indexDocument(Queue<CodeIndexDocument> documentQueue) throws IOException {

        PreparedStatement preparedStatement = null;
        HashMap<String, Optional<Connection>> connections = null;

        try {
            connections = this.sphinxSearchConfig.getAllConnection();
        } catch (SQLException ex) {
            this.logger.severe(String.format("90cf00ef::error in class %s exception %s unable to connect to sphinx", ex.getClass(), ex.getMessage()));
            return;
        }

        // Pull out documents from the queue ready to be indexed
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
                    var shard = (codeResult.getId() % this.sphinxSearchConfig.getShardCount()) + 1;
                    preparedStatement = connections.get(Integer.toString(shard)).get()
                            .prepareStatement(String.format("REPLACE INTO codesearchrt%s VALUES(?,?,?,?,?,?,?,?,?,?)", shard));

                    var indexContents = this.indexContentPipeline(codeResult);

                    preparedStatement.setInt(1, codeResult.getId());
                    preparedStatement.setString(2, indexContents);
                    preparedStatement.setString(3, codeResult.getFileName());
                    preparedStatement.setString(4, this.helpers.replaceForIndex(codeResult.getDisplayLocation()));
                    preparedStatement.setInt(5, codeResult.getRepoNameId()); // RepoId
                    preparedStatement.setInt(6, codeResult.getLanguageNameId()); // LanguageId
                    preparedStatement.setInt(7, codeResult.getSourceId()); // SourceId
                    preparedStatement.setInt(8, 1); // OwnerId
                    preparedStatement.setInt(9, 1); // LicenseId
                    preparedStatement.setInt(10, codeResult.getLines());
                    preparedStatement.execute();
                } catch (SQLException ex) {
                    this.logger.severe(String.format("893321b2::error in class %s exception %s", ex.getClass(), ex.getMessage()));
                }
            }
        } finally {
            this.helpers.closeQuietly(preparedStatement);
            this.helpers.closeQuietly(connections);
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
    public int getCodeIndexLinesCount() {
        return 0;
    }

    @Override
    public void setCodeIndexLinesCount(int value) {

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
                    .setCommentLines(Integer.toString(x.comment))
                    .setCodeId(Integer.toString(x.id));
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
        var codeFacetSource = new ArrayList<CodeFacetSource>();
        var numTotalHits = 0;

        var start = this.PAGE_LIMIT * page;

        try {
            var connectionOptional = this.sphinxSearchConfig.getDefaultConnection();
            connection = connectionOptional.orElseThrow(() -> new IOException("Unable to connect to sphinx"));

            var searchQuery = " SELECT id FROM codesearchrealtime WHERE MATCH(?) " +
                    this.getLanguageFacets(facets) +
                    this.getRepoFacets(facets) +
                    this.getSourceFacets(facets) +
                    " LIMIT ?, 20 " +
                    " FACET repoid ORDER BY COUNT(*) DESC " +
                    " FACET languageid ORDER BY COUNT(*) DESC" +
                    " FACET sourceid ORDER BY COUNT(*) DESC;" +
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

            // Source Facets
            if (isResultSet) {
                resultSet = stmt.getResultSet();

                while (resultSet.next()) {
                    codeFacetSource.add(new CodeFacetSource(resultSet.getString("sourceid"), this.helpers.tryParseInt(resultSet.getString("count(*)"), "0")));
                }

                isResultSet = stmt.getMoreResults();
            }

            // Meta results produce all sorts of interesting information about the search but the main one is the total number of results
            if (isResultSet) {
                resultSet = stmt.getResultSet();

                while (resultSet.next()) {
                    if ("total_found".equals(resultSet.getString("Variable_name"))) {
                        numTotalHits = this.helpers.tryParseInt(resultSet.getString("Value"), "0");
                    }
                }
            }

        } catch (SQLException | IOException ex) {
            this.logger.severe(String.format("c0ed0920::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        } finally {
            this.helpers.closeQuietly(resultSet);
            this.helpers.closeQuietly(stmt);
        }

        int noPages = numTotalHits / this.PAGE_LIMIT;
        List<Integer> pages = this.calculatePages(numTotalHits, noPages);

        codeFacetLanguages = this.transformLanguageType(codeFacetLanguages);
        codeFacetRepository = this.transformRepositoryType(codeFacetRepository);
        codeFacetSource = this.transformSourceType(codeFacetSource);

        // Need to convert

        var searchResult = new SearchResult(numTotalHits, page, queryString, codeResultList, pages, codeFacetLanguages, codeFacetRepository, new ArrayList<>(), codeFacetSource);

        return searchResult;
    }

    /**
     * Given the list of language facet id's from Sphinx convert them into a string
     * which matches the name EG 23 -> Ruby
     */
    public ArrayList<CodeFacetLanguage> transformLanguageType(ArrayList<CodeFacetLanguage> codeFacetLanguages) {
        for (var codeFacetLanguage : codeFacetLanguages) {
            var byId = this.languageType.getById(Integer.parseInt(codeFacetLanguage.languageName));
            byId.ifPresent(x -> {
                codeFacetLanguage.languageName = x.getType();
            });
        }

        return codeFacetLanguages;
    }

    /**
     * Given the list of repository facet id's from Sphinx convert them back so we
     * known which repository they actually are based on the name EG 763 -> github.com/boyter/scc/
     */
    public ArrayList<CodeFacetRepo> transformRepositoryType(ArrayList<CodeFacetRepo> codeFacetRepos) {
        for (var codeFacetRepo : codeFacetRepos) {
            var byId = this.repo.getRepoById(Integer.parseInt(codeFacetRepo.repoName));
            byId.ifPresent(x -> {
                codeFacetRepo.repoName = x.getName();
            });
        }

        return codeFacetRepos;
    }

    /**
     * Given the list of repository facet id's from Sphinx convert them back so we
     * known which repository they actually are based on the name EG 763 -> github.com/boyter/scc/
     */
    public ArrayList<CodeFacetSource> transformSourceType(ArrayList<CodeFacetSource> codeFacetSources) {
        for (var source : codeFacetSources) {
            var byId = this.source.getSourceById(Integer.parseInt(source.source));
            byId.ifPresent(x -> {
                source.source = x.name;
            });
        }

        return codeFacetSources;
    }

    public CodeResult sourceCodeDTOtoCodeResult(SourceCodeDTO sourceCodeDTO) {
        var codeResult = new CodeResult(Arrays.asList(sourceCodeDTO.content.split(Values.ALL_NEWLINE)), null);
        codeResult.setFilePath(sourceCodeDTO.location);

        codeResult.setCodePath(sourceCodeDTO.location);
        codeResult.setFileName(sourceCodeDTO.filename);
        codeResult.setLanguageName(this.languageType.getById(sourceCodeDTO.languageName).get().getType());
        codeResult.setMd5hash(sourceCodeDTO.hash);
        codeResult.setCodeLines(Integer.toString(sourceCodeDTO.linesCount));
        codeResult.setDocumentId(sourceCodeDTO.id);
        codeResult.setRepoName(this.repo.getRepoById(sourceCodeDTO.repoId).get().getName());
        codeResult.setRepoId(sourceCodeDTO.repoId);
        codeResult.setRepoLocation(sourceCodeDTO.location + Values.EMPTYSTRING);
        codeResult.setCodeId(sourceCodeDTO.id + Values.EMPTYSTRING);
        codeResult.setDisplayLocation(sourceCodeDTO.location);

        return codeResult;
    }

    /**
     * Takes in the facts map which contains types to ids and for languages (lan)
     * looks up the id that is in the database so that we can tell sphinx which facets to apply
     */
    public String getLanguageFacets(HashMap<String, String[]> facets) {
        if (facets == null || !facets.containsKey("lan")) {
            return Values.EMPTYSTRING;
        }

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

    public String getRepoFacets(HashMap<String, String[]> facets) {
        if (facets == null || !facets.containsKey("repo")) {
            return Values.EMPTYSTRING;
        }

        var repoFacets = Arrays.stream(facets.getOrDefault("repo", new String[0]))
                .map((s) -> this.repo.getRepoByName(s).orElse(null))
                .filter(Objects::nonNull)
                .map(x -> Integer.toString(x.getRowId()))
                .collect(Collectors.toList());

        var repoFacetsString = Values.EMPTYSTRING;

        if (!repoFacets.isEmpty()) {
            repoFacetsString = "AND repoid IN (" + String.join(",", repoFacets) + ")";
        }

        return repoFacetsString;
    }

    public String getSourceFacets(HashMap<String, String[]> facets) {
        if (facets == null || !facets.containsKey("source")) {
            return Values.EMPTYSTRING;
        }

        var repoFacets = Arrays.stream(facets.getOrDefault("source", new String[0]))
                .map((s) -> this.source.getSourceByName(s).orElse(null))
                .filter(Objects::nonNull)
                .map(x -> Integer.toString(x.id))
                .collect(Collectors.toList());

        var sourceFacetsString = Values.EMPTYSTRING;

        if (!repoFacets.isEmpty()) {
            sourceFacetsString = "AND sourceid IN (" + String.join(",", repoFacets) + ")";
        }

        return sourceFacetsString;
    }
}
