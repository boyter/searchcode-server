/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 */

package com.searchcode.app.model;

public class OWASPResult {
    public String name;
    public String desc;
    public String type;
    public String lang;

    public OWASPResult() {}

    public OWASPResult(String name, String desc, String type, String lang) {
        this.name = name;
        this.desc = desc;
        this.type = type;
        this.lang = lang;
    }
}
