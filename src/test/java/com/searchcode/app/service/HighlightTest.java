package com.searchcode.app.service;

import com.searchcode.app.dto.CodeResult;
import junit.framework.TestCase;

import java.util.HashMap;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class HighlightTest extends TestCase {

    private final Highlight highlight;

    public HighlightTest() {
        this.highlight = new Highlight();
    }

    public void testHighlightInternal() {
        var coderesult = new CodeResult();
        var map = new HashMap<String, Object>();

        this.highlight.highlightInternal(coderesult, map);
        assertThat(map.get("linenos")).isNotNull();
        assertThat(map.get("codeValue")).isNotNull();
    }

    public void testHighlightExternal() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        // Requirement that the highlighter has started
        Singleton.getJobService().startHighlighter();

        var coderesult = new CodeResult();
        var map = new HashMap<String, Object>();

        this.highlight.highlightExternal(coderesult, map);
        assertThat(map.get("chromaCss")).isNotNull();
        assertThat(map.get("chromaHtml")).isNotNull();
    }
}
