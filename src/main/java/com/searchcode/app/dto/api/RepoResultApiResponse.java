/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.14
 */

package com.searchcode.app.dto.api;


import com.searchcode.app.model.RepoResult;

import java.util.List;

public class RepoResultApiResponse extends ApiResponse {

    private List<RepoResult> repoResultList;

    public RepoResultApiResponse(boolean sucessful, String message, List<RepoResult> repoResultList) {
        this.setSucessful(sucessful);
        this.setMessage(message);
        this.setRepoResultList(repoResultList);
    }


    public List<RepoResult> getRepoResultList() {
        return repoResultList;
    }

    public void setRepoResultList(List<RepoResult> repoResultList) {
        this.repoResultList = repoResultList;
    }
}
