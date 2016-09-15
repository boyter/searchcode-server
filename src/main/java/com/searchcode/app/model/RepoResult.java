/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.0
 */

package com.searchcode.app.model;

import com.searchcode.app.config.Values;

public class RepoResult {
    private int rowId;
    private String name = "";
    private String scm = "";
    private String url = "";
    private String username = "";
    private String password = "";
    private String source = "";
    private String branch = "";

    public RepoResult(int rowId, String name, String scm, String url, String username, String password, String source, String branch) {
        this.setRowId(rowId);
        this.setName(name);
        this.setScm(scm);
        this.setUrl(url);
        this.setUsername(username);
        this.setPassword(password);
        this.setSource(source);
        this.setBranch(branch);
    }

    /**
     * Required becuse we use this in the UniqueRepoQueue and in a few other places to ensure we don't
     * add multiples of the same repo
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof RepoResult ==  false) {
            return false;
        }

        RepoResult rr = (RepoResult)o;

        if (this.getName().equals(rr.getName())) {
            return true;
        }

        return false;
    }

    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        // should clean the name here
        name = name.replace(' ', '-');
        this.name = name;
    }

    public String getScm() {
        return scm;
    }

    public void setScm(String scm) {
        this.scm = scm;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null) {
            username = Values.EMPTYSTRING;
        }
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null) {
            password = Values.EMPTYSTRING;
        }
        this.password = password;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        if (source == null) {
            source = Values.EMPTYSTRING;
        }
        this.source = source;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s %s %s %s %s", this.getName(), this.getScm(), this.getUrl(), this.getUsername(), this.getPassword(), this.getSource(), this.getBranch());
    }
}
