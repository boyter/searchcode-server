package com.searchcode.app.dao;

import com.searchcode.app.config.IDatabaseConfig;
import com.searchcode.app.config.MySQLDatabaseConfig;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;

public class Repository {
    private final Helpers helpers;
    private final IDatabaseConfig dbConfig;

    public Repository() {
        this(new MySQLDatabaseConfig(), Singleton.getHelpers());
    }

    public Repository(IDatabaseConfig dbConfig, Helpers helpers) {
        this.dbConfig = dbConfig;
        this.helpers = helpers;
    }

    
}
