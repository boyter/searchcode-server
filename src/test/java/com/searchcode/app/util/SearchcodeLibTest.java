package com.searchcode.app.util;

import com.searchcode.app.dao.Data;
import com.searchcode.app.dto.*;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.dto.FileClassifierResult;
import junit.framework.TestCase;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;

public class SearchcodeLibTest extends TestCase {

    public void testCleanPipeline() {
        SearchCodeLib sl = new SearchCodeLib();
        String actual = sl.codeCleanPipeline("{AB3FBE3A-410C-4FB2-84E0-B2D3434D1995}");
        assertThat(actual).contains(" AB3FBE3A-410C-4FB2-84E0-B2D3434D1995 ");
        assertThat(actual).contains(" {AB3FBE3A-410C-4FB2-84E0-B2D3434D1995} ");
        assertThat(actual).contains(" AB3FBE3A 410C 4FB2 84E0 B2D3434D1995 ");
    }

    public void testCleanPipelineTwo() {
        SearchCodeLib sl = new SearchCodeLib();
        String actual = sl.codeCleanPipeline("\"_updatedDate\"");

        assertThat(actual).contains(" _updatedDate ");
        assertThat(actual).contains(" updatedDate ");
        assertThat(actual).contains(" \"_updatedDate\" ");
    }

    public void testCleanPipelineThree() {
        SearchCodeLib sl = new SearchCodeLib();
        String actual = sl.codeCleanPipeline("'shop_order_log',");

        assertThat(actual.contains(" 'shop_order_log' ")).isTrue();
    }

    public void testCleanPipelineDots() {
        SearchCodeLib sl = new SearchCodeLib();
        String actual = sl.codeCleanPipeline("actual.contains");

        assertThat(actual).contains(" actual.contains ");
    }

    public void testCleanPipelineCustom() {
        SearchCodeLib sl = new SearchCodeLib();
        String actual = sl.codeCleanPipeline("context.config.URL_REWRITE.iteritems():");

        assertThat(actual).contains(" URL_REWRITE ");
    }

    public void testCleanPipelineCustom2() {
        SearchCodeLib sl = new SearchCodeLib();
        String actual = sl.codeCleanPipeline("task :install_something do");
        assertThat(actual).contains(" install_something ");
    }

    public void testCodeCleanPipelineIssue165() {
        SearchCodeLib searchCodeLib = new SearchCodeLib();
        String actual = searchCodeLib.codeCleanPipeline("handler: com.origin.lambda.Main::hourlySummary");
        assertThat(actual).contains(" hourlySummary ");
    }

    public void testCodeCleanPipelineIssue188() {
        SearchCodeLib searchCodeLib = new SearchCodeLib();
        String actual = searchCodeLib.codeCleanPipeline("PhysicsServer::get_singleton()->area_set_monitorable(get_rid(), monitorable);");
        assertThat(actual).contains(" PhysicsServer::get_singleton ");
    }

    public void testIsBinaryBlackListWithNoDotFileName() {
        SearchCodeLib sl = new SearchCodeLib();

        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add("a");

        sl.BLACK_LIST = new String[1];
        sl.BLACK_LIST[0] = "license";

        assertThat(sl.isBinary(codeLines, "license").isBinary()).isTrue();
    }

    public void testIsBinary() {
        SearchCodeLib sl = new SearchCodeLib();

        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add("a");

        assertThat(sl.isBinary(codeLines, "somefilename").isBinary()).isFalse();
    }

    public void testIsBinaryFalse() {
        SearchCodeLib sl = new SearchCodeLib();

        StringBuilder minified = new StringBuilder();
        for (int i=0; i < 256; i++) {
            minified.append("a");
        }
        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add(minified.toString());

        assertThat(sl.isBinary(codeLines, "somefilename").isBinary()).isFalse();
    }

