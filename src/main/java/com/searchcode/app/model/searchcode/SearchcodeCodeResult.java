package com.searchcode.app.model.searchcode;

public class SearchcodeCodeResult {
    private int id;
    private int repoid;
    private int filetypeid;
    private int langugeid;
    private int sourceid;
    private String content;
    private String filename;
    private int linescount;

    public SearchcodeCodeResult(int id, int repoid, int filetypeid, int langugeid, int sourceid, String content, String filename, int linescount) {
        setId(id);
        setRepoid(repoid);
        setFiletypeid(filetypeid);
        setLangugeid(langugeid);
        setSourceid(sourceid);
        setContent(content);
        setFilename(filename);
        setLinescount(linescount);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRepoid() {
        return repoid;
    }

    public void setRepoid(int repoid) {
        this.repoid = repoid;
    }

    public int getFiletypeid() {
        return filetypeid;
    }

    public void setFiletypeid(int filetypeid) {
        this.filetypeid = filetypeid;
    }

    public int getLangugeid() {
        return langugeid;
    }

    public void setLangugeid(int langugeid) {
        this.langugeid = langugeid;
    }

    public int getSourceid() {
        return sourceid;
    }

    public void setSourceid(int sourceid) {
        this.sourceid = sourceid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getLinescount() {
        return linescount;
    }

    public void setLinescount(int linescount) {
        this.linescount = linescount;
    }
}
