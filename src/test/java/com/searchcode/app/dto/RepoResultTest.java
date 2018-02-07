package com.searchcode.app.dto;

import com.searchcode.app.model.RepoResult;
import junit.framework.TestCase;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class RepoResultTest extends TestCase {

    public void testSetNameNoReplacement() {
        RepoResult repoResult = new RepoResult();
        repoResult.setName(" Name");
        String actual = repoResult.getName();
        assertThat(actual).isEqualTo(" Name");
    }

    public void testGetDirectoryNameFileOnlyAscii() {
        RepoResult repoResult = new RepoResult();
        repoResult.setName("社會");
        repoResult.setRowId(1);
        String actual = repoResult.getDirectoryName();
        assertThat(actual).isEqualTo("_e33b0653ef3f30102497d6982026bc0240506204");
    }

    public void testGetDirectoryNameFileOnlyAsciiMultiple() {
        RepoResult repoResult = new RepoResult();
        repoResult.setName("社會test社會");
        repoResult.setRowId(1);
        String actual = repoResult.getDirectoryName();
        assertThat(actual).isEqualTo("test_31d0b93065fb1f0380f90f2212aaccca2e1855ff");
    }

    public void testGetDirectoryNameFileWithSpace() {
        RepoResult repoResult = new RepoResult();
        repoResult.setName(" test社會");
        repoResult.setRowId(1);
        String actual = repoResult.getDirectoryName();
        assertThat(actual).isEqualTo("test_289ae415363941adfeebaba5d79ccdd8edda7eed");
    }

    public void testGetDirectoryNameFileWithMultipleSpace() {
        RepoResult repoResult = new RepoResult();
        repoResult.setName("      test社會");
        repoResult.setRowId(99);
        String actual = repoResult.getDirectoryName();
        assertThat(actual).isEqualTo("test_5663c9a530a6735d342390d1cd492cc15a8c742e");
    }

    public void testGetDirectoryNameFileSlashes() {
        RepoResult repoResult = new RepoResult();
        repoResult.setName("something\\gerrit");
        repoResult.setRowId(99);
        String actual = repoResult.getDirectoryName();
        assertThat(actual).isEqualTo("somethinggerrit_0a92ad27ffbec1c1a5658a70298b49e820650a24");
    }

    public void testGetJSONCheck() {
        RepoResult repoResult = new RepoResult();

        RepoData repoData = new RepoData();
        repoData.averageIndexTimeSeconds = 20;
        repoData.indexStatus = "indexing";

        repoResult.setData(repoData);
        String actual = repoResult.getDataAsJson();

        assertThat(actual).contains("{\"averageIndexTimeSeconds\":20,\"indexStatus\":\"indexing\"");
        assertThat(actual).contains("\"jobRunTime\":{\"seconds\":");
    }
}