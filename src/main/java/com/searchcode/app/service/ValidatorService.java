package com.searchcode.app.service;


import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Repo;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.model.ValidatorResult;

public class ValidatorService {

    private final Repo repo;

    public ValidatorService() {
        this(Singleton.getRepo());
    }

    public ValidatorService(Repo repo) {
        this.repo = repo;
    }

    public ValidatorResult validate(RepoResult repoResult) {
        if (repoResult == null || repoResult.getName() == null) {
            return new ValidatorResult(false, "Name cannot be null");
        }

        boolean matches = repoResult.getName().matches("^[a-zA-Z0-9-]*$");

        if (!matches) {
            return new ValidatorResult(false, "Name must match the regular expression ^[a-zA-Z0-9-]*$");
        }

        RepoResult repoByName = this.repo.getRepoByName(repoResult.getName());
        if (repoByName != null) {
            return new ValidatorResult(false, "Name must be unique");
        }

        return new ValidatorResult(true, Values.EMPTYSTRING);
    }
}
