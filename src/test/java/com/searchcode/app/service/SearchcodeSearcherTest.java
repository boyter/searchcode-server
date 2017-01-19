package com.searchcode.app.service;

import junit.framework.TestCase;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class SearchcodeSearcherTest extends TestCase {
    public void testSearch() {
        SearchcodeSearcher searchcodeSearcher = new SearchcodeSearcher();


        System.out.println(searchcodeSearcher.search("test", 0).size());
        assertThat(searchcodeSearcher.search("test", 0).size()).isGreaterThan(0);
    }
}
