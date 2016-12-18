package com.searchcode.app.util;

import junit.framework.TestCase;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;
import java.util.logging.Level;

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

    public void testWrapperProperties() {
        LoggerWrapper logger = new LoggerWrapper();
        assertThat(logger.BYTESLOGSIZE).isEqualTo(10485760);
        assertThat(logger.LOGCOUNT).isEqualTo(10);
    }

    public void testIsLoggableInfo() {
        LoggerWrapper logger = new LoggerWrapper();
        logger.LOGLEVELENUM = Level.INFO;

        assertThat(logger.isLoggable(Level.INFO)).isTrue();
        assertThat(logger.isLoggable(Level.WARNING)).isTrue();
        assertThat(logger.isLoggable(Level.SEVERE)).isTrue();
    }

    public void testIsLoggableWarning() {
        LoggerWrapper logger = new LoggerWrapper();
        logger.LOGLEVELENUM = Level.WARNING;

        assertThat(logger.isLoggable(Level.INFO)).isFalse();
        assertThat(logger.isLoggable(Level.WARNING)).isTrue();
        assertThat(logger.isLoggable(Level.SEVERE)).isTrue();
    }

    public void testIsLoggableSevere() {
        LoggerWrapper logger = new LoggerWrapper();
        logger.LOGLEVELENUM = Level.SEVERE;

        assertThat(logger.isLoggable(Level.INFO)).isFalse();
        assertThat(logger.isLoggable(Level.WARNING)).isFalse();
        assertThat(logger.isLoggable(Level.SEVERE)).isTrue();
    }

    public void testIsLoggableOff() {
        LoggerWrapper logger = new LoggerWrapper();
        logger.LOGLEVELENUM = Level.OFF;

        assertThat(logger.isLoggable(Level.INFO)).isFalse();
        assertThat(logger.isLoggable(Level.WARNING)).isFalse();
        assertThat(logger.isLoggable(Level.SEVERE)).isFalse();
    }

    public void testLoggerWrapperMemoryLeak() {
        LoggerWrapper logger = new LoggerWrapper();
        Random rand = new Random();

        for (int i = 0; i< 1100; i++) {
            logger.severe(RandomStringUtils.randomAscii(rand.nextInt(20) + 1));
            logger.info(RandomStringUtils.randomAscii(rand.nextInt(20) + 1));
            logger.warning(RandomStringUtils.randomAscii(rand.nextInt(20) + 1));
            logger.searchLog(RandomStringUtils.randomAscii(rand.nextInt(20) + 1));
        }


        assertThat(logger.getInfoLogs().size()).isEqualTo(1000);
        assertThat(logger.getSevereLogs().size()).isEqualTo(1000);
        assertThat(logger.getWarningLogs().size()).isEqualTo(1000);
        assertThat(logger.getAllLogs().size()).isEqualTo(1000);
        assertThat(logger.getSearchLogs().size()).isEqualTo(1000);
    }

    public void testLoggerWrapperGetLogReversed() {
        LoggerWrapper logger = new LoggerWrapper();

        logger.severe("one");
        logger.severe("two");
        logger.info("one");
        logger.info("two");
        logger.warning("one");
        logger.warning("two");
        logger.searchLog("one");
        logger.searchLog("two");

        assertThat(logger.getInfoLogs().get(0)).contains("two");
        assertThat(logger.getInfoLogs().get(1)).contains("one");

        assertThat(logger.getSevereLogs().get(0)).contains("two");
        assertThat(logger.getSevereLogs().get(1)).contains("one");

        assertThat(logger.getWarningLogs().get(0)).contains("two");
        assertThat(logger.getWarningLogs().get(1)).contains("one");

        assertThat(logger.getSearchLogs().get(0)).contains("two");
        assertThat(logger.getSearchLogs().get(1)).contains("one");
    }

// TODO look into this, appears to be related to stressing the properties lookup more than anything
//    public void testLoggerWrapperStress() {
//        LoggerWrapper logger = new LoggerWrapper();
//
//        for (int i = 0; i < 40000; i++) {
//            logger.getAllLogs();
//            logger.getInfoLogs();
//            logger.getSevereLogs();
//            logger.getWarningLogs();
//            logger.severe("test");
//            logger.info("test");
//            logger.warning("test");
//
//            if (i % 100 == 0) {
//                logger = new LoggerWrapper();
//            }
//        }
//    }
}
