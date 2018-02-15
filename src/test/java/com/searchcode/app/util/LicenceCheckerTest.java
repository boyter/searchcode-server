package com.searchcode.app.util;

import com.searchcode.app.model.LicenseResult;
import com.searchcode.app.model.OWASPResult;
import junit.framework.TestCase;

import static org.assertj.core.api.Assertions.assertThat;

public class LicenceCheckerTest extends TestCase {

    public void testLoadDatabase() {
        LicenceChecker licenceChecker = new LicenceChecker();
        assertThat(licenceChecker.getDatabase()).hasAtLeastOneElementOfType(LicenseResult.class);
    }
}
