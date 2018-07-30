package com.searchcode.app.util;

import com.searchcode.app.dto.CodeIndexDocument;
import junit.framework.TestCase;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class SlocCounterTest extends TestCase {
    private final SlocCounter slocCounter;

    public SlocCounterTest() {
        this.slocCounter = new SlocCounter();
    }

    public void testCalculationCorrect() {
        CodeIndexDocument codeIndexDocument = new CodeIndexDocument();
        codeIndexDocument.setLanguageName("Python");
        codeIndexDocument.setContents("import this\n#comment\nprint this\n\nprint 'something'");

        SlocCounter.SlocCount slocCount = this.slocCounter.countStats(codeIndexDocument);

        assertThat(slocCount.linesCount).isEqualTo(5);
        assertThat(slocCount.commentCount).isEqualTo(1);
        assertThat(slocCount.codeCount).isEqualTo(3);
        assertThat(slocCount.blankCount).isEqualTo(1);
        assertThat(slocCount.complexity).isEqualTo(0);
    }

    public void testEmptyFile() {
        CodeIndexDocument codeIndexDocument = new CodeIndexDocument();
        codeIndexDocument.setLanguageName("C++");
        codeIndexDocument.setContents("");

        SlocCounter.SlocCount slocCount = this.slocCounter.countStats(codeIndexDocument);
        assertThat(slocCount.linesCount).isEqualTo(0);
    }

    public void testNullFile() {
        CodeIndexDocument codeIndexDocument = new CodeIndexDocument();
        codeIndexDocument.setLanguageName("Lisp");
        codeIndexDocument.setContents(null);

        SlocCounter.SlocCount slocCount = this.slocCounter.countStats(codeIndexDocument);
        assertThat(slocCount.linesCount).isEqualTo(0);
    }

    public void testNullDocument() {
        SlocCounter.SlocCount slocCount = this.slocCounter.countStats(null);
        assertThat(slocCount.linesCount).isEqualTo(0);
    }

    public void testBoundsExceptions() {
        CodeIndexDocument codeIndexDocument = new CodeIndexDocument();
        codeIndexDocument.setLanguageName("Java");
        codeIndexDocument.setContents("if switch for while do loop != == && || ");

        SlocCounter.SlocCount slocCount = this.slocCounter.countStats(codeIndexDocument);
        assertThat(slocCount.complexity).isEqualTo(8);
    }
}
