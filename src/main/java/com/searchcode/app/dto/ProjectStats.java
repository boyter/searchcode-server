/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.10
 */

package com.searchcode.app.dto;


import java.util.List;

public class ProjectStats {
    private List<CodeFacetLanguage> codeFacetLanguages;
    private List<CodeFacetOwner> repoFacetOwner;
    private int totalCodeLines;
    private int totalFiles;

    public ProjectStats(int totalCodeLines, int totalFiles, List<CodeFacetLanguage> codeFacetLanguages, List<CodeFacetOwner> repoFacetOwner) {
        this.setTotalCodeLines(totalCodeLines);
        this.setTotalFiles(totalFiles);
        this.setCodeFacetLanguages(codeFacetLanguages);
        this.setRepoFacetOwner(repoFacetOwner);
    }

    public List<CodeFacetLanguage> getCodeFacetLanguages() {
        return codeFacetLanguages;
    }

    public void setCodeFacetLanguages(List<CodeFacetLanguage> codeFacetLanguages) {
        this.codeFacetLanguages = codeFacetLanguages;
    }

    public List<CodeFacetOwner> getRepoFacetOwner() {
        return repoFacetOwner;
    }

    public void setRepoFacetOwner(List<CodeFacetOwner> repoFacetOwner) {
        this.repoFacetOwner = repoFacetOwner;
    }

    public int getTotalCodeLines() {
        return totalCodeLines;
    }

    public void setTotalCodeLines(int totalCodeLines) {
        this.totalCodeLines = totalCodeLines;
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }
}
