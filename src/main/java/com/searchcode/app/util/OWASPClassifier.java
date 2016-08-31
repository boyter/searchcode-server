package com.searchcode.app.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.searchcode.app.config.Values;
import com.searchcode.app.model.OWASPResult;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OWASPClassifier {

    public String DATABASEPATH = Properties.getProperties().getProperty(Values.OWASPDATABASELOCATION, Values.DEFAULTOWASPDATABASELOCATION);
    public List<OWASPResult> database = new ArrayList<>();

    public OWASPClassifier() {
        this.database = this.loadDatabase();
    }

    public List<OWASPResult> classifyCode(List<String> codeLines) {
        return null;
    }

    private List<OWASPResult> loadDatabase() {
        List<OWASPResult> database = new ArrayList<>();

        try {
            Gson gson = new GsonBuilder().create();
            OWASPResult[] myArray = gson.fromJson(new FileReader(this.DATABASEPATH), OWASPResult[].class);
            database = Arrays.asList(myArray);
        }
        catch (FileNotFoundException | JsonSyntaxException ex) {
            System.out.println(ex);
        }

        return database;
    }
}
