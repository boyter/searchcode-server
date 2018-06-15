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

import com.searchcode.app.config.Values;

import java.time.Instant;

public class RepoData {
    public int averageIndexTimeSeconds = 0;
    public String indexStatus = Values.EMPTYSTRING;
    public Instant jobRunTime = Instant.parse("1800-01-01T00:00:00.000Z");

    public String source = Values.EMPTYSTRING;
    public String user = Values.EMPTYSTRING;
    public String project = Values.EMPTYSTRING;

    public String indexError = Values.EMPTYSTRING;

    public RepoData() {}

    public String getSource() {
        return this.source;
    }

    public String getUser() {
        return this.user;
    }

    public String getProject() {
        return this.project;
    }

    public String getIndexError() {
        return this.indexError;
    }
}
