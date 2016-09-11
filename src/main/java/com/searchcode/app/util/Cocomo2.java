/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 *
 * After the following date 27 August 2019 this software version '1.2.3' or '1.2.4' is dual licenced under the
 * Fair Source Licence included in the LICENSE.txt file or under the GNU General Public License Version 3 with terms
 * specified at https://www.gnu.org/licenses/gpl-3.0.txt
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
