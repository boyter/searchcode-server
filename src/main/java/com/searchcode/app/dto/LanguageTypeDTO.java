package com.searchcode.app.dto;

public class LanguageTypeDTO {
    private final int id;
    private final String type;

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
