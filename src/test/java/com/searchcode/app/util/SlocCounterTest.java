package com.searchcode.app.util;

import junit.framework.TestCase;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class SlocCounterTest extends TestCase {
    private final SlocCounter slocCounter;

    public SlocCounterTest() {
        this.slocCounter = new SlocCounter();
    }

    public void testCalculationCorrect() {
        String language = "Python";
        String contents = "import this\n#comment\nprint this\n\nprint 'something'";

        SlocCounter.SlocCount slocCount = this.slocCounter.countStats(contents, language);

        assertThat(slocCount.linesCount).isEqualTo(5);
        assertThat(slocCount.commentCount).isEqualTo(1);
        assertThat(slocCount.codeCount).isEqualTo(3);
        assertThat(slocCount.blankCount).isEqualTo(1);
        assertThat(slocCount.complexity).isEqualTo(0);
    }

    public void testEmptyFile() {
        String language = "C++";
        String contents = "";

        SlocCounter.SlocCount slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(0);
    }

    public void testNullFile() {
        String language = "Lisp";

        SlocCounter.SlocCount slocCount = this.slocCounter.countStats(null, language);
        assertThat(slocCount.linesCount).isEqualTo(0);
    }

    // This triggers a bounds exception if there are problems with the calculations
    public void testBoundsExceptions() {
        String language = "Java";
        String contents = "if switch for while do loop != == && || ";

        SlocCounter.SlocCount slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.complexity).isEqualTo(8);
    }

    public void testMultiLine() {
        String language = "C++";
        String contents = "/*\n" +
                "*\n" +
                "*/";

        SlocCounter.SlocCount slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(3);
        assertThat(slocCount.codeCount).isEqualTo(0);
        assertThat(slocCount.commentCount).isEqualTo(3);
    }

    public void testRegression() {
        String language = "C++";
        String contents = "/**/\n" +
                "i\n";

        SlocCounter.SlocCount slocCount = this.slocCounter.countStats(contents, language);

        assertThat(slocCount.linesCount).isEqualTo(2);
        assertThat(slocCount.codeCount).isEqualTo(1);
        assertThat(slocCount.commentCount).isEqualTo(1);
        assertThat(slocCount.blankCount).isEqualTo(0);
    }

    public void testRakefile() {
        String language = "Rakefile";
        String contents = "# 10 lines 4 code 2 comments 4 blanks\n" +
                "\n" +
                "# this is a rakefile\n" +
                "\n" +
                "task default: %w[test]\n" +
                "\n" +
                "task :test do # not counted\n" +
                "  ruby \"test/unittest.rb\"\n" +
                "end\n" +
                "\n";

        SlocCounter.SlocCount slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(10);
        assertThat(slocCount.codeCount).isEqualTo(4);
        assertThat(slocCount.commentCount).isEqualTo(2);
        assertThat(slocCount.blankCount).isEqualTo(4);
    }

    public void testCPlusPlus() {
        String language = "C++";
        String contents = "/* 15 lines 7 code 4 comments 4 blanks */\n" +
                "\n" +
                "#include <iostream>\n" +
                "\n" +
                "\n" +
                "using namespace std;\n" +
                "\n" +
                "/*\n" +
                " * Simple test\n" +
                " */\n" +
                "int main()\n" +
                "{\n" +
                "    cout<<\"Hello world\"<<endl;\n" +
                "    return 0;\n" +
                "}\n";

        SlocCounter.SlocCount slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(15);
        assertThat(slocCount.codeCount).isEqualTo(7);
        assertThat(slocCount.commentCount).isEqualTo(4);
        assertThat(slocCount.blankCount).isEqualTo(4);
    }

    public void testRuby() {
        String language = "Ruby";
        String contents = "# 20 lines 9 code 8 comments 3 blanks\n" +
                "x = 3\n" +
                "if x < 2\n" +
                "  p = \"Smaller\"\n" +
                "else\n" +
                "  p = \"Bigger\"\n" +
                "end\n" +
                "\n" +
                "=begin\n" +
                "  Comments\n" +
                "  Comments\n" +
                "  Comments\n" +
                "  Comments\n" +
                "=end\n" +
                "\n" +
                "# testing.\n" +
                "while x > 2 and x < 10\n" +
                "  x += 1\n" +
                "end\n" +
                "\n";

        SlocCounter.SlocCount slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(20);
        assertThat(slocCount.codeCount).isEqualTo(9);
        assertThat(slocCount.commentCount).isEqualTo(8);
        assertThat(slocCount.blankCount).isEqualTo(3);
    }

    public void testTokeiTest() {
        String language = "Rust";
        String contents = "// 39 lines 32 code 2 comments 5 blanks\n" +
                "\n" +
                "/* /**/ */\n" +
                "fn main() {\n" +
                "  let start = \"/*\";\n" +
                "  loop {\n" +
                "      if x.len() >= 2 && x[0] == '*' && x[1] == '/' { // found the */\n" +
                "          break;\n" +
                "      }\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "fn foo() {\n" +
                "  let this_ends = \"a \\\"test/*.\";\n" +
                "  call1();\n" +
                "  call2();\n" +
                "  let this_does_not = /* a /* nested */ comment \" */\n" +
                "      \"*/another /*test\n" +
                "          call3();\n" +
                "          */\";\n" +
                "}\n" +
                "\n" +
                "fn foobar() {\n" +
                "  let does_not_start = // \"\n" +
                "      \"until here,\n" +
                "      test/*\n" +
                "      test\"; // a quote: \"\n" +
                "  let also_doesnt_start = /* \" */\n" +
                "      \"until here,\n" +
                "      test,*/\n" +
                "      test\"; // another quote: \"\n" +
                "}\n" +
                "\n" +
                "fn foo() {\n" +
                "  let a = 4; // /*\n" +
                "  let b = 5;\n" +
                "  let c = 6; // */\n" +
                "}\n" +
                "\n";

        SlocCounter.SlocCount slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(39);
        assertThat(slocCount.codeCount).isEqualTo(33);
        assertThat(slocCount.blankCount).isEqualTo(5);
        assertThat(slocCount.commentCount).isEqualTo(1);
    }
}
