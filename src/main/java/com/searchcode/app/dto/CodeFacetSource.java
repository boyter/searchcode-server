package com.searchcode.app.dto;

import com.searchcode.app.config.Values;
import com.searchcode.app.service.Singleton;

public class CodeFacetSource {
    public String source = Values.EMPTYSTRING;
    public int count = 0;
    public boolean selected = false;

    public CodeFacetSource(String source, int count) {
        this.source = source;
        this.count = count;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean getSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getLanguageNameSearchable() {
        return Singleton.getHelpers().replaceForIndex(this.source.toLowerCase());
    }
}
