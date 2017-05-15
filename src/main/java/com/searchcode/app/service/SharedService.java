package com.searchcode.app.service;

public class SharedService {
    private int codeIndexLinesCount = 0; // Used to store how many lines we have ready to index for throttling
    private boolean backgroundJobsEnabled = true; // Controls if all background queue jobs should run or not
    private boolean pauseBackgroundJobs = false; // Controls if all jobs should pause

    public synchronized void incrementCodeIndexLinesCount(int incrementBy) {
        codeIndexLinesCount = codeIndexLinesCount + incrementBy;
    }

    public synchronized void decrementCodeIndexLinesCount(int decrementBy) {
        codeIndexLinesCount = codeIndexLinesCount - decrementBy;

        if (codeIndexLinesCount < 0) {
            codeIndexLinesCount = 0;
        }
    }

    public synchronized void setCodeIndexLinesCount(int value) {
        this.codeIndexLinesCount = value;
    }

    public synchronized int getCodeIndexLinesCount() {
        return this.codeIndexLinesCount;
    }

    public synchronized boolean getBackgroundJobsEnabled() {
        return this.backgroundJobsEnabled;
    }

    public synchronized void setBackgroundJobsEnabled(boolean jobsEnabled) {
        this.backgroundJobsEnabled = jobsEnabled;
    }

    public synchronized boolean getPauseBackgroundJobs() {
        return this.pauseBackgroundJobs;
    }

    public synchronized void setPauseBackgroundJobs(boolean pauseBackgroundJobs) {
        this.pauseBackgroundJobs = pauseBackgroundJobs;
    }
}
