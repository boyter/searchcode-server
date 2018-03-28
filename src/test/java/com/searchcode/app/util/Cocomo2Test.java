package com.searchcode.app.util;

import junit.framework.TestCase;

public class Cocomo2Test extends TestCase {

    /**
     * Actual values were taken from the output of CLOC to ensure that this implementation works as expected
     * TODO make this a little less fuzzy
     */
    public void testCocomo26Lines() {
        Cocomo2 sl = new Cocomo2();
        double estimatedCost = sl.estimateCost(sl.estimateEffort(26), 56000); // ~ 585

        assertTrue(estimatedCost >= 560);
        assertTrue(estimatedCost <= 600);
    }

    public void testCocomo1740Lines() {
        Cocomo2 sl = new Cocomo2();
        double estimatedCost = sl.estimateCost(sl.estimateEffort(1740), 56000); // ~ 48330

        assertTrue(estimatedCost >= 46000);
        assertTrue(estimatedCost <= 50000);
    }

    public void testCocomo77873Lines() {
        Cocomo2 sl = new Cocomo2();
        double estimatedCost = sl.estimateCost(sl.estimateEffort(77873), 56000); // ~ 2615760

        assertTrue(estimatedCost >= 2600000);
        assertTrue(estimatedCost <= 2650000);
    }

}