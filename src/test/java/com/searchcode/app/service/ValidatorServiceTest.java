package com.searchcode.app.service;

import com.searchcode.app.model.RepoResult;
import junit.framework.TestCase;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class ValidatorServiceTest extends TestCase {

    public void testRepoResultNull() {
        ValidatorService validatorService = new ValidatorService();
        boolean validate = validatorService.validate(null);
        assertThat(validate).isFalse();
    }

    public void testRepoResultValidReponame() {
        ValidatorService validatorService = new ValidatorService();
        boolean validate = validatorService.validate(new RepoResult(0, "something", "something", "url", "", "", "source", "branch", "{}"));
        assertThat(validate).isTrue();
    }

    public void testRepoResultInValidReponame() {
        ValidatorService validatorService = new ValidatorService();
        boolean validate = validatorService.validate(new RepoResult(0, "some/thing", "something", "url", "", "", "source", "branch", "{}"));
        assertThat(validate).isFalse();
    }

    public void testValidatorServiceExistingName() {
        ValidatorService validatorService = new ValidatorService();

        boolean validate = validatorService.validate(new RepoResult(0, "exists", "something", "url", "", "", "source", "branch", "{}"));
        assertThat(validate).isFalse();
    }
}
