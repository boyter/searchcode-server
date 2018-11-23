package com.searchcode.app.dao;

import com.searchcode.app.config.MySQLDatabaseConfig;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;
import junit.framework.TestCase;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class MySQLRepoTest extends TestCase {

    private MySQLRepo repo;

    public void setUp() throws Exception {
        super.setUp();
        this.repo = new MySQLRepo(new MySQLDatabaseConfig(), new Helpers(), new LoggerWrapper());
    }

    public void testSaveRepo() {
        if (Singleton.getHelpers().isLocalInstance()) return;

        this.repo.saveRepo(new RepoResult()
                .setName("test")
                .setUrl("boyter"));
        var result = this.repo.saveRepo(new RepoResult()
                .setName("test")
                .setUrl("boyter"));

        assertThat(result).isFalse();
    }
}
