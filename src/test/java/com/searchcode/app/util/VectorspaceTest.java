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
    }
}
