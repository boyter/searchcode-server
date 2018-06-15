/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.14
 */

package com.searchcode.app.util;


import com.searchcode.app.config.Values;
import com.searchcode.app.service.Singleton;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A simple spell checker based on a few implementations such as the infamous Peter Noving spell checker and
 * the like. Attempts to be highly performing by never changing the first character (for the first pass)
 * since we can usually assume that the user got that correct.
 */
public class SearchcodeSpellingCorrector implements ISpellingCorrector {

    // How many terms to keep in the LRUCACHE
    private int LRUCOUNT = Integer.parseInt(Values.DEFAULTSPELLINGCORRECTORSIZE);

    private int VARIATIONSCOUNT = 200000;

    // word to count map - how may times a word is present - or a weight attached to a word
    private Map<String, Integer> dictionary = null;

    public SearchcodeSpellingCorrector() {
        this.LRUCOUNT = Integer.parseInt(Properties.getProperties().getProperty(Values.SPELLINGCORRECTORSIZE, Values.DEFAULTSPELLINGCORRECTORSIZE));
        if (this.LRUCOUNT <= 0) {
            this.LRUCOUNT = Integer.parseInt(Values.DEFAULTSPELLINGCORRECTORSIZE);
        }

        this.dictionary = Collections.synchronizedMap(new LruCache<>(this.LRUCOUNT));
    }

    @Override
    public int getWordCount() {
        return dictionary.size();
    }

    @Override
    public boolean reset() {
        this.dictionary.clear();
        return true;
    }

    @Override
    public List<String> getSampleWords(int count) {
        List<String> sampleWords = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : this.dictionary.entrySet()) {
            sampleWords.add(entry.getValue() + " - " + entry.getKey());
        }

        int end = sampleWords.size() >= 10 ? 10 : sampleWords.size();

        return sampleWords.subList(0, end);
    }

    @Override
    public void putWord(String word) {
        word = word.toLowerCase();
        if (dictionary.containsKey(word)) {
            dictionary.put(word, (dictionary.get(word) + 1));
        }
        else {
            dictionary.put(word, 1);
        }
    }

    @Override
    public String correct(String word) {
        if (Singleton.getHelpers().isNullEmptyOrWhitespace(word)) {
            return word;
        }

        word = word.toLowerCase();

        // If the word exists in our dictionary then return
        if (dictionary.containsKey(word)) {
            return word;
        }

        Map<String, Integer> possibleMatches = new HashMap<>();

        List<String> closeEdits = this.wordEdits(word);
        for (String closeEdit: closeEdits) {
            if (dictionary.containsKey(closeEdit)) {
                possibleMatches.put(closeEdit, this.dictionary.get(closeEdit));
            }
        }

        if (closeEdits.size() > VARIATIONSCOUNT) {
            closeEdits = closeEdits.subList(0, VARIATIONSCOUNT);
        }

        if (!possibleMatches.isEmpty()) {
            // Sorted least likely first
            Object[] matches = Singleton.getHelpers().sortByValue(possibleMatches).keySet().toArray();

            // Try to match anything of the same length first
            String bestMatch = Values.EMPTYSTRING;
            for (Object o: matches) {
                if (o.toString().length() == word.length()) {
                    bestMatch = o.toString();
                }
            }

            if (!Singleton.getHelpers().isNullEmptyOrWhitespace(bestMatch)) {
                return bestMatch;
            }

            // Just return whatever is the best match
            return matches[matches.length - 1].toString();
        }

        // Ok we did't find anything, so lets run the edits function on the previous results and use those
        // this gives us results which are 2 characters away from whatever was entered
        List<String> furtherEdits = new ArrayList<>();
        for (String closeEdit: closeEdits) {
            furtherEdits.addAll(this.wordEdits(closeEdit));

            if (furtherEdits.size() > this.VARIATIONSCOUNT) {
                break;
            }
        }

        for (String furtherEdit: furtherEdits) {
            if (dictionary.containsKey(furtherEdit)) {
                possibleMatches.put(furtherEdit, this.dictionary.get(furtherEdit));
            }
        }

        if (!possibleMatches.isEmpty()) {
            // Sorted least likely first
            Object[] matches = Singleton.getHelpers().sortByValue(possibleMatches).keySet().toArray();

            // Try to match anything of the same length first
            String bestMatch = Values.EMPTYSTRING;
            for (Object o: matches) {
                if (o.toString().length() == word.length()) {
                    bestMatch = o.toString();
                }
            }

            if (!Singleton.getHelpers().isNullEmptyOrWhitespace(bestMatch)) {
                return bestMatch;
            }

            // Just return whatever is the best match
            return matches[matches.length - 1].toString();
        }


        // If unable to find something better return the same string
        return word;
    }

    @Override
    public boolean containsWord(String word) {
        return dictionary.containsKey(word);
    }


    /**
     * Return a list of strings which are words similar to our one which could potentially be misspellings
     * Abuse the fact that a char can be used as an integer
     * Assume that they got the first letter correct for all edits to cut on CPU burn time
     */
    private List<String> wordEdits(String word) {
        List<String> closeWords = new ArrayList<String>();

        for (int i = 1; i < word.length() + 1; i++) {
            for (char character = 'a'; character <= 'z'; character++) {
                // Maybe they forgot to type a letter? Try adding one
                StringBuilder sb = new StringBuilder(word);
                sb.insert(i, character);
                closeWords.add(sb.toString());
            }

            if (closeWords.size() > this.VARIATIONSCOUNT) {
                return closeWords;
            }
        }

        for (int i = 1; i < word.length(); i++) {
            for (char character = 'a'; character <= 'z'; character++) {
                // Maybe they mistyped a single letter? Try replacing them all
                StringBuilder sb = new StringBuilder(word);
                sb.setCharAt(i, character);
                closeWords.add(sb.toString());

                // Maybe they added an extra letter? Try deleting one
                sb = new StringBuilder(word);
                sb.deleteCharAt(i);
                closeWords.add(sb.toString());
            }

            if (closeWords.size() > this.VARIATIONSCOUNT) {
                return closeWords;
            }
        }

        return closeWords;
    }
}
