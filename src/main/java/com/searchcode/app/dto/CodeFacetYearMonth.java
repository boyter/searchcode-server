/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 */


package com.searchcode.app.dto;

import com.searchcode.app.config.Values;

public class CodeFacetYearMonth {
    public String languageName = Values.EMPTYSTRING;
    public int count = 0;
    public boolean selected = false;

    public CodeFacetYearMonth(String languageName, int count) {
        this.setYearMonth(languageName);
        this.setCount(count);
    }

    public String getYearMonth() {
        return languageName;
    }

    public void setYearMonth(String languageName) {
        this.languageName = languageName;
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
}
