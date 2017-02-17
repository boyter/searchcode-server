/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.8
 */

package com.searchcode.app.dto;


import java.time.Instant;

public class RepoData {

    public String indexStatus;
    public int averageIndexTimeSeconds;
    public int currentIndexTimeSeconds;
    public Instant lastJobStartInstant;


    public RepoData() {}
}
