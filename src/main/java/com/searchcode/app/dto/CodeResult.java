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

// Holds a result after a search with full text
// and metadata
public class CodeResult {
    public List<String> code = new ArrayList<>();
    public List<CodeMatchResult> matchingResults = new ArrayList<>();
    public String filePath = Values.EMPTYSTRING;
    public String codePath = Values.EMPTYSTRING;
    public String fileName = Values.EMPTYSTRING;
    public String fileLocation = Values.EMPTYSTRING;
    public String md5hash = Values.EMPTYSTRING;
    public String languageName = Values.EMPTYSTRING;
    public String lines = Values.EMPTYSTRING;
    public String codeLines = Values.EMPTYSTRING;
    public String blankLines = Values.EMPTYSTRING;
    public String commentLines = Values.EMPTYSTRING;
    public String complexity = Values.EMPTYSTRING;
    public int documentId = 0;
    public String repoName = Values.EMPTYSTRING;
    public String repoLocation = Values.EMPTYSTRING;
    public String codeOwner = Values.EMPTYSTRING;
    public String revision = Values.EMPTYSTRING;
    public String yearMonthDay = Values.EMPTYSTRING;
    public String deleted = Values.EMPTYSTRING;
    public String message = Values.EMPTYSTRING;
    public String codeId = Values.EMPTYSTRING;
    public String displayLocation = Values.EMPTYSTRING;
    public String source = Values.EMPTYSTRING;

    public CodeResult(){}

    public CodeResult(List<String> code, List<CodeMatchResult>matchingResults) {
        this.setCode(code);
        this.setMatchingResults(matchingResults);
    }

    public List<String> getCode() {
        return code;
    }

    public CodeResult setCode(List<String> code) {
        this.code = code;
        return this;
    }

    public List<CodeMatchResult> getMatchingResults() {
        return matchingResults;
    }

    public CodeResult setMatchingResults(List<CodeMatchResult> matchingResults) {
        this.matchingResults = matchingResults;
        return this;
    }

    public String getCodePath() {
        return codePath;
    }

    public CodeResult setCodePath(String codePath) {
        this.codePath = codePath;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public CodeResult setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public CodeResult setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
        return this;
    }

    public String getMd5hash() {
        return md5hash;
    }

    public CodeResult setMd5hash(String md5hash) {
        this.md5hash = md5hash;
        return this;
    }

    public String getLanguageName() {
        return languageName;
    }

    public CodeResult setLanguageName(String languageName) {
        this.languageName = languageName;
        return this;
    }

    public String getLines() {
        return lines;
    }

    public CodeResult setLines(String lines) {
        this.lines = lines;
        return this;
    }

    public String getCodeLines() {
        return codeLines;
    }

    public CodeResult setCodeLines(String codeLines) {
        this.codeLines = codeLines;
        return this;
    }

    public String getCommentLines() {
        return commentLines;
    }

    public CodeResult setCommentLines(String commentLines) {
        this.commentLines = commentLines;
        return this;
    }

    public String getBlankLines() {
        return blankLines;
    }

    public CodeResult setBlankLines(String blankLines) {
        this.blankLines = blankLines;
        return this;
    }

    public String getComplexity() {
        return complexity;
    }

    public CodeResult setComplexity(String complexity) {
        this.complexity = complexity;
        return this;
    }

    public int getDocumentId() {
        return documentId;
    }

    public CodeResult setDocumentId(int documentId) {
        this.documentId = documentId;
        return this;
    }

    public String getRepoName() {
        return repoName;
    }

    public CodeResult setRepoName(String repoName) {
        this.repoName = repoName;
        return this;
    }

    public String getRepoLocation() {
        return repoLocation;
    }

    public CodeResult setRepoLocation(String repoLocation) {
        this.repoLocation = repoLocation;
        return this;
    }

    public String getCodeOwner() {
        return codeOwner;
    }

    public CodeResult setCodeOwner(String codeOwner) {
        this.codeOwner = codeOwner;
        return this;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getYearMonthDay() {
        return yearMonthDay;
    }

    public void setYearMonthDay(String yearMonthDay) {
        this.yearMonthDay = yearMonthDay;
    }

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCodeId() {
        return codeId;
    }

    public CodeResult setCodeId(String codeId) {
        this.codeId = codeId;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public CodeResult setFilePath(String path) {
        this.filePath = path;
        return this;
    }

    public String getDisplayLocation() {
        return displayLocation;
    }

    public CodeResult setDisplayLocation(String displayLocation) {
        this.displayLocation = displayLocation;
        return this;
    }

    public String getSource() {
        return source;
    }

    public CodeResult setSource(String source) {
        this.source = source;
        return this;
    }
}
