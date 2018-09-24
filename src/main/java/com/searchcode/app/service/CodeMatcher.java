/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.service;

import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dto.CodeMatchResult;
import com.searchcode.app.dto.CodeResult;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Responsible for formatting the code results so they appear nicely with relevant lines. Changes to anything in
 * here need to be made with great care for performance as this is the "hottest" collection of methods with regards
 * to user performance.
 */
public class CodeMatcher {

    public int MATCHLINES = Integer.parseInt(Values.DEFAULTMATCHLINES);
    public int MAXLINEDEPTH = Integer.parseInt(Values.DEFAULTMAXLINEDEPTH);

    public CodeMatcher()  {
        this(Singleton.getData());
    }

    public CodeMatcher(Data data) {
        this.MATCHLINES = Singleton.getHelpers().tryParseInt(data.getDataByName(Values.MATCHLINES, Values.DEFAULTMATCHLINES), Values.DEFAULTMATCHLINES);
        this.MAXLINEDEPTH = Singleton.getHelpers().tryParseInt(data.getDataByName(Values.MAXLINEDEPTH, Values.DEFAULTMAXLINEDEPTH), Values.DEFAULTMAXLINEDEPTH);
    }

    /**
     * Entry point for matching lines
     */
    public List<CodeResult> formatResults(List<CodeResult> codeResult, String matchTerms, boolean highlightLine) {
        List<String> lstMatchTerms = this.splitTerms(matchTerms);

        List<CodeResult> results = new ArrayList<>();

        for (CodeResult code: codeResult) {
            List<CodeMatchResult> result = this.matchResults(code.getCode(), lstMatchTerms, highlightLine);

            if (result != null) {
                code.setMatchingResults(result);
                results.add(code);
            }
        }

        return results;
    }

    /**
     * Actually does the matching for a single code result given the match terms
     */
    public List<CodeMatchResult> matchResults(List<String> code, List<String> matchTerms, boolean highlightLine) {
        List<CodeMatchResult> resultLines = findMatchingLines(code, matchTerms, highlightLine);
        List<CodeMatchResult> newResultLines = new ArrayList<>();

        // get the top matching lines for this result
        resultLines.sort((p1, p2) -> Integer.valueOf(p2.getLineMatches()).compareTo(p1.getLineMatches()));

        // gets the best snippets based on number of matches
        for(int i = 0; i < resultLines.size(); i++) {
            CodeMatchResult match = resultLines.get(i);
            match.setLineNumber(match.getLineNumber() + 1);

            if (!resultExists(newResultLines, match.getLineNumber())) {
                newResultLines.add(match);
            }

            CodeMatchResult resultBefore = getResultByLineNumber(resultLines, match.getLineNumber() - 1);
            CodeMatchResult resultAfter  = getResultByLineNumber(resultLines, match.getLineNumber() + 1);

            if (resultBefore != null && !resultExists(newResultLines, match.getLineNumber() - 1)) {
                newResultLines.add(resultBefore);
            }
            if (resultAfter != null && !resultExists(newResultLines, match.getLineNumber() + 1)) {
                newResultLines.add(resultAfter);
            }

            if (newResultLines.size() >= MATCHLINES) {
                break;
            }
        }

        newResultLines.sort((p1, p2) -> Integer.valueOf(p1.getLineNumber()).compareTo(p2.getLineNumber()));

        if (!newResultLines.isEmpty()) {
            newResultLines.get(0).addBreak = false;
            return newResultLines;
        }

        return null;
    }

    /**
     * If changing anything in here be wary of performance issues as it is the slowest method by a long shot.
     * Be especially careful of branch prediction issues which is why this method has been re-written several times
     * just to avoid those issues even though the result was a LONGER method
     * TODO wring more performance out of this method where possible
     */
    public List<CodeMatchResult> findMatchingLines(List<String> code, List<String> matchTerms, boolean highlightLine) {
        List<CodeMatchResult> resultLines = new LinkedList<>();

        int codesize = code.size();
        int searchThrough = codesize > this.MAXLINEDEPTH ? this.MAXLINEDEPTH : codesize;
        int matching = 0;

        // Go through each line finding matching lines
        for (int i = 0; i < searchThrough; i++) {
            String matchRes = code.get(i).toLowerCase().replaceAll("\\s+", " ");
            matching = 0;

            for (String matchTerm: matchTerms) {
                if (matchRes.contains(matchTerm.replace("*", ""))) {
                    matching++;
                }
            }

            if (matching != 0) {
                resultLines.add(new CodeMatchResult(code.get(i), true, false, matching, i));
            }
        }

        // Get the adjacent lines
        List<CodeMatchResult> adjacentLines = new LinkedList<>();
        for (CodeMatchResult cmr: resultLines) {
            int linenumber = cmr.getLineNumber();
            int previouslinenumber = linenumber - 1;
            int nextlinenumber = linenumber + 1;

            if (previouslinenumber >= 0 && !this.resultExists(resultLines, previouslinenumber)) {
                adjacentLines.add(new CodeMatchResult(code.get(previouslinenumber), false, false, 0, previouslinenumber));
            }

            if (nextlinenumber < codesize && !this.resultExists(resultLines, nextlinenumber)) {
                adjacentLines.add(new CodeMatchResult(code.get(nextlinenumber), false, false, 0, nextlinenumber));
            }
        }

        resultLines.addAll(adjacentLines);

        // If not matching we probably matched on the filename or past 10000
        if (resultLines.size() == 0) {
            searchThrough = codesize > MATCHLINES ? MATCHLINES : codesize;

            for (int i = 0; i < searchThrough; i++) {
                resultLines.add(new CodeMatchResult(code.get(i), false, false, 0, i));
            }
        }

        // Highlight the lines if required but always escape everything
        if (highlightLine) {
            for (CodeMatchResult cmr: resultLines) {
                if (cmr.isMatching()) {
                    String line = Values.EMPTYSTRING;
                    try {
                        line = this.highlightLine(cmr.getLine(), matchTerms);
                    }
                    catch(StringIndexOutOfBoundsException ex) {
                        Singleton.getLogger().severe("Unable to highlightLine " + cmr.getLine() + " using terms " + String.join(",", matchTerms) +  " "+ ex.toString());
                    }
                    cmr.setLine( line );
                } else {
                    cmr.setLine( StringEscapeUtils.escapeHtml4(cmr.getLine()) );
                }
            }
        } else {
            for (CodeMatchResult cmr: resultLines) {
                cmr.setLine( StringEscapeUtils.escapeHtml4(cmr.getLine()) );
            }
        }

        return resultLines;
    }

