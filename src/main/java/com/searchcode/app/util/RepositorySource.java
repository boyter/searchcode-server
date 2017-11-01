package com.searchcode.app.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.searchcode.app.config.Values;
import com.searchcode.app.dto.Source;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class RepositorySource {

    private String DATABASEPATH = Properties.getProperties().getProperty(Values.SOURCE_DATABASE_LOCATION, Values.DEFAULT_SOURCE_DATABASE_LOCATION);
    private ArrayList<Source> database = new ArrayList<>();

    public RepositorySource() {
        this.database = this.loadDatabase();
    }

    public ArrayList<Source> getDatabase() {
        return database;
    }

    public Optional<Source> getSourceByName(String name) {

        for (Source source: this.database) {
            if (source.getName().equals(name)) {
                return Optional.of(source);
            }
        }

        return Optional.empty();
    }

    /**
     * Loads the OWASP database from a JSON file on disk
     */
    public ArrayList<Source> loadDatabase() {
        ArrayList<Source> database = new ArrayList<>();

        try {
            Gson gson = new GsonBuilder().create();
            Source[] myArray = gson.fromJson(new FileReader(this.DATABASEPATH), Source[].class);
            database = new ArrayList<>(Arrays.asList(myArray));
        }
        catch (FileNotFoundException | JsonSyntaxException ex) {
            System.out.println(ex);
        }

        return database;
    }
}
