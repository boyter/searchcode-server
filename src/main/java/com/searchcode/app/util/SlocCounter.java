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

import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.dto.FileClassifierResult;
import com.searchcode.app.service.Singleton;

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
        boolean potentialMatch = true;

        // TODO add end of content checks
        for (int i = 0; i < matches.length; i++) { // For each match
            if (currentByte == matches[i].charAt(0)) { // If the first character matches
                for (int j = 0; j < matches[i].length(); j++) { // Check if the rest match
                    if (matches[i].charAt(j) != content.charAt(index + j)) {
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
        boolean potentialMatch = true;

        // TODO add end of content checks
        if (match.length() != 0 && currentByte == match.charAt(0)) { // If the first character matches
            for (int j = 0; j < match.length(); j++) { // Check if the rest match
                if (match.charAt(j) != content.charAt(index + j)) {
                    potentialMatch = false;
                }
            }

            if (potentialMatch) {
                return true;
            }
        }

        return false;
    }


    public boolean checkForMatchMultiOpen(char currentByte, int index, int endPoint, String[][] matches, String content) {
        boolean potentialMatch = true;

        for (int i = 0; i < matches.length; i++) { // For each match
            if (currentByte == matches[i][0].charAt(0)) { // If the first character matches
                for (int j = 0; j < matches[i][0].length(); j++) { // Check if the rest match
                    if (matches[i][0].charAt(j) != content.charAt(index + j)) {
                        potentialMatch = false;
                        break;
                    }
                }

                if (potentialMatch) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean checkForMatchMultiClose(char currentByte, int index, int endPoint, String[][] matches, String content) {
        boolean potentialMatch = true;

        for (int i = 0; i < matches.length; i++) { // For each match
            if (currentByte == matches[i][1].charAt(0)) { // If the first character matches
                for (int j = 0; j < matches[i][1].length(); j++) { // Check if the rest match
                    if (matches[i][1].charAt(j) != content.charAt(index + j)) {
                        potentialMatch = false;
                        break;
                    }
                }

                if (potentialMatch) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isWhitespace(char currentByte) {
        if (currentByte != ' ' && currentByte != '\t' && currentByte != '\n' && currentByte != '\r') {
            return false;
        }

        return true;
    }

    public int countStats(CodeIndexDocument codeIndexDocument) {
        String contents = codeIndexDocument.getContents();

        if (contents.isEmpty()) {
            return 0;
        }

        FileClassifierResult fileClassifierResult = this.database.get(codeIndexDocument.getLanguageName());

        State currentState = State.S_BLANK;

        int linesCount = 1;
        int blankCount = 0;
        int codeCount = 0;
        int commentCount = 0;
        int complexity = 0;

        for (int index=0; index < contents.length(); index++) {
            switch (currentState) {
                case S_BLANK:
                case S_MULTICOMMENT_BLANK:
                    if (this.checkForMatch(contents.charAt(index), index, 0, fileClassifierResult.line_comment, contents)) {
                        currentState = State.S_COMMENT;
                        break;
                    }

                    if (this.checkForMatchMultiOpen(contents.charAt(index), index, 0, fileClassifierResult.multi_line, contents)) {
                        currentState = State.S_MULTICOMMENT;
                        break;
                    }

                    if (this.checkForMatchMultiOpen(contents.charAt(index), index, 0, fileClassifierResult.quotes, contents)) {
                        currentState = State.S_STRING;
                        break;
                    }

                    if (!this.isWhitespace(contents.charAt(index))) {
                        currentState = State.S_CODE;

                        if (this.checkForMatch(contents.charAt(index), index, 0, fileClassifierResult.complexitychecks, contents)) {
                            complexity++;
                        }
                    }
                    break;
                case S_CODE:
                    if (this.checkForMatchMultiOpen(contents.charAt(index), index, 0, fileClassifierResult.multi_line, contents)) {
                        currentState = State.S_MULTICOMMENT_CODE;
                        break;
                    }

                    if (this.checkForMatchMultiOpen(contents.charAt(index), index, 0, fileClassifierResult.quotes, contents)) {
                        currentState = State.S_STRING;
                        break;
                    } else if (this.checkForMatch(contents.charAt(index), index, 0, fileClassifierResult.complexitychecks, contents)) {
                        complexity++;
                    }
                    break;
                case S_STRING:
                    // TODO actually store and pass in the matching condition we want
                    if (contents.charAt(index-1) != '\\' && this.checkForMatchSingle(contents.charAt(index), index, 0, "", contents)) {
                        currentState = State.S_CODE;
                    }
                    break;
                case S_MULTICOMMENT:
                case S_MULTICOMMENT_CODE:
                    if (this.checkForMatchMultiClose(contents.charAt(index), index, 0, fileClassifierResult.multi_line, contents)) {
                        if (currentState == State.S_MULTICOMMENT_CODE) {
                            currentState = State.S_CODE;
                        } else {
                            // TODO check if out of bounds
                            if (this.isWhitespace(contents.charAt(index+1))) {
                                currentState = State.S_MULTICOMMENT_BLANK;
                            } else {
                                currentState = State.S_MULTICOMMENT_CODE;
                            }
                        }
                    }
                    break;
            }

            // This means the end of processing the line so calculate the stats according to what state
            // we are currently in
            if (contents.charAt(index) == '\n') {
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

                // If we are in a multiline comment that started after some code then we need
                // to move to a multiline comment if a multiline comment then stay there
                // otherwise we reset back into a blank state
                if (currentState != State.S_MULTICOMMENT && currentState != State.S_MULTICOMMENT_CODE) {
                    currentState = State.S_BLANK;
                } else {
                    currentState = State.S_MULTICOMMENT;
                }
            }
        }

        return linesCount;
    }
}
