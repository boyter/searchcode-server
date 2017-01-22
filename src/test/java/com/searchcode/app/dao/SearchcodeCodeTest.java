package com.searchcode.app.dao;

import com.searchcode.app.dao.searchcode.SearchcodeCode;
import com.searchcode.app.dto.searchcode.SearchcodeSearchResult;
import com.searchcode.app.model.searchcode.SearchcodeCodeResult;
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
        ids.add(3);

        List<SearchcodeSearchResult> searchcodeCodeResults = searchcodeCode.getByIds(ids);
        assertThat(searchcodeCodeResults.size()).isEqualTo(3);
    }

    public void testGetCodeBetween() {
        SearchcodeCode searchcodeCode = new SearchcodeCode();

        List<SearchcodeCodeResult> codeBetween = searchcodeCode.getCodeBetween(0, 1000);
        assertThat(codeBetween.size()).isGreaterThan(10);
    }
}
