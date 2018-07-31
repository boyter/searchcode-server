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

import junit.framework.TestCase;

import static org.assertj.core.api.Assertions.assertThat;

public class CodeIndexDocumentTest extends TestCase {
    public void testMd5CalcA() {
        CodeIndexDocument cd = new CodeIndexDocument();
        cd.setRepoLocationRepoNameLocationFilename("a");
        String result = cd.getHash();
        assertThat(result).isEqualTo("86f7e437faa5a7fce15d1ddcb9eaeaea377667b8");
    }

    public void testMd5CalcB() {
        CodeIndexDocument cd = new CodeIndexDocument();
        cd.setRepoLocationRepoNameLocationFilename("b");
        String result = cd.getHash();
        assertThat(result).isEqualTo("e9d71f5ee7c92d6dc9e92ffdad17b8bd49418f98");
    }

    public void testSetRepoLocationReplacement() {
        CodeIndexDocument cd = new CodeIndexDocument();

        cd.setRepoLocationRepoNameLocationFilename(".//this/");
        assertThat(cd.getRepoLocationRepoNameLocationFilename()).isEqualTo("./this/");
    }

    public void testTesty() {
        CodeIndexDocument cd = new CodeIndexDocument()
                                    .setContents("something")
                                    .setRepoRemoteLocation("something");

        assertThat(cd.getContents()).isEqualTo("something");
        assertThat(cd.getRepoRemoteLocation()).isEqualTo("something");
    }
}
