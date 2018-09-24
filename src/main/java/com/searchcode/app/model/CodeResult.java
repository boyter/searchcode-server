/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.model;

public class CodeResult {
    public String repoName;
    public String location;
    public String filename;
    public String repoNameLocationFilename;
    public String hash;
    public String languageName;
    public int lines;

    public CodeResult(String repoName, String location, String filename, String repoNameLocationFilename, String hash, String languageName, int lines)
    {
        this.repoName = repoName;
        this.location = location;
        this.filename = filename;
        this.repoNameLocationFilename = repoNameLocationFilename;
        this.hash = hash;
        this.languageName = languageName;
        this.lines = lines;
    }
}
