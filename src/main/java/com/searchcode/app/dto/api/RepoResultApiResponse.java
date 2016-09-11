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
