package com.searchcode.app.service.index;

import junit.framework.TestCase;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class IndexSharedTest extends TestCase {
    private IndexService indexService;

    public void testCalculatePages() throws IOException {
        this.indexService = new IndexService();

        var integers = this.indexService.calculatePages(0, 20);
        assertThat(integers).hasSize(0);

        integers = this.indexService.calculatePages(5, 0);
        assertThat(integers).hasSize(1);
    }
}
