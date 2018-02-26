package com.searchcode.app.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.searchcode.app.config.Values;
import com.searchcode.app.model.LicenseResult;
import com.searchcode.app.service.Singleton;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LicenceChecker {
    private String DATABASEPATH = Properties.getProperties().getProperty(Values.LICENSE_DATABASE_LOCATION, Values.DEFAULT_LICENSE_DATABASE_LOCATION);
    private ArrayList<LicenseResult> database = new ArrayList<>();

    public LicenceChecker() {
        this.database = this.loadDatabase();
    }

    public List<LicenseResult> getDatabase() {
        return this.database;
    }

    public void processFile() {
//        licenseGuesses = guessLicense(string(content), deepGuess, loadDatabase())
//        licenseIdentified = identifierGuessLicence(string(content), loadDatabase())
    }

    public ArrayList<LicenseResult> guessLicense(String content) {
        for(LicenseResult licenseResult: this.database) {

        }
        return null;
    }

//    func guessLicense(content string, deepguess bool, licenses []License) []LicenseMatch {
//        matchingLicenses := []LicenseMatch{}
//
//        for _, license := range keywordGuessLicense(content, licenses) {
//            matchingLicense := License{}
//
//            for _, l := range licenses {
//                if l.LicenseId == license.LicenseId {
//                    matchingLicense = l
//                    break
//                }
//            }
//
//            runecontent := []rune(content)
//            trimto := utf8.RuneCountInString(matchingLicense.LicenseText)
//
//            if trimto > len(runecontent) {
//                trimto = len(runecontent)
//            }
//
//            contentConcordance := vectorspace.BuildConcordance(string(runecontent[:trimto]))
//            relation := vectorspace.Relation(matchingLicense.Concordance, contentConcordance)
//
//            if relation >= confidence {
//                matchingLicenses = append(matchingLicenses, LicenseMatch{LicenseId: license.LicenseId, Percentage: relation})
//            }
//        }
//
//        if len(matchingLicenses) == 0 && deepguess == true {
//            for _, license := range licenses {
//                runecontent := []rune(content)
//                trimto := utf8.RuneCountInString(license.LicenseText)
//
//                if trimto > len(runecontent) {
//                    trimto = len(runecontent)
//                }
//
//                contentConcordance := vectorspace.BuildConcordance(string(runecontent[:trimto]))
//                relation := vectorspace.Relation(license.Concordance, contentConcordance)
//
//                if relation >= confidence {
//                    matchingLicenses = append(matchingLicenses, LicenseMatch{LicenseId: license.LicenseId, Percentage: relation})
//                }
//            }
//        }
//
//        sort.Slice(matchingLicenses, func(i, j int) bool {
//            return matchingLicenses[i].Percentage > matchingLicenses[j].Percentage
//        })
//
//        return matchingLicenses
//    }


    public List<String> identifierGuessLicence(String content) {
        Matcher matcher = Pattern.compile("SPDX-License-Identifier:\\s+(.*?)[ |\\n|\\r\\n]").matcher(content);

        ArrayList<String> matches = new ArrayList<>();

        while (matcher.find()) {
            matches.add(matcher.group(1));
        }

        return matches;
    }

    /**
     * Loads the License database from a JSON file on disk
     */
    private ArrayList<LicenseResult> loadDatabase() {
        ArrayList<LicenseResult> database = new ArrayList<>();

        try {
            Gson gson = new GsonBuilder().create();
            LicenseResult[] myArray = gson.fromJson(new FileReader(this.DATABASEPATH), LicenseResult[].class);
            database = new ArrayList<>(Arrays.asList(myArray));
        }
        catch (FileNotFoundException | JsonSyntaxException ex) {
            Singleton.getLogger().warning("Unable to load OWASP Database from disk " + ex);
        }

        return database;
    }
}
