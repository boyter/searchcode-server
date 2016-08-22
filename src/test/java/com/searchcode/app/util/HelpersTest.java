package com.searchcode.app.util;

import junit.framework.TestCase;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class HelpersTest extends TestCase {

    public void testOtherThing() throws FileNotFoundException {
        List<String> result = Helpers.readFileLines("./README.txt", 10);
        assertEquals(10, result.size());

        result = Helpers.readFileLines("./README.txt", 5);
        assertEquals(5, result.size());
    }

    public void testIsNullEmptyOrWhitespace() {
        assertTrue(Helpers.isNullEmptyOrWhitespace(null));
        assertTrue(Helpers.isNullEmptyOrWhitespace(""));
        assertTrue(Helpers.isNullEmptyOrWhitespace("   "));
        assertFalse(Helpers.isNullEmptyOrWhitespace("test"));
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
