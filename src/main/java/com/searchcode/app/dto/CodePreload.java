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
