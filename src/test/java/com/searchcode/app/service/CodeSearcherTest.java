package com.searchcode.app.service;

import com.searchcode.app.dto.CodeResult;
import junit.framework.TestCase;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class CodeSearcherTest extends TestCase {

    public void testCalculatePages() {
        CodeSearcher cs = new CodeSearcher();

        assertThat(cs.calculatePages(0, 0)).isEmpty();
        assertThat(cs.calculatePages(1, 0)).hasSize(1);
        assertThat(cs.calculatePages(10, 10)).hasSize(10);
        assertThat(cs.calculatePages(10, 20)).hasSize(20);

        List<Integer> integers = cs.calculatePages(20, 20);

        for(Integer integer: integers) {
            assertThat(integers.get(integer)).isEqualTo(integer);
        }
    }

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
