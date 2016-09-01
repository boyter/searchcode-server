package com.searchcode.app.dto;

public class OWASPMatchingResult {
    public String name;
    public String desc;
    public String type;
    public int matchingLine;

    public OWASPMatchingResult(String name, String desc, String type, int matchingLine) {
        this.name = name;
        this.desc = desc;
        this.type = type;
        this.matchingLine = matchingLine;
    }
}
