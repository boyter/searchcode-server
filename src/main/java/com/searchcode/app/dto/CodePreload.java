/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.11
 */

package com.searchcode.app.dto;

import java.util.List;

public class CodePreload {
    private String query;
    private int page;
    private List<String> languageFacets;
    private List<String> repositoryFacets;
    private List<String> ownerFacets;
    private String pathValue;

    public CodePreload(String query, int page, List<String> languageFacets, List<String> repositoryFacets, List<String> ownerFacets, String pathValue) {
        this.query = query;
        this.page = page;
        this.languageFacets = languageFacets;
        this.repositoryFacets = repositoryFacets;
        this.ownerFacets = ownerFacets;
        this.pathValue = pathValue;
    }

    public String getQuery() {
        return query;
    }

    public int getPage() {
        return page;
    }

    public List<String> getLanguageFacets() {
        return languageFacets;
    }

    public List<String> getRepositoryFacets() {
        return repositoryFacets;
    }

    public List<String> getOwnerFacets() {
        return ownerFacets;
    }


    public String getPathValue() {
        return pathValue;
    }
}
