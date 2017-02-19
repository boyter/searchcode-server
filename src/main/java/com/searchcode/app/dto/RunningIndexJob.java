package com.searchcode.app.dto;

import com.searchcode.app.config.Values;

public class RunningIndexJob {
    public String status = Values.EMPTYSTRING;
    public int startTime = 0;

    public RunningIndexJob(String status, int startTime) {
        this.status = status;
        this.startTime = startTime;
    }
}
