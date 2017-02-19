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

        repoResult.setData(repoData);
        String actual = repoResult.getDataAsJson();

        assertThat(actual).isEqualTo("{\"indexStatus\":\"Running\",\"averageIndexTimeSeconds\":20,\"currentIndexTimeSeconds\":99,\"lastJobStartInstant\":{\"seconds\":-31557014167219200,\"nanos\":0}}");
    }
}