package com.searchcode.app;

import junit.framework.TestCase;
import org.mindrot.jbcrypt.BCrypt;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class AppTest extends TestCase {
    public void testCrypt() {
        var hash = BCrypt.hashpw("password", BCrypt.gensalt());

        assertThat(BCrypt.checkpw("password", hash)).isTrue();
    }
}
