package com.searchcode.app.dto;

/**
 * Represents a single record inside the searchcode.com database
 * NB not all fields are represented here as they are not required
 * they should be removed from the DB at some point
 */
public class SourceCodeDTO {

    public int id;
    public int repoId; // Foreign key to id of repo
    public int fileTypeId; // Foreign key to file type DEPRECATED
    public String location; // Location of the file in the repository
    public String filename; // Name of the file with extension
    public String content; // The content stored in a MySQL COMPRESS blob column
    public String hash; // MD5 hash of the content field
    public int languageName; // Foreign key to language type

    // The below are used for filters inside the DB itself but we can calculate them on the fly
    // cheaply so dont bother pulling them back from the DB most of the time
    public int blank; // Count of the number of blank lines
    public int comment; // Count of the number of comment lines
    public int code; // Count of the number of code lines
    public int estimatedCost; // COCOMO calculation figure
    public int linesCount; // Count of the number of lines in the file blank + comment + code

    public String repo; // Used to set the git location used by searchcode.com in its search results

    public SourceCodeDTO() {}

    public SourceCodeDTO setId(int id) {
        this.id = id;
        return this;
    }

    public SourceCodeDTO setRepoId(int repoId) {
        this.repoId = repoId;
        return this;
    }

    public SourceCodeDTO setFileTypeId(int fileTypeId) {
        this.fileTypeId = fileTypeId;
        return this;
    }

    public SourceCodeDTO setLocation(String location) {
        this.location = location;
        return this;
    }

    public SourceCodeDTO setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public SourceCodeDTO setContent(String content) {
        this.content = content;
        return this;
    }

    public SourceCodeDTO setHash(String hash) {
        this.hash = hash;
        return this;
    }

    public SourceCodeDTO setLanguageName(int languageName) {
        this.languageName = languageName;
        return this;
    }

    public SourceCodeDTO setBlank(int blank) {
        this.blank = blank;
        return this;
    }

    public SourceCodeDTO setComment(int comment) {
        this.comment = comment;
        return this;
    }

    public SourceCodeDTO setCode(int code) {
        this.code = code;
        return this;
    }

    public SourceCodeDTO setEstimatedCost(int estimatedCost) {
        this.estimatedCost = estimatedCost;
        return this;
    }

    public SourceCodeDTO setLinesCount(int linesCount) {
        this.linesCount = linesCount;
        return this;
    }

    public SourceCodeDTO setRepo(String repo) {
        this.repo = repo;
        return this;
    }
}
