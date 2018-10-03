package com.searchcode.app.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.searchcode.app.config.Values;
import com.searchcode.app.dto.Source;
import com.searchcode.app.service.Singleton;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

public class RepositorySource {

    private final LoggerWrapper logger;
    private String DATABASEPATH = Properties.getProperties().getProperty(Values.SOURCE_DATABASE_LOCATION, Values.DEFAULT_SOURCE_DATABASE_LOCATION);
    private ArrayList<Source> database;

    public RepositorySource() {
        this.database = this.loadDatabase();
        this.logger = Singleton.getLogger();
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

    public String getLink(String name, HashMap<String, String> replacements) {
        String link = Values.EMPTYSTRING;

        Optional<Source> sourceByName = this.getSourceByName(name);

        if (!sourceByName.isPresent()) {
            return link;
        }

        link = sourceByName.map(Source::getLink).orElse(Values.EMPTYSTRING);

        for (String key: replacements.keySet()) {
            link = link.replace(String.format("{%s}", key), replacements.get(key));
        }

        return link;
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
            this.logger.severe(String.format("5f78543c::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        }

        return database;
    }
}
