package com.searchcode.app.service;

import com.searchcode.app.dto.CodeMatchResult;
import com.searchcode.app.dto.CodeResult;
import junit.framework.TestCase;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class CodeMatcherTest extends TestCase {

    public void testFormatResults() {
        CodeMatcher cm = new CodeMatcher();

        List<CodeResult> codeResults = new ArrayList<>();
        List<String> code = new ArrayList<>();
        code.add("this is some test code");
        code.add("List<String> something = *p;");
        code.add("List<String> test = *p;");
        code.add("this does not match at all");

        codeResults.add(new CodeResult(code, null));

        cm.formatResults(codeResults, "List<String> test = *p;", true);
    }

    public void testMatchResults() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<String>();
        matchTerms.add("code");
        matchTerms.add("this");
        matchTerms.add("is");

        List<String> code = new ArrayList<>();
        code.add("this is");
        code.add("some code");

        cm.matchResults(code, matchTerms, true);
    }

    public void testMatchResultsBadCase() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<String>();
        matchTerms.add("eval");
        matchTerms.add("$_GET");

        List<String> code = new ArrayList<>();
        code.add("<?php eval($_GET['id']); ?>\n");
        code.add("fatal-error\n");

        List<CodeMatchResult> codeMatchResults = cm.findMatchingLines(code, matchTerms, true);

        assertEquals(2, codeMatchResults.size());
        assertEquals("fatal-error\n", codeMatchResults.get(1).line);
    }

    public void testMatchResultsVerifySorting() {
        CodeMatcher codeMatcher = new CodeMatcher();
        codeMatcher.MATCHLINES = 2;

        List<String> matchTerms = new ArrayList<>();
        matchTerms.add("test");
        matchTerms.add("test1");
        matchTerms.add("test2");

        List<String> code = new ArrayList<>();
        code.add("test\n");
        code.add("test\n");
        code.add("test\n");
        code.add("test\n");
        code.add("test\n");
        code.add("test\n");
        code.add("test\n");
        code.add("test\n");
        code.add("test\n");
        code.add("test\n");
        code.add("test\n");
        code.add("test\n");
        code.add("test\n");
        code.add("test test1\n");
        code.add("test test1 test2\n");

        List<CodeMatchResult> codeMatchResults = codeMatcher.matchResults(code, matchTerms, true);
        assertThat(codeMatchResults.get(0).getLine()).isEqualTo("<strong>test</strong> <strong>test1</strong>\n");
        assertThat(codeMatchResults.get(1).getLine()).isEqualTo("<strong>test</strong> <strong>test1</strong> <strong>test2</strong>\n");
    }

    public void testHighlightLineMultiNonOverlapping() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<String>();
        matchTerms.add("validate");
        matchTerms.add("data");

        String result = cm.highlightLine("def validate(data)", matchTerms);
        assertEquals("def <strong>validate</strong>(<strong>data</strong>)", result);
    }

    public void testHighlightLineWildcardMiddleString() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<>();
        matchTerms.add("data*");

        String result = cm.highlightLine("expect(data).to_be_empty()", matchTerms);
        assertEquals("expect(<strong>data).to_be_empty()</strong>", result);
    }

    public void testHighlightLineIgnoresOperators() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<>();
        matchTerms.add("AND");
        matchTerms.add("OR");
        matchTerms.add("NOT");
        matchTerms.add("code");

        String result = cm.highlightLine("code and or not", matchTerms);
        assertEquals("<strong>code</strong> and or not", result);
    }

    public void testHighlightLineWildcardMiddleStringWithBracket() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<>();
        matchTerms.add("data*)");

        String result = cm.highlightLine("expect(data).to_be_empty()", matchTerms);
        assertEquals("expect(<strong>data).to_be_empty()</strong>", result);
    }

    // TODO this is a case that should work but currently does not, need to investigate
