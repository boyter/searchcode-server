/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.dao;

import com.searchcode.app.config.IDatabaseConfig;
import com.searchcode.app.config.Values;
import com.searchcode.app.dto.ConnStmtRs;
import com.searchcode.app.dto.DataData;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides access to all methods required to get Data details from the database.
 * Designed to work with both MySQL and SQLite with tests to ensure this is the case.
 */
public class Data {

    private final Helpers helpers;
    private final IDatabaseConfig dbConfig;
    private final LoggerWrapper logger;

    public Data() {
        this(Singleton.getDatabaseConfig(), Singleton.getHelpers(), Singleton.getLogger());
    }

    public Data(IDatabaseConfig dbConfig, Helpers helpers, LoggerWrapper logger) {
        this.dbConfig = dbConfig;
        this.helpers = helpers;
        this.logger = logger;

        if (Singleton.getHelpers().isLocalInstance()) {
            this.createTableIfMissing();
        }
    }

    public synchronized List<DataData> getAllData() {
        var values = new ArrayList<DataData>();
        var connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("select `key`, `value` from `data`;");
            connStmtRs.rs = connStmtRs.stmt.executeQuery();

            while (connStmtRs.rs.next()) {
                values.add(new DataData(connStmtRs.rs.getString("key"), connStmtRs.rs.getString("value")));
            }
        } catch (SQLException ex) {
            this.logger.severe(String.format("e897086c::error in class %s exception %s searchcode was unable get all data, this is likely to break all sorts of things, most likely the table has changed or is missing", ex.getClass(), ex.getMessage()));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return values;
    }

    public synchronized String getDataByName(String key, String defaultValue) {
        var value = this.getDataByName(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public synchronized String getDataByName(String key) {
        String value = null;
        var connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("select `key`, `value` from `data` where `key` = ?;");
            connStmtRs.stmt.setString(1, key);

            connStmtRs.rs = connStmtRs.stmt.executeQuery();

            while (connStmtRs.rs.next()) {
                value = connStmtRs.rs.getString("value");
            }
        } catch (SQLException ex) {
            this.logger.severe(String.format("52f85254::error in class %s exception %s searchcode was unable get data by name %s, this is likely to break all sorts of things, most likely the table has changed or is missing", ex.getClass(), ex.getMessage(), key));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return value;
    }

    public synchronized boolean saveData(String key, String value) {
        var existing = this.getDataByName(key);
        var isNew = false;
        var connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();

            if (existing != null) {
                connStmtRs.stmt = connStmtRs.conn.prepareStatement("UPDATE `data` SET `key` = ?, `value` = ? WHERE `key` = ?");
                connStmtRs.stmt.setString(1, key);
                connStmtRs.stmt.setString(2, value);
                connStmtRs.stmt.setString(3, key);
            } else {
                isNew = true;
                connStmtRs.stmt = connStmtRs.conn.prepareStatement("INSERT INTO data(`key`, `value`) VALUES (?,?)");
                connStmtRs.stmt.setString(1, key);
                connStmtRs.stmt.setString(2, value);
            }

            connStmtRs.stmt.execute();
        } catch (SQLException ex) {
            this.logger.severe(String.format("e241d7cd::error in class %s exception %s searchcode was unable save data name %s, this is likely to break all sorts of things, most likely the table has changed or is missing", ex.getClass(), ex.getMessage(), key));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return isNew;
    }

    /**
     * This method is specific to SQLite and will fail if run against MySQL
     * TODO implement this for MySQL
     */
    public synchronized void createTableIfMissing() {
        var connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name='data';");

            connStmtRs.rs = connStmtRs.stmt.executeQuery();
            var value = Values.EMPTYSTRING;
            while (connStmtRs.rs.next()) {
                value = connStmtRs.rs.getString("name");
            }

            if (this.helpers.isNullEmptyOrWhitespace(value)) {
                connStmtRs.stmt = connStmtRs.conn.prepareStatement("CREATE TABLE \"data\" (\"key\" VARCHAR PRIMARY KEY  NOT NULL , \"value\" VARCHAR)");
                connStmtRs.stmt.execute();
            }
        } catch (SQLException ex) {
            this.logger.severe(String.format("3deb6433::error in class %s exception %s searchcode was unable create the data table, this is likely to break all sorts of things", ex.getClass(), ex.getMessage()));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }
    }
}
