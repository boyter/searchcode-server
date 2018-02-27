package com.searchcode.app.util;

import junit.framework.TestCase;

import java.util.HashMap;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class VectorspaceTest extends TestCase {

    public void testMagnitude() {
        Vectorspace vectorspace = new Vectorspace();

        HashMap<String, Integer> hashMap = new HashMap<>();
        hashMap.put("test", 1);
        hashMap.put("testing", 2);

        double magnitude = vectorspace.magnitude(hashMap);

        assertThat(magnitude).isEqualTo(2.23606797749979);
    }

    public void testRelationSameValue() {
        Vectorspace vectorspace = new Vectorspace();

        HashMap<String, Integer> hashMap = new HashMap<>();
        hashMap.put("test", 1);
        hashMap.put("testing", 2);

        double relation = vectorspace.relation(hashMap, hashMap);

        assertThat(relation).isGreaterThanOrEqualTo(0.999999999);
    }

    public void testRelationDifferentValues() {
        Vectorspace vectorspace = new Vectorspace();

        HashMap<String, Integer> hashMap1 = new HashMap<>();
        hashMap1.put("test", 1);
        hashMap1.put("testing", 2);

        HashMap<String, Integer> hashMap2 = new HashMap<>();
        hashMap2.put("test", 2);
        hashMap2.put("testing", 1);

        double relation = vectorspace.relation(hashMap1, hashMap2);

        assertThat(relation).isGreaterThanOrEqualTo(0.79);
        assertThat(relation).isLessThanOrEqualTo(0.8);
    }

    public void testEndToEnd() {
        Vectorspace vectorspace = new Vectorspace();
        HashMap<String, Integer> concordance1 = vectorspace.concordance("The code search solution for companies that build or maintain software who want to improve productivity and shorten development time by getting value from their existing source code.");
        HashMap<String, Integer> concordance2 = vectorspace.concordance("Uncover the hidden value of your existing code. Why re-write what you already have? Find it quickly, reuse and save time.");

        double relation = vectorspace.relation(concordance1, concordance2);

        assertThat(relation).isGreaterThanOrEqualTo(0.2724);
        assertThat(relation).isLessThanOrEqualTo(0.2725);
    }

    public void testCleanText() {
        Vectorspace vectorspace = new Vectorspace();
        assertThat(vectorspace.cleanText("This is a document")).isEqualTo("this is a document");
        assertThat(vectorspace.cleanText("This is a  document")).isEqualTo("this is a document");
        assertThat(vectorspace.cleanText("This is a document    ")).isEqualTo("this is a document");
        assertThat(vectorspace.cleanText("     This is a document")).isEqualTo("this is a document");
        assertThat(vectorspace.cleanText("This is a Document")).isEqualTo("this is a document");
        assertThat(vectorspace.cleanText("99This is a document")).isEqualTo("99this is a document");
        assertThat(vectorspace.cleanText("!@##$%^&*()This is a document")).isEqualTo("this is a document");
    }
}
