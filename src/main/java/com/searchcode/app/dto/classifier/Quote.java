package com.searchcode.app.dto.classifier;

public class Quote {
    public String start;
    public String end;
    public boolean ignoreescape;
    public boolean docstring;

    public Quote(String start, String end, boolean ignoreescape, boolean docstring) {
        this.start = start;
        this.end = end;
        this.ignoreescape = ignoreescape;
        this.docstring = docstring;
    }
}
