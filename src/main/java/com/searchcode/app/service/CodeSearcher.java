/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.10
 */

package com.searchcode.app.service;

import com.searchcode.app.config.Values;
import com.searchcode.app.dto.*;
import com.searchcode.app.util.*;
import com.searchcode.app.util.Properties;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Does all of the queries which happen against the Lucene index, including search queries and working out
 * how many documents have been indexed.
 */
public class CodeSearcher implements ICodeSearcher {

    public String INDEXPATH = Properties.getProperties().getProperty(Values.INDEXLOCATION, Values.DEFAULTINDEXLOCATION);
    public String CODEFIELD = Values.CONTENTS;
    public int PAGELIMIT = 20;

    private static final LoggerWrapper LOGGER = Singleton.getLogger();

    private StatsService statsService = new StatsService();

    /**
     * Returns the total number of documents that are present in the index at this time
     */
    public int getTotalNumberDocumentsIndexed() {
        int numDocs = 0;
        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(this.INDEXPATH)));

            numDocs = reader.numDocs();
            reader.close();
        }
        catch(Exception ex) {
            LOGGER.info(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        return numDocs;
    }

    /**
     * Given a query and what page of results we are on return the matching results for that search
     */
    public SearchResult search(String queryString, int page) {
        SearchResult searchResult = new SearchResult();
        statsService.incrementSearchCount();


        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(this.INDEXPATH)));
            IndexSearcher searcher = new IndexSearcher(reader);

            Analyzer analyzer = new CodeAnalyzer();

            QueryParser parser = new QueryParser(CODEFIELD, analyzer);

            Query query = parser.parse(queryString);
            LOGGER.info("Searching for: " + query.toString(CODEFIELD));
            LOGGER.searchLog(query.toString(CODEFIELD) + " " + page);

            searchResult = this.doPagingSearch(reader, searcher, query, page);
            reader.close();
        }
        catch(Exception ex) {
            LOGGER.warning(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        return searchResult;
    }

    /**
     * Only used as fallback if getByRepoFileName fails for some reason due to what appears to be a lucene index bug
     * this should always work as the path used is sha1 and should be unique for anything the current codebase can
     * deal with
     */
    public CodeResult getByCodeId(String codeId) {
        CodeResult codeResult = null;

        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(this.INDEXPATH)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new CodeAnalyzer();
            QueryParser parser = new QueryParser(CODEFIELD, analyzer);

            Query query = parser.parse(Values.CODEID + ":" + QueryParser.escape(codeId));
            Singleton.getLogger().info("Query to get by " + Values.CODEID + ":" + QueryParser.escape(codeId));

            TopDocs results = searcher.search(query, 1);
            ScoreDoc[] hits = results.scoreDocs;

            if (hits.length != 0) {
                Document doc = searcher.doc(hits[0].doc);

                String filepath = doc.get(Values.PATH);

                List<String> code = new ArrayList<>();
                try {
                    code = Singleton.getHelpers().readFileLinesGuessEncoding(filepath, Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.MAXFILELINEDEPTH, Values.DEFAULTMAXFILELINEDEPTH), Values.DEFAULTMAXFILELINEDEPTH));
                } catch (Exception ex) {
                    Singleton.getLogger().info("Indexed file appears to binary: " + filepath);
                }

                codeResult = new CodeResult(code, null);
                codeResult.setFilePath(filepath);
                codeResult.setCodePath(doc.get(Values.FILELOCATIONFILENAME));
                codeResult.setFileName(doc.get(Values.FILENAME));
                codeResult.setLanguageName(doc.get(Values.LANGUAGENAME));
                codeResult.setMd5hash(doc.get(Values.MD5HASH));
                codeResult.setCodeLines(doc.get(Values.CODELINES));
                codeResult.setDocumentId(hits[0].doc);
                codeResult.setRepoName(doc.get(Values.REPONAME));
                codeResult.setRepoLocation(doc.get(Values.REPOLOCATION));
                codeResult.setCodeOwner(doc.get(Values.CODEOWNER));
                codeResult.setCodeId(doc.get(Values.CODEID));
            }

            reader.close();
        }
        catch(Exception ex) {
            LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        return codeResult;
    }


    public ProjectStats getProjectStats(String repoName) {
        int totalCodeLines = 0;
        int totalFiles = 0;
        List<CodeFacetLanguage> codeFacetLanguages = new ArrayList<>();
        List<CodeFacetOwner> repoFacetOwners = new ArrayList<>();
        List<CodeFacetLanguage> codeByLines = new ArrayList<>();
        SearchcodeLib searchcodeLib = Singleton.getSearchCodeLib();

        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(this.INDEXPATH)));
            IndexSearcher searcher = new IndexSearcher(reader);

            Analyzer analyzer = new CodeAnalyzer();
            QueryParser parser = new QueryParser(CODEFIELD, analyzer);
            Query query = parser.parse(Values.REPONAME + ":" + repoName);

            TopDocs results = searcher.search(query, Integer.MAX_VALUE);
            ScoreDoc[] hits = results.scoreDocs;

            Map<String, Integer> linesCount = new HashMap<>();

            for (int i = 0; i < results.totalHits; i++) {
                Document doc = searcher.doc(hits[i].doc);

                if (!searchcodeLib.languageCostIgnore(doc.get(Values.LANGUAGENAME))) {
                    int lines = Singleton.getHelpers().tryParseInt(doc.get(Values.CODELINES), "0");
                    totalCodeLines += lines;
                    String languageName = doc.get(Values.LANGUAGENAME).replace("_", " ");

                    if (linesCount.containsKey(languageName)) {
                        linesCount.put(languageName, linesCount.get(languageName) + lines);
                    }
                    else {
                        linesCount.put(languageName, lines);
                    }
                }
            }

            for (String key: linesCount.keySet()) {
                codeByLines.add(new CodeFacetLanguage(key, linesCount.get(key)));
            }
            codeByLines.sort((a, b) -> b.getCount() - a.getCount());

            totalFiles = results.totalHits;
            codeFacetLanguages = this.getLanguageFacetResults(searcher, reader, query);
            repoFacetOwners = this.getOwnerFacetResults(searcher, reader, query);

            reader.close();
        }
        catch(Exception ex) {
            LOGGER.severe("CodeSearcher getProjectStats caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        return new ProjectStats(totalCodeLines, totalFiles, codeFacetLanguages, codeByLines, repoFacetOwners);
    }

    /**
     * Due to very large repositories (500,000 files) this needs to support
     * paging. Also need to consider the fact that is a list of strings
     * TODO maybe convert to hash so lookups are faster
     */
    public List<String> getRepoDocuments(String repoName, int page) {
        int REPOPAGELIMIT = 1000;
        List<String> fileLocations = new ArrayList<>(REPOPAGELIMIT);
        int start = REPOPAGELIMIT * page;

        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(this.INDEXPATH)));
            IndexSearcher searcher = new IndexSearcher(reader);

            Analyzer analyzer = new CodeAnalyzer();
            QueryParser parser = new QueryParser(CODEFIELD, analyzer);
            Query query = parser.parse(Values.REPONAME + ":" + repoName);

            TopDocs results = searcher.search(query, Integer.MAX_VALUE);
            int end = Math.min(results.totalHits, (REPOPAGELIMIT * (page + 1)));
            ScoreDoc[] hits = results.scoreDocs;

            for (int i = start; i < end; i++) {
                Document doc = searcher.doc(hits[i].doc);
                fileLocations.add(doc.get(Values.PATH));
            }

            reader.close();
        }
        catch(Exception ex) {
            LOGGER.severe("CodeSearcher getRepoDocuments caught a " + ex.getClass() + " on page " + page + "\n with message: " + ex.getMessage());
        }

        return fileLocations;
    }

    /**
     * Only really used internally but does the heavy lifting of actually converting the index document on disk to the
     * format used internally including reading the file from disk.
     */
    public SearchResult doPagingSearch(IndexReader reader, IndexSearcher searcher, Query query, int page) throws IOException {
        TopDocs results = searcher.search(query, 20 * this.PAGELIMIT); // 20 pages worth of documents
        ScoreDoc[] hits = results.scoreDocs;

        int numTotalHits = results.totalHits;
        int start = this.PAGELIMIT * page;
        int end = Math.min(numTotalHits, (this.PAGELIMIT * (page + 1)));
        int noPages = numTotalHits / this.PAGELIMIT;

        if (noPages > 20) {
            noPages = 19;
        }

        List<Integer> pages = this.calculatePages(numTotalHits, noPages);

        List<CodeResult> codeResults = new ArrayList<>();

        for (int i = start; i < end; i++) {
            Document doc = searcher.doc(hits[i].doc);

            String filepath = doc.get(Values.PATH);

            if (filepath != null) {
                // This line is occasionally useful for debugging ranking, but not useful enough to have as log info
                //System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score);

                List<String> code = new ArrayList<>();
                try {
                    // This should probably be limited by however deep we are meant to look into the file
                    // or the value we use here whichever is less
                    code = Singleton.getHelpers().readFileLinesGuessEncoding(filepath, Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.MAXFILELINEDEPTH, Values.DEFAULTMAXFILELINEDEPTH), Values.DEFAULTMAXFILELINEDEPTH));
                }
                catch(Exception ex) {
                    LOGGER.warning("Indexed file appears to binary or missing: " + filepath);
                }

                CodeResult cr = new CodeResult(code, null);
                cr.setCodePath(doc.get(Values.FILELOCATIONFILENAME));
                cr.setFileName(doc.get(Values.FILENAME));
                cr.setLanguageName(doc.get(Values.LANGUAGENAME));
                cr.setMd5hash(doc.get(Values.MD5HASH));
                cr.setCodeLines(doc.get(Values.CODELINES));
                cr.setDocumentId(hits[i].doc);
                cr.setRepoLocation(doc.get(Values.REPOLOCATION));
                cr.setRepoName(doc.get(Values.REPONAME));
                cr.setCodeOwner(doc.get(Values.CODEOWNER));
                cr.setCodeId(doc.get(Values.CODEID));

                codeResults.add(cr);
            } else {
                LOGGER.warning((i + 1) + ". " + "No path for this document");
            }
        }

        List<CodeFacetLanguage> codeFacetLanguages = this.getLanguageFacetResults(searcher, reader, query);
        List<CodeFacetRepo> repoFacetLanguages = this.getRepoFacetResults(searcher, reader, query);
        List<CodeFacetOwner> repoFacetOwner= this.getOwnerFacetResults(searcher, reader, query);

        return new SearchResult(numTotalHits, page, query.toString(), codeResults, pages, codeFacetLanguages, repoFacetLanguages, repoFacetOwner);
    }

    public List<Integer> calculatePages(int numTotalHits, int noPages) {
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

    /**
     * Returns the matching language facets for a given query
     */
    private List<CodeFacetLanguage> getLanguageFacetResults(IndexSearcher searcher, IndexReader reader, Query query) {
        List<CodeFacetLanguage> codeFacetLanguages = new ArrayList<>();

        try {
            SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(reader, Values.LANGUAGENAME);
            FacetsCollector fc = new FacetsCollector();
            FacetsCollector.search(searcher, query, 10, fc);
            Facets facets = new SortedSetDocValuesFacetCounts(state, fc);
            FacetResult result = facets.getTopChildren(200, Values.LANGUAGENAME);

            if (result != null) {
                int stepThru = result.childCount > 200 ? 200 : result.childCount;

                for (int i = 0; i < stepThru; i++) {
                    LabelAndValue lv = result.labelValues[i];

                    if (lv != null && lv.value != null) {
                        codeFacetLanguages.add(new CodeFacetLanguage(lv.label, lv.value.intValue()));
                    }
                }
            }
        }
        catch(IOException ex) {
        }
        catch(Exception ex) {
        }

        return codeFacetLanguages;
    }

    /**
     * Returns the matching repository facets for a given query
     */
    private List<CodeFacetRepo> getRepoFacetResults(IndexSearcher searcher, IndexReader reader, Query query) {
        List<CodeFacetRepo> codeFacetRepo = new ArrayList<>();

        try {
            SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(reader, Values.REPONAME);
            FacetsCollector fc = new FacetsCollector();
            FacetsCollector.search(searcher, query, 10, fc);
            Facets facets = new SortedSetDocValuesFacetCounts(state, fc);
            FacetResult result = facets.getTopChildren(200, Values.REPONAME);

            if (result != null) {
                int stepThru = result.childCount > 200 ? 200 : result.childCount;

                for (int i = 0; i < stepThru; i++) {
                    LabelAndValue lv = result.labelValues[i];

                    if (lv != null && lv.value != null) {
                        codeFacetRepo.add(new CodeFacetRepo(lv.label, lv.value.intValue()));
                    }
                }
            }
        }
        catch(IOException ex) {
        }
        catch(Exception ex) {
        }

        return codeFacetRepo;
    }

    /**
     * Returns the matching owner facets for a given query
     */
    private List<CodeFacetOwner> getOwnerFacetResults(IndexSearcher searcher, IndexReader reader, Query query) {
        List<CodeFacetOwner> codeFacetRepo = new ArrayList<>();

        try {
            SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(reader, Values.CODEOWNER);
            FacetsCollector fc = new FacetsCollector();
            FacetsCollector.search(searcher, query, 10, fc);
            Facets facets = new SortedSetDocValuesFacetCounts(state, fc);
            FacetResult result = facets.getTopChildren(200, Values.CODEOWNER);

            if (result != null) {
                int stepThru = result.childCount > 200 ? 200 : result.childCount;

                for (int i = 0; i < stepThru; i++) {
                    LabelAndValue lv = result.labelValues[i];

                    if (lv != null && lv.value != null) {
                        codeFacetRepo.add(new CodeFacetOwner(lv.label, lv.value.intValue()));
                    }
                }
            }
        }
        catch(IOException ex) {
        }
        catch(Exception ex) {
        }

        return codeFacetRepo;
    }
}
