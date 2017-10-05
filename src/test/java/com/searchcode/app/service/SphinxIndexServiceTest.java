package com.searchcode.app.service;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class SphinxIndexServiceTest extends TestCase {
    public void testSearch() {
        SphinxIndexService sphinxIndexService = new SphinxIndexService();


        List<String> someList = new ArrayList<>();

        for (int i=0; i < 100; i++) {
            someList.add("" + i);
        }

        someList.parallelStream()
                .forEach(x -> {
                    sphinxIndexService.search("test", 0);
                });

    }
}