    public void testIsBinaryTrue() {
        SearchCodeLib sl = new SearchCodeLib();

        StringBuilder minified = new StringBuilder();
        for (int i=0; i < 256; i++) {
            minified.append("你");
        }
        char nul = 0;
        minified.append(nul);
        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add(minified.toString());

        assertThat(sl.isBinary(codeLines, "somefilename").isBinary()).isTrue();
    }

    public void testIsBinaryWhiteListedExtension() {
        SearchCodeLib sl = new SearchCodeLib();
        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add("你你你你你你你你你你你你你你你你你你你你你你你你你你你");
        char nul = 0;
        codeLines.add("" + nul);

        FileClassifier fileClassifier = new FileClassifier();

        HashMap<String, FileClassifierResult> database = fileClassifier.getDatabase();

        for (String key: database.keySet()) {
            FileClassifierResult fileClassifierResult = database.get(key);
            for (String extension: fileClassifierResult.extensions) {
                BinaryFinding isBinary = sl.isBinary(codeLines, "myfile." + extension);
                assertThat(isBinary.isBinary()).isFalse();
            }
        }
    }

    public void testIsBinaryWhiteListedPropertyExtension() {
        SearchCodeLib sl = new SearchCodeLib();
        sl.WHITE_LIST = new String[1];
        sl.WHITE_LIST[0] = "java";

        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add("你你你你你你你你你你你你你你你你你你你你你你你你你你你");
        char nul = 0;
        codeLines.add("" + nul);

        assertThat(sl.isBinary(codeLines, "myfile.JAVA").isBinary()).isFalse();
    }

    public void testIsBinaryBlackListedPropertyExtension() {
        FileClassifier fileClassifier = new FileClassifier();
        fileClassifier.setDatabase(new HashMap<>());

        Data dataMock = mock(Data.class);

        SearchCodeLib sl = new SearchCodeLib(null, fileClassifier, dataMock, new Helpers());

        sl.BLACK_LIST = new String[1];
        sl.BLACK_LIST[0] = "png";

        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add("this file is not binary");

        assertThat(sl.isBinary(codeLines, "myfile.PNG").isBinary()).isTrue();
    }

    public void testIsBinaryEmptyTrue() {
        SearchCodeLib sl = new SearchCodeLib();
        ArrayList<String> codeLines = new ArrayList<>();
        assertThat(sl.isBinary(codeLines, "").isBinary()).isTrue();
    }

    public void testIsBinaryNullByte() {
        SearchCodeLib sl = new SearchCodeLib();

        ArrayList<String> codeLines = new ArrayList<>();
        char nul = 0;
        codeLines.add("" + nul);

        assertThat(sl.isBinary(codeLines, "somefilename").isBinary()).isTrue();
    }

    public void testIsMinifiedTrue() {
        SearchCodeLib sl = new SearchCodeLib();

        StringBuilder minified = new StringBuilder();
        for (int i=0; i < 256; i++) {
            minified.append("a");
        }
        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add(minified.toString());

        assertThat(sl.isMinified(codeLines, "something.something")).isTrue();
    }

    public void testIsMinifiedWhiteListAlwaysWins() {
        SearchCodeLib sl = new SearchCodeLib();


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
        SearchCodeLib sl = new SearchCodeLib();

        StringBuilder minified = new StringBuilder();
        for (int i=0; i < 255; i++) {
            minified.append("a");
        }
        ArrayList<String> codeLines = new ArrayList<>();
        codeLines.add(minified.toString());

        assertThat(sl.isMinified(codeLines, "something.something")).isFalse();
    }

    public void testCodeOwnerSameTimeDifferntCount() {
        SearchCodeLib sl = new SearchCodeLib();

        List<CodeOwner> codeOwners = new ArrayList<>();
        codeOwners.add(new CodeOwner("Ben", 20, 1449809107));
        codeOwners.add(new CodeOwner("Tim", 50, 1449809107));

        String result = sl.codeOwner(codeOwners);
        assertThat(result).isEqualTo("Tim");
    }

