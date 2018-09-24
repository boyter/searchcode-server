/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.dto;

import java.util.ArrayList;
import java.util.List;

public class RepositoryChanged {
    private boolean changed;
    private List<String> changedFiles;
    private List<String> deletedFiles;
    private boolean clone;

    public RepositoryChanged(boolean changed) {
        this.clone = false;
        this.changed = changed;
        this.changedFiles = new ArrayList<>();
        this.deletedFiles = new ArrayList<>();
    }

    public RepositoryChanged(boolean changed, List<String> changedFiles, List<String> deletedFiles) {
        this.clone = false;
        this.changed = changed;
        this.changedFiles = changedFiles;
        this.deletedFiles = deletedFiles;
    }

    public boolean isChanged() {
        return changed;
    }

    public List<String> getChangedFiles() {
        return changedFiles;
    }

    public List<String> getDeletedFiles() {
        return deletedFiles;
    }

    public boolean isClone() {
        return clone;
    }

    public void setClone(boolean clone) {
        this.clone = clone;
    }
}
