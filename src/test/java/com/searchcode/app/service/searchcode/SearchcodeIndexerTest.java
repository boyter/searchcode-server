package com.searchcode.app.service.searchcode;

import com.searchcode.app.dao.searchcode.SearchcodeCode;
import com.searchcode.app.model.searchcode.SearchcodeCodeResult;
import junit.framework.TestCase;

import java.util.List;

public class SearchcodeIndexerTest extends TestCase {

    public void testIndex() {
        SearchcodeCode code = new SearchcodeCode();
        List<SearchcodeCodeResult> codeBetween = code.getCodeBetween(0, 200);
        SearchcodeIndexer.indexDocuments(codeBetween);
    }
}
