package com.searchcode.app.util;

import com.searchcode.app.service.Singleton;
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
        List<String> result = Singleton.getHelpers().readFileLines("./README.md", 10);
        assertEquals(10, result.size());

        result = Singleton.getHelpers().readFileLines("./README.md", 5);
        assertEquals(5, result.size());
    }

    public void testIsNullEmptyOrWhitespace() {
        assertTrue(Singleton.getHelpers().isNullEmptyOrWhitespace(null));
        assertTrue(Singleton.getHelpers().isNullEmptyOrWhitespace(""));
        assertTrue(Singleton.getHelpers().isNullEmptyOrWhitespace("   "));
        assertFalse(Singleton.getHelpers().isNullEmptyOrWhitespace("test"));
    }

    public void testGetLogPath() {
        String result = Singleton.getHelpers().getLogPath();
        assertThat(result).isNotEmpty();
    }

    public void testSortByValue() {
        Random random = new Random(System.currentTimeMillis());
        Map<String, Integer> testMap = new HashMap<String, Integer>(1000);
        for (int i = 0 ; i < 1000 ; ++i) {
            testMap.put( "SomeString" + random.nextInt(), random.nextInt());
        }

        testMap = Singleton.getHelpers().sortByValue( testMap );
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

    public void testIgnoreFiles() {

    }
}
