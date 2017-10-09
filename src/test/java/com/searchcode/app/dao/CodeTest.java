package com.searchcode.app.dao;


import com.searchcode.app.model.searchcode.SearchcodeCodeResult;
import junit.framework.TestCase;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class CodeTest extends TestCase {

    public void testGetCodeBetween() {
        SourceCode code = new SourceCode();
        List<SearchcodeCodeResult> codeBetween = code.getCodeBetween(0, 200);
        assertThat(codeBetween).hasAtLeastOneElementOfType(SearchcodeCodeResult.class);
    }

    public void testGetMaxId() {
        SourceCode code = new SourceCode();
        int maxId = code.getMaxId();
        assertThat(maxId).isEqualTo(200);
    }
}
