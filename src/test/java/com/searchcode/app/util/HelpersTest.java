package com.searchcode.app.util;

import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.Singleton;
import junit.framework.TestCase;
import org.mockito.Mockito;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class HelpersTest extends TestCase {

    private Helpers helpers;

    public void setUp() {
        this.helpers = new Helpers();
    }

    public void testReadFileLines() throws FileNotFoundException {
        List<String> result = this.helpers.readFileLines("./README.md", 10);
        assertEquals(10, result.size());

        result = Singleton.getHelpers().readFileLines("./README.md", 5);
        assertEquals(5, result.size());
    }

    public void testReadFileLinesIssue168() throws IOException {
//        List<String> result = this.helpers.readFileLines("./assets/integration_test/odd_files/no_newlines", 10);
//        assertEquals(3, result.size());

        StringBuilder stringBuilder = new StringBuilder();

        Reader reader = new FileReader("./assets/integration_test/odd_files/no_newlines");
        try {
            char[] chars = new char[8192];
            for (int len; (len = reader.read(chars)) > 0;) {
                // process chars.
                stringBuilder.append(String.copyValueOf(chars));
            }


        } finally {
            reader.close();
        }

        String temp = stringBuilder.toString();
        int length = temp.length();
        System.out.println(length);
    }

    public void testIsNullEmptyOrWhitespace() {
        assertTrue(this.helpers.isNullEmptyOrWhitespace(null));
        assertTrue(this.helpers.isNullEmptyOrWhitespace(""));
        assertTrue(this.helpers.isNullEmptyOrWhitespace("   "));
        assertFalse(this.helpers.isNullEmptyOrWhitespace("test"));
    }

    public void testGetLogPath() {
        String result = this.helpers.getLogPath();
        assertThat(result).isNotEmpty();
    }

    public void testSortByValue() {
        Random random = new Random(System.currentTimeMillis());
        Map<String, Integer> testMap = new HashMap<>(1000);
        for (int i = 0 ; i < 1000 ; ++i) {
            testMap.put( "SomeString" + random.nextInt(), random.nextInt());
        }

        testMap = this.helpers.sortByValue( testMap );
        assertEquals(1000, testMap.size());

        Integer previous = null;
        for (Map.Entry<String, Integer> entry : testMap.entrySet()) {
            assertNotNull(entry.getValue());
            if (previous != null) {
                assertTrue(entry.getValue() >= previous);
            }
            previous = entry.getValue();
        }
    }

    public void testIgnoreFilesDefaults() {
        assertThat(this.helpers.ignoreFiles("/git")).isFalse();

        assertThat(this.helpers.ignoreFiles("/.git")).isTrue();
        assertThat(this.helpers.ignoreFiles("/.git/")).isTrue();
        assertThat(this.helpers.ignoreFiles(".git/")).isTrue();
        assertThat(this.helpers.ignoreFiles(".git")).isTrue();

        assertThat(this.helpers.ignoreFiles("/.svn")).isTrue();
        assertThat(this.helpers.ignoreFiles("/.svn/")).isTrue();
    }

    public void testIgnoreFilesBlackListNoResults() {
        java.util.Properties mockProperties = Mockito.mock(java.util.Properties.class);
        when(mockProperties.get(any())).thenReturn("");

        this.helpers = new Helpers(mockProperties);

        assertThat(this.helpers.ignoreFiles("/target")).isFalse();
    }

    public void testIgnoreFilesBlackListSingleResult() {
        java.util.Properties mockProperties = Mockito.mock(java.util.Properties.class);
        when(mockProperties.get(any())).thenReturn("target");

        this.helpers = new Helpers(mockProperties);

        assertThat(this.helpers.ignoreFiles("./target/")).isTrue();
        assertThat(this.helpers.ignoreFiles("/target/")).isTrue();
        assertThat(this.helpers.ignoreFiles("/target")).isTrue();

        assertThat(this.helpers.ignoreFiles("target")).isFalse();
    }

    public void testTryParseInt() {
        this.helpers = new Helpers();
        assertThat(this.helpers.tryParseInt(null, "0")).isZero();
    }

    public void testTryParseDouble() {
        this.helpers = new Helpers();
        assertThat(this.helpers.tryParseDouble(null, "0")).isZero();
        assertThat(this.helpers.tryParseDouble("0", "0")).isZero();
    }

    public void testReplaceForIndex() {
        assertThat(this.helpers.replaceForIndex("Something")).isEqualTo("something");
        assertThat(this.helpers.replaceForIndex("Something123")).isEqualTo("something123");
        assertThat(this.helpers.replaceForIndex("Something.123")).isEqualTo("something_123");
        assertThat(this.helpers.replaceForIndex("Something 123")).isEqualTo("something_123");
    }

    public void testReplaceNonAlphanumeric() {
        this.helpers = new Helpers();
        assertThat(this.helpers.replaceNonAlphanumeric("Something", "")).isEqualTo("Something");
        assertThat(this.helpers.replaceNonAlphanumeric("Something123", "")).isEqualTo("Something123");
        assertThat(this.helpers.replaceNonAlphanumeric("Something.123", "")).isEqualTo("Something123");
        assertThat(this.helpers.replaceNonAlphanumeric("Something.123", "_")).isEqualTo("Something_123");
        assertThat(this.helpers.replaceNonAlphanumeric("Something 123", "_")).isEqualTo("Something_123");
    }

    public void testAllUnique() {
        assertThat(this.helpers.allUnique(new ArrayList<String>() {{
            add("a");
            add("a");
        }})).isFalse();

        assertThat(this.helpers.allUnique(new ArrayList<String>() {{
            add("a");
            add("b");
        }})).isTrue();

        assertThat(this.helpers.allUnique(new ArrayList<String>() {{
            add("a");
            add("b");
            add("b");
        }})).isFalse();
    }

    public void testFilterRunningAndDeletedRepoJobs() {
        assertThat(this.helpers.filterRunningAndDeletedRepoJobs(new ArrayList<>()).size()).isEqualTo(0);
        assertThat(this.helpers.filterRunningAndDeletedRepoJobs(new ArrayList<RepoResult>() {{
            add(new RepoResult(0, "reallyreallyuniquename", "", "", "", "", "", "", ""));
        }}).size()).isEqualTo(1);
    }
}
