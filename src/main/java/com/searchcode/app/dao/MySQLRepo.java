package com.searchcode.app.dao;

import com.searchcode.app.model.RepoResult;

import java.util.List;
import java.util.Optional;

public class MySQLRepo implements IRepo {
    @Override
    public boolean saveRepo(RepoResult repoResult) {
        return false;
    }

    @Override
    public void deleteRepoByName(String repositoryName) {

    }

    @Override
    public Optional<RepoResult> getRepoByUrl(String repositoryUrl) {
        return Optional.empty();
    }

    @Override
    public Optional<RepoResult> getRepoByName(String repositoryName) {
        return Optional.empty();
    }

    @Override
    public int getRepoCount() {
        return 0;
    }

    @Override
    public List<RepoResult> getPagedRepo(int offset, int pageSize) {
        return null;
    }

    @Override
    public List<RepoResult> searchRepo(String searchTerms) {
        return null;
    }

    @Override
    public List<RepoResult> getAllRepo() {
        return null;
    }
}
