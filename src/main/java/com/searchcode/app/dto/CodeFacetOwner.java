/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.14
 */

package com.searchcode.app.dto;

import com.searchcode.app.config.Values;
import com.searchcode.app.service.Singleton;

public class CodeFacetOwner {
    public String owner = Values.EMPTYSTRING;
    public int count = 0;
    public boolean selected = false;

    public CodeFacetOwner(String owner, int count) {
        this.setOwner(owner);
        this.setCount(count);
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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

    public String getOwnerSearchable() {
        return Singleton.getHelpers().replaceForIndex(this.owner.toLowerCase());
    }
}
