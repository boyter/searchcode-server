package com.searchcode.app.util;

import com.searchcode.app.model.LicenseResult;
import junit.framework.TestCase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LicenceCheckerTest extends TestCase {

    public void testLoadDatabase() {
        LicenceChecker licenceChecker = new LicenceChecker();
        assertThat(licenceChecker.getDatabase()).hasAtLeastOneElementOfType(LicenseResult.class);
    }

    public void testSingleIdentifier() {
        LicenceChecker licenceChecker = new LicenceChecker();
        List<String> matches = licenceChecker.identifierGuessLicence("# SPDX-License-Identifier: GPL-2.0 \n import os");
        assertThat(matches.get(0)).isEqualTo("GPL-2.0");
    }

    public void testMultipleIdentifier() {
        LicenceChecker licenceChecker = new LicenceChecker();
        List<String> matches = licenceChecker.identifierGuessLicence("# SPDX-License-Identifier: GPL-2.0 \n import os \n # SPDX-License-Identifier: MIT ");
        assertThat(matches.get(0)).isEqualTo("GPL-2.0");
        assertThat(matches.get(1)).isEqualTo("MIT");
    }
}
