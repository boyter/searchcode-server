package com.searchcode.app.service;

import junit.framework.TestCase;

public class SphinxIndexServiceTest extends TestCase {
    public void testSearch() {
        SphinxIndexService sphinxIndexService = new SphinxIndexService();

        sphinxIndexService.search("test", 0);
    }
}
