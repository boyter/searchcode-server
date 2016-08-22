package com.searchcode.app.dao;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.searchcode.app.config.InjectorConfig;
import com.searchcode.app.model.RepoResult;
import junit.framework.TestCase;

public class RepoTest extends TestCase {
    public void testRepoSaveDelete() {
        Injector injector = Guice.createInjector(new InjectorConfig());
        Repo repo = injector.getInstance(Repo.class);

        repo.saveRepo(new RepoResult(-1, "myname", "git", "myurl", "username", "password", "mysource", "mybranch"));
        RepoResult result = repo.getRepoByName("myname");
        assertNotNull(result);
        assertEquals("myname", result.getName());
        assertEquals("git", result.getScm());
        assertEquals("myurl", result.getUrl());
        assertEquals("username", result.getUsername());
        assertEquals("password", result.getPassword());
        assertEquals("mysource", result.getSource());
        assertEquals("mybranch", result.getBranch());
        repo.deleteRepoByName("myname");

        result = repo.getRepoByName("myname");
        assertNull(result);
    }

    public void testRepoSaveGetCacheBug() {
        Injector injector = Guice.createInjector(new InjectorConfig());
        Repo repo = injector.getInstance(Repo.class);
        repo.saveRepo(new RepoResult(-1, "myname", "git", "myurl", "username", "password", "mysource", "mybranch"));
        assertNotNull(repo.getRepoByName("myname"));
        assertNotNull(repo.getRepoByName("myname"));
        assertNotNull(repo.getRepoByName("myname"));
        assertNotNull(repo.getRepoByName("myname"));
        repo.deleteRepoByName("myname");
    }

    public void testDeleteRepoMultipleTimes() {
        Injector injector = Guice.createInjector(new InjectorConfig());
        Repo repo = injector.getInstance(Repo.class);
        repo.saveRepo(new RepoResult(-1, "myname", "git", "myurl", "username", "password", "mysource", "mybranch"));
        repo.deleteRepoByName("myname");
        repo.deleteRepoByName("myname");
        repo.deleteRepoByName("myname");
        repo.deleteRepoByName("myname");
        repo.deleteRepoByName("myname");
        repo.deleteRepoByName("myname");
    }

    public void testSaveRepoMultipleTimes() {
        Injector injector = Guice.createInjector(new InjectorConfig());
        Repo repo = injector.getInstance(Repo.class);
        repo.saveRepo(new RepoResult(-1, "myname", "git", "myurl", "username", "password", "mysource", "mybranch"));
        repo.saveRepo(new RepoResult(-1, "myname", "git", "myurl", "username", "password", "mysource", "mybranch"));
        repo.saveRepo(new RepoResult(-1, "myname", "git", "myurl", "username", "password", "mysource", "mybranch"));
        repo.saveRepo(new RepoResult(-1, "myname", "git", "myurl", "username", "password", "mysource", "mybranch"));
        repo.saveRepo(new RepoResult(-1, "myname", "git", "myurl", "username", "password", "mysource", "mybranch"));
        repo.saveRepo(new RepoResult(-1, "myname", "git", "myurl", "username", "password", "mysource", "mybranch"));
        repo.saveRepo(new RepoResult(-1, "myname", "git", "myurl", "username", "password", "mysource", "mybranch"));
        repo.deleteRepoByName("myname");
    }

    public void testGetAllRepo() {
        Injector injector = Guice.createInjector(new InjectorConfig());
        Repo repo = injector.getInstance(Repo.class);
        repo.saveRepo(new RepoResult(-1, "myname", "git", "myurl", "username", "password", "mysource", "mybranch"));
        assertTrue(repo.getAllRepo().size() >= 1);
        repo.deleteRepoByName("myname");
    }

    public void testGetPagedRepo() {
        Injector injector = Guice.createInjector(new InjectorConfig());
        Repo repo = injector.getInstance(Repo.class);
        repo.saveRepo(new RepoResult(-1, "testGetPagedRepo1", "git", "myurl", "username", "password", "mysource", "mybranch"));
        repo.saveRepo(new RepoResult(-1, "testGetPagedRepo2", "git", "myurl", "username", "password", "mysource", "mybranch"));
        repo.saveRepo(new RepoResult(-1, "testGetPagedRepo3", "git", "myurl", "username", "password", "mysource", "mybranch"));
        repo.saveRepo(new RepoResult(-1, "testGetPagedRepo4", "git", "myurl", "username", "password", "mysource", "mybranch"));
        repo.saveRepo(new RepoResult(-1, "testGetPagedRepo5", "git", "myurl", "username", "password", "mysource", "mybranch"));
        assertEquals(2, repo.getPagedRepo(0, 2).size());
        assertEquals(4, repo.getPagedRepo(0, 4).size());
        assertEquals(2, repo.getPagedRepo(2, 2).size());
        repo.deleteRepoByName("testGetPagedRepo1");
        repo.deleteRepoByName("testGetPagedRepo2");
        repo.deleteRepoByName("testGetPagedRepo3");
        repo.deleteRepoByName("testGetPagedRepo4");
        repo.deleteRepoByName("testGetPagedRepo5");
    }

    public void testSearchRepo() {
        Injector injector = Guice.createInjector(new InjectorConfig());
        Repo repo = injector.getInstance(Repo.class);
        repo.saveRepo(new RepoResult(-1, "testGetPagedRepo1", "git", "myurl", "username", "password", "mysource", "mybranch"));
        repo.saveRepo(new RepoResult(-1, "testGetPagedRepo2", "git", "myurl", "username", "password", "mysource", "mybranch"));
        repo.saveRepo(new RepoResult(-1, "testGetPagedRepo3", "git", "myurl", "username", "password", "mysource", "mybranch"));
        repo.saveRepo(new RepoResult(-1, "testGetPagedRepo4", "git", "myurl", "username", "password", "mysource", "mybranch"));
        repo.saveRepo(new RepoResult(-1, "testGetPagedRepo5", "svn", "myurl", "username", "password", "mysource", "mybranch"));

        assertEquals(5, repo.searchRepo("PassworD").size());
        assertEquals(1, repo.searchRepo("TESTGetPagedRepo1").size());
        assertEquals(1, repo.searchRepo("svn testGetPagedRepo5").size());
        assertEquals(1, repo.searchRepo("svn   testGetPagedRepo5").size());

        repo.deleteRepoByName("testGetPagedRepo1");
        repo.deleteRepoByName("testGetPagedRepo2");
        repo.deleteRepoByName("testGetPagedRepo3");
        repo.deleteRepoByName("testGetPagedRepo4");
        repo.deleteRepoByName("testGetPagedRepo5");
    }

    public void testMigrationCode() {
        Injector injector = Guice.createInjector(new InjectorConfig());
        Repo repo = injector.getInstance(Repo.class);
        repo.addSourceToTable();
        repo.addSourceToTable();
        repo.addBranchToTable();
        repo.addBranchToTable();
    }
}

