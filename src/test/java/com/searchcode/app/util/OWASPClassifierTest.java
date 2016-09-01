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
}