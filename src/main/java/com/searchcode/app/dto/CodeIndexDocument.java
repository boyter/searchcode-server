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
import org.apache.commons.codec.digest.DigestUtils;

public class CodeIndexDocument {
    private String repoLocationRepoNameLocationFilename; // Primary key and full path to file relative to where application is installed
    private String repoName;
    private String fileName;
    private String fileLocation; // Path to file relative to repo location
    private String fileLocationFilename;
    private String md5hash;
    private String languageName;
    private int lines; // How many lines in the file
    private int codeLines; // How many lines are code
    private int commentLines; // How many lines are comments
    private int blankLines; // How many lines are blank
    private int complexity; // Complexity calculation taken from scc
    private String contents;
    private String repoRemoteLocation;
    private String codeOwner;
    private String schash;
    private String displayLocation; // Actually used on the UI display
    private String source;

    // The below are used to support historical searches which is not currently used
    private String revision;
    private String yearMonthDay;
    private String yearMonth;
    private String year;
    private String message;
    private String deleted; // Used for time filter to know when this entry was removed

    public CodeIndexDocument() {
        this.schash = Values.EMPTYSTRING; // Needs to be set to avoid issues with indexing
    }

    /**
     * Used for identification for this specific file in the index
     */
    public String getHash() {
        return DigestUtils.sha1Hex(this.repoLocationRepoNameLocationFilename);
    }

    public String getRepoLocationRepoNameLocationFilename() {
        return repoLocationRepoNameLocationFilename;
    }

    public CodeIndexDocument setRepoLocationRepoNameLocationFilename(String repoLocationRepoNameLocationFilename) {
        // TODO move this out of here, lets keep these things as simple as possible
        this.repoLocationRepoNameLocationFilename = repoLocationRepoNameLocationFilename.replace("//", "/");
        return this;
    }

    public String getRepoName() {
        return repoName;
    }

    public CodeIndexDocument setRepoName(String repoName) {
        this.repoName = repoName;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public CodeIndexDocument setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public CodeIndexDocument setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
        return this;
    }

    public String getFileLocationFilename() {
        return fileLocationFilename;
    }

    public CodeIndexDocument setFileLocationFilename(String fileLocationFilename) {
        this.fileLocationFilename = fileLocationFilename;
        return this;
    }

    public String getMd5hash() {
        return md5hash;
    }

    public CodeIndexDocument setMd5hash(String md5hash) {
        this.md5hash = md5hash;
        return this;
    }

    public String getLanguageName() {
        return languageName;
    }

    public CodeIndexDocument setLanguageName(String languageName) {
        this.languageName = languageName;
        return this;
    }

    public int getLines() {
        return this.lines;
    }

    public CodeIndexDocument setLines(int lines) {
        this.lines = lines;
        return this;
    }

    public int getCodeLines() {
        return codeLines;
    }

    public CodeIndexDocument setCodeLines(int codeLines) {
        this.codeLines = codeLines;
        return this;
    }

    public int getBlankLines() {
        return this.blankLines;
    }

    public CodeIndexDocument setBlankLines(int blankLines) {
        this.blankLines = blankLines;
        return this;
    }

    public int getCommentLines() {
        return this.commentLines;
    }

    public CodeIndexDocument setCommentLines(int commentLines) {
        this.commentLines = commentLines;
        return this;
    }

    public int getComplexity() {
        return this.complexity;
    }

    public CodeIndexDocument setComplexity(int complexity) {
        this.complexity = complexity;
        return this;
    }

    public String getContents() {
        return contents;
    }

    public CodeIndexDocument setContents(String contents) {
        this.contents = contents;
        return this;
    }

    public String getRepoRemoteLocation() {
        return repoRemoteLocation;
    }

    public CodeIndexDocument setRepoRemoteLocation(String repoRemoteLocation) {
        this.repoRemoteLocation = repoRemoteLocation;
        return this;
    }

    public String getCodeOwner() {
        return codeOwner;
    }

    public CodeIndexDocument setCodeOwner(String codeOwner) {
        this.codeOwner = codeOwner;
        return this;
    }

    public String getRevision() {
        return revision;
    }

    public CodeIndexDocument setRevision(String revision) {
        this.revision = revision;
        return this;
    }

    public String getYearMonthDay() {
        return yearMonthDay;
    }

    public CodeIndexDocument setYearMonthDay(String yearMonthDay) {
        this.yearMonthDay = yearMonthDay;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public CodeIndexDocument setMessage(String message) {
        this.message = message;
        return this;
    }

    public String isDeleted() {
        return deleted;
    }

    public CodeIndexDocument setDeleted(String deleted) {
        this.deleted = deleted;
        return this;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public CodeIndexDocument setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
        return this;
    }

    public String getYear() {
        return year;
    }

    public CodeIndexDocument setYear(String year) {
        this.year = year;
        return this;
    }

    public String getSchash() {
        return schash;
    }

    public CodeIndexDocument setSchash(String schash) {
        this.schash = schash;
        return this;
    }

    public String getDisplayLocation() {
        return displayLocation;
    }

    public CodeIndexDocument setDisplayLocation(String displayLocation) {
        this.displayLocation = displayLocation;
        return this;
    }

    public String getSource() {
        return source;
    }

    public CodeIndexDocument setSource(String source) {
        this.source = source;
        return this;
    }
}
