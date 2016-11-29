package com.searchcode.app.service;

import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.dto.CodeResult;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class CodeSearcherTest extends TestCase {

    public void testCalculatePages() {
        CodeSearcher cs = new CodeSearcher();

        assertThat(cs.calculatePages(0, 0)).isEmpty();
        assertThat(cs.calculatePages(1, 0)).hasSize(1);
        assertThat(cs.calculatePages(10, 10)).hasSize(10);
        assertThat(cs.calculatePages(10, 20)).hasSize(20);

        List<Integer> integers = cs.calculatePages(20, 20);

        for(Integer integer: integers) {
            assertThat(integers.get(integer)).isEqualTo(integer);
        }
    }

    // Integration Test
    public void testGetRepoDocuments() throws IOException {
        CodeIndexDocument codeIndexDocument = new CodeIndexDocument("/", "testGetRepoDocuments", "/", "/", "/", "md5hash", "Java", 10, "", "/", "/");
        CodeIndexer.indexDocument(codeIndexDocument);
        CodeSearcher cs = new CodeSearcher();

        List<String> testGetRepoDocuments = cs.getRepoDocuments("testGetRepoDocuments", 0);
        assertThat(testGetRepoDocuments).hasSize(1);

        testGetRepoDocuments = cs.getRepoDocuments("testGetRepoDocuments", 1);
        assertThat(testGetRepoDocuments).hasSize(0);
    }
}
