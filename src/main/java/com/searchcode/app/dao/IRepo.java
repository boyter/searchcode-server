package com.searchcode.app.dao;

import com.searchcode.app.model.RepoResult;

import java.util.List;
import java.util.Optional;

public interface IRepo {
    boolean saveRepo(RepoResult repoResult);
    void deleteRepoByName(String repositoryName);
    Optional<RepoResult> getRepoByUrl(String repositoryUrl);
    Optional<RepoResult> getRepoByName(String repositoryName);
    Optional<RepoResult> getRepoById(int repoId);
    int getRepoCount();
    List<RepoResult> getPagedRepo(int offset, int pageSize);
    List<RepoResult> searchRepo(String searchTerms);
    List<RepoResult> getAllRepo();
}
