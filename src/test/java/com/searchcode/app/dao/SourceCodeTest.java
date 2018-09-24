package com.searchcode.app.dao;


import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.model.searchcode.SearchcodeCodeResult;
import com.searchcode.app.service.Singleton;
import junit.framework.TestCase;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class SourceCodeTest extends TestCase {

    public void testGetCodeBetween() {
        if (Singleton.getHelpers().isLocalInstance()) return;

        SourceCode code = new SourceCode();
        List<SearchcodeCodeResult> codeBetween = code.getCodeBetween(0, 200);
        assertThat(codeBetween).hasAtLeastOneElementOfType(SearchcodeCodeResult.class);
    }

    public void testGetMaxId() {
        if (Singleton.getHelpers().isLocalInstance()) return;

        SourceCode code = new SourceCode();
        int maxId = code.getMaxId();
        assertThat(maxId).isEqualTo(200);
    }

    public void testGetLocation() {
        if (Singleton.getHelpers().isLocalInstance()) return;

        SourceCode sourceCode = new SourceCode();

        String location = sourceCode.getLocation(new CodeIndexDocument());
        assertThat(location).isEmpty();

        location = sourceCode.getLocation(
            new CodeIndexDocument().setDisplayLocation("displaylocation/fileName")
        );
        assertThat(location).isEqualTo("displaylocation/");
    }
}
