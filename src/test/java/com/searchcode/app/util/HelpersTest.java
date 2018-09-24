package com.searchcode.app.util;

import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.Singleton;
import junit.framework.TestCase;
import org.mockito.Mockito;

import java.io.*;
import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class HelpersTest extends TestCase {

    private Helpers helpers;

    public void setUp() {
        this.helpers = new Helpers();
    }

    public void testReadFileLines() throws IOException {
        List<String> result = this.helpers.readFileLinesGuessEncoding("./README.md", 10);
        assertThat(result.size()).isEqualTo(10);

        result = Singleton.getHelpers().readFileLinesGuessEncoding("./README.md", 5);
        assertThat(result.size()).isEqualTo(5);
    }

    public void testReadFileLinesIssue168() throws IOException {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        File tempDir = new File(baseDir, "SearchcodeServerIssue168");

        if (!tempDir.exists()) {
            tempDir.mkdir();
        }

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempDir + "/no_newlines"), "utf-8"))) {
            for (int i=0; i < 100000000; i++) { // About 100 MB
                writer.write("a");
            }
        }

        this.helpers.MAX_FILE_LENGTH_READ = 8192;
        List<String> result = this.helpers.readFileLinesGuessEncoding(tempDir + "/no_newlines", 10);
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).length()).isEqualTo(8192);
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
            add(new RepoResult()
                    .setRowId(0)
                    .setName("reallyuniquenameihope")
                    .setScm("something")
                    .setUrl("url")
                    .setUsername("")
                    .setPassword("")
                    .setSource("source")
                    .setBranch("branch")
                    .setData("{}"));
        }}).size()).isEqualTo(1);
    }
}
