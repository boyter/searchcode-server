package com.searchcode.app.util;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.searchcode.app.config.Values;
import com.searchcode.app.dto.FileClassifierResult;
import com.searchcode.app.service.Singleton;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class FileClassifier {

    private final Helpers helpers;
    private String DATABASEPATH = Properties.getProperties().getProperty(Values.CLASSIFIER_DATABASE_LOCATION, Values.DEFAULT_CLASSIFIER_DATABASE_LOCATION);
    public boolean DEEP_GUESS = Boolean.parseBoolean(Properties.getProperties().getProperty(Values.DEEP_GUESS_FILES, Values.DEFAULT_DEEP_GUESS_FILES));
    private List<FileClassifierResult> database = new ArrayList<>();

    public FileClassifier() {
        this.database = this.loadDatabase();
        this.helpers = Singleton.getHelpers();
    }

    public FileClassifier(List<FileClassifierResult> database) {
        this.database = database;
        this.helpers = Singleton.getHelpers();
    }

    public List<FileClassifierResult> getDatabase() {
        return this.database;
    }

    public void setDatabase(List<FileClassifierResult> database) {
        this.database = database;
    }

    /**
     * Given a filename and the lines inside the file attempts to guess the type of the file.
     * TODO When no match attempt to identify using the file keywords
     */
    public String languageGuesser(String fileName, List<String> codeLines) {
        String lowerCaseFileName = fileName.toLowerCase();

        if (lowerCaseFileName.endsWith(".txt")) {
            return "Text";
        }

        // Find all languages that might be this one
        List<FileClassifierResult> fileClassifierResults = new ArrayList<>();
        List<String> matchingExtensions = new ArrayList<>();

        for (FileClassifierResult fileClassifierResult: this.database) {
            boolean shouldAdd = false;
            for (String extension: fileClassifierResult.extensions) {
                if (lowerCaseFileName.endsWith("." + extension) || lowerCaseFileName.equals(extension)) {
                    matchingExtensions.add(extension);
                    shouldAdd = true;
                }
            }

            if (shouldAdd) {
                fileClassifierResults.add(fileClassifierResult);
            }
        }


        if (fileClassifierResults.size() == 0) {
            // Check against all using the pattern and see if we can guess
            if (this.DEEP_GUESS) {
                return this.deepGuess(fileName, codeLines);
            }

            return "Unknown";
        }

        if (fileClassifierResults.size() == 1) {
            return fileClassifierResults.get(0).language;
        }

        // More than one possible match check if one is a better fit
        if (this.helpers.allUnique(matchingExtensions)) {
            Optional<String> longest = matchingExtensions.stream()
                                        .sorted(String::compareTo)
                                        .findFirst();

            if (longest.isPresent()) {
                for (FileClassifierResult fileClassifierResult : fileClassifierResults) {
                    if (ArrayUtils.contains(fileClassifierResult.extensions, longest.get())) {
                        return fileClassifierResult.language;
                    }
                }
            }
        }

        // More then one possible match, check which one is most likely is and return that
        String languageGuess = this.guessLanguage(codeLines, fileClassifierResults.toArray());

        // If there is still no decision then go for the first match
        if (this.helpers.isNullEmptyOrWhitespace(languageGuess)) {
            return fileClassifierResults.get(0).language;
        }

        return languageGuess;
    }

    public String deepGuess(String fileName, List<String> codeLines) {
        Object[] matching = this.database.stream().filter(x -> x.keywords.length != 0).toArray();
        return this.guessLanguage(codeLines, matching);
    }

    private String guessLanguage(List<String> codeLines, Object[] matching) {
        // More then one possible match, check which one is most likely is and return that
        String languageGuess = "Unknown";
        int bestKeywords = 0;

        for (Object c: matching) {
            FileClassifierResult fileClassifierResult = (FileClassifierResult)c;
            int matchingKeywords = 0;
            for (String line: codeLines) {
                line = line.toLowerCase().replaceAll("[^A-Za-z]", " ");
                for (String keyword: fileClassifierResult.keywords) {
                    matchingKeywords += StringUtils.countMatches(line, keyword);
                }
            }

            if (matchingKeywords > bestKeywords) {
                bestKeywords = matchingKeywords;
                languageGuess = fileClassifierResult.language;
            }
        }

        return languageGuess;
    }


    /**
     * Loads the File Classifier database from a JSON file on disk
     */
    private ArrayList<FileClassifierResult> loadDatabase() {
        ArrayList<FileClassifierResult> database = new ArrayList<>();

        try {
            Gson gson = new GsonBuilder().create();
            FileClassifierResult[] myArray = gson.fromJson(new FileReader(this.DATABASEPATH), FileClassifierResult[].class);
            database = new ArrayList<>(Arrays.asList(myArray));
        }
        catch (FileNotFoundException | JsonSyntaxException ex) {
            Singleton.getLogger().warning("Unable to load '" + DATABASEPATH+ "' file. File classification will not work. " + ex.toString());
        }

        return database;
    }
}
