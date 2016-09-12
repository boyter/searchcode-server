/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 */


package com.searchcode.app.dto;

import com.searchcode.app.config.Values;

public class CodeFacetDeleted {
    public String deleted = Values.EMPTYSTRING;
    public int count = 0;
    public boolean selected = false;

    public CodeFacetDeleted(String deleted, int count) {
        this.setDeleted(deleted);
        this.setCount(count);
    }

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String revision) {
        this.deleted = revision;
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
