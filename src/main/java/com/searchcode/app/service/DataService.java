package com.searchcode.app.service;


import com.google.gson.Gson;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;

import java.util.ArrayList;
import java.util.List;

public class DataService {
    private final Data data;

    public DataService() {
        this.data = Singleton.getData();
    }

    public DataService(Data data) {
        this.data = data;
    }

    public void addToPersistentDelete(String value) {
        List<String> persistentDelete = this.getPersistentDelete();
        persistentDelete.add(value);
        Gson gson = new Gson();
        this.data.saveData(Values.PERSISTENT_DELETE_QUEUE, gson.toJson(persistentDelete));
    }

    public void removeFromPersistentDelete(String value) {
        List<String> persistentDelete = this.getPersistentDelete();
        persistentDelete.remove(value);
        Gson gson = new Gson();
        this.data.saveData(Values.PERSISTENT_DELETE_QUEUE, gson.toJson(persistentDelete));
    }

    public List<String> getPersistentDelete() {
        String dataByName = this.data.getDataByName(Values.PERSISTENT_DELETE_QUEUE, "[]");
        Gson gson = new Gson();
        ArrayList<String> arrayList = gson.fromJson(dataByName, ArrayList.class);
        return arrayList;
    }
}
