/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.0
 */

package com.searchcode.app.dto;

import junit.framework.TestCase;

import static org.assertj.core.api.Assertions.assertThat;

public class CodeIndexDocumentTest extends TestCase {
    public void testMd5CalcA() {
        CodeIndexDocument cd = new CodeIndexDocument();
        cd.setRepoLocationRepoNameLocationFilename("a");
        String result = cd.getHash();
        assertThat(result).isEqualTo("0cc175b9c0f1b6a831c399e269772661");
    }

    public void testMd5CalcB() {
        CodeIndexDocument cd = new CodeIndexDocument();
        cd.setRepoLocationRepoNameLocationFilename("b");
        String result = cd.getHash();
        assertThat(result).isEqualTo("92eb5ffee6ae2fec3ad71c777531578f");
    }
}
