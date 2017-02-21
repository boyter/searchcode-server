package com.searchcode.app.util;

import junit.framework.TestCase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class HelpersTest extends TestCase {

    public void testReadFileLines() throws FileNotFoundException {
        List<String> result = Helpers.readFileLines("./README.md", 10);
        assertEquals(10, result.size());

        result = Helpers.readFileLines("./README.md", 5);
        assertEquals(5, result.size());
    }

    public void testIsNullEmptyOrWhitespace() {
        assertTrue(Helpers.isNullEmptyOrWhitespace(null));
        assertTrue(Helpers.isNullEmptyOrWhitespace(""));
        assertTrue(Helpers.isNullEmptyOrWhitespace("   "));
        assertFalse(Helpers.isNullEmptyOrWhitespace("test"));
    }

    public void testGetLogPath() {
        String result = Helpers.getLogPath();
        assertThat(result).isNotEmpty();
    }

    public void testSortByValue() {
        Random random = new Random(System.currentTimeMillis());
        Map<String, Integer> testMap = new HashMap<String, Integer>(1000);
        for (int i = 0 ; i < 1000 ; ++i) {
            testMap.put( "SomeString" + random.nextInt(), random.nextInt());
        }

        testMap = Helpers.sortByValue( testMap );
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
}
