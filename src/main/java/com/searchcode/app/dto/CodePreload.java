/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.14
 */

package com.searchcode.app.dto;

import java.util.List;

public class CodePreload {
    private final boolean isLiteral;
    private final String query;
    private final int page;
    private final List<String> languageFacets;
    private final List<String> repositoryFacets;
    private final List<String> ownerFacets;
    private final List<String> srcFacets;
    private final String pathValue;

    public CodePreload(String query, int page, List<String> languageFacets, List<String> repositoryFacets, List<String> ownerFacets, List<String> srcFacets, String pathValue, boolean isLiteral) {
        this.query = query;
        this.page = page;
        this.languageFacets = languageFacets;
        this.repositoryFacets = repositoryFacets;
        this.ownerFacets = ownerFacets;
        this.srcFacets = srcFacets;
        this.pathValue = pathValue;
        this.isLiteral = isLiteral;
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

    public boolean isLiteral() {
        return isLiteral;
    }
}
