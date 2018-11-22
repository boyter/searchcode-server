package com.searchcode.app.service;

import com.searchcode.app.dao.SQLiteRepo;
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
        ValidatorResult validate = validatorService.validate(new RepoResult()
                .setRowId(0)
                .setName("some_thing")
                .setScm("something")
                .setUrl("url")
                .setUsername("")
                .setPassword("")
                .setSource("source")
                .setBranch("branch")
                .setData("{}"), false);
        assertThat(validate.isValid).isTrue();
    }

    public void testRepoResultValidSlashesEtcReponame() {
        ValidatorService validatorService = new ValidatorService();
        ValidatorResult validate = validatorService.validate(new RepoResult()
                .setRowId(0)
                .setName("some/thing")
                .setScm("something")
                .setUrl("url")
                .setUsername("")
                .setPassword("")
                .setSource("source")
                .setBranch("branch")
                .setData("{}"), false);
        assertThat(validate.isValid).isTrue();

        validate = validatorService.validate(new RepoResult()
                .setRowId(0)
                .setName("some/thing")
                .setScm("something")
                .setUrl("url")
                .setUsername("")
                .setPassword("")
                .setSource("source")
                .setBranch("branch")
                .setData("{}"), false);
        assertThat(validate.isValid).isTrue();

        validate = validatorService.validate(new RepoResult()
                .setRowId(0)
                .setName("some\\thing")
                .setScm("something")
                .setUrl("url")
                .setUsername("")
                .setPassword("")
                .setSource("source")
                .setBranch("branch")
                .setData("{}"), false);
        assertThat(validate.isValid).isTrue();

        validate = validatorService.validate(new RepoResult()
                .setRowId(0)
                .setName("some\\thing@!#&^%!@&*@)(&!@(#&^@!#")
                .setScm("something")
                .setUrl("url")
                .setUsername("")
                .setPassword("")
                .setSource("source")
                .setBranch("branch")
                .setData("{}"), false);
        assertThat(validate.isValid).isTrue();
    }

    public void testValidatorServiceExistingName() {
        SQLiteRepo mockSQLiteRepo = mock(SQLiteRepo.class);

        ValidatorService validatorService = new ValidatorService(mockSQLiteRepo, new Helpers());

        when(mockSQLiteRepo.getRepoByName("exists")).thenReturn(Optional.of(new RepoResult()));
        RepoResult repoResult = new RepoResult()
                .setRowId(0)
                .setName("exists")
                .setScm("something")
                .setUrl("url")
                .setUsername("")
                .setPassword("")
                .setSource("source")
                .setBranch("branch")
                .setData("{}");
        ValidatorResult validate = validatorService.validate(repoResult, false);
        assertThat(validate.isValid).isFalse();
    }

    public void testValidatorServiceExistingNameIgnored() {
        SQLiteRepo mockSQLiteRepo = mock(SQLiteRepo.class);

        ValidatorService validatorService = new ValidatorService(mockSQLiteRepo, new Helpers());

        when(mockSQLiteRepo.getRepoByName("exists")).thenReturn(Optional.of(new RepoResult()));
        RepoResult repoResult = new RepoResult()
                .setRowId(0)
                .setName("exists")
                .setScm("something")
                .setUrl("url")
                .setUsername("")
                .setPassword("")
                .setSource("source")
                .setBranch("branch")
                .setData("{}");
        ValidatorResult validate = validatorService.validate(repoResult, true);
        assertThat(validate.isValid).isTrue();
    }

    public void testRepoResultInValidUrl() {
        ValidatorService validatorService = new ValidatorService();
        ValidatorResult validate = validatorService.validate(new RepoResult()
                .setRowId(0)
                .setName("something")
                .setScm("")
                .setUrl("")
                .setUsername("")
                .setPassword("")
                .setSource("source")
                .setBranch("branch")
                .setData("{}"), false);
        assertThat(validate.isValid).isFalse();
    }
}
