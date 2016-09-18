package com.searchcode.app.service;

import com.searchcode.app.dto.CodeResult;
import junit.framework.TestCase;

public class CodeSearcherTest extends TestCase {

    // TODO fix tests in here so they do more than just call methods to ensure no exceptions thrown
    public void testIndex() {
        CodeSearcher cs = new CodeSearcher();
        cs.search("test", 0);
        cs.search("search", 0);
        cs.search("other", 0);

        for(int i=0; i<100;i++) {
            CodeResult cr = cs.getById(i);
        }
    }

    public void testGetByRepoFileName() {
        CodeSearcher cs = new CodeSearcher();

        CodeResult cr1 = cs.getByRepoFileName("authoring", "src/app/Ninemsn.Portal.SitecoreItems.Master/sitecore/media library/Files/MediaService/angrywhale.item");
        CodeResult cr2 = cs.getByRepoFileName("authoring", "src/tests/acceptance/Ninemsn.Portal.News.Authoring.Tests.Acceptance/Steps/ImagePickerStepDefintions.cs");

        cr1 = null;
    }
}
