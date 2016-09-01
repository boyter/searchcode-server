/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 */

package com.searchcode.app.util;

import com.searchcode.app.model.OWASPResult;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OWASPClassifierTest extends TestCase {

    @Test
    public void testOWASPLoader() {
        OWASPClassifier oc = new OWASPClassifier();
        assertThat(oc.getDatabase()).hasAtLeastOneElementOfType(OWASPResult.class);
    }

    @Test
    public void testClassifyCodeNullReturnsEmpty() {
        OWASPClassifier oc = new OWASPClassifier();
        assertThat(oc.classifyCode(null)).isNotNull()
                                         .isEmpty();
    }

    @Test
    public void testClassifyCodeEmptyReturnsEmpty() {
        OWASPClassifier oc = new OWASPClassifier();
        List<String> codeLines = new ArrayList<>();
        assertThat(oc.classifyCode(codeLines)).isNotNull()
                                              .isEmpty();
    }

    @Test
    public void testClassifyCodeNoMatch() {
        OWASPClassifier oc = new OWASPClassifier();
        oc.clearDatabase();
        oc.addToDatabase(new OWASPResult("notmatch", "", ""));

        List<String> codeLines = new ArrayList<>();
        codeLines.add("something");
        assertThat(oc.classifyCode(codeLines)).isEmpty();
    }

    @Test
    public void testClassifyCodeSingleMatch() {
        OWASPClassifier oc = new OWASPClassifier();
        oc.clearDatabase();
        oc.addToDatabase(new OWASPResult("match", "", ""));

        List<String> codeLines = new ArrayList<>();
        codeLines.add("match");
        assertThat(oc.classifyCode(codeLines)).hasSize(1);
    }

    @Test
    public void testClassifyCodeMultipleMatch() {
        OWASPClassifier oc = new OWASPClassifier();
        oc.clearDatabase();
        oc.addToDatabase(new OWASPResult("match", "", ""));
        oc.addToDatabase(new OWASPResult("something", "", ""));

        List<String> codeLines = new ArrayList<>();
        codeLines.add("match something");
        assertThat(oc.classifyCode(codeLines)).hasSize(2);
    }

    @Test
    public void testClassifyCodeSingleMatchFirstLineCorrectLineNumber() {
        OWASPClassifier oc = new OWASPClassifier();
        oc.clearDatabase();
        oc.addToDatabase(new OWASPResult("match", "", ""));

        List<String> codeLines = new ArrayList<>();
        codeLines.add("match");

        assertThat(oc.classifyCode(codeLines).get(0).getMatchingLines().get(0)).isEqualTo(0);
    }

    @Test
    public void testClassifyCodeSingleMatchSecondLineCorrectLineNumber() {
        OWASPClassifier oc = new OWASPClassifier();
        oc.clearDatabase();
        oc.addToDatabase(new OWASPResult("match", "", ""));

        List<String> codeLines = new ArrayList<>();
        codeLines.add("nope");
        codeLines.add("match");

        assertThat(oc.classifyCode(codeLines).get(0).getMatchingLines().get(0)).isEqualTo(1);
    }

    @Test
    public void testClassifyCodeMultipleMatchCorrectLineNumbersSingleMatch() {
        OWASPClassifier oc = new OWASPClassifier();
        oc.clearDatabase();
        oc.addToDatabase(new OWASPResult("match", "", ""));

        List<String> codeLines = new ArrayList<>();
        codeLines.add("nope");
        codeLines.add("match");
        codeLines.add("nope");
        codeLines.add("match");

        assertThat(oc.classifyCode(codeLines)).hasSize(1);
        assertThat(oc.classifyCode(codeLines).get(0).getMatchingLines().get(0)).isEqualTo(1);
        assertThat(oc.classifyCode(codeLines).get(0).getMatchingLines().get(1)).isEqualTo(3);
    }
}