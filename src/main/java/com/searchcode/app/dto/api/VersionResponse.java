package com.searchcode.app.dto.api;

public class VersionResponse {
    private String version;

    public VersionResponse(){}

    public VersionResponse setVersion(String version) {
        this.version = version;
        return this;
    }
}
