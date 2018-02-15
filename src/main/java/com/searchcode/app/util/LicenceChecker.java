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

public class LicenceChecker {
    private String DATABASEPATH = Properties.getProperties().getProperty(Values.LICENSE_DATABASE_LOCATION, Values.DEFAULT_LICENSE_DATABASE_LOCATION);
    private ArrayList<LicenseResult> database = new ArrayList<>();

    public LicenceChecker() {
        this.database = this.loadDatabase();
    }

    public List<LicenseResult> getDatabase() {
        return this.database;
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
