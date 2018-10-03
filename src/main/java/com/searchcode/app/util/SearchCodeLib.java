/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.util;

import com.google.common.collect.Iterables;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dto.*;
import com.searchcode.app.service.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchCodeLib {

    private final ISpellingCorrector spellingCorrector;
    private final FileClassifier fileClassifier;
    private final int MINIFIED_LENGTH;

    private final int MAX_SPLIT_LENGTH = 100000;
    private final Pattern MULTIPLE_UPPERCASE = Pattern.compile("[A-Z]{2,}");
    private final boolean GUESS_BINARY = Boolean.parseBoolean(Properties.getProperties().getProperty(Values.GUESS_BINARY, Values.DEFAULT_GUESS_BINARY));
    private final boolean AND_MATCH = Boolean.parseBoolean(com.searchcode.app.util.Properties.getProperties().getProperty(Values.AND_MATCH, Values.DEFAULT_AND_MATCH));

    public String[] WHITE_LIST = Properties.getProperties().getProperty(Values.BINARY_WHITE_LIST, Values.DEFAULT_BINARY_WHITE_LIST).split(",");
    public String[] BLACK_LIST = Properties.getProperties().getProperty(Values.BINARY_BLACK_LIST, Values.DEFAULT_BINARY_BLACK_LIST).split(",");

    public SearchCodeLib() {
        this(Singleton.getSpellingCorrector(), new FileClassifier(), Singleton.getData(), Singleton.getHelpers());
    }

    public SearchCodeLib(ISpellingCorrector spellingCorrector, FileClassifier fileClassifier, Data data, Helpers helpers) {
        this.spellingCorrector = spellingCorrector;
        this.fileClassifier = fileClassifier;

        int minifiedLength = helpers.tryParseInt(data.getDataByName(Values.MINIFIEDLENGTH, Values.DEFAULTMINIFIEDLENGTH), Values.DEFAULTMINIFIEDLENGTH);
        this.MINIFIED_LENGTH = minifiedLength <= 0 ? Integer.parseInt(Values.DEFAULTMINIFIEDLENGTH) : minifiedLength;
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
    public String splitKeywords(String contents, boolean runningJoin) {
        if (contents == null) {
            return Values.EMPTYSTRING;
        }

        StringBuilder indexContents = new StringBuilder();

        contents = contents.replaceAll("[^a-zA-Z0-9]", " ");

        // Performance improvement hack
        if (contents.length() > this.MAX_SPLIT_LENGTH) {

            // Add AAA to ensure we dont split the last word if it was cut off
            contents = contents.substring(0, MAX_SPLIT_LENGTH) + "AAA";
        }

        for (String splitContents: contents.split(" ")) {
            if (splitContents.length() >= 7) {
                Matcher m = MULTIPLE_UPPERCASE.matcher(splitContents);

                if (!m.find()) {
                    String[] splitStrings = splitContents.split("(?=\\p{Upper})");

                    if (splitStrings.length > 1) {
                        indexContents.append(" ").append(StringUtils.join(splitStrings, " "));

                        if (runningJoin) {
                            StringBuilder running = new StringBuilder();
                            for (String split : splitStrings) {
                                running.append(split);
                                indexContents.append(" ").append(running.toString());
                            }
                        }
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
        if (contents.length() > this.MAX_SPLIT_LENGTH) {
            // Add AAA to ensure we dont split the last word if it was cut off
            contents = contents.substring(0, MAX_SPLIT_LENGTH) + "AAA";
        }

        // Finds versions with words at the front, eg linux2.7.4
        Matcher m = Pattern.compile("[a-z]+(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)").matcher(contents);

        while (m.find()) {
            indexContents.append(" ");
            indexContents.append(m.group());
        }

        return indexContents.toString();
    }

    public String findInterestingCharacters(String contents) {
        if (contents == null) {
            return Values.EMPTYSTRING;
        }

        String replaced = contents.replaceAll("\\w", "");

        StringBuilder stringBuilder = new StringBuilder();
        for (char c: replaced.toCharArray()) {
            stringBuilder.append(c).append(" ");
        }

        return stringBuilder.toString();
    }

    /**
     * List of languages to ignore displaying the cost for
     * TODO move this into the database so it is configurable
     */
    public boolean languageCostIgnore(String languagename) {

        boolean ignore;

        switch (languagename) {
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
     * Adds a string into the spelling corrector.
     * TODO move this into the spelling corrector class itself
     */
    public void addToSpellingCorrector(String contents) {
        if (contents == null) {
            return;
        }

        // Limit to reduce performance impacts
        if (contents.length() > this.MAX_SPLIT_LENGTH) {
            contents = contents.substring(0, MAX_SPLIT_LENGTH);
        }

        List<String> splitString = Arrays.asList(contents.replaceAll("[^a-zA-Z0-9]", " ").toLowerCase().split(" "));

        // Only the first 10000 to avoid causing too much slow-down
        if (splitString.size() > 10000) {
            splitString = splitString.subList(0, 10000);
        }

        for (String s: splitString) {
            if (s.length() >= 3) {
                this.spellingCorrector.putWord(s);
            }
        }
    }

    /**
     * Determine if a List<String> which is used to represent a code file contains a code file that is
     * suspected to be minified. This is for the purposes of excluding it from the index.
     */
    public boolean isMinified(List<String> codeLines, String fileName) {

        String lowerFileName = fileName.toLowerCase();

        for (String extension: this.WHITE_LIST) {
            if (lowerFileName.endsWith("." + extension)) {
                return false;
            }
        }

        OptionalDouble average = codeLines.stream().map(x -> x.trim().replace(" ", "")).mapToInt(String::length).average();
        if (average.isPresent() && average.getAsDouble() > this.MINIFIED_LENGTH) {
            return true;
        }

        return false;
    }

    /**
     * Determine if a List<String> which is used to represent a code file contains a code file that is
     * suspected to be ascii or non ascii. This is for the purposes of excluding it from the index.
     */
    public BinaryFinding isBinary(List<String> codeLines, String fileName) {
        if (codeLines.isEmpty()) {
            return new BinaryFinding(true, "file is empty");
        }

        String lowerFileName = fileName.toLowerCase();
        // Check against user set whitelist
        for (String extension: this.WHITE_LIST) {
            if (lowerFileName.endsWith("." + extension)) {
                return new BinaryFinding(false, "appears in extension whitelist");
            }
        }

        // Check against user set blacklist
        for (String extension: this.BLACK_LIST) {
            if (lowerFileName.endsWith("." + extension) || lowerFileName.equals(extension)) {
                return new BinaryFinding(true, "appears in extension blacklist");
            }
        }

        // Check if whitelisted extention IE what we know about
        HashMap<String, FileClassifierResult> database = fileClassifier.getDatabase();
        for (String key: database.keySet()) {
            FileClassifierResult fileClassifierResult = database.get(key);
            for (String extention: fileClassifierResult.extensions) {
                if (lowerFileName.endsWith("." + extention)) {
                    return new BinaryFinding(false, "appears in internal extension whitelist");
                }
            }
        }

        // If we aren't meant to guess then assume it isn't binary
        if (!this.GUESS_BINARY) {
            return new BinaryFinding(false, Values.EMPTYSTRING);
        }

        // GNU Grep, ripgrep and git all take the approach that if a file as a nul
        // byte in it then it is binary. If its good enough for those giants
        // its good enough for us.
        for (int i = 0; i < codeLines.size(); i++) {
            String line = codeLines.get(i);
            for (int j = 0; j < line.length(); j++) {
                if (line.charAt(j) == 0) {
                    return new BinaryFinding(true, "nul byte found");
                }
            }
        }

        return new BinaryFinding(false, Values.EMPTYSTRING);
    }

    /**
     * Determines who owns a piece of code weighted by time based on current second (IE time now)
     * NB if a commit is very close to this time it will always win
     */
    public String codeOwner(List<CodeOwner> codeOwners) {
        long currentUnix = System.currentTimeMillis() / 1000L;

        double best = 0;
        String owner = "Unknown";

        for (CodeOwner codeOwner: codeOwners) {
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
    public String codeCleanPipeline(String originalContents) {
        if (originalContents == null) {
            return Values.EMPTYSTRING;
        }

        String modifiedContents = originalContents;

        StringBuilder indexContents = new StringBuilder();

        // Change how we replace strings
        // Modify the contents to match strings correctly
        char[] firstReplacements = {'<', '>', ')', '(', '[', ']', '|', '=', ',', ':'};
        for (char c : firstReplacements) {
            modifiedContents = modifiedContents.replace(c, ' ');
        }
        indexContents.append(" ").append(modifiedContents);

        char[] otherReplacements = {'.'};
        for (char c : otherReplacements) {
            modifiedContents = modifiedContents.replace(c, ' ');
        }
        indexContents.append(" ").append(modifiedContents);

        char[] secondReplacements = {';', '{', '}', '/'};
        for (char c : secondReplacements) {
            modifiedContents = modifiedContents.replace(c, ' ');
        }
        indexContents.append(" ").append(modifiedContents);

        char[] forthReplacements = {'"', '\''};
        for (char c : forthReplacements) {
            modifiedContents = modifiedContents.replace(c, ' ');
        }
        indexContents.append(" ").append(modifiedContents);

        // Now do it for other characters
        char[] replacements = {'\'', '"', '.', ';', '=', '(', ')', '[', ']', '_', ';', '@', '#'};
        for (char c : replacements) {
            modifiedContents = modifiedContents.replace(c, ' ');
        }
        indexContents.append(" ").append(modifiedContents);

        char[] thirdReplacements = {'-'};
        for (char c : thirdReplacements) {
            modifiedContents = modifiedContents.replace(c, ' ');
        }
        indexContents.append(" ").append(modifiedContents);

        // Issue 188 Fixes
        modifiedContents = originalContents;
        char[] replacements188 = {'(', ')', '<', '>'};
        for (char c : replacements188) {
            modifiedContents = modifiedContents.replace(c, ' ');
        }
        indexContents.append(" ").append(modifiedContents);


        return indexContents.toString();
    }

    /**
     * Parse the query and escape it as per Lucene but without affecting search operators such as AND OR and NOT
     */
    public String formatQueryString(String query) {
        if (this.AND_MATCH) {
            return this.formatQueryStringAndDefault(query);
        }

        return this.formatQueryStringOrDefault(query);
    }

    public String formatQueryStringAndDefault(String query) {
        String[] split = query.trim().split("\\s+");

        List<String> stringList = new ArrayList<>();

        String and = " AND ";
        String or = " OR ";
        String not = " NOT ";

        for (String term: split) {
            switch (term) {
                case "AND":
                    if (Iterables.getLast(stringList, null) != null && !Iterables.getLast(stringList).equals(and)) {
                        stringList.add(and);
                    }
                    break;
                case "OR":
                    if (Iterables.getLast(stringList, null) != null && !Iterables.getLast(stringList).equals(or)) {
                        stringList.add(or);
                    }
                    break;
                case "NOT":
                    if (Iterables.getLast(stringList, null) != null && !Iterables.getLast(stringList).equals(not)) {
                        stringList.add(not);
                    }
                    break;
                default:
                    if (Iterables.getLast(stringList, null) == null ||
                            Iterables.getLast(stringList).equals(and) ||
                            Iterables.getLast(stringList).equals(or) ||
                            Iterables.getLast(stringList).equals(not)) {
                        stringList.add(" " + QueryParser.escape(term.toLowerCase()).replace("\\(", "(").replace("\\)", ")").replace("\\*", "*") + " ");
                    }
                    else {
                        stringList.add(and + QueryParser.escape(term.toLowerCase()).replace("\\(", "(").replace("\\)", ")").replace("\\*", "*") + " ");
                    }
                    break;
            }
        }
        String temp = StringUtils.join(stringList, " ");
        return temp.trim();
    }

    public String formatQueryStringOrDefault(String query) {
        String[] split = query.trim().split("\\s+");

        StringBuilder sb = new StringBuilder();

        String and = " AND ";
        String or = " OR ";
        String not = " NOT ";

        for(String term: split) {
            switch (term) {
                case "AND":
                    sb.append(and);
                    break;
                case "OR":
                    sb.append(or);
                    break;
                case "NOT":
                    sb.append(not);
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

        if (!altquery.equals(query) && !Values.EMPTYSTRING.equals(altquery)) {
            altQueries.add(altquery);
        }

        altquery = this.splitKeywords(query, false).trim();
        if (!altquery.equals("") && !altquery.equals(query) && !altQueries.contains(altquery)) {
            altQueries.add(altquery);
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String word: query.replaceAll(" +", " ").split(" ")) {
            if (!word.trim().equals("AND") && !word.trim().equals("OR") && !word.trim().equals("NOT")) {
                stringBuilder.append(" ").append(this.spellingCorrector.correct(word));
            }
        }
        altquery = stringBuilder.toString().trim();

        if (!altquery.toLowerCase().equals(query.toLowerCase()) && !altQueries.contains(altquery)) {
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


    public String generateBusBlurb(ProjectStats projectStats) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("In this repository ").append(projectStats.getRepoFacetOwner().size());

        if (projectStats.getRepoFacetOwner().size() == 1) {
            stringBuilder.append(" committer has contributed to ");
        }
        else {
            stringBuilder.append(" committers have contributed to ");
        }

        if (projectStats.getTotalFiles() == 1) {
            stringBuilder.append(projectStats.getTotalFiles()).append(" file. ");
        }
        else {
            stringBuilder.append(projectStats.getTotalFiles()).append(" files. ");
        }

        List<CodeFacetLanguage> codeFacetLanguages = projectStats.getCodeFacetLanguages();

        if (codeFacetLanguages.size() == 1) {
            stringBuilder.append("The most important language in this repository is ").append(codeFacetLanguages.get(0).getLanguageName()).append(". ");
        }
        else {
            stringBuilder.append("The most important languages in this repository are ");

            if (!codeFacetLanguages.isEmpty()) {
                if (codeFacetLanguages.size() > 3) {
                    codeFacetLanguages = codeFacetLanguages.subList(0, 3);
                }
                for (int i = 0; i < codeFacetLanguages.size() - 1; i++) {
                    stringBuilder.append(codeFacetLanguages.get(i).getLanguageName()).append(", ");
                }
                stringBuilder.append(" and ").append(codeFacetLanguages.get(codeFacetLanguages.size() - 1).getLanguageName()).append(". ");
            }
        }

        if (!projectStats.getRepoFacetOwner().isEmpty()) {
            if (projectStats.getRepoFacetOwner().size() < 5) {
                stringBuilder.append("The project has a low bus factor of ").append(projectStats.getRepoFacetOwner().size());
                stringBuilder.append(" and will be in trouble if ").append(projectStats.getRepoFacetOwner().get(0).getOwner()).append(" is hit by a bus. ");
            } else if (projectStats.getRepoFacetOwner().size() < 15) {
                stringBuilder.append("The project has bus factor of ").append(projectStats.getRepoFacetOwner().size()).append(". ");
            } else {
                stringBuilder.append("The project has high bus factor of ").append(projectStats.getRepoFacetOwner().size()).append(". ");
            }
        }

        List<String> highKnowledge = new ArrayList<>();
        double sumAverageFilesWorked = 0;
        for (CodeFacetOwner codeFacetOwner: projectStats.getRepoFacetOwner()) {
            double currentAverage = (double)codeFacetOwner.getCount() / (double)projectStats.getTotalFiles();
            sumAverageFilesWorked += currentAverage;

            if (currentAverage > 0.1) {
                highKnowledge.add(codeFacetOwner.getOwner());
            }
        }

        int averageFilesWorked = (int)(sumAverageFilesWorked / projectStats.getRepoFacetOwner().size() * 100);

        stringBuilder.append("The average person who commits this project has ownership of ");
        stringBuilder.append(averageFilesWorked).append("% of files. ");

        if (!highKnowledge.isEmpty()) {
            stringBuilder.append("The project relies on the following people; ");
            stringBuilder.append(StringUtils.join(highKnowledge, ", ")).append(". ");
        }

        return stringBuilder.toString().replace(",  and", " and");
    }

//    /**
//     * Currently not used but meant to replicate the searchcode.com hash which is used to identify duplicate files
//     * even when they have a few characters or lines missing. It should in these cases produce identical hashes.
//     */
//    public String hash(String contents) {
//        int hashLength = 20;
//
//        if (contents.length() == 0) {
//            return Strings.padStart("", hashLength, '0');
//        }
//
//        String allowedCharacters = "BCDFGHIJKLMNOPQRSUVWXYZbcdfghijklmnopqrsuvwxyz1234567890";
//
//        // remove all spaces
//        Joiner joiner = Joiner.on("").skipNulls();
//        String toHash = joiner.join(Splitter.on(' ')
//                            .trimResults()
//                            .omitEmptyStrings()
//                            .split(contents));
//
//        // remove all non acceptable characters
//        for(int i=0; i< toHash.length(); i++) {
//            char c = toHash.charAt(i);
//
//            if (allowedCharacters.indexOf(c) != -1) {
//                // allowed so keep it
//            }
//        }
//
//        return "";
//    }
}

