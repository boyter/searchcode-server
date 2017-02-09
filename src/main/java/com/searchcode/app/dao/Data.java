/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.8
 */

package com.searchcode.app.dao;

import com.searchcode.app.config.IDatabaseConfig;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;

/**
 * Provides access to all methods required to get Data details from the database.
 * Note that we use an in memory cache to avoid hitting the database too much. This was because when hit really hard
 * that there would be timeouts and other database connection issues with the dreaded "Too many connections".
 */
public class Data implements IData {
    private IDatabaseConfig dbConfig;
    private AbstractMap<String, String> cache = Singleton.getDataCache();

    public Data(IDatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public synchronized String getDataByName(String key, String defaultValue) {
        String value = this.getDataByName(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public synchronized String getDataByName(String key) {
        String value = this.cache.get(key);
        if (value != null) {
            return value;
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.dbConfig.getConnection();
            preparedStatement = connection.prepareStatement("select key,value from \"data\" where key = ?;");
            preparedStatement.setString(1, key);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                value = resultSet.getString("value");
            }
        }
        catch(SQLException ex) {
            Singleton.getLogger().severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage() + " while trying to get " + key);
        }
        finally {
            Helpers.closeQuietly(resultSet);
            Helpers.closeQuietly(preparedStatement);
            Helpers.closeQuietly(connection);
        }

        if (value != null) {
            this.cache.put(key, value);
        }

        return value;
    }

    public synchronized boolean saveData(String key, String value) {
        String existing = this.getDataByName(key);
        boolean isNew = false;

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = this.dbConfig.getConnection();

            if (existing != null) {
                preparedStatement = connection.prepareStatement("UPDATE \"data\" SET \"key\" = ?, \"value\" = ? WHERE  \"key\" = ?");
                preparedStatement.setString(1, key);
                preparedStatement.setString(2, value);
                preparedStatement.setString(3, key);
            }
            else {
                isNew = true;
                preparedStatement = connection.prepareStatement("INSERT INTO data(\"key\",\"value\") VALUES (?,?)");
                preparedStatement.setString(1, key);
                preparedStatement.setString(2, value);
            }

            preparedStatement.execute();
        }
        catch(SQLException ex) {
            Singleton.getLogger().severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            Helpers.closeQuietly(preparedStatement);
            Helpers.closeQuietly(connection);
        }

        // Update cache with new value
        if (value != null) {
            this.cache.put(key, value);
        }
        else {
            this.cache.remove(key);
        }

        return isNew;
    }

    // Avoid migrations by creating if its missing
    public synchronized void createTableIfMissing() {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.dbConfig.getConnection();
            preparedStatement = connection.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name='data';");

            resultSet = preparedStatement.executeQuery();
            String value = "";
            while (resultSet.next()) {
                value = resultSet.getString("name");
            }

            if (value.equals("")) {
                preparedStatement = connection.prepareStatement("CREATE TABLE \"data\" (\"key\" VARCHAR PRIMARY KEY  NOT NULL , \"value\" VARCHAR);");
                preparedStatement.execute();
            }
        }
        catch(SQLException ex) {
            Singleton.getLogger().severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            Helpers.closeQuietly(resultSet);
            Helpers.closeQuietly(preparedStatement);
            Helpers.closeQuietly(connection);
        }
    }
}
