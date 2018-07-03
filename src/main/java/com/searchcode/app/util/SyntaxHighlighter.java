package com.searchcode.app.util;

import org.python.util.PythonInterpreter;

public class SyntaxHighlighter {

    PythonInterpreter interpreter;

    public SyntaxHighlighter() {
        interpreter = new PythonInterpreter();
    }

    public HighlighterResponse highlightCode(String code) {
        interpreter.set("code", String.join(System.getProperty("line.separator"), code));

        // Lifted from the original code used in searchcode.com
        interpreter.exec("from pygments import highlight\n" +
                "from pygments.lexers import PythonLexer, guess_lexer, BashLexer\n" +
                "from pygments.formatters import HtmlFormatter\n" +
                "\n" +
                "try:\n" +
                "    lexer = guess_lexer(code)\n" +
                "    content = highlight(code, lexer, HtmlFormatter(linenos=True, lineanchors='l'))\n" +
                "    css = HtmlFormatter().get_style_defs('.highlight')\n" +
                "except:\n" +
                "    lexer = BashLexer()\n" +
                "    content = highlight(code, lexer, HtmlFormatter(linenos=True, lineanchors='l'))\n" +
                "    css = HtmlFormatter().get_style_defs('.highlight')");

        return new HighlighterResponse(interpreter.get("content", String.class),
                                       interpreter.get("css", String.class));
    }

    public class HighlighterResponse {
        public String code;
        public String css;

        public HighlighterResponse(String code, String css) {
            this.code = code;
            this.css = css;
        }
    }
}


