/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 *
 * After the following date 27 August 2019 this software version '1.2.3' or '1.2.4' is dual licenced under the
 * Fair Source Licence included in the LICENSE.txt file or under the GNU General Public License Version 3 with terms
 * specified at https://www.gnu.org/licenses/gpl-3.0.txt
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
