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

public class CodeFacetLanguage {
    public String languageName = Values.EMPTYSTRING;
    public int count = 0;
    public boolean selected = false;

    public CodeFacetLanguage(String languageName, int count) {
        this.setLanguageName(languageName);
        this.setCount(count);
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
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

    public String getLanguageNameSearchable() {
        return Singleton.getHelpers().replaceForIndex(this.languageName.toLowerCase());
    }
}
