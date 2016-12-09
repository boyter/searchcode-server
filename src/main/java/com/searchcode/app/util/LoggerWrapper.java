/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.5
 */

package com.searchcode.app.util;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import com.searchcode.app.config.Values;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.*;

/**
 * Wrapper around logging so that we can store the logging inside a in memory queue
 * which can then be displayed rather then hit the filesystem. Should in theory allow
 * quick filters and the like.
 */
public class LoggerWrapper {

    private Logger logger = null;
    private EvictingQueue allCache = null;
    private EvictingQueue infoRecentCache = null;
    private EvictingQueue warningRecentCache = null;
    private EvictingQueue severeRecentCache = null;
    private EvictingQueue searchLog = null;

    public int BYTESLOGSIZE = 10 * 1024 * 1024;
    public int LOGCOUNT = 10;

    public LoggerWrapper() {
        this.LOGCOUNT = Helpers.tryParseInt((String)Properties.getProperties().getOrDefault(Values.LOG_COUNT, Values.DEFAULT_LOG_COUNT), Values.DEFAULT_LOG_COUNT);

        String path = Values.EMPTYSTRING;
        try {
            path = Helpers.getLogPath();
            path += "searchcode-server-%g.log";
            Handler handler = new FileHandler(path, this.BYTESLOGSIZE, this.LOGCOUNT);

            String logLevel = (String)Properties.getProperties().getOrDefault(Values.LOG_LEVEL, Values.DEFAULT_LOG_LEVEL);

            handler.setFormatter(new SimpleFormatter());

            logger = Logger.getLogger(Values.EMPTYSTRING);
            logger.addHandler(handler);

            switch (logLevel.toUpperCase()) {
                case "INFO":
                    handler.setLevel(Level.INFO);
                    logger.setLevel(Level.INFO);
                    break;
                case "FINE":
                    handler.setLevel(Level.FINE);
                    logger.setLevel(Level.FINE);
                    break;
                case "WARNING":
                    handler.setLevel(Level.WARNING);
                    logger.setLevel(Level.WARNING);
                    break;
                case "OFF":
                    handler.setLevel(Level.OFF);
                    logger.setLevel(Level.OFF);
                    break;
                case "SEVERE":
                default:
                    handler.setLevel(Level.SEVERE);
                    logger.setLevel(Level.SEVERE);
                    break;
            }

        } catch (IOException ex) {
            logger = Logger.getLogger(Values.EMPTYSTRING);
            logger.setLevel(Level.WARNING);

            logger.warning("\n//////////////////////////////////////////////////////////////////////\n" +
                    "// Unable to write to logging file" + (!path.isEmpty() ? ": " + path : ".") + "\n" +
                    "// Logs will be written to STDOUT.\n" +
                    "//////////////////////////////////////////////////////////////////////\n");
        }

        this.allCache = EvictingQueue.create(1000);
        this.infoRecentCache = EvictingQueue.create(1000);
        this.warningRecentCache = EvictingQueue.create(1000);
        this.severeRecentCache = EvictingQueue.create(1000);
        this.searchLog = EvictingQueue.create(1000);
    }

    public void info(String toLog) {
        String message = "INFO: " + new Date().toString() + ": " + toLog;
        try {
            this.allCache.add(message);
            this.infoRecentCache.add(message);
            this.logger.info(toLog);
        }
        catch (NoSuchElementException ex) {}
    }

    public void warning(String toLog) {
        String message = "WARNING: " + new Date().toString() + ": " + toLog;

        try {
            this.allCache.add(message);
            this.warningRecentCache.add(message);
            this.logger.warning(toLog);
        }
        catch (NoSuchElementException ex) {}
    }

    public void severe(String toLog) {
        String message = "SEVERE: " + new Date().toString() + ": " + toLog;

        try {
            this.allCache.add(message);
            this.severeRecentCache.add(message);
            this.logger.severe(toLog);
        }
        catch (NoSuchElementException ex) {}
    }

    public void searchLog(String toLog) {
        String message = "SEARCH: " + new Date().toString() + ": " + toLog;

        try {
            this.searchLog.add(message);
        }
        catch (NoSuchElementException ex) {}
    }

    public List<String> getAllLogs() {
        List<String> values = new ArrayList(this.allCache);
        return Lists.reverse(values);
    }

    public List<String> getInfoLogs() {
        List<String> values = new ArrayList(this.infoRecentCache);
        return Lists.reverse(values);
    }

    public List<String> getWarningLogs() {
        List<String> values = new ArrayList(this.warningRecentCache);
        return Lists.reverse(values);
    }

    public List<String> getSevereLogs() {
        List<String> values = new ArrayList(this.severeRecentCache);
        return Lists.reverse(values);
    }

    public List<String> getSearchLogs() {
        List<String> values = new ArrayList(this.searchLog);
        return Lists.reverse(values);
    }
}
