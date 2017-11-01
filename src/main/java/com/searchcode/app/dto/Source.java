package com.searchcode.app.dto;

public class Source {
    private String name;
    private String url;
    private String link;

    public Source(String name, String url, String link) {
        this.name = name;
        this.url = url;
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getLink() {
        return link;
    }
}
