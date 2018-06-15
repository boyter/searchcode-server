/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.14
 */

package com.searchcode.app.util;

import com.searchcode.app.dto.OWASPMatchingResult;
import com.searchcode.app.model.OWASPResult;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OWASPFileClassifierResultTest extends TestCase {

    public void testOWASPLoader() {
        OWASPClassifier oc = new OWASPClassifier();
        assertThat(oc.getDatabase()).hasAtLeastOneElementOfType(OWASPResult.class);
    }

    public void testClassifyCodeNullReturnsEmpty() {
        OWASPClassifier oc = new OWASPClassifier();
        assertThat(oc.classifyCode(null, "")).isNotNull()
                                             .isEmpty();
    }

    public void testClassifyCodeEmptyReturnsEmpty() {
        OWASPClassifier oc = new OWASPClassifier();
        List<String> codeLines = new ArrayList<>();
        assertThat(oc.classifyCode(codeLines, "")).isNotNull()
                                                  .isEmpty();
    }

    public void testClassifyCodeNoMatch() {
        OWASPClassifier oc = new OWASPClassifier();
        oc.clearDatabase();
        oc.addToDatabase(new OWASPResult("notmatch", "", "", ""));

        List<String> codeLines = new ArrayList<>();
        codeLines.add("something");
        assertThat(oc.classifyCode(codeLines, "")).isEmpty();
    }

    public void testClassifyCodeSingleMatch() {
        OWASPClassifier oc = new OWASPClassifier();
        oc.clearDatabase();
        oc.addToDatabase(new OWASPResult("match", "", "", ""));

        List<String> codeLines = new ArrayList<>();
        codeLines.add("match");
        assertThat(oc.classifyCode(codeLines, "")).hasSize(1);
    }

    public void testClassifyCodeMultipleMatch() {
        OWASPClassifier oc = new OWASPClassifier();
        oc.clearDatabase();
        oc.addToDatabase(new OWASPResult("match", "", "", ""));
        oc.addToDatabase(new OWASPResult("something", "", "", ""));

        List<String> codeLines = new ArrayList<>();
        codeLines.add("match something");
        assertThat(oc.classifyCode(codeLines, "")).hasSize(2);
    }

    public void testClassifyCodeSingleMatchFirstLineCorrectLineNumber() {
        OWASPClassifier oc = new OWASPClassifier();
        oc.clearDatabase();
        oc.addToDatabase(new OWASPResult("match", "", "", ""));

        List<String> codeLines = new ArrayList<>();
        codeLines.add("match");

        OWASPMatchingResult result = oc.classifyCode(codeLines, "").get(0);
        assertThat(result.getMatchingLines().get(0)).isEqualTo(1);
    }

    public void testClassifyCodeSingleMatchSecondLineCorrectLineNumber() {
        OWASPClassifier oc = new OWASPClassifier();
        oc.clearDatabase();
        oc.addToDatabase(new OWASPResult("match", "", "", ""));

        List<String> codeLines = new ArrayList<>();
        codeLines.add("nope");
        codeLines.add("match");

        assertThat(oc.classifyCode(codeLines, "").get(0).getMatchingLines().get(0)).isEqualTo(2);
    }

    public void testClassifyCodeMultipleMatchCorrectLineNumbersSingleMatch() {
        OWASPClassifier oc = new OWASPClassifier();
        oc.clearDatabase();
        oc.addToDatabase(new OWASPResult("match", "", "", ""));

        List<String> codeLines = new ArrayList<>();
        codeLines.add("nope");
        codeLines.add("match");
        codeLines.add("nope");
        codeLines.add("match");

        List<OWASPMatchingResult> result = oc.classifyCode(codeLines, "");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMatchingLines().get(0)).isEqualTo(2);
        assertThat(result.get(0).getMatchingLines().get(1)).isEqualTo(4);
    }

    public void testClassifyCodeSingleMatchWrongLanguage() {
        OWASPClassifier oc = new OWASPClassifier();
        oc.clearDatabase();
        oc.addToDatabase(new OWASPResult("match", "", "", "C#"));

        List<String> codeLines = new ArrayList<>();
        codeLines.add("match");
        assertThat(oc.classifyCode(codeLines, "Java")).hasSize(0);
    }

    public void testClassifyCodeSingleMatchRightLanguage() {
        OWASPClassifier oc = new OWASPClassifier();
        oc.clearDatabase();
        oc.addToDatabase(new OWASPResult("match", "", "", "java"));

        List<String> codeLines = new ArrayList<>();
        codeLines.add("match");
        assertThat(oc.classifyCode(codeLines, "Java")).hasSize(1);
    }
}