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
import java.util.HashMap;

/**
 * This class uses the database from the scc project in order to classify files under a specific language.
 * It used to work by allowing for duplicate entries, but now assumes that there is only 1 extension for each
 * file type.
 */
public class FileClassifier {

    private String DATABASEPATH = Properties.getProperties().getProperty(Values.CLASSIFIER_DATABASE_LOCATION, Values.DEFAULT_CLASSIFIER_DATABASE_LOCATION);
    private HashMap<String, FileClassifierResult> database;

    public FileClassifier() {
        this.database = this.loadDatabase();
    }

    public FileClassifier(HashMap<String, FileClassifierResult> database) {
        this.database = database;
    }

    public HashMap<String, FileClassifierResult> getDatabase() {
        return this.database;
    }

    public void setDatabase(HashMap<String, FileClassifierResult> database) {
        this.database = database;
    }

    public String getExtension(String fileName) {
        int loc = fileName.lastIndexOf(".");

        if (loc == 0 || loc == -1) {
            return fileName;
        }

        return fileName.substring(loc + 1);
    }

    /**
     * Given a filename guesses the file type
     */
    public String languageGuesser(String fileName) {

        fileName = fileName.toLowerCase();
        String extension = this.getExtension(fileName);

        for (String key: database.keySet()) {
            FileClassifierResult fileClassifierResult = database.get(key);

            for (String ext: fileClassifierResult.extensions) {
                if (extension.equals(ext)) {
                    return key;
                }
            }
        }

        return Values.UNKNOWN_LANGUAGE;
    }

    /**
     * Loads the File Classifier database from a JSON file on disk
     */
    private HashMap<String, FileClassifierResult> loadDatabase() {
        HashMap<String, FileClassifierResult> database = new HashMap<>();

        try {
            Gson gson = new GsonBuilder().create();
            Type type = new TypeToken<HashMap<String, FileClassifierResult>>(){}.getType();
            database = gson.fromJson(new FileReader(this.DATABASEPATH),  type);
        }
        catch (FileNotFoundException | JsonSyntaxException ex) {
            Singleton.getLogger().warning("Unable to load '" + DATABASEPATH+ "' file. File classification will not work. " + ex.toString());
        }

        return database;
    }
}
