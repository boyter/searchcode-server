package com.searchcode.app.model;

public class LicenseMatch {
    public final String licenseId;
    public final float percentage;

    public LicenseMatch(String licenseId, float percentage) {
        this.licenseId = licenseId;
        this.percentage = percentage;
    }
}
