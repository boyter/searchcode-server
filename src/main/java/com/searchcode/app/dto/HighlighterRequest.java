package com.searchcode.app.dto;

public class HighlighterRequest {
    public String languageName;
    public String fileName;
    public String style;
    public String content;
    public boolean withLineNumbers;

    public HighlighterRequest setContent(String content) {
        this.content = content;
        return this;
    }

    public HighlighterRequest setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public HighlighterRequest setStyle(String style) {
        this.style = style;
        return this;
    }
}
