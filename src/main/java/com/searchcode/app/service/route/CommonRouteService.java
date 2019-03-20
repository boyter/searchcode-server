/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.service.route;

import com.searchcode.app.App;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Properties;

import java.util.Random;

public class CommonRouteService {
    public static String getLogo() {
        if (App.IS_COMMUNITY) {
            return Values.EMPTYSTRING;
        }

        Data data = Singleton.getData();
        return data.getDataByName(Values.LOGO, Values.EMPTYSTRING);
    }

    public static double getAverageSalary() {
        if (App.IS_COMMUNITY) {
            return Double.parseDouble(Values.DEFAULTAVERAGESALARY);
        }

        Data data = Singleton.getData();
        String salary = data.getDataByName(Values.AVERAGESALARY);

        if (salary == null) {
            data.saveData(Values.AVERAGESALARY, Values.DEFAULTAVERAGESALARY);
            salary = Values.DEFAULTAVERAGESALARY;
        }

        return Double.parseDouble(salary);
    }

    public static double getMatchLines() {
        if (App.IS_COMMUNITY) {
            return Double.parseDouble(Values.DEFAULTMATCHLINES);
        }

        Data data = Singleton.getData();
        String matchLines = data.getDataByName(Values.MATCHLINES);

        if (matchLines == null) {
            data.saveData(Values.MATCHLINES, Values.DEFAULTMATCHLINES);
            matchLines = Values.DEFAULTMATCHLINES;
        }

        return Double.parseDouble(matchLines);
    }

    public static double getMaxLineDepth() {
        if (App.IS_COMMUNITY) {
            return Double.parseDouble(Values.DEFAULTMAXLINEDEPTH);
        }

        var data = Singleton.getData();
        var matchLines = data.getDataByName(Values.MAXLINEDEPTH);

        if (matchLines == null) {
            data.saveData(Values.MAXLINEDEPTH, Values.DEFAULTMAXLINEDEPTH);
            matchLines = Values.DEFAULTMAXLINEDEPTH;
        }

        return Double.parseDouble(matchLines);
    }

    public static double getMinifiedLength() {
        if (App.IS_COMMUNITY) {
            return Double.parseDouble(Values.DEFAULTMINIFIEDLENGTH);
        }

        var data = Singleton.getData();
        var minifiedLength = data.getDataByName(Values.MINIFIEDLENGTH);

        if (minifiedLength == null) {
            data.saveData(Values.MINIFIEDLENGTH, Values.DEFAULTMINIFIEDLENGTH);
            minifiedLength = Values.DEFAULTMINIFIEDLENGTH;
        }

        return Double.parseDouble(minifiedLength);
    }

    public static double getBackoffValue() {
        if (App.IS_COMMUNITY) {
            return Double.parseDouble(Values.DEFAULTBACKOFFVALUE);
        }

        var data = Singleton.getData();
        var backoffValue = data.getDataByName(Values.BACKOFFVALUE);

        if (backoffValue == null) {
            data.saveData(Values.BACKOFFVALUE, Values.DEFAULTBACKOFFVALUE);
            backoffValue = Values.DEFAULTBACKOFFVALUE;
        }

        return Double.parseDouble(backoffValue);
    }

    public static String getEmbed() {
        if (App.IS_COMMUNITY) {
            return Values.EMPTYSTRING;
        }

        var data = Singleton.getData();
        return data.getDataByName(Values.EMBED, Values.EMPTYSTRING);
    }

    public static boolean owaspAdvisoriesEnabled() {
        if (App.IS_COMMUNITY) {
            return false;
        }

        var data = Singleton.getData();
        var owaspEnabledString = data.getDataByName(Values.OWASPENABLED);
        var owaspEnabled = Boolean.parseBoolean(owaspEnabledString);

        if (owaspEnabledString == null) {
            data.saveData(Values.OWASPENABLED, "false");
            owaspEnabled = false;
        }

        return owaspEnabled;
    }

    public static String getSyntaxHighlighter() {
        if (App.IS_COMMUNITY) {
            return Values.DEFAULTSYNTAXHIGHLIGHTER;
        }

        var data = Singleton.getData();
        var highlighter = data.getDataByName(Values.SYNTAXHIGHLIGHTER);

        if (highlighter == null || highlighter.trim().equals("")) {
            highlighter = Properties.getProperties().getProperty(Values.SYNTAXHIGHLIGHTER, Values.DEFAULTSYNTAXHIGHLIGHTER);
            data.saveData(Values.SYNTAXHIGHLIGHTER, highlighter);
        }

        return highlighter;
    }

    public static int getPhotoId(int seed) {
        var random = new Random(seed);
        return random.nextInt(42) + 1;
    }
}
