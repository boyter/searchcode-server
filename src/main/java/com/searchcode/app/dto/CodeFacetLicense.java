package com.searchcode.app.dto;

import com.searchcode.app.config.Values;
import com.searchcode.app.service.Singleton;

public class CodeFacetLicense {
    public String license = Values.EMPTYSTRING;
    public int count = 0;
    public boolean selected = false;

    public CodeFacetLicense(String license, int count) {
        this.license = license;
        this.count = count;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String source) {
        this.license = source;
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

    public String getLicenseNameSearchable() {
        return Singleton.getHelpers().replaceForIndex(this.license.toLowerCase());
    }
}
