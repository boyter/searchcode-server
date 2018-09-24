package com.searchcode.app.dto;

import com.google.gson.Gson;

public class SourceCodeDTO {
    private int id;
    private int repoid;
    private int languageid;
    private String languagename;
    private int sourceid;
    private int ownerid;
    private int licenseid;
    private String location;
    private String filename;
    private String content;
    private String hash;
    private String simhash;
    private int linescount;
    private SourceCodeDTOData sourceCodeData;

    public SourceCodeDTO(int id, int repoid, int languageid, String languagename, int sourceid, int ownerid, int licenseid, String location, String filename, String content, String hash, String simhash, int linescount, String data) {
        Gson gson = new Gson();

        this.id = id;
        this.repoid = repoid;
        this.languageid = languageid;
        this.languagename = languagename;
        this.sourceid = sourceid;
        this.ownerid = ownerid;
        this.licenseid = licenseid;
        this.location = location;
        this.filename = filename;
        this.content = content;
        this.hash = hash;
        this.simhash = simhash;
        this.linescount = linescount;
        this.sourceCodeData = gson.fromJson(data, SourceCodeDTOData.class);
    }

    public int getId() {
        return id;
    }

    public int getRepoid() {
        return repoid;
    }

    public int getLanguageid() {
        return languageid;
    }

    public String getLanguagename() {
        return languagename;
    }

    public int getSourceid() {
        return sourceid;
    }

    public int getOwnerid() {
        return ownerid;
    }

    public int getLicenseid() {
        return licenseid;
    }

    public String getLocation() {
        return location;
    }

    public String getFilename() {
        return filename;
    }

    public String getContent() {
        return content;
    }

    public String getHash() {
        return hash;
    }

    public String getSimhash() {
        return simhash;
    }

    public int getLinescount() {
        return linescount;
    }

    public SourceCodeDTOData getSourceCodeData() {
        return sourceCodeData;
    }
}
