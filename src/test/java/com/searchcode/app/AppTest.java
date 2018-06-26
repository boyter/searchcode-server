package com.searchcode.app;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.mindrot.jbcrypt.BCrypt;

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
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        engine.eval("print('Hello, World')");
    }
}
