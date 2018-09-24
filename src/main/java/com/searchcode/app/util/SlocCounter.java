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

import com.searchcode.app.dto.FileClassifierResult;
import com.searchcode.app.service.Singleton;

import java.util.ArrayList;
import java.util.HashMap;

public class SlocCounter {

    private final HashMap<String, FileClassifierResult> database;

    public enum State {
        S_BLANK,
        S_CODE,
        S_COMMENT,
        S_COMMENT_CODE,
        S_MULTICOMMENT,
        S_MULTICOMMENT_CODE,
        S_MULTICOMMENT_BLANK,
        S_STRING,
    }

    public SlocCounter() {
        this.database = Singleton.getFileClassifier().getDatabase();
    }

    public boolean checkForMatch(char currentByte, int index, int endPoint, String[] matches, String content) {
        for (int i = 0; i < matches.length; i++) { // For each match
            if (currentByte == matches[i].charAt(0)) { // If the first character matches
                boolean potentialMatch = true;

                for (int j = 0; j < matches[i].length(); j++) { // Check if the rest match
                    if (index + j <= endPoint && matches[i].charAt(j) != content.charAt(index + j)) {
                        potentialMatch = false;
                    }
                }

                if (potentialMatch) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean checkForMatchSingle(char currentByte, int index, int endPoint, String match, String content) {
        if (match.length() != 0 && currentByte == match.charAt(0)) { // If the first character matches
            boolean potentialMatch = true;

            for (int j = 0; j < match.length(); j++) { // Check if the rest match
                if (index + j <= endPoint && match.charAt(j) != content.charAt(index + j)) {
                    potentialMatch = false;
                }
            }

            if (potentialMatch) {
                return true;
            }
        }

        return false;
    }


    public String checkForMatchMultiOpen(char currentByte, int index, int endPoint, String[][] matches, String content) {
        for (int i = 0; i < matches.length; i++) { // For each match
            if (currentByte == matches[i][0].charAt(0)) { // If the first character matches
                boolean potentialMatch = true;

                for (int j = 0; j < matches[i][0].length(); j++) { // Check if the rest match
                    if (index + j <= endPoint && matches[i][0].charAt(j) != content.charAt(index + j)) {
                        potentialMatch = false;
                        break;
                    }
                }

                if (potentialMatch) {
                    // Refers to the closing condition for the matching open
                    return matches[i][1];
                }
            }
        }

        return null;
    }

    public boolean isWhitespace(char currentByte) {
        if (currentByte != ' ' && currentByte != '\t' && currentByte != '\n' && currentByte != '\r') {
            return false;
        }

        return true;
    }

    /**
     * Reimplementation of scc https://github.com/boyter/scc/ 1.9.0 ported from
     * Go into Java and specific for the searchcode project.
     */
    public SlocCount countStats(String contents, String languageName) {
        if (contents == null || contents.isEmpty()) {
            return new SlocCount();
        }

        FileClassifierResult fileClassifierResult = this.database.get(languageName);

        State currentState = State.S_BLANK;

        int endPoint = contents.length() - 1;
        String endString = null;
        ArrayList<String> endComments = new ArrayList<>();
        int linesCount = 0;
        int blankCount = 0;
        int codeCount = 0;
        int commentCount = 0;
        int complexity = 0;

        for (int index=0; index < contents.length(); index++) {

            if (!isWhitespace(contents.charAt(index))) {
                switch (currentState) {
                    case S_CODE:
                        if (fileClassifierResult.nestedmultiline || endComments.size() == 0) {
                            endString = this.checkForMatchMultiOpen(contents.charAt(index), index, endPoint, fileClassifierResult.multi_line, contents);
                            if (endString != null) {
                                index += endString.length() - 1;
                                endComments.add(endString);
                                currentState = State.S_MULTICOMMENT_CODE;
                                break;
                            }
                        }

                        if (this.checkForMatch(contents.charAt(index), index, endPoint, fileClassifierResult.line_comment, contents)) {
                            currentState = State.S_COMMENT_CODE;
                            break;
                        }

                        endString = this.checkForMatchMultiOpen(contents.charAt(index), index, endPoint, fileClassifierResult.quotes, contents);
                        if (endString != null) {
                            currentState = State.S_STRING;
                            break;
                        } else if (this.checkForMatch(contents.charAt(index), index, endPoint, fileClassifierResult.complexitychecks, contents)) {
                            complexity++;
                        }
                        break;
                    case S_STRING:
                        if (contents.charAt(index - 1) != '\\' && this.checkForMatchSingle(contents.charAt(index), index, endPoint, endString, contents)) {
                            currentState = State.S_CODE;
                        }
                        break;
                    case S_MULTICOMMENT:
                    case S_MULTICOMMENT_CODE:

                        if (fileClassifierResult.nestedmultiline || endComments.size() == 0) {
                            endString = this.checkForMatchMultiOpen(contents.charAt(index), index, endPoint, fileClassifierResult.multi_line, contents);
                            if (endString != null) {
                                index += endString.length() - 1;
                                endComments.add(endString);
                                currentState = State.S_MULTICOMMENT_CODE;
                                break;
                            }
                        }

                        if (this.checkForMatchSingle(contents.charAt(index), index, endPoint, endComments.get(endComments.size()-1), contents)) {
                            index += endComments.get(endComments.size()-1).length() - 1;
                            endComments.remove(endComments.size()-1);

                            if (endComments.size() == 0) {
                                if (currentState == State.S_MULTICOMMENT_CODE) {
                                    currentState = State.S_CODE;
                                } else {
                                    currentState = State.S_MULTICOMMENT_BLANK;
                                }
                            }
                        }
                        break;
                    case S_BLANK:
                    case S_MULTICOMMENT_BLANK:
                        if (this.checkForMatch(contents.charAt(index), index, endPoint, fileClassifierResult.line_comment, contents)) {
                            currentState = State.S_COMMENT;
                            break;
                        }

                        if (fileClassifierResult.nestedmultiline || endComments.size() == 0) {
                            endString = this.checkForMatchMultiOpen(contents.charAt(index), index, endPoint, fileClassifierResult.multi_line, contents);
                            if (endString != null) {
                                index += endString.length() - 1;
                                endComments.add(endString);
                                currentState = State.S_MULTICOMMENT;
                                break;
                            }
                        }

                        endString = this.checkForMatchMultiOpen(contents.charAt(index), index, endPoint, fileClassifierResult.quotes, contents);
                        if (endString != null) {
                            currentState = State.S_STRING;
                            break;
                        }

                        currentState = State.S_CODE;
                        if (this.checkForMatch(contents.charAt(index), index, endPoint, fileClassifierResult.complexitychecks, contents)) {
                            complexity++;
                        }
                        break;
                }
            }

            // This means the end of processing the line so calculate the stats according to what state
            // we are currently in
            if (contents.charAt(index) == '\n' || index == endPoint) {
                linesCount++;

                switch (currentState) {
                    case S_BLANK:
                        blankCount++;
                        break;
                    case S_COMMENT:
                    case S_MULTICOMMENT:
                    case S_MULTICOMMENT_BLANK:
                        commentCount++;
                        break;
                    case S_CODE:
                    case S_STRING:
                    case S_COMMENT_CODE:
                    case S_MULTICOMMENT_CODE:
                        codeCount++;
                        break;
                }


                if (currentState == State.S_MULTICOMMENT || currentState == State.S_MULTICOMMENT_CODE) {
                    currentState = State.S_MULTICOMMENT;
                } else if (currentState == State.S_STRING) {
                    currentState = State.S_STRING;
                } else {
                    currentState = State.S_BLANK;
                }
            }
        }

        return new SlocCount(linesCount, blankCount, codeCount, commentCount, complexity);
    }

    public class SlocCount {
        public int linesCount = 0;
        public int blankCount = 0;
        public int codeCount = 0;
        public int commentCount = 0;
        public int complexity = 0;

        public SlocCount() {}

        public SlocCount(int linesCount, int blankCount, int codeCount, int commentCount, int complexity) {
            this.linesCount = linesCount;
            this.blankCount = blankCount;
            this.codeCount = codeCount;
            this.commentCount = commentCount;
            this.complexity = complexity;
        }
    }
}
