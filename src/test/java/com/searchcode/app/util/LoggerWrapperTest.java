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
        logger.severe("ignore this severe message");
        assertThat(logger.getSevereLogs()).hasSize(1);
    }

    public void testLoggerWrapperSearchAdd() {
        LoggerWrapper logger = new LoggerWrapper();
        assertThat(logger.getSearchLogs()).isEmpty();
        logger.searchLog("test");
        assertThat(logger.getSearchLogs()).hasSize(1);
    }

    public void testLoggerWrapperFineAdd() {
        LoggerWrapper logger = new LoggerWrapper();
        assertThat(logger.getSearchLogs()).isEmpty();
        logger.info("test");
        assertThat(logger.getInfoLogs()).hasSize(1);
    }

    public void testLoggerWrapperApiAdd() {
        LoggerWrapper logger = new LoggerWrapper();
        assertThat(logger.getApiLogs()).isEmpty();
        logger.apiLog("test");
        assertThat(logger.getApiLogs()).hasSize(1);
    }

    public void testLoggerWrapperAll() {
        LoggerWrapper logger = new LoggerWrapper();
        assertThat(logger.getAllLogs()).isEmpty();
        logger.severe("ignore this severe message");
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

        for (int i = 0; i < 2100; i++) {
            logger.info(RandomStringUtils.randomAscii(rand.nextInt(20) + 1));
            logger.searchLog(RandomStringUtils.randomAscii(rand.nextInt(20) + 1));
            logger.fine(RandomStringUtils.randomAscii(rand.nextInt(20) + 1));
        }

        assertThat(logger.getInfoLogs().size()).isEqualTo(1000);
        assertThat(logger.getSevereLogs().size()).isEqualTo(0);
        assertThat(logger.getWarningLogs().size()).isEqualTo(0);
        assertThat(logger.getAllLogs().size()).isEqualTo(1000);
        assertThat(logger.getSearchLogs().size()).isEqualTo(1000);
        assertThat(logger.getFineLogs().size()).isEqualTo(1000);
    }

    public void testLoggerWrapperGetLogReversed() {
        LoggerWrapper logger = new LoggerWrapper();

        logger.severe("ignore this severe message one");
        logger.severe("ignore this severe message two");
        logger.info("one");
        logger.info("two");
        logger.warning("one");
        logger.warning("two");
        logger.searchLog("one");
        logger.searchLog("two");

        assertThat(logger.getInfoLogs().get(0)).contains("two");
        assertThat(logger.getInfoLogs().get(1)).contains("one");

        assertThat(logger.getSevereLogs().get(0)).contains("ignore this severe message two");
        assertThat(logger.getSevereLogs().get(1)).contains("ignore this severe message one");

        assertThat(logger.getWarningLogs().get(0)).contains("two");
        assertThat(logger.getWarningLogs().get(1)).contains("one");

        assertThat(logger.getSearchLogs().get(0)).contains("two");
        assertThat(logger.getSearchLogs().get(1)).contains("one");
    }

    public void testLoggerWithThreads() throws InterruptedException {
        // You can only prove the presence of concurrent bugs, not their absence.
        // Although that's true of any code. Anyway let's see if we can identify any...

        LoggerWrapper loggerWrapper = new LoggerWrapper();
        Random rand = new Random();

        for(int i = 0; i < 100; i++) {
            new Thread(() -> {
                int count = 1000;
                while (count > 0) {
                    loggerWrapper.info(RandomStringUtils.randomAscii(rand.nextInt(20) + 1));
                    loggerWrapper.warning(RandomStringUtils.randomAscii(rand.nextInt(20) + 1));
                    loggerWrapper.searchLog(RandomStringUtils.randomAscii(rand.nextInt(20) + 1));
                    count--;
                }
            }).start();
        }

        int count = 1000;
        while (count > 0) {
            loggerWrapper.info(RandomStringUtils.randomAscii(rand.nextInt(20) + 1));
            loggerWrapper.warning(RandomStringUtils.randomAscii(rand.nextInt(20) + 1));
            loggerWrapper.searchLog(RandomStringUtils.randomAscii(rand.nextInt(20) + 1));
            count--;
        }

        assertThat(loggerWrapper.getInfoLogs().size()).isEqualTo(1000);
        assertThat(loggerWrapper.getWarningLogs().size()).isEqualTo(1000);
        assertThat(loggerWrapper.getAllLogs().size()).isEqualTo(1000);
        assertThat(loggerWrapper.getSearchLogs().size()).isEqualTo(1000);
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
