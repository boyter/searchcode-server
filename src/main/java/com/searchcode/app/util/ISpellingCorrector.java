/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.util;


import java.util.List;

public interface ISpellingCorrector {
    int getWordCount();
    void putWord(String word);
    String correct(String word);
    boolean containsWord(String word);
    boolean reset();
    List<String> getSampleWords(int count);
}
