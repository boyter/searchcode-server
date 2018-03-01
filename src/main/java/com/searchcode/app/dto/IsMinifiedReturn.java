package com.searchcode.app.dto;

import java.util.List;

public class IsMinifiedReturn {
    private final boolean isMinified;
    private final List<String[]> reportList;

    public IsMinifiedReturn(boolean isMinified, List<String[]> reportList) {
        this.isMinified = isMinified;
        this.reportList = reportList;
    }

    public boolean isMinified() {
        return isMinified;
    }

    public List<String[]> getReportList() {
        return reportList;
    }
}