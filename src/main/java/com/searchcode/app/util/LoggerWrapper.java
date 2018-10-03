/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.util;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import com.searchcode.app.config.Values;
import com.searchcode.app.service.Singleton;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.*;

/**
 * Wrapper around logging so that we can store the logging inside a in memory queue
 * which can then be displayed rather then hit the filesystem. Should in theory allow
 * quick filters and the like.
 * TODO refactor this it is starting to get ugly
 */
public class LoggerWrapper {

    private Logger logger = null;
    private EvictingQueue allCache = null;
    private EvictingQueue infoRecentCache = null;
    private EvictingQueue severeRecentCache = null;
    private EvictingQueue searchLog = null;
    private EvictingQueue apiLog = null;
    private EvictingQueue fineRecentCache = null;

    int BYTESLOGSIZE = 10 * 1024 * 1024;
    int LOGCOUNT = 10;
    private boolean LOGSENABLED = true;
    Level LOGLEVELENUM = Level.SEVERE;
    private String LOGLEVEL = Values.DEFAULT_LOG_LEVEL;
    private String LOGPATH = Values.DEFAULT_LOG_PATH;
    private boolean LOGSTDOUT = false;

    public LoggerWrapper() {
        this.LOGCOUNT = Singleton.getHelpers().tryParseInt((String)Properties.getProperties().getOrDefault(Values.LOG_COUNT, Values.DEFAULT_LOG_COUNT), Values.DEFAULT_LOG_COUNT);
        this.LOGLEVEL = (String)Properties.getProperties().getOrDefault(Values.LOG_LEVEL, Values.DEFAULT_LOG_LEVEL);
        this.LOGPATH = Singleton.getHelpers().getLogPath();

        if (this.LOGLEVEL.equals("OFF")) {
            this.LOGSENABLED = false;
        }

        switch (this.LOGLEVEL.toUpperCase()) {
            case "INFO":
                this.LOGLEVELENUM = Level.INFO;
                break;
            case "SEVERE":
            default:
                this.LOGLEVELENUM = Level.SEVERE;
                break;
        }

        if (this.LOGPATH.equals("STDOUT")) {
            this.LOGSTDOUT = true;
            this.LOGSENABLED = false;
            this.LOGLEVEL = "OFF";
        }

        if (!this.LOGLEVEL.equals("OFF")) {
            try {
                this.LOGPATH += "searchcode-server-%g.log";
                Handler handler = new FileHandler(this.LOGPATH, this.BYTESLOGSIZE, this.LOGCOUNT);

                handler.setFormatter(new SimpleFormatter());

                logger = Logger.getLogger(Values.EMPTYSTRING);
                logger.addHandler(handler);


                switch (this.LOGLEVEL.toUpperCase()) {
                    case "INFO":
                        handler.setLevel(Level.INFO);
                        this.logger.setLevel(Level.INFO);
                        break;
                    case "SEVERE":
                    default:
                        handler.setLevel(Level.SEVERE);
                        this.logger.setLevel(Level.SEVERE);
                        break;
                }

            } catch (IOException ex) {
                this.logger = Logger.getLogger(Values.EMPTYSTRING);
                this.logger.setLevel(Level.WARNING);

                this.logger.warning("\n//////////////////////////////////////////////////////////////////////\n" +
                        "// Unable to write to logging file" + (!this.LOGPATH.isEmpty() ? ": " + this.LOGPATH : ".") + "\n" +
                        "// Logs will be written to STDOUT.\n" +
                        "//////////////////////////////////////////////////////////////////////\n");
            }
        }

        this.allCache = EvictingQueue.create(1000);
        this.infoRecentCache = EvictingQueue.create(1000);
        this.severeRecentCache = EvictingQueue.create(1000);
        this.searchLog = EvictingQueue.create(1000);
        this.apiLog = EvictingQueue.create(1000);
        this.fineRecentCache = EvictingQueue.create(1000);
    }

    public synchronized void clearAllLogs() {
        this.allCache.clear();
        this.infoRecentCache.clear();
        this.severeRecentCache.clear();
        this.searchLog.clear();
        this.apiLog.clear();
        this.fineRecentCache.clear();
    }

    public synchronized void info(String toLog) {
        String message = "INFO: " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + ": " + Thread.currentThread().getName() + " " + Thread.currentThread().getId() + ": " + toLog;
        try {
            this.allCache.add(message);
            this.infoRecentCache.add(message);

            if (this.LOGSENABLED) {
                this.logger.info(toLog);
            }

            if (this.LOGSTDOUT && this.isLoggable(Level.INFO)) {
                System.out.println(message);
            }
        }
        catch (NoSuchElementException ignored) {}
    }

    public synchronized void severe(String toLog) {
        String message = "SEVERE: " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + ": " + Thread.currentThread().getName() + " " + Thread.currentThread().getId() + ": " + toLog;

        try {
            this.allCache.add(message);
            this.severeRecentCache.add(message);
            if (this.LOGSENABLED) {
                this.logger.severe(toLog);
            }

            if (this.LOGSTDOUT && this.isLoggable(Level.SEVERE)) {
                System.out.println(message);
            }
        }
        catch (NoSuchElementException ignored) {}
    }

    public synchronized void searchLog(String toLog) {
        String message = "SEARCH: " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + ": " + Thread.currentThread().getName() + " " + Thread.currentThread().getId() + ": " + toLog;

        try {
            this.searchLog.add(message);
        }
        catch (NoSuchElementException ignored) {}
    }

    public synchronized void apiLog(String toLog) {
        String message = "API: " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + ": " + Thread.currentThread().getName() + " " + Thread.currentThread().getId() + ": " + toLog;

        try {
            this.apiLog.add(message);
        }
        catch (NoSuchElementException ignored) {}
    }

    public synchronized List<String> getAllLogs() {
        List<String> values = new ArrayList<>();
        try {
            values = new ArrayList(this.allCache);
            values = Lists.reverse(values);
        }
        catch (ArrayIndexOutOfBoundsException ignored) {}

        return values;
    }

    public synchronized List<String> getInfoLogs() {
        List<String> values = new ArrayList<>();
        try {
            values = new ArrayList(this.infoRecentCache);
            values = Lists.reverse(values);
        }
        catch (ArrayIndexOutOfBoundsException ignored) {}

        return values;
    }

    public synchronized List<String> getSevereLogs() {
        List<String> values = new ArrayList<>();
        try {
            values = new ArrayList(this.severeRecentCache);
            values = Lists.reverse(values);
        }
        catch (ArrayIndexOutOfBoundsException ignored) {}

        return values;
    }

    public synchronized List<String> getSearchLogs() {
        List<String> values = new ArrayList<>();
        try {
            values = new ArrayList(this.searchLog);
            values = Lists.reverse(values);
        }
        catch (ArrayIndexOutOfBoundsException ignored) {}

        return values;
    }

    public synchronized List<String> getApiLogs() {
        List<String> values = new ArrayList<>();
        try {
            values = new ArrayList(this.apiLog);
            values = Lists.reverse(values);
        }
        catch (ArrayIndexOutOfBoundsException ignored) {}

        return values;
    }

    public boolean isLoggable(Level level) {
        int levelValue = level.intValue();
        int mainValue = this.LOGLEVELENUM.intValue();

        return levelValue >= mainValue;
    }
}
