/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.8
 */

package com.searchcode.app.util.classifier;


public class Classifier {
    public String language = null;
    public String[] extensions = {};
    public String[] keywords = {};

    public Classifier(String language, String extensions, String keywords) {
        this.language = language;
        this.extensions = extensions.toLowerCase().split(",");
        this.keywords = keywords.toLowerCase().split(",");
    }
}
