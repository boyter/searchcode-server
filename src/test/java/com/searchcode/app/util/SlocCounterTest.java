package com.searchcode.app.util;

import junit.framework.TestCase;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class SlocCounterTest extends TestCase {
    private final SlocCounter slocCounter;

    public SlocCounterTest() {
        this.slocCounter = new SlocCounter();
    }

    public void testCalculationCorrect() {
        String language = "Python";
        String contents = "import this\n#comment\nprint this\n\nprint 'something'";

        SlocCounter.SlocCount slocCount = this.slocCounter.countStats(contents, language);

        assertThat(slocCount.linesCount).isEqualTo(5);
        assertThat(slocCount.commentCount).isEqualTo(1);
        assertThat(slocCount.codeCount).isEqualTo(3);
        assertThat(slocCount.blankCount).isEqualTo(1);
        assertThat(slocCount.complexity).isEqualTo(0);
    }

    public void testEmptyFile() {
        String language = "C++";
        String contents = "";

        SlocCounter.SlocCount slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(0);
    }

    public void testNullFile() {
        String language = "Lisp";

        SlocCounter.SlocCount slocCount = this.slocCounter.countStats(null, language);
        assertThat(slocCount.linesCount).isEqualTo(0);
    }

    // This triggers a bounds exception if there are problems with the calculations
    public void testBoundsExceptions() {
        String language = "Java";
        String contents = "if switch for while do loop != == && || ";

        SlocCounter.SlocCount slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.complexity).isEqualTo(8);
    }
}
