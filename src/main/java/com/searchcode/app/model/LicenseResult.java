package com.searchcode.app.model;

import java.util.ArrayList;

public class LicenseResult {
    public String name;
    public String licenseId;
    public String licenseText;
    public ArrayList<String> keywords;

    public LicenseResult() {}

    public LicenseResult(String name, String licenseId, String licenseText, ArrayList<String> keywords) {
        this.name = name;
        this.licenseId = licenseId;
        this.licenseText = licenseText;
        this.keywords = keywords;
    }
}
