package com.searchcode.app.util;

import com.searchcode.app.dao.Data;
import com.searchcode.app.dto.*;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.dto.FileClassifierResult;
import junit.framework.TestCase;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;

public class SearchcodeLibTest extends TestCase {

    public void testCleanPipeline() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.codeCleanPipeline("{AB3FBE3A-410C-4FB2-84E0-B2D3434D1995}");
        assertThat(actual).contains(" AB3FBE3A-410C-4FB2-84E0-B2D3434D1995 ");
        assertThat(actual).contains(" {AB3FBE3A-410C-4FB2-84E0-B2D3434D1995} ");
        assertThat(actual).contains(" AB3FBE3A 410C 4FB2 84E0 B2D3434D1995 ");
    }

    public void testCleanPipelineTwo() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.codeCleanPipeline("\"_updatedDate\"");

        assertThat(actual).contains(" _updatedDate ");
        assertThat(actual).contains(" updatedDate ");
        assertThat(actual).contains(" \"_updatedDate\" ");
    }

    public void testCleanPipelineThree() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.codeCleanPipeline("'shop_order_log',");

        assertThat(actual.contains(" 'shop_order_log' ")).isTrue();
    }

    public void testCleanPipelineCustom() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.codeCleanPipeline("context.config.URL_REWRITE.iteritems():");

        assertThat(actual).contains(" URL_REWRITE ");
    }

    public void testCleanPipelineCustom2() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.codeCleanPipeline("task :install_something do");

        assertThat(actual).contains(" install_something ");
    }

    public void testIsBinaryBlackListWithNoDotFileName() {
        SearchcodeLib sl = new SearchcodeLib();

        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add("a");

        sl.BLACK_LIST = new String[1];
        sl.BLACK_LIST[0] = "license";

        assertThat(sl.isBinary(codeLines, "license").isBinary()).isTrue();
    }

    public void testIsBinary() {
        SearchcodeLib sl = new SearchcodeLib();

        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add("a");

        assertThat(sl.isBinary(codeLines, "somefilename").isBinary()).isFalse();
    }

    public void testIsBinaryAllNonAscii() {
        SearchcodeLib sl = new SearchcodeLib();

        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add("你");

        assertThat(sl.isBinary(codeLines, "somefilename").isBinary()).isTrue();
    }

    public void testIsBinaryFalse() {
        SearchcodeLib sl = new SearchcodeLib();

        StringBuilder minified = new StringBuilder();
        for (int i=0; i < 256; i++) {
            minified.append("a");
        }
        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add(minified.toString());

        assertThat(sl.isBinary(codeLines, "somefilename").isBinary()).isFalse();
    }

    public void testIsBinaryTrue() {
        SearchcodeLib sl = new SearchcodeLib();

        StringBuilder minified = new StringBuilder();
        for (int i=0; i < 256; i++) {
            minified.append("你");
        }
        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add(minified.toString());

        assertThat(sl.isBinary(codeLines, "somefilename").isBinary()).isTrue();
    }

    public void testIsBinaryWhiteListedExtension() {
        SearchcodeLib sl = new SearchcodeLib();
        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add("你你你你你你你你你你你你你你你你你你你你你你你你你你你");

        FileClassifier fileClassifier = new FileClassifier();

        for (FileClassifierResult fileClassifierResult: fileClassifier.getDatabase()) {
            for (String extension: fileClassifierResult.extensions) {
                BinaryFinding isBinary = sl.isBinary(codeLines, "myfile." + extension);
                assertThat(isBinary.isBinary()).isFalse();
            }
        }
    }

    public void testIsBinaryWhiteListedPropertyExtension() {
        SearchcodeLib sl = new SearchcodeLib();
        sl.WHITE_LIST = new String[1];
        sl.WHITE_LIST[0] = "java";

        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add("你你你你你你你你你你你你你你你你你你你你你你你你你你你");

        assertThat(sl.isBinary(codeLines, "myfile.JAVA").isBinary()).isFalse();
    }

    public void testIsBinaryBlackListedPropertyExtension() {
        FileClassifier fileClassifier = new FileClassifier();
        fileClassifier.setDatabase(new ArrayList<>());

        Data dataMock = mock(Data.class);

        SearchcodeLib sl = new SearchcodeLib(null, fileClassifier, dataMock, new Helpers());

        sl.BLACK_LIST = new String[1];
        sl.BLACK_LIST[0] = "png";

        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add("this file is not binary");

        assertThat(sl.isBinary(codeLines, "myfile.PNG").isBinary()).isTrue();
    }

    public void testIsBinaryEmptyTrue() {
        SearchcodeLib sl = new SearchcodeLib();
        ArrayList<String> codeLines = new ArrayList<>();
        assertThat(sl.isBinary(codeLines, "").isBinary()).isTrue();
    }

    public void testIsBinaryEdge1() {
        SearchcodeLib sl = new SearchcodeLib();

        StringBuilder minified = new StringBuilder();
        for (int i=0; i < 95; i++) {
            minified.append("你");
        }
        minified.append("aaaaa");

        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add(minified.toString());

        assertThat(sl.isBinary(codeLines, "").isBinary()).isTrue();
    }

    public void testIsBinaryEdge2() {
        SearchcodeLib sl = new SearchcodeLib();

        StringBuilder minified = new StringBuilder();
        for (int i=0; i < 96; i++) {
            minified.append("你");
        }
        minified.append("aaaa");

        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add(minified.toString());

        assertThat(sl.isBinary(codeLines, "").isBinary()).isTrue();
    }

    public void testIsBinaryEdge3() {
        SearchcodeLib sl = new SearchcodeLib();

        StringBuilder minified = new StringBuilder();
        for (int i=0; i < 200; i++) {
            minified.append("a");
        }
        ArrayList<String> codeLines = new ArrayList<>();

        for (int i=0; i < 200; i++) {
            codeLines.add(minified.toString());
        }

        assertThat(sl.isBinary(codeLines, "somefilename").isBinary()).isFalse();
    }

    public void testIsMinifiedTrue() {
        SearchcodeLib sl = new SearchcodeLib();

        StringBuilder minified = new StringBuilder();
        for (int i=0; i < 256; i++) {
            minified.append("a");
        }
        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add(minified.toString());

        assertThat(sl.isMinified(codeLines, "something.something")).isTrue();
    }

    public void testIsMinifiedWhiteListAlwaysWins() {
        SearchcodeLib sl = new SearchcodeLib();


        ArrayList<String> whiteList = new ArrayList<>();
        whiteList.add("something");
        sl.WHITE_LIST = whiteList.toArray(new String[whiteList.size()]);

        StringBuilder minified = new StringBuilder();
        for (int i=0; i < 500; i++) {
            minified.append("a");
        }
        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add(minified.toString());

        assertThat(sl.isMinified(codeLines, "something.something")).isFalse();
    }

    public void testIsMinifiedFalse() {
        SearchcodeLib sl = new SearchcodeLib();

        StringBuilder minified = new StringBuilder();
        for (int i=0; i < 255; i++) {
            minified.append("a");
        }
        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add(minified.toString());

        assertThat(sl.isMinified(codeLines, "something.something")).isFalse();
    }

    public void testCodeOwnerSameTimeDifferntCount() {
        SearchcodeLib sl = new SearchcodeLib();

        List<CodeOwner> codeOwners = new ArrayList<>();
        codeOwners.add(new CodeOwner("Ben", 20, 1449809107));
        codeOwners.add(new CodeOwner("Tim", 50, 1449809107));

        String result = sl.codeOwner(codeOwners);
        assertThat(result).isEqualTo("Tim");
    }

    public void testCodeOwnerManyOwners() {
        SearchcodeLib sl = new SearchcodeLib();

        // 86400 seconds in a day
        int daySeconds = 86400;

        long currentUnix = System.currentTimeMillis() / 1000L;

        List<CodeOwner> codeOwners = new ArrayList<>();
        codeOwners.add(new CodeOwner("Ben", 250, ((int) currentUnix - (daySeconds * 22 ))));
        codeOwners.add(new CodeOwner("Steve", 5, ((int) currentUnix - (daySeconds * 50 ))));
        codeOwners.add(new CodeOwner("Tim", 1, (int) currentUnix - (daySeconds * 1)));

        String result = sl.codeOwner(codeOwners);
        assertThat(result).isEqualTo("Tim");
    }

    public void testCodeOwnerManyOwnersFirstWins() {
        SearchcodeLib sl = new SearchcodeLib();

        // 86400 seconds in a day
        int daySeconds = 86400;

        long currentUnix = System.currentTimeMillis() / 1000L;

        List<CodeOwner> codeOwners = new ArrayList<>();
        codeOwners.add(new CodeOwner("Ben", 250,  (int)currentUnix - (daySeconds * 22 )));
        codeOwners.add(new CodeOwner("Steve", 5,  (int)currentUnix - (daySeconds * 50 )));
        codeOwners.add(new CodeOwner("Tim",   1,  (int)currentUnix - (daySeconds * 1  )));
        codeOwners.add(new CodeOwner("Terry", 1,  (int)currentUnix - (daySeconds * 1  )));
        codeOwners.add(new CodeOwner("Zhang", 1,  (int)currentUnix - (daySeconds * 1  )));

        String result = sl.codeOwner(codeOwners);
        assertThat(result).isEqualTo("Tim");
    }

    public void testCodeOwnerManyOwnersRandom() {
        SearchcodeLib sl = new SearchcodeLib();

        // 86400 seconds in a day
        int daySeconds = 86400;

        long currentUnix = System.currentTimeMillis() / 1000L;

        List<CodeOwner> codeOwners = new ArrayList<>();
        codeOwners.add(new CodeOwner("Ben",  40,  (int)currentUnix - (daySeconds * 365 )));
        codeOwners.add(new CodeOwner("Steve", 5,  (int)currentUnix - (daySeconds * 50  )));
        codeOwners.add(new CodeOwner("Tim",   1,  (int)currentUnix - (daySeconds * 1   )));
        codeOwners.add(new CodeOwner("Terry", 1,  (int)currentUnix - (daySeconds * 1   )));
        codeOwners.add(new CodeOwner("Zhang", 8,  (int)currentUnix - (daySeconds * 1   )));

        String result = sl.codeOwner(codeOwners);
        assertThat(result).isEqualTo("Zhang");
    }

    public void testCodeOwnerManyOwnersOldFile() {
        SearchcodeLib sl = new SearchcodeLib();

        // 86400 seconds in a day
        int daySeconds = 86400;

        long currentUnix = System.currentTimeMillis() / 1000L;

        List<CodeOwner> codeOwners = new ArrayList<>();
        codeOwners.add(new CodeOwner("Ben",  40,  (int)currentUnix - (daySeconds * 365 )));
        codeOwners.add(new CodeOwner("Steve", 5,  (int)currentUnix - (daySeconds * 300  )));

        String result = sl.codeOwner(codeOwners);
        assertThat(result).isEqualTo("Ben");
    }

    public void testSplitKeywords() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.splitKeywords("testSplitKeywords");
        assertThat(actual).isEqualTo(" test Split Keywords");
    }

    public void testSplitKeywords2() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.splitKeywords("map.put(\"isCommunity\", ISCOMMUNITY);");
        assertThat(actual).isEqualTo(" is Community");
    }

    public void testSplitKeywords3() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.splitKeywords("TestSplitKeywords");
        assertThat(actual).isEqualTo(" Test Split Keywords");
    }

    public void testInterestingKeywords() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.findInterestingKeywords("PURIFY_EXE=/depot/pure/purify.i386_linux2.7.4.14/purify");
        assertThat(actual).isEqualTo(" i386 linux2.7.4");
    }

    public void testInterestingKeywordsNull() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.findInterestingKeywords(null);
        assertThat(actual).isEqualTo("");
    }

    public void testInterestingCharacters() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.findInterestingCharacters("this 你好 chinese");
        assertThat(actual).contains(" 你 ");
        assertThat(actual).contains(" 好 ");
    }

    public void testInterestingCharactersNullExpectEmpty() {
        SearchcodeLib sl = new SearchcodeLib();
        String actual = sl.findInterestingCharacters(null);
        assertThat(actual).isEqualTo("");
    }


    public void testCountFilteredLinesSingleLine() {
        SearchcodeLib scl = new SearchcodeLib();

        ArrayList<String> lst = new ArrayList<>();
        lst.add("one");
        lst.add("");

        assertThat(scl.countFilteredLines(lst)).isEqualTo(1);
    }

    public void testCountFilteredLinesCommentLines() {
        SearchcodeLib scl = new SearchcodeLib();

        ArrayList<String> lst = new ArrayList<>();
        lst.add("// one");
        lst.add("    // one");
        lst.add("# comment");
        lst.add("    # comment");
        lst.add("");

        assertThat(scl.countFilteredLines(lst)).isEqualTo(0);
    }

    public void testCountFilteredLinesMixCommentLines() {
        SearchcodeLib scl = new SearchcodeLib();

        ArrayList<String> lst = new ArrayList<>();
        lst.add("// one");
        lst.add("    // one");
        lst.add("not a comment");
        lst.add("# comment");
        lst.add("    # comment");
        lst.add("");
        lst.add("Also not a comment but has one // comment");

        assertThat(scl.countFilteredLines(lst)).isEqualTo(2);
    }

    public void testCountFilteredCommentTypes() {
        SearchcodeLib scl = new SearchcodeLib();

        ArrayList<String> lst = new ArrayList<>();
        lst.add("// comment");
        lst.add("# comment");
        lst.add("<!-- comment ");
        lst.add("!* comment");
        lst.add("-- comment");
        lst.add("% comment");
        lst.add("; comment");
        lst.add("/* comment");
        lst.add("* comment");
        lst.add("* comment");
        lst.add("* comment");

        assertThat(scl.countFilteredLines(lst)).isEqualTo(0);
    }

    public void testLanguageCostIgnore() {
        SearchcodeLib scl = new SearchcodeLib();
        assertThat(scl.languageCostIgnore("Text")).isTrue();
        assertThat(scl.languageCostIgnore("JSON")).isTrue();
        assertThat(scl.languageCostIgnore("Unknown")).isTrue();
        assertThat(scl.languageCostIgnore("INI File")).isTrue();
        assertThat(scl.languageCostIgnore("ReStructuredText")).isTrue();
        assertThat(scl.languageCostIgnore("Configuration")).isTrue();
    }

    public void testFormatQueryStringAnd() {
        SearchcodeLib scl = new SearchcodeLib();

        assertThat(scl.formatQueryStringAndDefault("test string")).isEqualTo("test   AND string");
        assertThat(scl.formatQueryStringAndDefault("test string other|")).isEqualTo("test   AND string   AND other\\|");
        assertThat(scl.formatQueryStringAndDefault("test")).isEqualTo("test");
        assertThat(scl.formatQueryStringAndDefault("test  ")).isEqualTo("test");
        assertThat(scl.formatQueryStringAndDefault("    test  ")).isEqualTo("test");
        assertThat(scl.formatQueryStringAndDefault("    test")).isEqualTo("test");
    }

    public void testFormatQueryStringOperators() {
        SearchcodeLib scl = new SearchcodeLib();
        assertEquals("test   AND   string", scl.formatQueryStringAndDefault("test AND string"));
        assertEquals("(test   AND   string)", scl.formatQueryStringAndDefault("(test AND string)"));
    }

    public void testFormatQueryStringDefaultAnd() {
        SearchcodeLib scl = new SearchcodeLib();
        assertEquals("test   AND string", scl.formatQueryStringAndDefault("test string"));
    }

    public void testFormatQueryStringOperatorsOr() {
        SearchcodeLib scl = new SearchcodeLib();
        assertEquals("test  AND  string", scl.formatQueryStringOrDefault("test AND string"));
        assertEquals("(test  AND  string)", scl.formatQueryStringOrDefault("(test AND string)"));
    }

    public void testFormatQueryStringDefaultOr() {
        SearchcodeLib scl = new SearchcodeLib();
        assertEquals("test  string", scl.formatQueryStringOrDefault("test string"));
    }

    public void testGenerateAltQueries() {
        SearchcodeSpellingCorrector spellingCorrector = new SearchcodeSpellingCorrector();
        Data dataMock = mock(Data.class);
        SearchcodeLib scl = new SearchcodeLib(spellingCorrector, null, dataMock, new Helpers());

        assertEquals(0, scl.generateAltQueries("supercalifragilisticexpialidocious").size());
        assertEquals("something", scl.generateAltQueries("something*").get(0));
        assertEquals("a b", scl.generateAltQueries("a* b*").get(0));
    }

    public void testGenerateAltQueriesOther() {
        SearchcodeSpellingCorrector spellingCorrector = new SearchcodeSpellingCorrector();
        Data dataMock = mock(Data.class);
        SearchcodeLib scl = new SearchcodeLib(spellingCorrector, null, dataMock, new Helpers());

        spellingCorrector.putWord("deh");
        assertEquals("dep", scl.generateAltQueries("dep*").get(0));
        assertEquals("deh", scl.generateAltQueries("den*").get(1));
    }

    public void testGenerateAltQueriesAnother() {
        SearchcodeSpellingCorrector spellingCorrector = new SearchcodeSpellingCorrector();
        Data dataMock = mock(Data.class);
        SearchcodeLib scl = new SearchcodeLib(spellingCorrector, null, dataMock, new Helpers());

        spellingCorrector.putWord("ann");
        assertEquals("stuff OR other", scl.generateAltQueries("stuff AND other").get(1));
        assertEquals("stuff other", scl.generateAltQueries("stuff NOT other").get(0));
    }

    public void testGenerateAltQueriesNoDupes() {
        SearchcodeLib scl = new SearchcodeLib();
        assertEquals(1, scl.generateAltQueries("test*").size());
    }

    public void testGenerateAltNeverEmptyString() {
        SearchcodeLib scl = new SearchcodeLib();
        assertEquals(0, scl.generateAltQueries("+").size());
    }

    public void testGenerateBusBlurb() {
        SearchcodeLib scl = new SearchcodeLib();

        List<CodeFacetOwner> codeFacetOwners = new ArrayList<>();
        codeFacetOwners.add(new CodeFacetOwner("Ben", 1));
        List<CodeFacetLanguage> codeFacetLanguages = new ArrayList<>();
        codeFacetLanguages.add(new CodeFacetLanguage("Java", 10));
        codeFacetLanguages.add(new CodeFacetLanguage("Javascript", 8));
        codeFacetLanguages.add(new CodeFacetLanguage("C#", 7));

        String busBlurb = scl.generateBusBlurb(new ProjectStats(10, 1, codeFacetLanguages, null, codeFacetOwners));
        assertThat(busBlurb).contains("In this repository 1 committer has contributed to 1 file.");
        assertThat(busBlurb).contains("The most important languages in this repository are Java, Javascript and C#.");
        assertThat(busBlurb).contains("The project has a low bus factor of 1 and will be in trouble if Ben is hit by a bus.");
    }

    public void testGenerateBusBlurbMore() {
        SearchcodeLib scl = new SearchcodeLib();

        List<CodeFacetOwner> codeFacetOwners = new ArrayList<>();
        codeFacetOwners.add(new CodeFacetOwner("Ben", 6));
        codeFacetOwners.add(new CodeFacetOwner("Terry", 4));
        List<CodeFacetLanguage> codeFacetLanguages = new ArrayList<>();
        codeFacetLanguages.add(new CodeFacetLanguage("Java", 10));

        String busBlurb = scl.generateBusBlurb(new ProjectStats(10, 10, codeFacetLanguages, null, codeFacetOwners));
        assertThat(busBlurb).contains("In this repository 2 committers have contributed to 10 files.");
        assertThat(busBlurb).contains("The most important language in this repository is Java");
        assertThat(busBlurb).contains("The average person who commits this project has ownership of 50% of files.");
        assertThat(busBlurb).contains("The project relies on the following people; Ben, Terry.");
    }

    public void testGenerateBusBlurbStress() {
        SearchcodeLib scl = new SearchcodeLib();

        for (int i=0; i < 1000; i++) {

            List<CodeFacetOwner> codeFacetOwners = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                codeFacetOwners.add(new CodeFacetOwner("" + j, j));
            }


            List<CodeFacetLanguage> codeFacetLanguages = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                codeFacetLanguages.add(new CodeFacetLanguage("" + j, j));
            }

            scl.generateBusBlurb(new ProjectStats(i, i, codeFacetLanguages, null, codeFacetOwners));
        }
    }

    /**
     * Fuzzy testing of the generate alt queries where we try random things to see if we can introduce an exception
     */
    public void testGenerateAltQueriesFuzz() {
        Random rand = new Random();
        SearchcodeLib scl = new SearchcodeLib();

        for(int i = 0; i < 10; i++) {

            StringBuilder bf = new StringBuilder();
            for(int j=0; j < 5; j++) {

                if (j % 2 == 0) {
                    bf.append(RandomStringUtils.randomAscii(rand.nextInt(10) + 1) + " ");
                }
                else {
                    bf.append(RandomStringUtils.randomAlphabetic(rand.nextInt(10) + 1) + " ");
                }

                Singleton.getSpellingCorrector().putWord(RandomStringUtils.randomAlphabetic(rand.nextInt(10) + 1));

                switch(rand.nextInt(5)) {
                    case 1:
                        bf.append(" AND ");
                        break;
                    case 2:
                        bf.append(" OR ");
                        break;
                    case 3:
                        bf.append(" NOT ");
                        break;
                    case 4:
                        bf.append(RandomStringUtils.randomAlphabetic(rand.nextInt(10) + 1));
                        break;
                    default:
                        break;
                }
            }

            scl.generateAltQueries(bf.toString());
        }
    }
}