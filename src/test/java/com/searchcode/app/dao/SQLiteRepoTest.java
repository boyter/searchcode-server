package com.searchcode.app.dao;

import com.searchcode.app.config.SQLiteMemoryDatabaseConfig;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import junit.framework.TestCase;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class SQLiteRepoTest extends TestCase {

    private SQLiteRepo sqLiteRepo;

    public void setUp() throws Exception {
        super.setUp();
        this.sqLiteRepo = new SQLiteRepo(new SQLiteMemoryDatabaseConfig(), new Helpers(), Singleton.getLogger());
        this.sqLiteRepo.createTableIfMissing();
    }

    public void testRepoSaveDelete() {
        this.sqLiteRepo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("myname")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));

        Optional<RepoResult> repoResult = this.sqLiteRepo.getRepoByName("myname");
        RepoResult result = repoResult.get();


        assertThat(result.getName()).isEqualTo("myname");
        assertThat(result.getScm()).isEqualTo("git");
        assertThat(result.getUrl()).isEqualTo("myurl");
        assertThat(result.getUsername()).isEqualTo("username");
        assertThat(result.getPassword()).isEqualTo("password");
        assertThat(result.getSource()).isEqualTo("mysource");
        assertThat(result.getBranch()).isEqualTo("mybranch");
        assertThat(result.getData().averageIndexTimeSeconds).isEqualTo(0);

        this.sqLiteRepo.deleteRepoByName("myname");

        Optional<RepoResult> repoResult2 = this.sqLiteRepo.getRepoByName("myname");
        assertThat(repoResult2.isPresent()).isFalse();
    }

    public void testRepoSaveGetCacheBug() {
        this.sqLiteRepo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("myname")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));

        for (int i = 0; i < 200; i++) {
            assertThat(sqLiteRepo.getRepoByName("myname")).isNotNull();
        }

        this.sqLiteRepo.deleteRepoByName("myname");
    }

    public void testRepoByUrl() {
        this.sqLiteRepo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("myname")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        assertThat(this.sqLiteRepo.getRepoByUrl("myurl").isPresent()).isTrue();
        this.sqLiteRepo.deleteRepoByName("myname");
    }

    public void testRepoByUrlMemoryLeak() {
        for (int i = 0; i < 200; i++) {
            this.sqLiteRepo.saveRepo(new RepoResult()
                    .setRowId(-1)
                    .setName("myname")
                    .setScm("git")
                    .setUrl("myurl")
                    .setUsername("username")
                    .setPassword("password")
                    .setSource("mysource")
                    .setBranch("mybranch")
                    .setData("{}"));
            assertThat(this.sqLiteRepo.getRepoByUrl("myurl")).isNotNull();
            this.sqLiteRepo.deleteRepoByName("myname");
        }
    }

    public void testDeleteRepoMultipleTimes() {
        this.sqLiteRepo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("myname")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));

        for (int i = 0; i < 200; i++) {
            this.sqLiteRepo.deleteRepoByName("myname");
        }
    }

    public void testSaveRepoMultipleTimes() {
        for (int i = 0; i < 200; i++) {
            this.sqLiteRepo.saveRepo(new RepoResult()
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

        this.sqLiteRepo.deleteRepoByName("myname");
    }

    public void testGetAllRepo() {
        this.sqLiteRepo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("myname")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        assertThat(this.sqLiteRepo.getAllRepo().size()).isGreaterThanOrEqualTo(1);
        this.sqLiteRepo.deleteRepoByName("myname");
    }

    public void testGetPagedRepo() {
        this.sqLiteRepo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("testGetPagedRepo1")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        this.sqLiteRepo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("testGetPagedRepo2")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        this.sqLiteRepo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("testGetPagedRepo3")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        this.sqLiteRepo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("testGetPagedRepo4")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        this.sqLiteRepo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("testGetPagedRepo5")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));

        assertThat(this.sqLiteRepo.getPagedRepo(0, 2).size()).isEqualTo(2);
        assertThat(this.sqLiteRepo.getPagedRepo(0, 4).size()).isEqualTo(4);
        assertThat(this.sqLiteRepo.getPagedRepo(2, 2).size()).isEqualTo(2);

        this.sqLiteRepo.deleteRepoByName("testGetPagedRepo1");
        this.sqLiteRepo.deleteRepoByName("testGetPagedRepo2");
        this.sqLiteRepo.deleteRepoByName("testGetPagedRepo3");
        this.sqLiteRepo.deleteRepoByName("testGetPagedRepo4");
        this.sqLiteRepo.deleteRepoByName("testGetPagedRepo5");
    }

    public void testSearchRepo() {
        this.sqLiteRepo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("testGetPagedRepo1")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        this.sqLiteRepo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("testGetPagedRepo2")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        this.sqLiteRepo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("testGetPagedRepo3")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        this.sqLiteRepo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("testGetPagedRepo4")
                .setScm("git")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        this.sqLiteRepo.saveRepo(new RepoResult()
                .setRowId(-1)
                .setName("testGetPagedRepo5")
                .setScm("svn")
                .setUrl("myurl")
                .setUsername("username")
                .setPassword("password")
                .setSource("mysource")
                .setBranch("mybranch")
                .setData("{}"));
        assertThat(this.sqLiteRepo.searchRepo("PassworD").size()).isEqualTo(5);
        assertThat(this.sqLiteRepo.searchRepo("TESTGetPagedRepo1").size()).isEqualTo(1);
        assertThat(this.sqLiteRepo.searchRepo("svn testGetPagedRepo5").size()).isEqualTo(1);
        assertThat(this.sqLiteRepo.searchRepo("svn   testGetPagedRepo5").size()).isEqualTo(1);

        this.sqLiteRepo.deleteRepoByName("testGetPagedRepo1");
        this.sqLiteRepo.deleteRepoByName("testGetPagedRepo2");
        this.sqLiteRepo.deleteRepoByName("testGetPagedRepo3");
        this.sqLiteRepo.deleteRepoByName("testGetPagedRepo4");
        this.sqLiteRepo.deleteRepoByName("testGetPagedRepo5");
    }


    public void testGetRepoByNameUsingNull() {
        Optional<RepoResult> repoResult = this.sqLiteRepo.getRepoByName(null);
        assertThat(repoResult.isPresent()).isFalse();
    }
}