    // TODO Investigate issues such as "List<String> test = *p;"
    // which produces string> and <string which is not what
    // we want although they are cleared later so not a huge issue
    public List<String> splitTerms(String matchTerms) {
        List<String> splitMatchTerms = new ArrayList<>();
        List<String> newTerms = new ArrayList<>();

        for (String s: matchTerms.trim().split(" ")) {
            if (!s.isEmpty()) {
                switch (s) {
                    case "AND":
                    case "OR":
                    case "NOT":
                        splitMatchTerms.add(s);
                        break;
                    default:
                        splitMatchTerms.add(s.toLowerCase());
                }
            }
        }

        for (String s: splitMatchTerms) {
            for (String t: s.split("\\.")) {
                if (!t.isEmpty()) {
                    newTerms.add(t);
                }
            }
            for (String t: s.split("\\(")) {
                if (!t.isEmpty()) {
                    newTerms.add(t);
                }
            }
            for (String t: s.split("\\-")) {
                if (!t.isEmpty()) {
                    newTerms.add(t);
                }
            }
            for (String t: s.split("<")) {
                if (!t.isEmpty()) {
                    newTerms.add(t);
                }
            }
            for (String t: s.split(">")) {
                if (!t.isEmpty()) {
                    newTerms.add(t);
                }
            }
            newTerms.add(s);
        }

        // Remove duplicates
        List<String> depdupeTerms = new ArrayList<>(new LinkedHashSet<>(newTerms));
        // Sort largest to smallest to produce largest matching results
        depdupeTerms.sort((p1, p2) -> Integer.valueOf(p2.length()).compareTo(p1.length()));
        return depdupeTerms;
    }

    /**
     * Given a string and the terms we want to highlight attempts to parse it apart and surround the matching
     * terms with <strong> tags.
     * TODO a bug exists here, see test cases for details
     */
    public String highlightLine(String line, List<String> matchTerms) throws StringIndexOutOfBoundsException {

        List<String> terms = matchTerms.stream()
                .filter(s -> !"AND".equals(s) && !"OR".equals(s) && !"NOT".equals(s))
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        List<String> tokens = Arrays.asList(line.split(" "));
        List<String> returnList = new ArrayList<>();

        for (String token: tokens) {

            String longestTerm = "";

            for(String term: terms) {
                // Find the longest matching
                if (term.replace(")", "").endsWith("*")) {
                    if (token.toLowerCase().contains(term.replace(")", "").replace("*", ""))) {
                        if (term.length() > longestTerm.length()) {
                            longestTerm = term;
                        }
                    }
                }
                else {
                    if (token.toLowerCase().contains(term)) {
                        if (term.length() > longestTerm.length()) {
                            longestTerm = term;
                        }
                    }
                }
            }

            if (!"".equals(longestTerm)) {
                if (longestTerm.replace(")", "").endsWith("*")) {
                    int loc = token.toLowerCase().indexOf(longestTerm.replace(")", "").replace("*", ""));

                    returnList.add(StringEscapeUtils.escapeHtml4(
                            token.substring(0, loc)) +
                            "<strong>" +
                            StringEscapeUtils.escapeHtml4(token.substring(loc, token.length())) +
                            "</strong>");
                }
                else {
                    int loc = token.toLowerCase().indexOf(longestTerm);

                    returnList.add(StringEscapeUtils.escapeHtml4(
                            token.substring(0, loc)) +
                            "<strong>" +
                            StringEscapeUtils.escapeHtml4(token.substring(loc, loc + longestTerm.length())) +
                            "</strong>" +
                            this.highlightLine(token.substring(loc + longestTerm.length(), token.length()), matchTerms));
                }
            }
            else {
                returnList.add(StringEscapeUtils.escapeHtml4(token));
            }
        }

        return StringUtils.join(returnList, " ");
    }

    /**
     * Helper to check if result exists in the collection based on line number
     */
    private boolean resultExists(List<CodeMatchResult> lst, int value) {
        for(CodeMatchResult s: lst){
            if (s.getLineNumber() == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper to pull value out of the list based on line number
     */
    private CodeMatchResult getResultByLineNumber(List<CodeMatchResult> lst, int value) {
        for(CodeMatchResult s: lst){
            if (s.getLineNumber() == value) {
                return s;
            }
        }
        return null;
    }
}