package com.searchcode.app.util;

import junit.framework.TestCase;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class SlocCounterTest extends TestCase {
    private final SlocCounter slocCounter;

    public SlocCounterTest() {
        this.slocCounter = new SlocCounter();
    }

    public void testCalculationCorrect() {
        var language = "Python";
        var contents = "import this\n#comment\nprint this\n\nprint 'something'";

        var slocCount = this.slocCounter.countStats(contents, language);

        assertThat(slocCount.linesCount).isEqualTo(5);
        assertThat(slocCount.commentCount).isEqualTo(1);
        assertThat(slocCount.codeCount).isEqualTo(3);
        assertThat(slocCount.blankCount).isEqualTo(1);
        assertThat(slocCount.complexity).isEqualTo(0);
    }

    public void testEmptyFile() {
        var language = "C++";
        var contents = "";

        var slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(0);
    }

    public void testNullFile() {
        var language = "Lisp";

        var slocCount = this.slocCounter.countStats(null, language);
        assertThat(slocCount.linesCount).isEqualTo(0);
    }

    // This triggers a bounds exception if there are problems with the calculations
    public void testBoundsExceptions() {
        var language = "Java";
        var contents = "if switch for while do loop != == && || ";

        var slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.complexity).isEqualTo(8);
    }

    public void testMultiLine() {
        var language = "C++";
        var contents = "/*\n" +
                "*\n" +
                "*/";

        var slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(3);
        assertThat(slocCount.codeCount).isEqualTo(0);
        assertThat(slocCount.commentCount).isEqualTo(3);
    }

    public void testUnknownLanguage() {
        var language = "This Is Nothing";
        var contents = "/*\n" +
                "*\n" +
                "*/";

        var slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(3);
        assertThat(slocCount.codeCount).isEqualTo(0);
        assertThat(slocCount.commentCount).isEqualTo(0);
    }

    public void testNuspecRegressions() {
        var language = "nuspec";
        var contents = "this\n" +
                "is\n" +
                "nuspec";

        var slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(3);
        assertThat(slocCount.codeCount).isEqualTo(3);
        assertThat(slocCount.commentCount).isEqualTo(0);
    }

    public void testRegression() {
        var language = "C++";
        var contents = "/**/\n" +
                "i\n";

        var slocCount = this.slocCounter.countStats(contents, language);

        assertThat(slocCount.linesCount).isEqualTo(2);
        assertThat(slocCount.codeCount).isEqualTo(1);
        assertThat(slocCount.commentCount).isEqualTo(1);
        assertThat(slocCount.blankCount).isEqualTo(0);
    }

    public void testRakefile() {
        var language = "Rakefile";
        var contents = "# 10 lines 4 code 2 comments 4 blanks\n" +
                "\n" +
                "# this is a rakefile\n" +
                "\n" +
                "task default: %w[test]\n" +
                "\n" +
                "task :test do # not counted\n" +
                "  ruby \"test/unittest.rb\"\n" +
                "end\n" +
                "\n";

        var slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(10);
        assertThat(slocCount.codeCount).isEqualTo(4);
        assertThat(slocCount.commentCount).isEqualTo(2);
        assertThat(slocCount.blankCount).isEqualTo(4);
    }

    public void testCPlusPlus() {
        var language = "C++";
        var contents = "/* 15 lines 7 code 4 comments 4 blanks */\n" +
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

        var slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(15);
        assertThat(slocCount.codeCount).isEqualTo(7);
        assertThat(slocCount.commentCount).isEqualTo(4);
        assertThat(slocCount.blankCount).isEqualTo(4);
    }

    public void testRuby() {
        var language = "Ruby";
        var contents = "# 20 lines 9 code 8 comments 3 blanks\n" +
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

        var slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(20);
        assertThat(slocCount.codeCount).isEqualTo(9);
        assertThat(slocCount.commentCount).isEqualTo(8);
        assertThat(slocCount.blankCount).isEqualTo(3);
    }

    /**
     * TODO this is still wrong but close enough for multiline comments
     * Multiline comments are a real pain in the neck. Thankfully
     * they appear to not be that common in the real world.
     */
    public void testTokeiRust() {
        var language = "Rust";
        var contents = "// 39 lines 32 code 2 comments 5 blanks\n" +
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

        var slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(39);
        assertThat(slocCount.codeCount).isEqualTo(33);
        assertThat(slocCount.commentCount).isEqualTo(1);
        assertThat(slocCount.blankCount).isEqualTo(5);
    }

    public void testTokeiJava() {
        var language = "Java";
        var contents = "/* 23 lines 16 code 4 comments 3 blanks */\n" +
                "\n" +
                "/*\n" +
                "* Simple test class\n" +
                "*/\n" +
                "public class Test\n" +
                "{\n" +
                " int j = 0; // Not counted\n" +
                " public static void main(String[] args)\n" +
                " {\n" +
                "     Foo f = new Foo();\n" +
                "     f.bar();\n" +
                "\n" +
                " }\n" +
                "}\n" +
                "\n" +
                "class Foo\n" +
                "{\n" +
                " public void bar()\n" +
                " {\n" +
                "   System.out.println(\"FooBar\"); //Not counted\n" +
                " }\n" +
                "}";

        var slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(23);
        assertThat(slocCount.codeCount).isEqualTo(16);
        assertThat(slocCount.commentCount).isEqualTo(4);
        assertThat(slocCount.blankCount).isEqualTo(3);
    }

    public void testTokeiCPlusPlus() {
        var language = "Java";
        var contents = "/* 15 lines 7 code 4 comments 4 blanks */\n" +
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

        var slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(15);
        assertThat(slocCount.codeCount).isEqualTo(7);
        assertThat(slocCount.commentCount).isEqualTo(4);
        assertThat(slocCount.blankCount).isEqualTo(4);
    }

    public void testTokeiRakeFile() {
        var language = "Rakefile";
        var contents = "# 10 lines 4 code 2 comments 4 blanks\n" +
                "\n" +
                "# this is a rakefile\n" +
                "\n" +
                "task default: %w[test]\n" +
                "\n" +
                "task :test do # not counted\n" +
                "  ruby \"test/unittest.rb\"\n" +
                "end\n\n";

        var slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(10);
        assertThat(slocCount.codeCount).isEqualTo(4);
        assertThat(slocCount.commentCount).isEqualTo(2);
        assertThat(slocCount.blankCount).isEqualTo(4);
    }

    public void testBomSkip() {
        var language = "C#";
        var contents = "   // Comment 1\n" +
                "namespace Baz";
        char[] chars = contents.toCharArray();
        chars[0] = 239;
        chars[1] = 187;
        chars[2] = 191;

        contents = new String(chars);

        var slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(2);
        assertThat(slocCount.codeCount).isEqualTo(1);
        assertThat(slocCount.commentCount).isEqualTo(1);
        assertThat(slocCount.blankCount).isEqualTo(0);
    }

    public void testBomUTF8() {
        var language = "C#";
        var contents = "   //";
        char[] chars = contents.toCharArray();
        chars[0] = 239;
        chars[1] = 187;
        chars[2] = 191;

        contents = new String(chars);

        var slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(1);
        assertThat(slocCount.codeCount).isEqualTo(0);
        assertThat(slocCount.commentCount).isEqualTo(1);
        assertThat(slocCount.blankCount).isEqualTo(0);
    }

    /**
     * For very small files which are invalid anyway the count will be off
     * because we don't check the length of the token. However this is probably
     * fine for the moment because as mentioned this would be invalid anyway so
     * even a tokenizer would get this wrong
     */
    public void testIncorrectCountWhenSmallFile() {
        var language = "C#";
        var contents = "/";

        var slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(1);
        assertThat(slocCount.codeCount).isEqualTo(0); // NB this should be 1
        assertThat(slocCount.commentCount).isEqualTo(1); // NB this should be 0
        assertThat(slocCount.blankCount).isEqualTo(0);
    }

    public void testEscapedQuoteString() {
        var language = "Java";
        var contents = "'\\\"'{\n" +
                "code";

        var slocCount = this.slocCounter.countStats(contents, language);
        assertThat(slocCount.linesCount).isEqualTo(2);
        assertThat(slocCount.codeCount).isEqualTo(2);
        assertThat(slocCount.commentCount).isEqualTo(0);
        assertThat(slocCount.blankCount).isEqualTo(0);
    }

    public void testBomSkipAll() {
        for (List<Integer> bom : slocCounter.getByteOrderMarks()) {
            var contents = "              ";
            char[] chars = contents.toCharArray();

            for (int i = 0; i < bom.size(); i++) {
                chars[i] = (char) (int) bom.get(i);
            }

            contents = new String(chars);
            int skipSize = this.slocCounter.checkBomSkip(contents);

            assertThat(skipSize).isEqualTo(bom.size());
            assertThat(skipSize).isNotZero();
        }
    }
}
