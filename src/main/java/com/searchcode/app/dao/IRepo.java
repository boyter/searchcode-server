/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 */


package com.searchcode.app.dao;

import com.searchcode.app.model.RepoResult;

import java.util.List;

public interface IRepo {
    List<RepoResult> getAllRepo();
    List<RepoResult> getPagedRepo(int offset, int pageSize);
    RepoResult getRepoByName(String repositoryName);
    boolean saveRepo(RepoResult repoResult);
    void deleteRepoByName(String repositoryName);
}
