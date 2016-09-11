/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 *
 * After the following date 27 August 2019 this software version '1.2.3' or '1.2.4' is dual licenced under the
 * Fair Source Licence included in the LICENSE.txt file or under the GNU General Public License Version 3 with terms
 * specified at https://www.gnu.org/licenses/gpl-3.0.txt
 */


package com.searchcode.app.dto;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {
    private int totalHits = 0;
    private int page = 0;
    private String query = "";
    private List<String> altQuery = new ArrayList<>();
    private List<CodeResult> codeResultList = new ArrayList<>();
    private List<Integer> pages = new ArrayList<>();
    private List<CodeFacetLanguage> languageFacetResults = new ArrayList<>();
    private List<CodeFacetRepo> repoFacetResults = new ArrayList<>();
    private List<CodeFacetOwner> repoOwnerResults = new ArrayList<>();

    public SearchResult() {}

    public SearchResult(int totalHits, int page, String query, List<CodeResult> codeResultList, List<Integer> pages, List<CodeFacetLanguage> languageFacetResults, List<CodeFacetRepo> repoFacetResults, List<CodeFacetOwner> repoOwnerResults) {
        this.setTotalHits(totalHits);
        this.setPage(page);
        this.setQuery(query);
        this.setCodeResultList(codeResultList);
        this.setPages(pages);
        this.setLanguageFacetResults(languageFacetResults);
        this.setRepoFacetResults(repoFacetResults);
        this.setOwnerFacetResults(repoOwnerResults);
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
}
