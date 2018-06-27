package com.searchcode.app;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.lang3.RandomStringUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.python.util.PythonInterpreter;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigourous :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    public void testCrypt() {
        String hash = BCrypt.hashpw("password", BCrypt.gensalt());

        if (BCrypt.checkpw("password", hash)) {
            // Login successful.
            assertTrue(true);
        }else {
            assertTrue(false);
        }
    }

    public void testTest() throws ScriptException {
        PythonInterpreter interpreter = new PythonInterpreter();
        for (int i=0; i<50; i++) {


        // Set a variable with the content you want to work with


        // Simple use Pygments as you would in Python



            String code = "PythonInterpreter interpreter = new PythonInterpreter(); " + i;
            for (int j = 0; j < 500; j++) {
                code += RandomStringUtils.randomAscii(9);
            }

            long l = System.currentTimeMillis();
            interpreter.set("code", code);

            interpreter.exec("from pygments import highlight\n" +
                    "from pygments.lexers import PythonLexer, guess_lexer, BashLexer\n" +
                    "from pygments.formatters import HtmlFormatter\n" +
                    "\n" +
                    "# TODO make this a bit nicer\n" +
                    "try:\n" +
                    "    lexer = guess_lexer(code)\n" +
                    "    content = highlight(code, lexer, HtmlFormatter(linenos=True, lineanchors='l'))\n" +
                    "    css = HtmlFormatter().get_style_defs('.highlight')\n" +
                    "except:\n" +
                    "    lexer = BashLexer()\n" +
                    "    content = highlight(code, lexer, HtmlFormatter(linenos=True, lineanchors='l'))\n" +
                    "    css = HtmlFormatter().get_style_defs('.highlight')");

            // Get the result that has been set in a variable
//            System.out.println(interpreter.get("content", String.class));
//            System.out.println(interpreter.get("css", String.class));
            System.out.println(System.currentTimeMillis() - l);
        }


    }
}
