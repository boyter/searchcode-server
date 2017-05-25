package com.searchcode.app.service;


import com.searchcode.app.dao.Repo;
import com.searchcode.app.model.RepoResult;

public class ValidatorService {

    private final Repo repo;

    public ValidatorService() {
        this(Singleton.getRepo());
    }

    public ValidatorService(Repo repo) {
        this.repo = repo;
    }

    public boolean validate(RepoResult repoResult) {
        if (repoResult == null || repoResult.getName() == null) {
            return false;
        }

        boolean matches = repoResult.getName().matches("^[a-zA-Z0-9-]*$");

        if (!matches) {
            return false;
        }

        RepoResult repoByName = this.repo.getRepoByName(repoResult.getName());
        return repoByName == null;
    }
}