    public void testCodeOwnerManyOwners() {
        SearchCodeLib sl = new SearchCodeLib();

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
        SearchCodeLib sl = new SearchCodeLib();

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
        SearchCodeLib sl = new SearchCodeLib();

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
        SearchCodeLib sl = new SearchCodeLib();

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
        SearchCodeLib sl = new SearchCodeLib();
        String actual = sl.splitKeywords("testSplitKeywords", false);
        assertThat(actual).contains("test Split Keywords");
    }

    public void testSplitKeywords2() {
        SearchCodeLib sl = new SearchCodeLib();
        String actual = sl.splitKeywords("map.put(\"isCommunity\", ISCOMMUNITY);", false);
        assertThat(actual).contains("is Community");
    }

    public void testSplitKeywords3() {
        SearchCodeLib sl = new SearchCodeLib();
        String actual = sl.splitKeywords("TestSplitKeywords", false);
        assertThat(actual).contains("Test Split Keywords");
    }

    public void testSplitKeywords4() {
        SearchCodeLib sl = new SearchCodeLib();
        String actual = sl.splitKeywords("SimpleThreadPool", true);
        assertThat(actual).contains(" SimpleThread ");
    }

    public void testSplitKeywords5() {
        SearchCodeLib sl = new SearchCodeLib();
        String actual = sl.splitKeywords("SimpleThreadPool", false);
        assertThat(actual).doesNotContain("SimpleThread");
    }

    public void testInterestingKeywords() {
        SearchCodeLib sl = new SearchCodeLib();
        String actual = sl.findInterestingKeywords("PURIFY_EXE=/depot/pure/purify.i386_linux2.7.4.14/purify");
        assertThat(actual).isEqualTo(" i386 linux2.7.4");
    }

    public void testInterestingKeywordsNull() {
        SearchCodeLib sl = new SearchCodeLib();
        String actual = sl.findInterestingKeywords(null);
        assertThat(actual).isEqualTo("");
    }

    public void testInterestingCharacters() {
        SearchCodeLib sl = new SearchCodeLib();
        String actual = sl.findInterestingCharacters("this 你好 chinese");
        assertThat(actual).contains(" 你 ");
        assertThat(actual).contains(" 好 ");
    }

    public void testInterestingCharactersNullExpectEmpty() {
        SearchCodeLib sl = new SearchCodeLib();
        String actual = sl.findInterestingCharacters(null);
        assertThat(actual).isEqualTo("");
    }


    public void testCountFilteredLinesSingleLine() {
        SearchCodeLib scl = new SearchCodeLib();

        ArrayList<String> lst = new ArrayList<>();
        lst.add("one");
        lst.add("");

        assertThat(scl.countFilteredLines(lst)).isEqualTo(1);
    }

    public void testCountFilteredLinesCommentLines() {
        SearchCodeLib scl = new SearchCodeLib();

        ArrayList<String> lst = new ArrayList<>();
        lst.add("// one");
        lst.add("    // one");
        lst.add("# comment");
        lst.add("    # comment");
        lst.add("");

        assertThat(scl.countFilteredLines(lst)).isEqualTo(0);
    }

    public void testCountFilteredLinesMixCommentLines() {
        SearchCodeLib scl = new SearchCodeLib();

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
        SearchCodeLib scl = new SearchCodeLib();

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
        SearchCodeLib scl = new SearchCodeLib();
        assertThat(scl.languageCostIgnore("Text")).isTrue();
        assertThat(scl.languageCostIgnore("JSON")).isTrue();
        assertThat(scl.languageCostIgnore("Unknown")).isTrue();
        assertThat(scl.languageCostIgnore("INI File")).isTrue();
        assertThat(scl.languageCostIgnore("ReStructuredText")).isTrue();
        assertThat(scl.languageCostIgnore("Configuration")).isTrue();
    }

