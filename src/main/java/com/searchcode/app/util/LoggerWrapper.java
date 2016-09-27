/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.1
 */

package com.searchcode.app.util;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import com.searchcode.app.config.Values;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.*;

/**
 * Wrapper around logging so that we can store the logging inside a in memory queue
 * which can then be displayed rather then hit the filesystem. Should in theory allow
 * quick filters and the like.
 */
public class LoggerWrapper {

    private Logger logger = null;

    private EvictingQueue recentCache = null;

    public LoggerWrapper() {
        String path = "";
        try {
            path = (String) Properties.getProperties().getOrDefault("log_path", "./");
            path = FilenameUtils.separatorsToUnix(path);
            if (path.charAt(path.length() - 1) != '/')
                path = path + "/";
            path += "searchcode-server-%g.log";
            Handler handler = new FileHandler(path, 10 * 1024 * 1024, 1);

            String logLevel = (String) Properties.getProperties().getOrDefault("log_level", "severe");

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

        this.recentCache = EvictingQueue.create(1000);
    }

    public void info(String toLog) {
        this.recentCache.add("INFO: " + new Date().toString() + ": " + toLog);
        this.logger.info(toLog);
    }

    public void warning(String toLog) {
        this.recentCache.add("WARNING: " + new Date().toString() + ": " + toLog);
        this.logger.warning(toLog);
    }

    public void severe(String toLog) {
        this.recentCache.add("SEVERE: " + new Date().toString() + ": " + toLog);
        this.logger.warning(toLog);
    }

    public List<String> getLogs() {
        List<String> values = new ArrayList(this.recentCache);
        return Lists.reverse(values);
    }
}