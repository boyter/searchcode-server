/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.3
 */

package com.searchcode.app.dto;

import org.apache.commons.codec.digest.DigestUtils;

public class CodeIndexDocument {
    private String repoLocationRepoNameLocationFilename;
    private String repoName;
    private String fileName;
    private String fileLocation;
    private String fileLocationFilename;
    private String md5hash;
    private String languageName;
    private int codeLines;
    private String contents;
    private String repoRemoteLocation;
    private String codeOwner;
    private String revision;
    private String yearMonthDay;
    private String yearMonth;
    private String year;
    private String message;
    private String deleted; // Used for time filter to know when this entry was removed

    public CodeIndexDocument() {}

    public CodeIndexDocument(String repoLocationRepoNameLocationFilename, String repoName, String fileName, String fileLocation, String fileLocationFilename, String md5hash, String languageName, int codeLines, String contents, String repoRemoteLocation, String codeOwner) {
        setRepoLocationRepoNameLocationFilename(repoLocationRepoNameLocationFilename);
        setRepoName(repoName);
        setFileName(fileName);
        setFileLocation(fileLocation);
        setFileLocationFilename(fileLocationFilename);
        setMd5hash(md5hash);
        setLanguageName(languageName);
        setCodeLines(codeLines);
        setContents(contents);
        setRepoRemoteLocation(repoRemoteLocation);
        setCodeOwner(codeOwner);
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

    public void setRepoLocationRepoNameLocationFilename(String repoLocationRepoNameLocationFilename) {
        this.repoLocationRepoNameLocationFilename = repoLocationRepoNameLocationFilename.replace("//", "/");
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String getFileLocationFilename() {
        return fileLocationFilename;
    }

    public void setFileLocationFilename(String fileLocationFilename) {
        this.fileLocationFilename = fileLocationFilename;
    }

    public String getMd5hash() {
        return md5hash;
    }

    public void setMd5hash(String md5hash) {
        this.md5hash = md5hash;
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public int getCodeLines() {
        return codeLines;
    }

    public void setCodeLines(int codeLines) {
        this.codeLines = codeLines;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getRepoRemoteLocation() {
        return repoRemoteLocation;
    }

    public void setRepoRemoteLocation(String repoRemoteLocation) {
        this.repoRemoteLocation = repoRemoteLocation;
    }

    public String getCodeOwner() {
        return codeOwner;
    }

    public void setCodeOwner(String codeOwner) {
        this.codeOwner = codeOwner;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String isDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
