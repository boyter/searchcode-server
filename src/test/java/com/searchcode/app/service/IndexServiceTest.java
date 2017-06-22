package com.searchcode.app.service;

import junit.framework.TestCase;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class IndexServiceTest extends TestCase {

    private IndexService indexService = null;

    public void testDeleteByCodeId() {
        this.indexService = new IndexService();
        try {
            this.indexService.deleteByCodeId("this should not do anything but not blow up either");
        } catch (IOException ex) {
            assertThat(true).isFalse();
        }
    }

    public void testDeleteByRepoName() {
        this.indexService = new IndexService();
        try {
            this.indexService.deleteByRepoName("this should not do anything but not blow up either");
        } catch (IOException ex) {
            assertThat(true).isFalse();
        }
    }

    public void testGetIndexLocation() {
        this.indexService = new IndexService();
        Path indexLocationA = this.indexService.getIndexLocation();
        this.indexService.flipIndex();
        Path indexLocationB = this.indexService.getIndexLocation();

//        assertThat(indexLocationA).isNotEqualTo(indexLocationB);
    }



    public void testGetTotalNumberDocumentsIndexed() {
        this.indexService = new IndexService();
        int totalNumberDocumentsIndexed = this.indexService.getTotalNumberDocumentsIndexed();

        assertThat(totalNumberDocumentsIndexed).isNotNegative();
    }
}
