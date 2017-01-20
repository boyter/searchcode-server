package com.searchcode.app.dto;


public class SearchcodeSearchResult {
    private int id;
    private String filename;
    private String location;
    private String content;
    private String hash;
    private String reponame;
    private String simhash;
    private int linescount;
    private String languagetype;
    private String sourcename;
    private String sourceurl;
    private String url;
    private int blankLines;
    private int commentLines;
    private String username;

    public SearchcodeSearchResult(int id, String filename, String location, String content, String hash, String reponame, String simhash, int linescount, String languagetype, String sourcename, String sourceurl, String url, int blankLines, int commentLines, String username) {
        this.id = id;
        this.filename = filename;
        this.location = location;
        this.content = content;
        this.hash = hash;
        this.reponame = reponame;
        this.simhash = simhash;
        this.linescount = linescount;
        this.languagetype = languagetype;
        this.sourcename = sourcename;
        this.sourceurl = sourceurl;
        this.url = url;
        this.blankLines = blankLines;
        this.commentLines = commentLines;
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public String getLocation() {
        return location;
    }

    public String getContent() {
        return content;
    }

    public String getHash() {
        return hash;
    }

    public String getReponame() {
        return reponame;
    }

    public String getSimhash() {
        return simhash;
    }

    public int getLinescount() {
        return linescount;
    }

    public String getLanguagetype() {
        return languagetype;
    }

    public String getSourcename() {
        return sourcename;
    }

    public String getSourceurl() {
        return sourceurl;
    }

    public String getUrl() {
        return url;
    }
}
