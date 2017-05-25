package com.searchcode.app.model;


public class ValidatorResult {
    public final boolean isValid;
    public final String reason;

    public ValidatorResult(boolean isValid, String reason) {
        this.isValid = isValid;
        this.reason = reason;
    }
}
