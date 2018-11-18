package com.searchcode.app.dto;

import com.searchcode.app.model.RepoResult;
import junit.framework.TestCase;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class RepoResultTest extends TestCase {

    public void testSetNameNoReplacement() {
        var repoResult = new RepoResult();
        repoResult.setName(" Name");
        var actual = repoResult.getName();
        assertThat(actual).isEqualTo(" Name");
    }

    public void testGetDirectoryNameFileOnlyAscii() {
        var repoResult = new RepoResult();
        repoResult.setName("社會");
        repoResult.setRowId(1);
        var actual = repoResult.getDirectoryName();
        assertThat(actual).isEqualTo("_e33b0653ef3f30102497d6982026bc0240506204");
    }

    public void testGetDirectoryNameFileOnlyAsciiMultiple() {
        var repoResult = new RepoResult();
        repoResult.setName("社會test社會");
        repoResult.setRowId(1);
        String actual = repoResult.getDirectoryName();
        assertThat(actual).isEqualTo("test_31d0b93065fb1f0380f90f2212aaccca2e1855ff");
    }

    public void testGetDirectoryNameFileWithSpace() {
        var repoResult = new RepoResult();
        repoResult.setName(" test社會");
        repoResult.setRowId(1);
        var actual = repoResult.getDirectoryName();
        assertThat(actual).isEqualTo("test_289ae415363941adfeebaba5d79ccdd8edda7eed");
    }

    public void testGetDirectoryNameFileWithMultipleSpace() {
        var repoResult = new RepoResult();
        repoResult.setName("      test社會");
        repoResult.setRowId(99);
        var actual = repoResult.getDirectoryName();
        assertThat(actual).isEqualTo("test_5663c9a530a6735d342390d1cd492cc15a8c742e");
    }

    public void testGetDirectoryNameFileSlashes() {
        var repoResult = new RepoResult();
        repoResult.setName("something\\gerrit");
        repoResult.setRowId(99);
        var actual = repoResult.getDirectoryName();
        assertThat(actual).isEqualTo("somethinggerrit_0a92ad27ffbec1c1a5658a70298b49e820650a24");
    }

    public void testGetJSONCheck() {
        var repoResult = new RepoResult();

        var repoData = new RepoData();
        repoData.averageIndexTimeSeconds = 20;
        repoData.indexStatus = "indexing";

        repoResult.setData(repoData);
        var actual = repoResult.getDataAsJson();

        assertThat(actual).contains("{\"averageIndexTimeSeconds\":20,\"indexStatus\":\"indexing\"");
        assertThat(actual).contains("\"jobRunTime\":{\"seconds\":");
    }
}