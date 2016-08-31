package com.searchcode.app.util;

import com.searchcode.app.model.OWASPResult;
import junit.framework.TestCase;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OWASPClassifierTest extends TestCase {

    @Test
    public void testOWASPLoader() {
        OWASPClassifier oc = new OWASPClassifier();
        assertThat(oc.database).hasAtLeastOneElementOfType(OWASPResult.class);
    }

    @Test
    public void testClassifyCodeNullReturnsEmpty() {
        OWASPClassifier oc = new OWASPClassifier();
        assertThat(oc.classifyCode(null)).isNotNull()
                                         .isEmpty();
    }
}