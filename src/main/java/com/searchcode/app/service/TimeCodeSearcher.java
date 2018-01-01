/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 */

package com.searchcode.app.service;

import com.google.common.collect.Lists;
import com.searchcode.app.config.Values;
import com.searchcode.app.dto.*;
import com.searchcode.app.util.CodeAnalyzer;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Does all of the queries which happen against the Lucene index, including search queries and working out
 * how many documents have been indexed.
 */
public class TimeCodeSearcher {

    public String INDEXPATH = Properties.getProperties().getProperty(Values.TIMEINDEXLOCATION, Values.DEFAULTTIMEINDEXLOCATION);
    public String CODEFIELD = Values.CONTENTS;
    public int PAGELIMIT = 20;

    private static final LoggerWrapper LOGGER = Singleton.getLogger();

    private StatsService statsService = new StatsService();
    private GitService gitService = new GitService();

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

            searchResult = this.doPagingSearch(reader, searcher, query, page);
            reader.close();
        }
        catch(Exception ex) {
            LOGGER.warning(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        return searchResult;
    }

    /**
     * Attempts to find a unique file given the repository name and the path/filename however
     * it seems to randomly not find things for some files. No idea of the root cause at this point and have implemented
     * a work around where we get the file by getById which is no ideal. The bug appears to be due to some issue
     * inside lucene itself as using raw queries to pull back the file results in no matches, and yet it does appear
     * when not limiting to the repo
     * TODO investigate the lucene issue that occurs here mentioned above
     * TODO needs to use the revision number here as well to get the right value
     */
    public CodeResult getByRepoFileName(String repo, String fileName) {
        CodeResult codeResult = null;

        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(this.INDEXPATH)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new CodeAnalyzer();
            QueryParser parser = new QueryParser(CODEFIELD, analyzer);

            // TODO I have a feeling this may not be unique if there are to files in the same directory with different case... something to investigate
            Query query = parser.parse(Values.FILELOCATIONFILENAME + ":" + QueryParser.escape(repo + "/" + fileName));
            Singleton.getLogger().info("Query to get by filename = " + Values.FILELOCATIONFILENAME + ":" + QueryParser.escape(repo + "/" + fileName));

            TopDocs results = searcher.search(query, 1);
            ScoreDoc[] hits = results.scoreDocs;

            if (hits.length != 0) {
                Document doc = searcher.doc(hits[0].doc);

                String filepath = doc.get(Values.PATH);

                List<String> code = new ArrayList<>();
                try {
                    code = Files.readAllLines(Paths.get(filepath), StandardCharsets.UTF_8);
                    code = Singleton.getHelpers().readFileLinesGuessEncoding(filepath, Singleton.getHelpers().tryParseInt(Properties.getProperties().getProperty(Values.MAXFILELINEDEPTH, Values.DEFAULTMAXFILELINEDEPTH), Values.DEFAULTMAXFILELINEDEPTH));
                } catch (Exception ex) {
                    Singleton.getLogger().info("Indexed file appears to binary: " + filepath);
                }

                codeResult = new CodeResult(code, null);
                codeResult.setCodePath(doc.get(Values.FILELOCATIONFILENAME));
                codeResult.setFileName(doc.get(Values.FILENAME));
                codeResult.setLanguageName(doc.get(Values.LANGUAGENAME));
                codeResult.setMd5hash(doc.get(Values.MD5HASH));
                codeResult.setCodeLines(doc.get(Values.CODELINES));
                codeResult.setDocumentId(hits[0].doc);
                codeResult.setRepoName(doc.get(Values.REPONAME));
                codeResult.setRepoLocation(doc.get(Values.REPOLOCATION));
                codeResult.setCodeOwner(doc.get(Values.CODEOWNER));
            }

            reader.close();

        }
        catch(Exception ex) {
            LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        return codeResult;
    }

    /**
     * Only used as fallback if getByRepoFileName fails for some reason due to what appears to be a lucene index bug
     * Using this is problematic because if the index is updated while this method is called it will possibly
     * return the incorrect result. We could add a shared lock between them both but that's hardly ideal especially
     * since when its called the index could already be updated
     */
    public CodeResult getById(int documentId) {
        CodeResult codeResult = null;

        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(this.INDEXPATH)));
            Document doc = reader.document(documentId);

            String filepath = doc.get(Values.PATH);

            List<String> code = new ArrayList<>();
            try {
                code = Files.readAllLines(Paths.get(filepath), StandardCharsets.UTF_8);
            }
            catch(Exception ex) {
                LOGGER.warning("Indexed file appears to binary: " + filepath);
            }

            codeResult = new CodeResult(code, null);
            codeResult.setCodePath(doc.get(Values.FILELOCATIONFILENAME));
            codeResult.setFileName(doc.get(Values.FILENAME));
            codeResult.setLanguageName(doc.get(Values.LANGUAGENAME));
            codeResult.setMd5hash(doc.get(Values.MD5HASH));
            codeResult.setCodeLines(doc.get(Values.CODELINES));
            codeResult.setDocumentId(documentId);
            codeResult.setRepoName(doc.get(Values.REPONAME));
            codeResult.setRepoLocation(doc.get(Values.REPOLOCATION));
            codeResult.setCodeOwner(doc.get(Values.CODEOWNER));

            reader.close();
        }
        catch(Exception ex) {
            LOGGER.warning(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        return codeResult;
    }

    public List<String> getRepoDocuments(String repoName) {
        List<String> fileLocations = new ArrayList<>();
        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(this.INDEXPATH)));
            IndexSearcher searcher = new IndexSearcher(reader);

            Analyzer analyzer = new CodeAnalyzer();
            QueryParser parser = new QueryParser(CODEFIELD, analyzer);
            Query query = parser.parse(Values.REPONAME + ":" + repoName);

            TopDocs results = searcher.search(query, Integer.MAX_VALUE);
            ScoreDoc[] hits = results.scoreDocs;

            for (int i = 0; i < hits.length; i++) {
                Document doc = searcher.doc(hits[i].doc);
                fileLocations.add(doc.get(Values.FILELOCATIONFILENAME));
            }

            reader.close();
        }
        catch(Exception ex) {
            LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
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
            noPages = 20;
        }

        List<Integer> pages = new ArrayList<>();
        for(int i=0; i< noPages; i++) {
            pages.add(i);
        }

        List<CodeResult> codeResults = new ArrayList<>();

        for (int i = start; i < end; i++) {
            Document doc = searcher.doc(hits[i].doc);

            String filepath = doc.get(Values.PATH);

            if (filepath != null) {
                // This line is occasionally useful for debugging ranking, but not useful enough to have as log info
                //System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score);

                CodeResult cr = new CodeResult(null, null);
                cr.setCodePath(doc.get(Values.FILELOCATIONFILENAME));
                cr.setFileName(doc.get(Values.FILENAME));
                cr.setLanguageName(doc.get(Values.LANGUAGENAME));
                cr.setMd5hash(doc.get(Values.MD5HASH));
                cr.setCodeLines(doc.get(Values.CODELINES));
                cr.setDocumentId(hits[i].doc);
                cr.setRepoLocation(doc.get(Values.REPOLOCATION));
                cr.setRepoName(doc.get(Values.REPONAME));
                cr.setCodeOwner(doc.get(Values.CODEOWNER));
                cr.setRevision(doc.get(Values.REVISION));
                cr.setYearMonthDay(doc.get(Values.DATEYEARMONTHDAY));
                cr.setMessage(doc.get(Values.MESSAGE));
                cr.setDeleted(doc.get(Values.DELETED));

                try {
                    // This should probably be limited by however deep we are meant to look into the file
                    // or the value we use here whichever is less
                    String repoLoc = "./repo/" + cr.getRepoName() + "/.git";
                    cr.setCode(Arrays.asList(gitService.fetchFileRevision(repoLoc, cr.getRevision(), cr.getCodePath()).split("\\r?\\n")));
                }
                catch(Exception ex) {
                    LOGGER.warning("Indexed file appears to binary or missing: " + filepath);
                }


                codeResults.add(cr);
            } else {
                LOGGER.warning((i + 1) + ". " + "No path for this document");
            }
        }

        List<CodeFacetLanguage> codeFacetLanguages = this.getLanguageFacetResults(searcher, reader, query);
        List<CodeFacetRepo> repoFacetLanguages = this.getRepoFacetResults(searcher, reader, query);
        List<CodeFacetOwner> repoFacetOwner= this.getOwnerFacetResults(searcher, reader, query);
        List<CodeFacetYearMonthDay> repoFacetYearMonthDay = this.getYearMonthDayFacetResults(searcher, reader, query);
        List<CodeFacetYearMonth> repoFacetYearMonth = this.getYearMonthFacetResults(searcher, reader, query);
        List<CodeFacetYear> repoFacetYear = this.getYearFacetResults(searcher, reader, query);
        List<CodeFacetRevision> repoFacetRevision = this.getRevisionFacetResults(searcher, reader, query);
        List<CodeFacetDeleted> repoFacetDeleted = this.getDeletedFacetResults(searcher, reader, query);

        SearchResult searchResult = new SearchResult(numTotalHits, page, query.toString(), codeResults, pages, codeFacetLanguages, repoFacetLanguages, repoFacetOwner, null);

        searchResult.setRepoFacetYearMonthDay(repoFacetYearMonthDay);
        searchResult.setRepoFacetYearMonth(repoFacetYearMonth);
        searchResult.setRepoFacetYear(repoFacetYear);
        searchResult.setRepoFacetRevision(repoFacetRevision);
        searchResult.setRepoFacetDeleted(repoFacetDeleted);

        return searchResult;
    }

    /**
     * Returns the matching revision facets for a given query
     */
    private List<CodeFacetDeleted> getDeletedFacetResults(IndexSearcher searcher, IndexReader reader, Query query) {
        List<CodeFacetDeleted> deletedFacets = new ArrayList<>();

        try {
            SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(reader, Values.DELETED);
            FacetsCollector fc = new FacetsCollector();
            FacetsCollector.search(searcher, query, 10, fc);
            Facets facets = new SortedSetDocValuesFacetCounts(state, fc);
            FacetResult result = facets.getTopChildren(200, Values.DELETED);

            if (result != null) {
                int stepThru = result.childCount > 200 ? 200 : result.childCount;

                for (int i = 0; i < stepThru; i++) {
                    LabelAndValue lv = result.labelValues[i];

                    if (lv != null && lv.value != null) {
                        deletedFacets.add(new CodeFacetDeleted(lv.label, lv.value.intValue()));
                    }
                }
            }
        }
        catch(IOException ex) {
            LOGGER.warning(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        catch(Exception ex) {
            LOGGER.warning(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        return deletedFacets;
    }

    /**
     * Returns the matching revision facets for a given query
     */
    private List<CodeFacetRevision> getRevisionFacetResults(IndexSearcher searcher, IndexReader reader, Query query) {
        List<CodeFacetRevision> revisionFacets = new ArrayList<>();

        try {
            SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(reader, Values.REVISION);
            FacetsCollector fc = new FacetsCollector();
            FacetsCollector.search(searcher, query, 10, fc);
            Facets facets = new SortedSetDocValuesFacetCounts(state, fc);
            FacetResult result = facets.getTopChildren(200, Values.REVISION);

            if (result != null) {
                int stepThru = result.childCount > 200 ? 200 : result.childCount;

                for (int i = 0; i < stepThru; i++) {
                    LabelAndValue lv = result.labelValues[i];

                    if (lv != null && lv.value != null) {
                        revisionFacets.add(new CodeFacetRevision(lv.label, lv.value.intValue()));
                    }
                }
            }
        }
        catch(IOException ex) {
            LOGGER.warning(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        catch(Exception ex) {
            LOGGER.warning(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        return revisionFacets;
    }

    /**
     * Returns the matching yearmonth facets for a given query
     */
    private List<CodeFacetYear> getYearFacetResults(IndexSearcher searcher, IndexReader reader, Query query) {
        List<CodeFacetYear> codeFacetYear = new ArrayList<>();

        try {
            SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(reader, Values.DATEYEAR);
            FacetsCollector fc = new FacetsCollector();
            FacetsCollector.search(searcher, query, 10, fc);
            Facets facets = new SortedSetDocValuesFacetCounts(state, fc);
            FacetResult result = facets.getTopChildren(200, Values.DATEYEAR);

            if (result != null) {
                int stepThru = result.childCount > 200 ? 200 : result.childCount;

                for (int i = 0; i < stepThru; i++) {
                    LabelAndValue lv = result.labelValues[i];

                    if (lv != null && lv.value != null) {
                        codeFacetYear.add(new CodeFacetYear(lv.label, lv.value.intValue()));
                    }
                }
            }
        }
        catch(IOException ex) {
            LOGGER.warning(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        catch(Exception ex) {
            LOGGER.warning(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        return codeFacetYear;
    }

    /**
     * Returns the matching yearmonth facets for a given query
     */
    private List<CodeFacetYearMonth> getYearMonthFacetResults(IndexSearcher searcher, IndexReader reader, Query query) {
        List<CodeFacetYearMonth> codeFacetYearMonth = new ArrayList<>();

        try {
            SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(reader, Values.DATEYEARMONTH);
            FacetsCollector fc = new FacetsCollector();
            FacetsCollector.search(searcher, query, 10, fc);
            Facets facets = new SortedSetDocValuesFacetCounts(state, fc);
            FacetResult result = facets.getTopChildren(200, Values.DATEYEARMONTH);

            if (result != null) {
                int stepThru = result.childCount > 200 ? 200 : result.childCount;

                for (int i = 0; i < stepThru; i++) {
                    LabelAndValue lv = result.labelValues[i];

                    if (lv != null && lv.value != null) {
                        codeFacetYearMonth.add(new CodeFacetYearMonth(lv.label, lv.value.intValue()));
                    }
                }
            }
        }
        catch(IOException ex) {
            LOGGER.warning(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        catch(Exception ex) {
            LOGGER.warning(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        return codeFacetYearMonth;
    }

    /**
     * Returns the matching yearmonthday facets for a given query
     */
    private List<CodeFacetYearMonthDay> getYearMonthDayFacetResults(IndexSearcher searcher, IndexReader reader, Query query) {
        List<CodeFacetYearMonthDay> codeFacetYearMonthDay = new ArrayList<>();

        try {
            SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(reader, Values.DATEYEARMONTHDAY);
            FacetsCollector fc = new FacetsCollector();
            FacetsCollector.search(searcher, query, 10, fc);
            Facets facets = new SortedSetDocValuesFacetCounts(state, fc);
            FacetResult result = facets.getTopChildren(200, Values.DATEYEARMONTHDAY);

            if (result != null) {
                int stepThru = result.childCount > 200 ? 200 : result.childCount;

                for (int i = 0; i < stepThru; i++) {
                    LabelAndValue lv = result.labelValues[i];

                    if (lv != null && lv.value != null) {
                        codeFacetYearMonthDay.add(new CodeFacetYearMonthDay(lv.label, lv.value.intValue()));
                    }
                }
            }
        }
        catch(IOException ex) {
            LOGGER.warning(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        catch(Exception ex) {
            LOGGER.warning(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        return codeFacetYearMonthDay;
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
            LOGGER.warning(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        catch(Exception ex) {
            LOGGER.warning(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
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
            LOGGER.warning(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        catch(Exception ex) {
            LOGGER.warning(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
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
            LOGGER.warning(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        catch(Exception ex) {
            LOGGER.warning(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        return codeFacetRepo;
    }
}
