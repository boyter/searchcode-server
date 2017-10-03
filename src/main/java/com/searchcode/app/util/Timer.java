package com.searchcode.app.util;

public class Timer {
    private long start_time;

    public Timer() {
        this.start_time = System.nanoTime();
    }

    public double tic() {
        return this.start_time = System.nanoTime();
    }

    public double toc() {
        return (System.nanoTime() - start_time) / 1000000000.0;
    }
}
