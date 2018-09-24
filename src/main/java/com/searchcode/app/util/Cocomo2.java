/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.util;

/**
 * Implementation of the COCOMO2 algorithm which is used to calculate the cost of a piece of code given how many
 * lines are inside it
 * TODO implement more of the algorithm so we can tune it further as its using default generic values
 */
public class Cocomo2 {

    /**
     * Calculate the cost in dollars applied using generic COCOMO2 weighted values based on the average
     * yearly wage.
     */
    public double estimateCost(double effortApplied, double averageWage) {
        double estimatedCost = effortApplied * (averageWage / 12) * 1.8;
        return estimatedCost;
    }

    /**
     * Calculate the effort applied using generic COCOMO2 weighted values
     */
    public double estimateEffort(double sloc) {

        double eaf = 1;
        double effortApplied = 3.2 * Math.pow(sloc / 1000, 1.05) * eaf;

        return effortApplied;
    }
}
