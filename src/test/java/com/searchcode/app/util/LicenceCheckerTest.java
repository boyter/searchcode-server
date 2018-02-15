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

    public void testIdentifier() {
        LicenceChecker licenceChecker = new LicenceChecker();
        licenceChecker.identifierGuessLicence("This is some text # SPDX-License-Identifier: GPL-2.0 and some other text\n\nThis is some text # SPDX-License-Identifier: GPL-2.0+ and some other text\n\n");
    }
}
