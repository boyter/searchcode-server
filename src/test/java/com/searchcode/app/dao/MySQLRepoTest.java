package com.searchcode.app.dao;

import com.searchcode.app.config.MySQLDatabaseConfig;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.CacheSingleton;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;
import junit.framework.TestCase;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class MySQLRepoTest extends TestCase {

    private MySQLRepo repo;

    public void setUp() throws Exception {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        super.setUp();
        this.repo = new MySQLRepo(new MySQLDatabaseConfig(), new Helpers(), new LoggerWrapper(), CacheSingleton.getRepoResultCache());
    }

    public void testGetRepoByUrl() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var result = this.repo.getRepoByUrl("boyter");

        assertThat(result.get().getUrl()).isEqualTo("boyter");
    }

    public void testGetRepoByUrlCache() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var cache = CacheSingleton.getRepoResultCache();
        cache.put("d.m.boyter", Optional.of(new RepoResult().setRowId(999).setName("ZeName").setUrl("boyter")));
        var newSource = new MySQLRepo(new MySQLDatabaseConfig(), new Helpers(), Singleton.getLogger(), cache);

        var result = newSource.getRepoByUrl("boyter");

        assertThat(result.get().getRowId()).isEqualTo(999);
        assertThat(result.get().getName()).isEqualTo("ZeName");
    }

    public void testGetRepoById() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        this.repo.saveRepo(new RepoResult()
                .setName("test")
                .setUrl("boyter"));

        var result = this.repo.getRepoByUrl("boyter");
        var result2 = this.repo.getRepoById(result.get().getRowId());

        assertThat(result2.get().getRowId()).isEqualTo(result.get().getRowId());
    }

    public void testGetRepoByIdCache() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var cache = CacheSingleton.getRepoResultCache();
        cache.put("d.m.999", Optional.of(new RepoResult().setRowId(999).setName("ZeName")));
        var newSource = new MySQLRepo(new MySQLDatabaseConfig(), new Helpers(), Singleton.getLogger(), cache);

        var result = newSource.getRepoById(999);

        assertThat(result.get().getRowId()).isEqualTo(999);
        assertThat(result.get().getName()).isEqualTo("ZeName");
    }

    public void testDeleteRepo() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        this.repo.saveRepo(new RepoResult()
                .setName("test")
                .setUrl("boyter"));

        var r1 = this.repo.getRepoByUrl("boyter");
        var result = this.repo.deleteRepoById(r1.get().getRowId());
        assertThat(result).isTrue();

        CacheSingleton.getRepoResultCache().clear();

        r1 = this.repo.getRepoByUrl("boyter");
        assertThat(r1.isPresent()).isFalse();
    }

    public void testSaveRepo() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        this.repo.saveRepo(new RepoResult()
                .setName("test")
                .setUrl("boyter"));

        var r = this.repo.getRepoByUrl("boyter");
        assertThat(r.get().getName()).isEqualTo("test");
        assertThat(r.get().getUrl()).isEqualTo("boyter");

        var result = this.repo.saveRepo(new RepoResult()
                .setName("test")
                .setUrl("boyter"));

        assertThat(result).isFalse();
        this.repo.deleteRepoById(r.get().getRowId());

    }
}
