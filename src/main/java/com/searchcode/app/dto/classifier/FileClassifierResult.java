/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.dto.classifier;

public class FileClassifierResult {
    public String[] extensions;
    public String[] extensionfile;
    public String[] line_comment;
    public String[] complexitychecks;
    public String[][] multi_line;
    public Quote[] quotes;
    public boolean nestedmultiline;
    public String[] keywords; // Used to identify languages that share extensions
    public String[] filenames;

    public FileClassifierResult(String extensions) {
        this.extensions = extensions.toLowerCase().split(",");
    }

    public FileClassifierResult(String extensions, String keywords) {
        this.extensions = extensions.toLowerCase().split(",");
        this.keywords = keywords.toLowerCase().split(",");
    }
}
