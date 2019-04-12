package com.searchcode.app.dao;

import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.model.searchcode.SearchcodeCodeResult;
import com.searchcode.app.service.Singleton;
import junit.framework.TestCase;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class SourceCodeTest extends TestCase {

    public void testGetCodeBetween() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var code = new SourceCode();
        var codeBetween = code.getCodeBetween(0, 200);
        assertThat(codeBetween).hasAtLeastOneElementOfType(SearchcodeCodeResult.class);
    }

    public void testGetMaxId() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var code = new SourceCode();
        var maxId = code.getMaxId();
        assertThat(maxId).isGreaterThan(1);
    }

    public void testLanguageFacet() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var code = new SourceCode();
        var languageFacet = code.getLanguageFacet(-1);
        assertThat(languageFacet).hasSize(0);
    }

    public void testLanguageLineFacet() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var code = new SourceCode();
        var languageFacet = code.getLanguageLineFacet(-1);
        assertThat(languageFacet).hasSize(0);
    }

    public void testGetLocation() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        SourceCode sourceCode = new SourceCode();

        String location = sourceCode.getLocation(new CodeIndexDocument());
        assertThat(location).isEmpty();

        location = sourceCode.getLocation(
                new CodeIndexDocument().setDisplayLocation("displaylocation/fileName")
        );
        assertThat(location).isEqualTo("displaylocation/");
    }
}
