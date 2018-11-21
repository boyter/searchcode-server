package com.searchcode.app.model;

import com.searchcode.app.config.Values;

public class SourceResult {
    public int id = -1;
    public String name = Values.EMPTYSTRING;

    public SourceResult setId(int id) {
        this.id = id;
        return this;
    }

    public SourceResult setName(String name) {
        this.name = name;
        return this;
    }
}
