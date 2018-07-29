package com.searchcode.app.util;

import com.searchcode.app.dto.CodeIndexDocument;
import junit.framework.TestCase;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class SlocCounterTest extends TestCase {
    private final SlocCounter slocCounter;

    public SlocCounterTest() {
        this.slocCounter = new SlocCounter();
    }

    public void testSomething() {
        CodeIndexDocument codeIndexDocument = new CodeIndexDocument();
        codeIndexDocument.setLanguageName("Python");
        codeIndexDocument.setContents("import this\n#comment\nprint this if something\nprint 'something'");

        int linesCount = this.slocCounter.countStats(codeIndexDocument);

        assertThat(linesCount).isNotZero();
    }

    public void testBounds() {
        CodeIndexDocument codeIndexDocument = new CodeIndexDocument();
        codeIndexDocument.setLanguageName("Java");
        codeIndexDocument.setContents("if switch for while do loop != == && || ");

//        int linesCount = this.slocCounter.countStats(codeIndexDocument);
//        assertThat(linesCount).isNotZero();
    }
}
