package com.searchcode.app.dto;

import com.searchcode.app.model.RepoResult;
import junit.framework.TestCase;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class RepoResultTest extends TestCase {
    public void testSetNameReplacement() {
        RepoResult repoResult = new RepoResult();
        repoResult.setName(" Name");
        assertThat(repoResult.getName()).isEqualTo("-Name");
    }
}
