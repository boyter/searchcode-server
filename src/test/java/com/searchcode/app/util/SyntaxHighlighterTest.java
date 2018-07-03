package com.searchcode.app.util;

import com.searchcode.app.service.Singleton;
import junit.framework.TestCase;

public class SyntaxHighlighterTest extends TestCase {

    public void testSyntaxHighlighter() {
        SyntaxHighlighter syntaxHighlighter = Singleton.getSyntaxHighlighter();
        syntaxHighlighter.highlightCode("this is some code");
    }
}
