package com.searchcode.app.service;

import com.searchcode.app.dao.Repo;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.model.ValidatorResult;
import com.searchcode.app.util.Helpers;
import junit.framework.TestCase;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValidatorServiceTest extends TestCase {

    public void testRepoResultNull() {
        ValidatorService validatorService = new ValidatorService();
        ValidatorResult validate = validatorService.validate(null, false);
        assertThat(validate.isValid).isFalse();
    }

    public void testRepoResultValidReponame() {
        ValidatorService validatorService = new ValidatorService();
        ValidatorResult validate = validatorService.validate(new RepoResult(0, "some_thing", "something", "url", "", "", "source", "branch", "{}"), false);
        assertThat(validate.isValid).isTrue();
    }

    public void testRepoResultValidSlashesEtcReponame() {
        ValidatorService validatorService = new ValidatorService();
        ValidatorResult validate = validatorService.validate(new RepoResult(0, "some/thing", "something", "url", "", "", "source", "branch", "{}"), false);
        assertThat(validate.isValid).isTrue();

        validate = validatorService.validate(new RepoResult(0, "some/thing", "something", "url", "", "", "source", "branch", "{}"), false);
        assertThat(validate.isValid).isTrue();

        validate = validatorService.validate(new RepoResult(0, "some\\thing", "something", "url", "", "", "source", "branch", "{}"), false);
        assertThat(validate.isValid).isTrue();

        validate = validatorService.validate(new RepoResult(0, "some\\thing@!#&^%!@&*@)(&!@(#&^@!#", "something", "url", "", "", "source", "branch", "{}"), false);
        assertThat(validate.isValid).isTrue();
    }

    public void testValidatorServiceExistingName() {
        Repo mockRepo = mock(Repo.class);

        ValidatorService validatorService = new ValidatorService(mockRepo, new Helpers());

        when(mockRepo.getRepoByName("exists")).thenReturn(Optional.of(new RepoResult()));

        RepoResult repoResult = new RepoResult(0, "exists", "something", "url", "", "", "source", "branch", "{}");
        ValidatorResult validate = validatorService.validate(repoResult, false);
        assertThat(validate.isValid).isFalse();
    }

    public void testValidatorServiceExistingNameIgnored() {
        Repo mockRepo = mock(Repo.class);

        ValidatorService validatorService = new ValidatorService(mockRepo, new Helpers());

        when(mockRepo.getRepoByName("exists")).thenReturn(Optional.of(new RepoResult()));

        RepoResult repoResult = new RepoResult(0, "exists", "something", "url", "", "", "source", "branch", "{}");
        ValidatorResult validate = validatorService.validate(repoResult, true);
        assertThat(validate.isValid).isTrue();
    }

    public void testRepoResultInValidUrl() {
        ValidatorService validatorService = new ValidatorService();
        ValidatorResult validate = validatorService.validate(new RepoResult(0, "something", "", "", "", "", "source", "branch", "{}"), false);
        assertThat(validate.isValid).isFalse();
    }
}
