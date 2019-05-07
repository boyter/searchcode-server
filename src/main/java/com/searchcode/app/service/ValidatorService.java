package com.searchcode.app.service;


import com.searchcode.app.config.Values;
import com.searchcode.app.dao.IRepo;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.model.ValidatorResult;
import com.searchcode.app.util.Helpers;

/**
 * Validation logic for anything that spits an error back to the user or though the
 * API should go in here.
 */
public class ValidatorService {

    private final IRepo repo;
    private final Helpers helpers;

    public ValidatorService() {
        this(Singleton.getRepo(), Singleton.getHelpers());
    }

    public ValidatorService(IRepo repo, Helpers helpers) {
        this.repo = repo;
        this.helpers = helpers;
    }

    public ValidatorResult validate(RepoResult repoResult, boolean ignoreDuplicates) {
        if (repoResult == null || this.helpers.isNullEmptyOrWhitespace(repoResult.getName())) {
            return new ValidatorResult(false, "Repository Name cannot be empty or whitespace");
        }

        if (!repoResult.getName().trim().equals(repoResult.getName())) {
            return new ValidatorResult(false, "Repository Name cannot begin or end with whitespace");
        }

        if (this.helpers.isNullEmptyOrWhitespace(repoResult.getUrl())) {
            return new ValidatorResult(false, "Repository Location cannot be empty or whitespace");
        }

        if (!ignoreDuplicates) {
            var repoByName = this.repo.getRepoByName(repoResult.getName());
            if (repoByName.isPresent()) {
                return new ValidatorResult(false, "Repository Name must be unique");
            }
        }

        return new ValidatorResult(true, Values.EMPTYSTRING);
    }
}
