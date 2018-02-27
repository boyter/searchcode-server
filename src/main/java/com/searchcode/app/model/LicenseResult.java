package com.searchcode.app.model;

import java.util.ArrayList;
import java.util.HashMap;

public class LicenseResult {
    public String name;
    public String licenseId;
    public String licenseText;
    public ArrayList<String> keywords;
    public HashMap<String, Integer> concordance;

    public LicenseResult() {}

    public LicenseResult(String name, String licenseId, String licenseText, ArrayList<String> keywords) {
        this.name = name;
        this.licenseId = licenseId;
        this.licenseText = licenseText;
        this.keywords = keywords;
        this.concordance = new HashMap<>();
    }

    public void setConcordance(HashMap<String, Integer> concordance) {
        this.concordance = concordance;
    }
}
