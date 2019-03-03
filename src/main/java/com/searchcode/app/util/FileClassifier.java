package com.searchcode.app.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.searchcode.app.config.Values;
import com.searchcode.app.dto.FileClassifierResult;
import com.searchcode.app.service.Singleton;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class uses the database from the scc project in order to classify files under a specific language.
 * It used to work by allowing for duplicate entries, but now assumes that there is only 1 extension for each
 * file type.
 */
public class FileClassifier {

    private final LoggerWrapper logger;
    private final Helpers helpers;
    private String DATABASE_PATH = Properties.getProperties().getProperty(Values.CLASSIFIER_DATABASE_LOCATION, Values.DEFAULT_CLASSIFIER_DATABASE_LOCATION);
    private HashMap<String, FileClassifierResult> database;

    public FileClassifier() {
        this.database = this.loadDatabase();
        this.logger = Singleton.getLogger();
        this.helpers = Singleton.getHelpers();
    }

    public FileClassifier(HashMap<String, FileClassifierResult> database) {
        this.database = database;
        this.logger = Singleton.getLogger();
        this.helpers = Singleton.getHelpers();
    }

    public HashMap<String, FileClassifierResult> getDatabase() {
        return this.database;
    }

    public void setDatabase(HashMap<String, FileClassifierResult> database) {
        this.database = database;
    }

    public String getExtension(String fileName) {
        if (fileName.lastIndexOf('.') == 0) {
            return fileName;
        }

        String[] split = fileName.split("\\.");

        switch (split.length) {
            case 0:
            case 1:
                return fileName;
            case 2:
                return split[split.length - 1];
        }

        return split[split.length - 2] + "." + split[split.length - 1];
    }

    /**
     * Given a filename guesses the file type
     */
    public String languageGuesser(String fileName, String content) {
        fileName = fileName.toLowerCase();
        var matches = new ArrayList<String>();
        var extension = Values.EMPTYSTRING;

        // Try finding using the whole name EG LICENSE
        matches = this.checkIfExtentionExists(fileName);

        // Try matching based on one level EG d.ts OR ts
        if (matches.isEmpty()) {
            extension = this.getExtension(fileName);
            matches = this.checkIfExtentionExists(extension);
        }

        // Catch all if the above did not work, IE turn d.ts into ts
        if (matches.isEmpty()) {
            extension = this.getExtension(extension);
            matches = this.checkIfExtentionExists(extension);
        }

        // If no idea at this point return that we don't know
        if (matches.isEmpty()) {
            return Values.UNKNOWN_LANGUAGE;
        }

        // If we have a single match then return it
        if (matches.size() == 1) {
            return matches.get(0);
        }

        // We have multiple matches, so try to work out which one is the most likely result
        var toSort = new HashMap<String, Integer>();

        for (var m : matches) {
            toSort.put(m, 0);
            for (var keyword: this.database.get(m).keywords) {
                if (content.contains(keyword)) {
                    toSort.put(m, toSort.get(m) + 1);
                }
            }
        }

        return this.helpers.sortByValue(toSort).keySet().stream().findFirst().orElse(Values.UNKNOWN_LANGUAGE);
    }

    private ArrayList<String> checkIfExtentionExists(String extension) {
        var matches = new ArrayList<String>();

        for (String key : database.keySet()) {
            var fileClassifierResult = database.get(key);

            for (var ext : fileClassifierResult.extensions) {
                if (extension.equals(ext)) {
                    matches.add(key);
                }
            }
        }

        return matches;
    }

    /**
     * Loads the File Classifier database from a JSON file on disk
     */
    private HashMap<String, FileClassifierResult> loadDatabase() {
        HashMap<String, FileClassifierResult> database = new HashMap<>();

        try {
            Gson gson = new GsonBuilder().create();
            Type type = new TypeToken<HashMap<String, FileClassifierResult>>() {
            }.getType();
            database = gson.fromJson(new FileReader(this.DATABASE_PATH), type);
        } catch (FileNotFoundException | JsonSyntaxException ex) {
            this.logger.severe(String.format("62bfa6c9::error in class %s exception %s unable to load file classifier, languages will not be recognised", ex.getClass(), ex.getMessage()));
        }

        return database;
    }
}
