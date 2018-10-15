package com.searchcode.app;

import junit.framework.TestCase;
import org.mindrot.jbcrypt.BCrypt;

public class AppTest extends TestCase {
    public void testCrypt() {
        String hash = BCrypt.hashpw("password", BCrypt.gensalt());

        if (BCrypt.checkpw("password", hash)) {
            // Login successful.
            assertTrue(true);
        } else {
            assertTrue(false);
        }
    }
}