//    public void testHighlightLineMultipleMatches() {
//        CodeMatcher cm = new CodeMatcher();
//        List<String> matchTerms = new ArrayList<>();
//        matchTerms.add("create");
//        matchTerms.add("my");
//        matchTerms.add("own");
//        matchTerms.add("storage");
//
//        String result = cm.highlightLine("create_my_own_storage", matchTerms);
//        assertEquals("<strong>create</strong>_<strong>my</strong>_<strong>own</strong>_<strong>storage</strong>", result);
//    }

    public void testHighlightLineExtended() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<String>();
        matchTerms.add("code*");

        String result = cm.highlightLine("codesomething", matchTerms);
        assertEquals("<strong>codesomething</strong>", result);
    }

    public void testHighlightLineExtendedTwo() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<String>();
        matchTerms.add("code*");

        String result = cm.highlightLine("codesomething another thing", matchTerms);
        assertEquals("<strong>codesomething</strong> another thing", result);
    }

    public void testHighlightLineExtendedThree() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<String>();
        matchTerms.add("code*");

        String result = cm.highlightLine("codesomething another codething", matchTerms);
        assertEquals("<strong>codesomething</strong> another <strong>codething</strong>", result);
    }

    public void testHighlightLineExtendedErrorOne() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<String>();
        matchTerms.add("b*");

        String result = cm.highlightLine("This is meant to be a really small repo", matchTerms);
        assertEquals("This is meant to <strong>be</strong> a really small repo", result);
    }

    public void testHighlightLineExtendedErrorTwo() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<String>();
        matchTerms.add("t*");

        String result = cm.highlightLine("this that", matchTerms);
        assertEquals("<strong>this</strong> <strong>that</strong>", result);
    }

    public void testHighlightLineExtendedErrorThree() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<String>();
        matchTerms.add("t*");
        matchTerms.add("a*");

        String result = cm.highlightLine("this that", matchTerms);
        assertEquals("<strong>this</strong> <strong>that</strong>", result);
    }


    public void testHighlightLineSimple() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<String>();
        matchTerms.add("code");

        String result = cm.highlightLine("code", matchTerms);
        assertEquals("<strong>code</strong>", result);
    }

    public void testHighlightLineEscape() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<String>();
        matchTerms.add("code");

        String result = cm.highlightLine("<code", matchTerms);
        assertEquals("&lt;<strong>code</strong>", result);
    }

    public void testHighlightLineEscapeCaseIgnore() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<String>();
        matchTerms.add("TESTINDEX");

        String result = cm.highlightLine("public  void testIndex() {", matchTerms);
        assertEquals("public  void <strong>testIndex</strong>() {", result);
    }

    public void testHighlightLineMultipleTerms() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<String>();
        matchTerms.add("code");
        matchTerms.add("this");
        matchTerms.add("is");

        String result = cm.highlightLine("this is", matchTerms);
        assertEquals("<strong>this</strong> <strong>is</strong>", result);
    }

    public void testHighlightLineNoOverlapMultipleTerms() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<String>();
        matchTerms.add("t");
        matchTerms.add("h");
        matchTerms.add("i");
        matchTerms.add("s");
        matchTerms.add("this");

        String result = cm.highlightLine("this qwer", matchTerms);
        assertEquals("<strong>this</strong> qwer", result);
    }

    public void testHighlightLineFuzzTest() {
        Random rand = new Random();
        CodeMatcher cm = new CodeMatcher();

        for(int i = 0; i < 1000; i++) {

            List<String> matchTerms = new ArrayList<String>();

            for(int j=0; j < rand.nextInt(10) + 1; j++) {
                matchTerms.add(RandomStringUtils.randomAlphabetic(rand.nextInt(10) + 1) + "*");
            }

            StringBuilder bf = new StringBuilder();
            for(int j=0; j < rand.nextInt(1000) + 1; j++) {
                bf.append(RandomStringUtils.randomAlphabetic(rand.nextInt(20) + 1)).append(" ");
            }

            cm.highlightLine(bf.toString(), matchTerms);
        }
    }

    /**
     * Fuzzy testing to catch random highlight issues
     */

    public void testHighlightLineEscapeFuzz() {
        Random rand = new Random();
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms;

        for(int i = 0; i < 1000; i++) {
            matchTerms = new ArrayList<String>();
            matchTerms.add(RandomStringUtils.randomAscii(rand.nextInt(1) + 1).replace("*", "A"));
            matchTerms.add(RandomStringUtils.randomAscii(rand.nextInt(2) + 1).replace("*", "A"));
            matchTerms.add(RandomStringUtils.randomAscii(rand.nextInt(5) + 1).replace("*", "A"));
            matchTerms.add(RandomStringUtils.randomAscii(rand.nextInt(10) + 1).replace("*", "A"));
            matchTerms.add(RandomStringUtils.randomAscii(rand.nextInt(15) + 1).replace("*", "A"));


            String line = RandomStringUtils.randomAscii(rand.nextInt(1000) + 1);
            cm.highlightLine(line, matchTerms);
        }
    }

    public void testHighlightLineIssue88() {
        Random rand = new Random();
        CodeMatcher cm = new CodeMatcher();

        List<String> matchTerms = new ArrayList<String>() {{
            add("q");
        }};

        String line = "???     o?`♂▲?z♠r☺??????GO??♦l????%=2↔♫??♥?☻←?B?rv??y?pW??@??~?]z???s?RK/??♫?♂♦ Yb?♀?\"f?m?o???o???9?►ZB????*?3?K`?▲?2???⌂??▬[l???f?}??~???5.??&]?u[????,??Y??♦)??/??h??'?b?=?3?;??▲{♀?♂$????1???ErV>j?\n" +
                "???$Ñ??9?W?D!q q q??8? d?i??H5♣?♦-";
        cm.highlightLine(line, matchTerms);

        line = "q";
        for (int i = 0; i < 1000; i++) {
            line += "q";
            cm.highlightLine(line, matchTerms);
        }

        StringBuilder bf = new StringBuilder();
        for(int j=0; j < 1000 + 1; j++) {
            bf.append(RandomStringUtils.randomAlphabetic(rand.nextInt(20) + 1)).append(" ");
        }

        cm.highlightLine(bf.toString(), matchTerms);

        bf = new StringBuilder();
        for(int j=0; j < 1000 + 1; j++) {
            bf.append(RandomStringUtils.random(rand.nextInt(20) + 1)).append(" ").append("q");
        }

        cm.highlightLine(bf.toString(), matchTerms);
    }


    /**
     * This tests the worst possible case of matching were the only match is literally at the end of the string
     * but there is a lot to check in each string.
     * Included because findMatchingLines is the slowest method and drags down the user experience if too slow
     * The longest the worst possible case should take to run is 1 wall clock second
     */
    public void testFindMatchingLinesPerformance() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<String>();
        matchTerms.add("code");
        matchTerms.add("this");

        // Simulates the worst possible case where the matching lines are right
        // at the end
        String addString = "some additional stuff that random stuff that should not match but force it to work a bit harder then it normally would";
        List<String> code = new ArrayList<>();
        for (int i = 0; i < 9999; i++) {
            StringBuilder stringBuilder = new StringBuilder();

            for(int j = 0; j < 5; j++) {
                stringBuilder.append(addString);
            }

            code.add(stringBuilder.toString());
        }
        code.add("this is some code");

        Instant start = Instant.now();
        List<CodeMatchResult> result = cm.findMatchingLines(code, matchTerms, true);

        assertThat(result).isNotNull(); // Force no optimisations by the JVM
        long seconds = Duration.between(start, Instant.now()).getSeconds();
        assertThat(seconds).isLessThanOrEqualTo(5);
    }

    public void testFindMatchingLines() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<String>();
        matchTerms.add("code");
        matchTerms.add("this");

        List<String> code = new ArrayList<>();
        code.add("this is some code");


        List<CodeMatchResult> result = cm.findMatchingLines(code, matchTerms, true);

        assertEquals(1, result.size());
        assertEquals("<strong>this</strong> is some <strong>code</strong>", result.get(0).getLine());
    }

    public void testFindMatchingLines2() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<String>();
        matchTerms.add("code");
        matchTerms.add("this");

        List<String> code = new ArrayList<>();
        code.add("this code is");
        code.add("some code");

        List<CodeMatchResult> result = cm.findMatchingLines(code, matchTerms, true);

        assertEquals(2, result.size());
        assertEquals("<strong>this</strong> <strong>code</strong> is", result.get(0).getLine());
        assertTrue(result.get(0).isMatching());
        assertFalse(result.get(0).isAddBreak());
        assertEquals(0, result.get(0).getLineNumber());
        assertEquals(1, result.get(1).getLineNumber());

    }

    public void testFindMatchingLines3() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<>();
        matchTerms.add("code");
        matchTerms.add("this");

        List<String> code = new ArrayList<>();
        code.add("this code is");
        code.add("some code");
        code.add("code");

        List<CodeMatchResult> result = cm.findMatchingLines(code, matchTerms, true);

        assertEquals(3, result.size());
    }


    public void testFindBestMatchingLinesOver1000Limit() {
        CodeMatcher cm = new CodeMatcher();
        List<String> matchTerms = new ArrayList<>();
        matchTerms.add("re.compile");
        matchTerms.add("compile");
        matchTerms.add("re");

        List<String> code = new ArrayList<>();
        for(int i=0;i<1000;i++) {
            code.add("re");
        }
        code.add("re.compile");


        List<CodeMatchResult> result = cm.findMatchingLines(code, matchTerms, true);

        // What we want to find here is the re.compile line since it is the best match even though the others
        // are also matches
        boolean found = false;

        for (CodeMatchResult line: result) {
            if (line.getLine().contains("re.compile")) {
                found = true;
            }
        }

        assertThat(found).isTrue();
    }

    public void testSplitTerms() {
        CodeMatcher cm = new CodeMatcher();
        List<String> strings = cm.splitTerms("these are some terms");
        assertEquals(4, strings.size());
    }

    public void testSplitTermsSingleSpace() {
        CodeMatcher cm = new CodeMatcher();
        List<String> strings = cm.splitTerms("these ");
        assertEquals(1, strings.size());
    }

    public void testSplitTermsWildCardAllowed() {
        CodeMatcher cm = new CodeMatcher();
        List<String> strings = cm.splitTerms("these*");
        assertEquals(1, strings.size());
        assertEquals("these*", strings.get(0));
    }

    public void testSplitTermsWildCardAllowedMultiple() {
        CodeMatcher cm = new CodeMatcher();
        List<String> strings = cm.splitTerms("these* are* wildcard* search* s*");
        assertEquals(5, strings.size());

        assertTrue(strings.contains("these*"));
        assertTrue(strings.contains("are*"));
        assertTrue(strings.contains("wildcard*"));
        assertTrue(strings.contains("search*"));
        assertTrue(strings.contains("s*"));
    }

    public void testSplitTermsOrder() {
        CodeMatcher cm = new CodeMatcher();
        List<String> strings = cm.splitTerms("ttt t tt");
        assertThat(strings).hasSize(3);
        assertThat(strings.get(0)).isEqualTo("ttt");
        assertThat(strings.get(1)).isEqualTo("tt");
        assertThat(strings.get(2)).isEqualTo("t");
    }
}