    public void testFormatQueryStringAnd() {
        SearchCodeLib scl = new SearchCodeLib();

        assertThat(scl.formatQueryStringAndDefault("test string")).isEqualTo("test   AND string");
        assertThat(scl.formatQueryStringAndDefault("test string other|")).isEqualTo("test   AND string   AND other\\|");
        assertThat(scl.formatQueryStringAndDefault("test")).isEqualTo("test");
        assertThat(scl.formatQueryStringAndDefault("test  ")).isEqualTo("test");
        assertThat(scl.formatQueryStringAndDefault("    test  ")).isEqualTo("test");
        assertThat(scl.formatQueryStringAndDefault("    test")).isEqualTo("test");
    }

    public void testFormatQueryStringOperators() {
        SearchCodeLib scl = new SearchCodeLib();
        assertEquals("test   AND   string", scl.formatQueryStringAndDefault("test AND string"));
        assertEquals("(test   AND   string)", scl.formatQueryStringAndDefault("(test AND string)"));
    }

    public void testFormatQueryStringDefaultAnd() {
        SearchCodeLib scl = new SearchCodeLib();
        assertEquals("test   AND string", scl.formatQueryStringAndDefault("test string"));
    }

    public void testFormatQueryStringOperatorsOr() {
        SearchCodeLib scl = new SearchCodeLib();
        assertEquals("test  AND  string", scl.formatQueryStringOrDefault("test AND string"));
        assertEquals("(test  AND  string)", scl.formatQueryStringOrDefault("(test AND string)"));
    }

    public void testFormatQueryStringDefaultOr() {
        SearchCodeLib scl = new SearchCodeLib();
        assertEquals("test  string", scl.formatQueryStringOrDefault("test string"));
    }

    public void testGenerateAltQueries() {
        SearchcodeSpellingCorrector spellingCorrector = new SearchcodeSpellingCorrector();
        Data dataMock = mock(Data.class);
        SearchCodeLib scl = new SearchCodeLib(spellingCorrector, null, dataMock, new Helpers());

        assertEquals(0, scl.generateAltQueries("supercalifragilisticexpialidocious").size());
        assertEquals("something", scl.generateAltQueries("something*").get(0));
        assertEquals("a b", scl.generateAltQueries("a* b*").get(0));
    }

    public void testGenerateAltQueriesOther() {
        SearchcodeSpellingCorrector spellingCorrector = new SearchcodeSpellingCorrector();
        Data dataMock = mock(Data.class);
        SearchCodeLib scl = new SearchCodeLib(spellingCorrector, null, dataMock, new Helpers());

        spellingCorrector.putWord("deh");
        assertEquals("dep", scl.generateAltQueries("dep*").get(0));
        assertEquals("deh", scl.generateAltQueries("den*").get(1));
    }

    public void testGenerateAltQueriesAnother() {
        SearchcodeSpellingCorrector spellingCorrector = new SearchcodeSpellingCorrector();
        Data dataMock = mock(Data.class);
        SearchCodeLib scl = new SearchCodeLib(spellingCorrector, null, dataMock, new Helpers());

        spellingCorrector.putWord("ann");
        assertEquals("stuff OR other", scl.generateAltQueries("stuff AND other").get(1));
        assertEquals("stuff other", scl.generateAltQueries("stuff NOT other").get(0));
    }

    public void testGenerateAltQueriesNoDupes() {
        SearchCodeLib scl = new SearchCodeLib();
        assertEquals(1, scl.generateAltQueries("test*").size());
    }

    public void testGenerateAltNeverEmptyString() {
        SearchCodeLib scl = new SearchCodeLib();
        assertEquals(0, scl.generateAltQueries("+").size());
    }

    public void testGenerateBusBlurb() {
        SearchCodeLib scl = new SearchCodeLib();

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
        SearchCodeLib scl = new SearchCodeLib();

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
        SearchCodeLib scl = new SearchCodeLib();

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
        SearchCodeLib scl = new SearchCodeLib();

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