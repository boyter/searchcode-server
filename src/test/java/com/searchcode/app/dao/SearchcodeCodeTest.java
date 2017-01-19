package com.searchcode.app.dao;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class SearchcodeCodeTest extends TestCase {

    public void testGetByIds() {
        SearchcodeCode searchcodeCode = new SearchcodeCode();

        List<Integer> ids = new ArrayList<>();
        ids.add(1);
        ids.add(2);

        searchcodeCode.getByids(ids);
    }
}
