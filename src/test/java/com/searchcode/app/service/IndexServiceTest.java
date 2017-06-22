package com.searchcode.app.service;

import junit.framework.TestCase;

import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class IndexServiceTest extends TestCase {

    IndexService indexService = null;

    public void testGetTotalNumberDocumentsIndexed() {
        this.indexService = new IndexService();
        int totalNumberDocumentsIndexed = this.indexService.getTotalNumberDocumentsIndexed();

        assertThat(totalNumberDocumentsIndexed).isNotNegative();
    }

    public void testGetIndexLocation() {
        this.indexService = new IndexService();
        Path indexLocationA = this.indexService.getIndexLocation();
        this.indexService.flipIndex();
        Path indexLocationB = this.indexService.getIndexLocation();

//        assertThat(indexLocationA).isNotEqualTo(indexLocationB);
    }
}
