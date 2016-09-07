package com.searchcode.app.service;

import com.searchcode.app.dto.CodeResult;
import junit.framework.TestCase;

public class TimeCodeSearcherTest extends TestCase {

    // TODO fix tests in here so they do more than just call methods to ensure no exceptions thrown
    // probably need to be more of an integration test where we write to the index then read back
    public void testIndex() {
        TimeCodeSearcher cs = new TimeCodeSearcher();
        cs.search("this", 0);
        cs.search("code", 0);
        cs.search("should", 0);

        for(int i=0; i<100;i++) {
            CodeResult cr = cs.getById(i);
        }
    }
}
