package com.searchcode.app.dao;


import com.searchcode.app.config.SQLiteMemoryDatabaseConfig;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;
import junit.framework.TestCase;

public class CodeTest extends TestCase {
    private Code code;

    public void setUp() throws Exception {
        super.setUp();
        this.code = new Code(new SQLiteMemoryDatabaseConfig(), new Helpers(), new LoggerWrapper());
    }

    public void testInsertOrReplace() {

    }
}
