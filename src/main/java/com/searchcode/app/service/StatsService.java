/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.3
 */

package com.searchcode.app.service;


import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

/**
 * Simple class used to show basic stats such ad the total number of searches and how long the application
 * has been online for.
 * TODO add things such as most frequent searches, most recent search etc...
 */
public class StatsService {

    private String totalSearchKey = "statsservice-totalsearch";

    public void incrementSearchCount() {
        int totalCount = (Integer) Singleton.getGenericCache().getOrDefault(totalSearchKey, 0);
        totalCount++;
        Singleton.getGenericCache().put(totalSearchKey, totalCount);
    }

    public String getLoadAverage() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        return "" + osBean.getSystemLoadAverage();
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
        return "" + osBean.getAvailableProcessors();
    }

    /**
     * Returns how long the application has been up in seconds, minutes or hours using larger
     * time units where appropriate
     * TODO add in larger time units such as days
     * TODO change to display something such as 3 days 4 hours 45 minutes 23 seconds
     */
    public String getUptime() {
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

    public int getSearchCount() {
        return (Integer) Singleton.getGenericCache().getOrDefault(totalSearchKey, 0);
    }
}
