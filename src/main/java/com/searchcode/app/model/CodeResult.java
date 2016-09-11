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
