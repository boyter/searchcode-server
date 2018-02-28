package com.searchcode.app.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.searchcode.app.config.Values;
import com.searchcode.app.model.LicenseMatch;
import com.searchcode.app.model.LicenseResult;
import com.searchcode.app.service.Singleton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LicenceChecker {

    private final Vectorspace vectorSpace;
    private String DATABASEPATH = Properties.getProperties().getProperty(Values.LICENSE_DATABASE_LOCATION, Values.DEFAULT_LICENSE_DATABASE_LOCATION);
    private String LICENSE_FILES = Properties.getProperties().getProperty(Values.LICENSE_FILES, Values.DEFAULT_LICENSE_FILES);
    private ArrayList<LicenseResult> database = new ArrayList<>();

    public LicenceChecker() {
        this.database = this.loadDatabase();
        this.vectorSpace = new Vectorspace();
    }

    public List<LicenseResult> getDatabase() {
        return this.database;
    }

    /**
     * Scan through a list of files returning those that look like they might be
     * license files
     */
    public List<File> identifyPotentialLicenseFiles(List<File> fileList) {
        String[] licenses = this.LICENSE_FILES.toLowerCase().split(",");
        List<File> matches = new ArrayList<>();

        for (File file: fileList) {
            for (String license: licenses) {
                if (file.getName().toLowerCase().contains(license)) {
                    matches.add(file);
                }
            }
        }

        return matches;
    }

    /**
     * Given a string will scan through it using keywords to try and
     * identify which license it has
     */
    public Optional<LicenseResult> keywordGuessLicense(String content) {
        List<LicenseMatch> licenseMatches = Collections.synchronizedList(new ArrayList<>());
        String cleanContent = this.vectorSpace.cleanText(content);

        // Parallel stream is about 3x faster for this
        this.database.parallelStream().forEach(licenseResult -> {
            int keywordMatch = 0;

            for (String keyword: licenseResult.keywords) {
                if (cleanContent.contains(keyword)) {
                    keywordMatch++;
                }
            }

            if (keywordMatch >= 1) {
                float percentage = (float)keywordMatch / (float)licenseResult.keywords.size();
                licenseMatches.add(new LicenseMatch(licenseResult.licenseId, percentage));
            }
        });

        licenseMatches.sort((x, y) -> Float.compare(y.percentage, x.percentage));

        for (LicenseMatch licenseMatch: licenseMatches) {
            for (LicenseResult licenseResult: this.database) {
                if (licenseResult.licenseId.equals(licenseMatch.licenseId)) {
                    return Optional.of(licenseResult);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Looks for licenses using the SPDX License Identifier syntax
     */
    public List<LicenseResult> identifierGuessLicence(String content) {
        Matcher matcher = Pattern.compile("SPDX-License-Identifier:\\s+(.*?)[ |\\n|\\r\\n]").matcher(content);

        ArrayList<String> matches = new ArrayList<>();

        while (matcher.find()) {
            matches.add(matcher.group(1));
        }

        List<LicenseResult> result = new ArrayList<>();
        for (String match: matches) {
            for (LicenseResult licenseResult: this.database) {
                if (match.toLowerCase().equals(licenseResult.licenseId.toLowerCase())) {
                    result.add(licenseResult);
                }
            }
        }

        return result;
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

            Vectorspace vec = new Vectorspace();

            for (LicenseResult licenseResult: database) {
                licenseResult.concordance = vec.concordance(licenseResult.licenseText);
            }
        }
        catch (FileNotFoundException | JsonSyntaxException ex) {
            Singleton.getLogger().warning("Unable to load License Database from disk " + ex);
        }

        return database;
    }
}
