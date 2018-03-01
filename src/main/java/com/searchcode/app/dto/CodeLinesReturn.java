package com.searchcode.app.dto;

import java.util.List;

public class CodeLinesReturn {
    private final List<String> codeLines;
    private final boolean error;
    private final List<String[]> reportList;

    public CodeLinesReturn(List<String> codeLines, List<String[]> reportList, boolean error) {
        this.codeLines = codeLines;
        this.reportList = reportList;
        this.error = error;
    }

    public List<String> getCodeLines() {
        return codeLines;
    }

    public List<String[]> getReportList() {
        return reportList;
    }

    public boolean isError() {
        return error;
    }
}
