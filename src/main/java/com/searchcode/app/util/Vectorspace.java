package com.searchcode.app.util;


import com.searchcode.app.service.Singleton;

import java.util.HashMap;

public class Vectorspace {

    private final Helpers helpers;

    public Vectorspace() {
        this.helpers = Singleton.getHelpers();
    }

    public double magnitude(HashMap<String, Integer> concordance) {
        double total = 0.0;

        for (String key: concordance.keySet()) {
            total += Math.pow((double)concordance.get(key), 2.0);
        }

        return Math.sqrt(total);
    }

    public double relation(HashMap<String, Integer> concordance1, HashMap<String, Integer> concordance2) {
        double relevance = 0;
        double topvalue = 0;

        for (String key: concordance1.keySet()) {
            if (concordance2.containsKey(key)) {
                topvalue += concordance1.get(key) * concordance2.get(key);
            }
        }

        double mag = this.magnitude(concordance1) * this.magnitude(concordance2);

        if (mag != 0) {
            return topvalue / mag;
        }

        return 0;
    }

    public HashMap<String, Integer> concordance(String document) {
        HashMap<String, Integer> concordance = new HashMap<>();

        for (String word: this.cleanText(document).split(" ")) {
            if (!this.helpers.isNullEmptyOrWhitespace(word)) {
                if (concordance.containsKey(word)) {
                    concordance.put(word, concordance.get(word) + 1);
                } else {
                    concordance.put(word, 1);
                }
            }
        }

        return concordance;
    }

    // TODO consider moving this out, must match the logic for building the licence database 100%
    public String cleanText(String document) {
        document = document.toLowerCase();
        document = document.replaceAll("[^a-zA-Z0-9 ]", " ");
        document = document.replaceAll("\\s+", " ");
        return document.trim();
    }
}
