/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.2
 */

package com.searchcode.app.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dto.CodeOwner;
import com.searchcode.app.service.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchcodeLib {

    private int MAXSPLITLENGTH = 100000;
    private Pattern MULTIPLEUPPERCASE = Pattern.compile("[A-Z]{2,}");
    private int MINIFIEDLENGTH = Integer.parseInt(Values.DEFAULTMINIFIEDLENGTH);

    public SearchcodeLib(){}

    public SearchcodeLib(Data data) {
        this.MINIFIEDLENGTH = Integer.parseInt(data.getDataByName(Values.MINIFIEDLENGTH, Values.DEFAULTMINIFIEDLENGTH));
        if (this.MINIFIEDLENGTH <= 0) {
            this.MINIFIEDLENGTH = Integer.parseInt(Values.DEFAULTMINIFIEDLENGTH);
        }
    }

    /**
     * Split "intelligently" on anything over 7 characters long
     * if it only contains [a-zA-Z]
     * split based on uppercase String[] r = s.split("(?=\\p{Upper})");
     * add those as additional words to index on
     * so that things like RegexIndexer becomes Regex Indexer
     * split the string by spaces
     * look for anything over 7 characters long
     * if its only [a-zA-Z]
     * split by uppercase
     */
    public String splitKeywords(String contents) {
        if (contents == null) {
            return Values.EMPTYSTRING;
        }

        StringBuilder indexContents = new StringBuilder();

        contents = contents.replaceAll("[^a-zA-Z0-9]", " ");

        // Performance improvement hack
        if (contents.length() > this.MAXSPLITLENGTH) {

            // Add AAA to ensure we dont split the last word if it was cut off
            contents = contents.substring(0, MAXSPLITLENGTH) + "AAA";
        }

        for (String splitContents: contents.split(" ")) {
            if (splitContents.length() >= 7) {
                Matcher m = MULTIPLEUPPERCASE.matcher(splitContents);

                if (!m.find()) {
                    String[] splitStrings = splitContents.split("(?=\\p{Upper})");

                    if (splitStrings.length > 1) {
                        indexContents.append(" ");
                        indexContents.append(StringUtils.join(splitStrings, " "));
                    }
                }
            }
        }

        return indexContents.toString();
    }

    public String findInterestingKeywords(String contents) {
        if (contents == null) {
            return Values.EMPTYSTRING;
        }

        StringBuilder indexContents = new StringBuilder();

        // Performance improvement hack
        if (contents.length() > this.MAXSPLITLENGTH) {

            // Add AAA to ensure we dont split the last word if it was cut off
            contents = contents.substring(0, MAXSPLITLENGTH) + "AAA";
        }

        // Finds versions with words at the front, eg linux2.7.4
        Matcher m = Pattern.compile("[a-z]+(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)").matcher(contents);

        while (m.find()) {
            indexContents.append(" ");
            indexContents.append(m.group());
        }

        return indexContents.toString();
    }


    /**
     * List of languages to ignore displaying the cost for
     */
    public boolean languageCostIgnore(String languagename) {

        boolean ignore;

        switch(languagename) {
            case "Unknown":
            case "Text":
            case "JSON":
            case "Markdown":
            case "INI File":
            case "ReStructuredText":
            case "Configuration":
                ignore = true;
                break;
            default:
                ignore = false;
                break;
        }

        return ignore;
    }

    /**
     * Tries to guess the true amount of lines in a code file ignoring those that are blank or comments
     * fairly crude however without resorting to parsing which is slow its good enough for our purposes
     */
    public int countFilteredLines(List<String> codeLines) {
        return codeLines.stream().map(x -> x.trim()).filter(x -> {
            return !(x.startsWith("//") ||
                    x.startsWith("#") ||
                    x.length() == 0 ||
                    x.startsWith("<!--") ||
                    x.startsWith("!*") ||
                    x.startsWith("--") ||
                    x.startsWith("%") ||
                    x.startsWith(";") ||
                    x.startsWith("*") ||
                    x.startsWith("/*"));
        }).toArray().length;
    }

    /**
     * Adds a string into the spelling corrector.
     * TODO move this into the spelling corrector class itself
     */
    public void addToSpellingCorrector(String contents) {
        if (contents == null) {
            return;
        }

        ISpellingCorrector sc = Singleton.getSpellingCorrector();

        // Limit to reduce performance impacts
        if (contents.length() > this.MAXSPLITLENGTH) {
            contents = contents.substring(0, MAXSPLITLENGTH);
        }

        List<String> splitString = Arrays.asList(contents.replaceAll("[^a-zA-Z0-9]", " ").toLowerCase().split(" "));

        // Only the first 10000 to avoid causing too much slow-down
        if (splitString.size() > 10000) {
            splitString = splitString.subList(0, 10000);
        }

        for (String s: splitString) {
            if (s.length() >= 3) {
                sc.putWord(s);
            }
        }
    }

    /**
     * Determine if a List<String> which is used to represent a code file contains a code file that is
     * suspected to be minified. This is for the purposes of excluding it from the index.
     */
    public boolean isMinified(List<String> codeLines) {
        OptionalDouble average = codeLines.stream().map(x -> x.trim().replace(" ", "")).mapToInt(String::length).average();
        if (average.isPresent() && average.getAsDouble() > this.MINIFIEDLENGTH) {
            return true;
        }

        return false;
    }

    /**
     * Determine if a List<String> which is used to represent a code file contains a code file that is
     * suspected to be ascii or non ascii. This is for the purposes of excluding it from the index.
     */
    public boolean isBinary(List<String> codeLines) {
        if (codeLines.isEmpty()) {
            return true;
        }

        int lines = codeLines.size() < 100 ? codeLines.size() : 100;
        double asciiCount = 0;
        double nonAsciiCount = 0;

        for (int i=0; i < lines; i++) {
            String line = codeLines.get(i);
            for (int j = 0; j < line.length(); j++) {
                if (((int)line.charAt(j)) <= 128) {
                    asciiCount++;
                }
                else {
                    nonAsciiCount++;
                }
            }
        }

        // If 95% of characters are not ascii then its probably binary
        double percent = asciiCount / (asciiCount + nonAsciiCount);

        if (percent < 0.95) {
            return true;
        }

        return false;
    }

    /**
     * Determines who owns a piece of code weighted by time based on current second (IE time now)
     * NB if a commit is very close to this time it will always win
     */
    public String codeOwner(List<CodeOwner> codeOwners) {
        long currentUnix = System.currentTimeMillis() / 1000L;

        double best = 0;
        String owner = "Unknown";

        for(CodeOwner codeOwner: codeOwners) {
            double age = (currentUnix - codeOwner.getMostRecentUnixCommitTimestamp()) / 60 / 60;
            double calc = codeOwner.getNoLines() / Math.pow((age), 1.8);

            if (calc > best) {
                best = calc;
                owner = codeOwner.getName();
            }
        }

        return owner;
    }

    /**
     * Cleans and formats the code into something that can be indexed by lucene while supporting searches such as
     * i++ matching for(int i=0;i<100;i++;){
     */
    public String codeCleanPipeline(String contents) {
        if (contents == null) {
            return Values.EMPTYSTRING;
        }

        StringBuilder indexContents = new StringBuilder();

        // Change how we replace strings
        // Modify the contents to match strings correctly
        char[] firstReplacements = {'<', '>', ')', '(', '[', ']', '|', '=', ','};
        for (char c : firstReplacements) {
            contents = contents.replace(c, ' ');
        }
        indexContents.append(" ");
        indexContents.append(contents);

        char[] secondReplacements = {';', '{', '}', '/'};
        for (char c : secondReplacements) {
            contents = contents.replace(c, ' ');
        }
        indexContents.append(" ");
        indexContents.append(contents);

        char[] forthReplacements = {'"', '\''};
        for (char c : forthReplacements) {
            contents = contents.replace(c, ' ');
        }
        indexContents.append(" ");
        indexContents.append(contents);

        // Now do it for other characters
        char[] replacements = {'\'', '"', '.', ';', '=', '(', ')', '[', ']', '_', ';', '@', '#'};
        for (char c : replacements) {
            contents = contents.replace(c, ' ');
        }
        indexContents.append(" ");
        indexContents.append(contents);

        char[] thirdReplacements = {'-'};
        for (char c : thirdReplacements) {
            contents = contents.replace(c, ' ');
        }
        indexContents.append(" ");
        indexContents.append(contents);

        return indexContents.toString();
    }

    /**
     * Parse the query and escape it as per Lucene but without affecting search operators such as AND OR and NOT
     */
    public String formatQueryString(String query) {
        String[] split = query.trim().split("\\s+");

        StringBuilder sb = new StringBuilder();

        for(String term: split) {
            switch (term) {
                case "AND":
                    sb.append("AND ");
                    break;
                case "OR":
                    sb.append(" OR ");
                    break;
                case "NOT":
                    sb.append(" NOT ");
                    break;
                default:
                    sb.append(" ");
                    sb.append(QueryParser.escape(term.toLowerCase()).replace("\\(", "(").replace("\\)", ")").replace("\\*", "*"));
                    sb.append(" ");
                    break;
            }
        }

        return sb.toString().trim();
    }

    /**
     * Given a query attempts to create alternative queries that should be looser and as such produce more matches
     * or give results where none may exist for the current query.
     */
    public List<String> generateAltQueries(String query) {
        List<String> altQueries = new ArrayList<>();
        query = query.trim().replaceAll(" +", " ");
        String altquery = query.replaceAll("[^A-Za-z0-9 ]", " ").trim().replaceAll(" +", " ");

        if(!altquery.equals(query) && !Values.EMPTYSTRING.equals(altquery)) {
            altQueries.add(altquery);
        }

        altquery = this.splitKeywords(query).trim();
        if(!altquery.equals("") && !altquery.equals(query) && !altQueries.contains(altquery)) {
            altQueries.add(altquery);
        }

        ISpellingCorrector sc = Singleton.getSpellingCorrector();
        altquery = Values.EMPTYSTRING;
        for(String word: query.replaceAll(" +", " ").split(" ")) {
            if(!word.trim().equals("AND") && !word.trim().equals("OR") && !word.trim().equals("NOT")) {
                altquery += " " + sc.correct(word);
            }
        }
        altquery = altquery.trim();

        if(!altquery.toLowerCase().equals(query.toLowerCase()) && !altQueries.contains(altquery)) {
            altQueries.add(altquery);
        }

        altquery = query.replace(" AND ", " OR ");
        if (!altquery.toLowerCase().equals(query.toLowerCase()) && !altQueries.contains(altquery)) {
            altQueries.add(altquery);
        }

        altquery = query.replace(" AND ", " ");
        if (!altquery.toLowerCase().equals(query.toLowerCase()) && !altQueries.contains(altquery)) {
            altQueries.add(altquery);
        }

        altquery = query.replace(" NOT ", " ");
        if (!altquery.toLowerCase().equals(query.toLowerCase()) && !altQueries.contains(altquery)) {
            altQueries.add(altquery);
        }

        return altQueries;
    }


    /**
     * Currently not used but meant to replicate the searchcode.com hash which is used to identify duplicate files
     * even when they have a few characters or lines missing. It should in these cases produce identical hashes.
     */
    public String hash(String contents) {
        int hashLength = 20;

        if(contents.length() == 0) {
            return Strings.padStart("", hashLength, '0');
        }

        String allowedCharacters = "BCDFGHIJKLMNOPQRSUVWXYZbcdfghijklmnopqrsuvwxyz1234567890";

        // remove all spaces
        Joiner joiner = Joiner.on("").skipNulls();
        String toHash = joiner.join(Splitter.on(' ')
                            .trimResults()
                            .omitEmptyStrings()
                            .split(contents));

        // remove all non acceptable characters
        for(int i=0; i< toHash.length(); i++) {
            char c = toHash.charAt(i);

            if(allowedCharacters.indexOf(c) != -1) {
                // allowed so keep it
            }
        }

        return "";
    }


    /**
     * A list containing all the known file types, their extensions and a selection of commonly used keywords inside
     * that file type. Used to identify files.
     */

    private static SourceLangClassifier sourceLangClassifier = new SourceLangClassifier();

    /**
     * Given a filename and the lines inside the file attempts to guess the type of the file.
     * TODO When no match attempt to identify using the file keywords
     */
    public String languageGuesser(String fileName, List<String> codeLines) {
        return sourceLangClassifier.detect(fileName, String.join("\n", codeLines));
    }
}

