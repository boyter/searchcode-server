/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.13
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
    private String schash;
    private String displayLocation; // What we actually should use for UI
    private String source;

    public CodeIndexDocument() {}

    public CodeIndexDocument(String repoLocationRepoNameLocationFilename, String repoName, String fileName, String fileLocation, String fileLocationFilename, String md5hash, String languageName, int codeLines, String contents, String repoRemoteLocation, String codeOwner, String displayLocation, String source) {
        this.setRepoLocationRepoNameLocationFilename(repoLocationRepoNameLocationFilename);
        this.setRepoName(repoName);
        this.setFileName(fileName);
        this.setFileLocation(fileLocation);
        this.setFileLocationFilename(fileLocationFilename);
        this.setMd5hash(md5hash);
        this.setLanguageName(languageName);
        this.setCodeLines(codeLines);
        this.setContents(contents);
        this.setRepoRemoteLocation(repoRemoteLocation);
        this.setCodeOwner(codeOwner);
        this.schash = Values.EMPTYSTRING;
        this.displayLocation = displayLocation;
        this.source = source;
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

    public String getSchash() {
        return schash;
    }

    public void setSchash(String schash) {
        this.schash = schash;
    }

    public String getDisplayLocation() {
        return displayLocation;
    }

    public String getSource() {
        return source;
    }
}
