package com.searchcode.app.dto;

import com.searchcode.app.config.Values;

public class LanguageTypeDTO {
    private final int id;
    private final String type;

    public LanguageTypeDTO() {
        this.id = 0;
        this.type = Values.EMPTYSTRING;
    }

    public LanguageTypeDTO(int id, String type) {
        this.id = id;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
