/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 *
 * After the following date 27 August 2019 this software version '1.2.3' or '1.2.4' is dual licenced under the
 * Fair Source Licence included in the LICENSE.txt file or under the GNU General Public License Version 3 with terms
 * specified at https://www.gnu.org/licenses/gpl-3.0.txt
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
