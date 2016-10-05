package com.searchcode.app.util;

import junit.framework.TestCase;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class LoggerWrapperTest extends TestCase {

    public void testLoggerWrapperInfoAdd() {
        LoggerWrapper logger = new LoggerWrapper();
        assertThat(logger.getInfoLogs()).isEmpty();
        logger.info("test");
        assertThat(logger.getInfoLogs()).hasSize(1);
    }

    public void testLoggerWrapperWarningAdd() {
        LoggerWrapper logger = new LoggerWrapper();
        assertThat(logger.getWarningLogs()).isEmpty();
        logger.warning("test");
        assertThat(logger.getWarningLogs()).hasSize(1);
    }

    public void testLoggerWrapperSevereAdd() {
        LoggerWrapper logger = new LoggerWrapper();
        assertThat(logger.getSevereLogs()).isEmpty();
        logger.severe("test");
        assertThat(logger.getSevereLogs()).hasSize(1);
    }

    public void testLoggerWrapperAll() {
        LoggerWrapper logger = new LoggerWrapper();
        assertThat(logger.getAllLogs()).isEmpty();
        logger.severe("test");
        assertThat(logger.getAllLogs()).hasSize(1);

        logger.info("test");
        logger.warning("test");
        assertThat(logger.getAllLogs()).hasSize(3);
    }
}
