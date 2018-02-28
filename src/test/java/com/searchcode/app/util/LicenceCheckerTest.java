package com.searchcode.app.util;

import com.searchcode.app.model.LicenseMatch;
import com.searchcode.app.model.LicenseResult;
import junit.framework.TestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LicenceCheckerTest extends TestCase {

    public void testLoadDatabase() {
        LicenceChecker licenceChecker = new LicenceChecker();
        assertThat(licenceChecker.getDatabase()).hasAtLeastOneElementOfType(LicenseResult.class);
    }

    public void testLoadDatabaseConcordance() {
        LicenceChecker licenceChecker = new LicenceChecker();
        assertThat(licenceChecker.getDatabase().get(0).concordance).isNotEmpty();
    }

    public void testSingleIdentifier() {
        LicenceChecker licenceChecker = new LicenceChecker();
        List<LicenseResult> licenseResults = licenceChecker.identifierGuessLicence("# SPDX-License-Identifier: GPL-2.0 \n import os");
        assertThat(licenseResults.get(0).licenseId).isEqualTo("GPL-2.0");
    }

    public void testMultipleIdentifier() {
        LicenceChecker licenceChecker = new LicenceChecker();
        List<LicenseResult> licenseResults = licenceChecker.identifierGuessLicence("# SPDX-License-Identifier: GPL-2.0 \n import os \n # SPDX-License-Identifier: MIT ");
        assertThat(licenseResults.get(0).licenseId).isEqualTo("GPL-2.0");
        assertThat(licenseResults.get(1).licenseId).isEqualTo("MIT");
    }

    public void testKeywordMatchNoMatch() {
        LicenceChecker licenceChecker = new LicenceChecker();
        List<LicenseMatch> licenseMatches = licenceChecker.keywordGuessLicense("something something");
        assertThat(licenseMatches).hasSize(0);
    }

    public void testKeywordMatchUnlicense() {
        LicenceChecker licenceChecker = new LicenceChecker();
        List<LicenseMatch> licenseMatches = licenceChecker.keywordGuessLicense("This is free and unencumbered software released into the public domain.  Anyone is free to copy, modify, publish, use, compile, sell, or distribute this software, either in source code form or as a compiled binary, for any purpose, commercial or non-commercial, and by any means.  In jurisdictions that recognize copyright laws, the author or authors of this software dedicate any and all copyright interest in the software to the public domain. We make this dedication for the benefit of the public at large and to the detriment of our heirs and successors. We intend this dedication to be an overt act of relinquishment in perpetuity of all present and future rights to this software under copyright law.  THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.  For more information, please refer to <http://unlicense.org/>");
        assertThat(licenseMatches.get(0).licenseId).isEqualTo("Unlicense");
    }

    public void testSomething() {
        File folder = new File("./");
        List<File> files = Arrays.asList(folder.listFiles());

        for (File file: files) {
            System.out.println(file.getName());
        }
    }
}
