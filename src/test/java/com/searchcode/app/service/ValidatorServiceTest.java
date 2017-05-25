package com.searchcode.app.service;

import com.searchcode.app.dao.Repo;
import com.searchcode.app.model.RepoResult;
import junit.framework.TestCase;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        Repo mockRepo = mock(Repo.class);

        ValidatorService validatorService = new ValidatorService(mockRepo);

        when(mockRepo.getRepoByName("exists")).thenReturn(new RepoResult());

        RepoResult repoResult = new RepoResult(0, "exists", "something", "url", "", "", "source", "branch", "{}");
        boolean validate = validatorService.validate(repoResult);
        assertThat(validate).isFalse();
    }
}
