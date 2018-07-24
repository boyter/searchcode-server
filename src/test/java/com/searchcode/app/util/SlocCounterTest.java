package com.searchcode.app.util;

import com.searchcode.app.dto.CodeIndexDocument;
import junit.framework.TestCase;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class SlocCounterTest extends TestCase {
    public void testSomething() {
        SlocCounter slocCounter = new SlocCounter();

        CodeIndexDocument codeIndexDocument = new CodeIndexDocument();
        codeIndexDocument.setLanguageName("Python");
        codeIndexDocument.setContents("import this\n#comment\nprint this\nprint 'something'");

        int linesCount = slocCounter.countStats(codeIndexDocument);

        assertThat(linesCount).isNotZero();
    }
}
