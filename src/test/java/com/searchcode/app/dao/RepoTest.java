package com.searchcode.app.dao;

import com.searchcode.app.config.SQLiteMemoryDatabaseConfig;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.AESEncryptor;
import com.searchcode.app.util.Helpers;
import junit.framework.TestCase;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class RepoTest extends TestCase {

    private Repo repo;

    public void setUp() throws Exception {
        super.setUp();
        this.repo = new Repo(new SQLiteMemoryDatabaseConfig(), new Helpers(), Singleton.getLogger());
        this.repo.createTableIfMissing();
    }

    public void testRepoSaveDelete() {
        this.repo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("myname")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));

        Optional<RepoResult> repoResult = this.repo.getRepoByName("myname");
        RepoResult result = repoResult.get();


        assertThat(result.getName()).isEqualTo("myname");
        assertThat(result.getScm()).isEqualTo("git");
        assertThat(result.getUrl()).isEqualTo("myurl");
        assertThat(result.getUsername()).isEqualTo("username");
        assertThat(result.getPassword()).isEqualTo("password");
        assertThat(result.getSource()).isEqualTo("mysource");
        assertThat(result.getBranch()).isEqualTo("mybranch");
        assertThat(result.getData().averageIndexTimeSeconds).isEqualTo(0);

        this.repo.deleteRepoByName("myname");

        Optional<RepoResult> repoResult2 = this.repo.getRepoByName("myname");
        assertThat(repoResult2.isPresent()).isFalse();
    }

    public void testRepoSaveGetCacheBug() {
        this.repo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("myname")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));

        for (int i = 0 ; i < 200; i++) {
            assertThat(repo.getRepoByName("myname")).isNotNull();
        }

        this.repo.deleteRepoByName("myname");
    }

    public void testRepoByUrl() {
        this.repo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("myname")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        assertThat(this.repo.getRepoByUrl("myurl").isPresent()).isTrue();
        this.repo.deleteRepoByName("myname");
    }

    public void testRepoByUrlMemoryLeak() {
        for (int i = 0; i < 200; i++) {
            this.repo.saveRepo(new RepoResult()
                    .setRowId(-1)
                    .setName("myname")
                    .setScm("git")
                    .setUrl("myurl")
                    .setUsername("username")
                    .setPassword("password")
                    .setSource("mysource")
                    .setBranch("mybranch")
                    .setData("{}"));
            assertThat(this.repo.getRepoByUrl("myurl")).isNotNull();
            this.repo.deleteRepoByName("myname");
        }
    }

    public void testDeleteRepoMultipleTimes() {
        this.repo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("myname")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));

        for (int i = 0 ; i < 200; i++) {
            this.repo.deleteRepoByName("myname");
        }
    }

    public void testSaveRepoMultipleTimes() {
        for (int i = 0 ; i < 200; i++) {
            this.repo.saveRepo(new RepoResult()
                    .setRowId(-1)
                    .setName("myname")
                    .setScm("git")
                    .setUrl("myurl")
                    .setUsername("username")
                    .setPassword("password")
                    .setSource("mysource")
                    .setBranch("mybranch")
                    .setData("{}"));
        }

        this.repo.deleteRepoByName("myname");
    }

    public void testGetAllRepo() {
        this.repo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("myname")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        assertThat(this.repo.getAllRepo().size()).isGreaterThanOrEqualTo(1);
        this.repo.deleteRepoByName("myname");
    }

    public void testGetPagedRepo() {
        this.repo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("testGetPagedRepo1")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        this.repo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("testGetPagedRepo2")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        this.repo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("testGetPagedRepo3")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        this.repo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("testGetPagedRepo4")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        this.repo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("testGetPagedRepo5")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));

        assertThat(this.repo.getPagedRepo(0, 2).size()).isEqualTo(2);
        assertThat(this.repo.getPagedRepo(0, 4).size()).isEqualTo(4);
        assertThat(this.repo.getPagedRepo(2, 2).size()).isEqualTo(2);

        this.repo.deleteRepoByName("testGetPagedRepo1");
        this.repo.deleteRepoByName("testGetPagedRepo2");
        this.repo.deleteRepoByName("testGetPagedRepo3");
        this.repo.deleteRepoByName("testGetPagedRepo4");
        this.repo.deleteRepoByName("testGetPagedRepo5");
    }

    public void testSearchRepo() {
        this.repo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("testGetPagedRepo1")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        this.repo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("testGetPagedRepo2")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        this.repo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("testGetPagedRepo3")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        this.repo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("testGetPagedRepo4")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        this.repo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("testGetPagedRepo5")
                .setScm("svn")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        assertThat(this.repo.searchRepo("PassworD").size()).isEqualTo(5);
        assertThat(this.repo.searchRepo("TESTGetPagedRepo1").size()).isEqualTo(1);
        assertThat(this.repo.searchRepo("svn testGetPagedRepo5").size()).isEqualTo(1);
        assertThat(this.repo.searchRepo("svn   testGetPagedRepo5").size()).isEqualTo(1);

        this.repo.deleteRepoByName("testGetPagedRepo1");
        this.repo.deleteRepoByName("testGetPagedRepo2");
        this.repo.deleteRepoByName("testGetPagedRepo3");
        this.repo.deleteRepoByName("testGetPagedRepo4");
        this.repo.deleteRepoByName("testGetPagedRepo5");
    }


    public void testGetRepoByNameUsingNull() {
        Optional<RepoResult> repoResult = this.repo.getRepoByName(null);
        assertThat(repoResult.isPresent()).isFalse();
    }
}

