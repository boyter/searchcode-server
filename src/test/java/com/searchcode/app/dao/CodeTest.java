package com.searchcode.app.dao;

import com.searchcode.app.model.SearchcodeCodeResult;
import com.searchcode.app.service.SearchcodeIndexer;
import junit.framework.TestCase;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class CodeTest extends TestCase {

    public void testGetCodeBetween() {
        SearchcodeCode code = new SearchcodeCode();
        List<SearchcodeCodeResult> codeBetween = code.getCodeBetween(0, 200);
        assertThat(codeBetween).hasAtLeastOneElementOfType(SearchcodeCodeResult.class);
    }

    public void testGetMaxId() {
        SearchcodeCode code = new SearchcodeCode();
        int maxId = code.getMaxId();
        assertThat(maxId).isEqualTo(200);
    }
}
