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


public class CodeOwner {

    private int noLines;
    private String name;
    private int mostRecentUnixCommitTimestamp;

    public CodeOwner(String name, int noLines, int mostRecentUnixCommitTimestamp) {
        this.setName(name);
        this.setNoLines(noLines);
        this.setMostRecentUnixCommitTimestamp(mostRecentUnixCommitTimestamp);
    }

    public void incrementLines() {
        this.noLines++;
    }

    public int getNoLines() {
        return noLines;
    }

    public void setNoLines(int noLines) {
        this.noLines = noLines;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMostRecentUnixCommitTimestamp() {
        return mostRecentUnixCommitTimestamp;
    }

    public void setMostRecentUnixCommitTimestamp(int mostRecentUnixCommitTimestamp) {
        this.mostRecentUnixCommitTimestamp = mostRecentUnixCommitTimestamp;
    }
}
