package com.searchcode.app.util;

import com.searchcode.app.service.Singleton;
import junit.framework.TestCase;
import org.mockito.Mockito;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    public void testIgnoreFilesBlackList() {
        java.util.Properties mockProperties = Mockito.mock(java.util.Properties.class);
        when(mockProperties.getProperty(any(), any())).thenReturn("");

        this.helpers = new Helpers(mockProperties);

        assertThat(this.helpers.ignoreFiles("/target")).isFalse();
    }
}
