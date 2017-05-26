package com.searchcode.app.model;


public class ValidatorResult {
    public final boolean isValid;
    public final String reason;
    public RepoResult repoResult;
    public String line;

    public ValidatorResult(boolean isValid, String reason) {
        this.isValid = isValid;
        this.reason = reason;
    }

    public RepoResult getRepoResult() {
        return repoResult;
    }

    public void setRepoResult(RepoResult repoResult) {
        this.repoResult = repoResult;
    }

    public String getReason() {
        return reason;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }
}
