/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.10
 */

package com.searchcode.app.dao;

import com.searchcode.app.model.RepoResult;

import java.util.List;

public interface IRepo {
    List<RepoResult> getAllRepo();
    List<RepoResult> getPagedRepo(int offset, int pageSize);
    RepoResult getRepoByName(String repositoryName);
    RepoResult getRepoByUrl(String repositoryUrl);
    boolean saveRepo(RepoResult repoResult);
    void deleteRepoByName(String repositoryName);
    List<RepoResult> searchRepo(String searchTerms);
    int getRepoCount();
}
