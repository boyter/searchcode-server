package com.searchcode.app.dao;

import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.Singleton;
import junit.framework.TestCase;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class RepoTest extends TestCase {

    public RepoTest() {
        // Tests need to bootstrap themselves
        Repo repo = Singleton.getRepo();
        repo.addSourceToTable();
    }

    public void testMigrationCode() {
        Repo repo = Singleton.getRepo();

        repo.addSourceToTable();
        repo.addSourceToTable();
        repo.addBranchToTable();
        repo.addBranchToTable();
    }

    public void testRepoSaveDelete() {

        Repo repo = Singleton.getRepo();

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
        Repo repo = Singleton.getRepo();

        repo.saveRepo(new RepoResult(-1, "myname", "git", "myurl", "username", "password", "mysource", "mybranch"));
        assertNotNull(repo.getRepoByName("myname"));
        assertNotNull(repo.getRepoByName("myname"));
        assertNotNull(repo.getRepoByName("myname"));
        assertNotNull(repo.getRepoByName("myname"));
        repo.deleteRepoByName("myname");
    }

    public void testRepoByUrl() {
        Repo repo = Singleton.getRepo();

        repo.saveRepo(new RepoResult(-1, "myname", "git", "myurl", "username", "password", "mysource", "mybranch"));
        assertNotNull(repo.getRepoByUrl("myurl"));
        repo.deleteRepoByName("myname");
    }

    public void testRepoByUrlMemoryLeak() {
        Repo repo = Singleton.getRepo();

        repo.saveRepo(new RepoResult(-1, "myname", "git", "myurl", "username", "password", "mysource", "mybranch"));
        assertNotNull(repo.getRepoByUrl("myurl"));
        repo.deleteRepoByName("myname");
    }

    public void testDeleteRepoMultipleTimes() {
        Repo repo = Singleton.getRepo();

        repo.saveRepo(new RepoResult(-1, "myname", "git", "myurl", "username", "password", "mysource", "mybranch"));
        repo.deleteRepoByName("myname");
        repo.deleteRepoByName("myname");
        repo.deleteRepoByName("myname");
        repo.deleteRepoByName("myname");
        repo.deleteRepoByName("myname");
        repo.deleteRepoByName("myname");
    }

    public void testSaveRepoMultipleTimes() {
        Repo repo = Singleton.getRepo();

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
        Repo repo = Singleton.getRepo();

        repo.saveRepo(new RepoResult(-1, "myname", "git", "myurl", "username", "password", "mysource", "mybranch"));
        assertTrue(repo.getAllRepo().size() >= 1);
        repo.deleteRepoByName("myname");
    }

    public void testGetPagedRepo() {
        Repo repo = Singleton.getRepo();

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
        Repo repo = Singleton.getRepo();

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



    public void testGetRepoByNameUsingNull() {
        Repo repo = Singleton.getRepo();
        RepoResult repoResult = repo.getRepoByName(null);
        assertThat(repoResult).isNull();
    }
}

