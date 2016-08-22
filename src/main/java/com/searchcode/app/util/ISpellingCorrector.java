/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 */

package com.searchcode.app.util;


public interface ISpellingCorrector {
    void putWord(String word);
    String correct(String word);
    boolean containsWord(String word);
}
