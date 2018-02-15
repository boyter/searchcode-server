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

//    func identifierGuessLicence(content string, licenses []License) []LicenseMatch {
//        matchingLicenses := []LicenseMatch{}
//        matches := spdxLicenceRegex.FindAllStringSubmatch(content, -1)
//
//        for _, val := range matches {
//            for _, license := range licenses {
//                if license.LicenseId == strings.TrimSpace(val[1]) {
//                    matchingLicenses = append(matchingLicenses, LicenseMatch{LicenseId: license.LicenseId, Percentage: 1})
//                }
//            }
//        }
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
