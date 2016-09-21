/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.1
 */

package com.searchcode.app.dto;

import java.util.List;

public class CodePreload {
    private String query;
    private int page;
    private List<String> languageFacets;
    private List<String> repositoryFacets;
    private List<String> ownerFacets;

    public CodePreload(String query, int page, List<String> languageFacets, List<String> repositoryFacets, List<String> ownerFacets) {
        this.setQuery(query);
        this.setPage(page);
        this.setLanguageFacets(languageFacets);
        this.setRepositoryFacets(repositoryFacets);
        this.setOwnerFacets(ownerFacets);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<String> getLanguageFacets() {
        return languageFacets;
    }

    public void setLanguageFacets(List<String> languageFacets) {
        this.languageFacets = languageFacets;
    }

    public List<String> getRepositoryFacets() {
        return repositoryFacets;
    }

    public void setRepositoryFacets(List<String> repositoryFacets) {
        this.repositoryFacets = repositoryFacets;
    }

    public List<String> getOwnerFacets() {
        return ownerFacets;
    }

    public void setOwnerFacets(List<String> ownerFacets) {
        this.ownerFacets = ownerFacets;
    }
}
