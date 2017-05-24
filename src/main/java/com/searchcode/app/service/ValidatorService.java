package com.searchcode.app.service;


import com.searchcode.app.model.RepoResult;

public class ValidatorService {
    public boolean validate(RepoResult repoResult) {
        if (repoResult == null || repoResult.getName() == null) {
            return false;
        }

        return repoResult.getName().matches("^[a-zA-Z0-9-]*$");
    }
}
