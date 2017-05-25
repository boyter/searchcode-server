package com.searchcode.app.service;


import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Repo;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.model.ValidatorResult;
import com.searchcode.app.util.Helpers;

public class ValidatorService {

    private final Repo repo;
    private final Helpers helpers;

    public ValidatorService() {
        this(Singleton.getRepo(), Singleton.getHelpers());
    }

    public ValidatorService(Repo repo, Helpers helpers) {
        this.repo = repo;
        this.helpers = helpers;
    }

    public ValidatorResult validate(RepoResult repoResult) {
        if (repoResult == null || this.helpers.isNullEmptyOrWhitespace(repoResult.getName())) {
            return new ValidatorResult(false, "Repository Name cannot be empty or whitespace");
        }

        if (this.helpers.isNullEmptyOrWhitespace(repoResult.getUrl())) {
            return new ValidatorResult(false, "Repository Location cannot be empty or whitespace");
        }

        boolean matches = repoResult.getName().matches("^[a-zA-Z0-9-]*$");

        if (!matches) {
            return new ValidatorResult(false, "Repository Name must match the regular expression ^[a-zA-Z0-9-]*$");
        }

        RepoResult repoByName = this.repo.getRepoByName(repoResult.getName());
        if (repoByName != null) {
            return new ValidatorResult(false, "Repository Name must be unique");
        }

        return new ValidatorResult(true, Values.EMPTYSTRING);
    }
}
