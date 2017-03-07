package com.searchcode.app.dto;

import com.searchcode.app.model.RepoResult;
import junit.framework.TestCase;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class RepoResultTest extends TestCase {

    public void testSetNameReplacement() {
        RepoResult repoResult = new RepoResult();
        repoResult.setName(" Name");
        String actual = repoResult.getName();
        assertThat(actual).isEqualTo("-Name");
    }

    public void testGetNameFileOnlyAscii() {
        RepoResult repoResult = new RepoResult();
        repoResult.setName("社會");
        repoResult.setRowId(1);
        String actual = repoResult.getDirectoryName();
        assertThat(actual).isEqualTo("1");
    }

    public void testGetNameFileOnlyAsciiMultiple() {
        RepoResult repoResult = new RepoResult();
        repoResult.setName("社會test社會");
        repoResult.setRowId(1);
        String actual = repoResult.getDirectoryName();
        assertThat(actual).isEqualTo("1test1");
    }

    public void testGetNameFileWithSpace() {
        RepoResult repoResult = new RepoResult();
        repoResult.setName(" test社會");
        repoResult.setRowId(1);
        String actual = repoResult.getDirectoryName();
        assertThat(actual).isEqualTo("1test1");
    }

    public void testGetNameFileWithMultipleSpace() {
        RepoResult repoResult = new RepoResult();
        repoResult.setName("      test社會");
        repoResult.setRowId(99);
        String actual = repoResult.getDirectoryName();
        assertThat(actual).isEqualTo("99test99");
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