/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.dto;

import com.searchcode.app.config.Values;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {
    private int totalHits = 0;
    private int page = 0;
    private String query = Values.EMPTYSTRING;
    private List<String> altQuery = new ArrayList<>();
    private List<CodeResult> codeResultList = new ArrayList<>();
    private List<Integer> pages = new ArrayList<>();
    private List<CodeFacetLanguage> languageFacetResults = new ArrayList<>();
    private List<CodeFacetRepo> repoFacetResults = new ArrayList<>();
    private List<CodeFacetOwner> repoOwnerResults = new ArrayList<>();
    private List<CodeFacetSource> codeFacetSources = new ArrayList<>();
    private List<CodeFacetYearMonthDay> repoFacetYearMonthDay = new ArrayList<>();
    private List<CodeFacetYearMonth> repoFacetYearMonth = new ArrayList<>();
    private List<CodeFacetYear> repoFacetYear = new ArrayList<>();
    private List<CodeFacetRevision> repoFacetRevision = new ArrayList<>();
    private List<CodeFacetDeleted> repoFacetDeleted = new ArrayList<>();


    public SearchResult() {}

    public SearchResult(int totalHits, int page, String query, List<CodeResult> codeResultList, List<Integer> pages, List<CodeFacetLanguage> languageFacetResults, List<CodeFacetRepo> repoFacetResults, List<CodeFacetOwner> repoOwnerResults, List<CodeFacetSource> codeFacetSources) {
        this.setTotalHits(totalHits);
        this.setPage(page);
        this.setQuery(query);
        this.setCodeResultList(codeResultList);
        this.setPages(pages);
        this.setLanguageFacetResults(languageFacetResults);
        this.setRepoFacetResults(repoFacetResults);
        this.setOwnerFacetResults(repoOwnerResults);
        this.setCodeFacetSources(codeFacetSources);
    }

    public int getTotalHits() {
        return totalHits;
    }

    public void setTotalHits(int totalHits) {
        this.totalHits = totalHits;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<CodeResult> getCodeResultList() {
        return codeResultList;
    }

    public void setCodeResultList(List<CodeResult> codeResultList) {
        this.codeResultList = codeResultList;
    }

    public List<Integer> getPages() {
        return pages;
    }

    public void setPages(List<Integer> pages) {
        this.pages = pages;
    }

    public List<CodeFacetLanguage> getLanguageFacetResults() {
        return languageFacetResults;
    }

    public void setLanguageFacetResults(List<CodeFacetLanguage> languageFacetResults) {
        this.languageFacetResults = languageFacetResults;
    }

    public List<CodeFacetRepo> getRepoFacetResults() {
        return repoFacetResults;
    }

    public List<CodeFacetOwner> getRepoOwnerResults() {
        return repoOwnerResults;
    }

    public void setRepoFacetResults(List<CodeFacetRepo> repoFacetResults) {
        this.repoFacetResults = repoFacetResults;
    }

    public List<String> getAltQuery() {
        return altQuery;
    }

    public void addAltQuery(String altQuery) {
        this.altQuery.add(altQuery);
    }

    public List<CodeFacetOwner> getOwnerFacetResults() {
        return repoOwnerResults;
    }

    public void setOwnerFacetResults(List<CodeFacetOwner> repoOwnerResults) {
        this.repoOwnerResults = repoOwnerResults;
    }

    public List<CodeFacetYearMonthDay> getRepoFacetYearMonthDay() {
        return repoFacetYearMonthDay;
    }

    public void setRepoFacetYearMonthDay(List<CodeFacetYearMonthDay> repoFacetYearMonthDay) {
        this.repoFacetYearMonthDay = repoFacetYearMonthDay;
    }

    public List<CodeFacetYearMonth> getRepoFacetYearMonth() {
        return repoFacetYearMonth;
    }

    public void setRepoFacetYearMonth(List<CodeFacetYearMonth> repoFacetYearMonth) {
        this.repoFacetYearMonth = repoFacetYearMonth;
    }

    public List<CodeFacetYear> getRepoFacetYear() {
        return repoFacetYear;
    }

    public void setRepoFacetYear(List<CodeFacetYear> repoFacetYear) {
        this.repoFacetYear = repoFacetYear;
    }

    public List<CodeFacetRevision> getRepoFacetRevision() {
        return repoFacetRevision;
    }

    public void setRepoFacetRevision(List<CodeFacetRevision> repoFacetRevision) {
        this.repoFacetRevision = repoFacetRevision;
    }

    public List<CodeFacetDeleted> getRepoFacetDeleted() {
        return repoFacetDeleted;
    }

    public void setRepoFacetDeleted(List<CodeFacetDeleted> repoFacetDeleted) {
        this.repoFacetDeleted = repoFacetDeleted;
    }

    public List<CodeFacetSource> getCodeFacetSources() {
        return codeFacetSources;
    }

    public void setCodeFacetSources(List<CodeFacetSource> codeFacetSources) {
        this.codeFacetSources = codeFacetSources;
    }
}
