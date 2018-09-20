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

public class FileClassifierResult {
    public String[] extensions;
    public String[] line_comment;
    public String[] complexitychecks;
    public String[][] multi_line;
    public String[][] quotes;
    public boolean nestedmultiline;

    public FileClassifierResult(String extensions) {
        this.extensions = extensions.toLowerCase().split(",");
    }
}
