/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.service;


import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;
import com.searchcode.app.util.Helpers;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.text.NumberFormat;

/**
 * Simple class used to show basic stats such ad the total number of searches and how long the application
 * has been online for.
 * TODO add things such as most frequent searches, most recent search etc...
 */
public class StatsService {

    private Data data;
    private Helpers helpers;

    public StatsService() {
        this.data = Singleton.getData();
        this.helpers = Singleton.getHelpers();
    }

    public StatsService(Data data, Helpers helpers) {
        this.data = data;
        this.helpers = helpers;
    }

    public void incrementSearchCount() {
        int totalCount = this.helpers.tryParseInt(data.getDataByName(Values.CACHE_TOTAL_SEARCH, "0"), "0");

        if (totalCount == Integer.MAX_VALUE) {
            totalCount = 0;
        }

        totalCount++;
        data.saveData(Values.CACHE_TOTAL_SEARCH, Values.EMPTYSTRING + totalCount);
    }

    public void clearSearchCount() {
        data.saveData(Values.CACHE_TOTAL_SEARCH, "0");
    }

    public int getSearchCount() {
        int totalCount = this.helpers.tryParseInt(data.getDataByName(Values.CACHE_TOTAL_SEARCH, "0"), "0");
        return totalCount;
    }

    public String getLoadAverage() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        String loadAverage = Values.DECIMAL_FORMAT.format(osBean.getSystemLoadAverage());

        if (loadAverage.equals("-1")) {
            loadAverage = "Unknown";
        }

        return loadAverage;
    }

    public String getArch() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        return osBean.getArch();
    }

    public String getOsVersion() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        return osBean.getVersion();
    }

    public String getProcessorCount() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        return Values.EMPTYSTRING + osBean.getAvailableProcessors();
    }

    public String getMemoryUsage(String seperator) {
        Runtime runtime = Runtime.getRuntime();

        NumberFormat format = NumberFormat.getInstance();

        StringBuilder sb = new StringBuilder();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMB = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;

        sb.append("free memory: ").append(format.format(freeMemory / 1024)).append(seperator);
        sb.append("allocated memory: ").append(format.format(allocatedMemory / 1024)).append(seperator);
        sb.append("max memory: ").append(format.format(maxMemory / 1024)).append(seperator);
        sb.append("total free memory: ").append(format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024)).append(seperator);
        sb.append("memory usage: ").append(usedMB).append("MB");

        return sb.toString();
    }

    /**
     * Returns how long the application has been up in seconds, minutes or hours using larger
     * time units where appropriate
     * TODO add in larger time units such as days
     * TODO change to display something such as 3 days 4 hours 45 minutes 23 seconds
     */
    public String getUpTime() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

        int upTime = (int)(runtimeBean.getUptime() / 1000);
        int minutes = upTime / 60;
        int hours = minutes / 60;


        if (upTime < 120) {
            return upTime + " seconds";
        }

        if (minutes < 120) {
            return minutes + " minutes";
        }

        return hours + " hours";
    }
}
