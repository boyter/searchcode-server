package com.searchcode.app.util;

import com.searchcode.app.service.Singleton;
import com.searchcode.app.service.StatsService;
import junit.framework.TestCase;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;
import java.util.Random;

public class SpellingCorrectorTest extends TestCase {

    public ISpellingCorrector getSpellingCorrector() {
        return new SearchcodeSpellingCorrector();
    }

    public void testEmptyNullTerms() {
        ISpellingCorrector sc = this.getSpellingCorrector();
        assertNull(sc.correct(null));
        assertEquals("", sc.correct(""));
        assertEquals(" ", sc.correct(" "));
    }

    public void testSingleLetter() {
        ISpellingCorrector sc = getSpellingCorrector();
        sc.correct("a");
    }

    public void testTermsLowercased() {
        ISpellingCorrector sc = this.getSpellingCorrector();
        sc.putWord("UPPERCASE");
        assertTrue(sc.containsWord("uppercase"));
        assertFalse(sc.containsWord("UPPERCASE"));
    }

    public void testSpellingCorrectorWordExistsInDictionary() {
        ISpellingCorrector sc = this.getSpellingCorrector();
        sc.putWord("test");

        String actual = sc.correct("test");
        assertEquals("test", actual);
    }

    public void testSpellingCorrectorEmptyDictionary() {
        ISpellingCorrector sc = this.getSpellingCorrector();
        String actual = sc.correct("testz");
        assertEquals("testz", actual);
    }


    public void testSpellingCorrectorMissingLetter() {
        ISpellingCorrector sc = this.getSpellingCorrector();
        sc.putWord("tests");

        String actual = sc.correct("test");
        assertEquals("tests", actual);
    }

    public void testSpellingCorrectorIncorrectLetter() {
        ISpellingCorrector sc = this.getSpellingCorrector();

        sc.putWord("default");

        String test = sc.correct("defaulz");
        assertEquals("default", test);
    }

    public void testSpellingCorrectorExtraLetter() {
        ISpellingCorrector sc = this.getSpellingCorrector();
        sc.putWord("default");

        String test = sc.correct("defaults");
        assertEquals("default", test);
    }

    public void testSpellingCorrectorTwoMatchesSameLengthWins() {
        ISpellingCorrector sc = this.getSpellingCorrector();
        sc.putWord("test");
        sc.putWord("tests");

        String test = sc.correct("testz");
        assertEquals("tests", test);
    }

    public void testSpellingCorrectorTwoMatchesOfSameLengthMostCommonWins() {
        ISpellingCorrector sc = this.getSpellingCorrector();
        sc.putWord("testy");
        sc.putWord("testy");
        sc.putWord("testy");
        sc.putWord("tests");

        String test = sc.correct("testz");
        assertEquals("testy", test);
    }

    public void testSpellingCorrectorSecondCycle() {
        ISpellingCorrector sc = this.getSpellingCorrector();

        sc.putWord("test");

        String test = sc.correct("testss");
        assertEquals("test", test);
    }

    /**
     * If there is a performance issue this takes forever to run
     */
    public void testLongStringPerformance() {
        ISpellingCorrector sc = this.getSpellingCorrector();
        sc.correct("thisisareallylongstringthatshouldcalusethingstorunreallyslow");
    }

    public void testFuzzSpellingCorrector() {
        Random rand = new Random();
        ISpellingCorrector sc = getSpellingCorrector();

        for (int j=0; j < 1000; j++) {
            sc.putWord(RandomStringUtils.randomAlphabetic(rand.nextInt(10) + 1));
        }

        for (int i = 0; i < 100; i++) {
            sc.correct(RandomStringUtils.randomAlphabetic(rand.nextInt(10) + 1));
            sc.getSampleWords(10);
        }
    }
}
