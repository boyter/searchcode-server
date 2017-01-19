package com.searchcode.app.dao;

import com.searchcode.app.model.SearchcodeCodeResult;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class SearchcodeCodeTest extends TestCase {

    public void testGetByIds() {
        SearchcodeCode searchcodeCode = new SearchcodeCode();

        List<Integer> ids = new ArrayList<>();
        ids.add(1);
        ids.add(2);

        List<SearchcodeCodeResult> searchcodeCodeResults = searchcodeCode.getByids(ids);
        assertThat(searchcodeCodeResults.size()).isEqualTo(2);
    }
}
