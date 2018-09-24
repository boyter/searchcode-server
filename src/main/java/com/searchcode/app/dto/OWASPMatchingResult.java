/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.dto;

import java.util.ArrayList;

public class OWASPMatchingResult {
    private String name;
    private String desc;
    private String type;
    private ArrayList<Integer> matchingLines = new ArrayList<>();

    public OWASPMatchingResult(String name, String desc, String type, int matchingLine) {
        this.setName(name);
        this.setDesc(desc);
        this.setType(type);
        this.matchingLines.add(matchingLine);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<Integer> getMatchingLines() {
        return matchingLines;
    }

    public void addMatchingLine(int matchingLine) {
        this.matchingLines.add(matchingLine);
    }
}
