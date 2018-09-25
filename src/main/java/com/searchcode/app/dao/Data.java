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
import com.searchcode.app.dto.DataData;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides access to all methods required to get Data details from the database.
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
        this.createTableIfMissing();
    }

    public synchronized List<DataData> getAllData() {
        List<DataData> values = new ArrayList<>();

        Connection connection;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.dbConfig.getConnection();
            preparedStatement = connection.prepareStatement("select key,value from \"data\";");

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                values.add(new DataData(resultSet.getString("key"), resultSet.getString("value")));
            }
        }
        catch (SQLException ex) {
            this.logger.severe(String.format("e897086c::error in class %s exception %s searchcode was unable get all data, this is likely to break all sorts of things, most likely the table has changed or is missing", ex.getClass(), ex.getMessage()));
        }
        finally {
            this.helpers.closeQuietly(resultSet);
            this.helpers.closeQuietly(preparedStatement);
        }

        return values;
    }

    public synchronized String getDataByName(String key, String defaultValue) {
        String value = this.getDataByName(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public synchronized String getDataByName(String key) {
        String value = null;

        Connection connection;
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
        catch (SQLException ex) {
            this.logger.severe(String.format("52f85254::error in class %s exception %s searchcode was unable get data by name %s, this is likely to break all sorts of things, most likely the table has changed or is missing", ex.getClass(), ex.getMessage(), key));
        }
        finally {
            this.helpers.closeQuietly(resultSet);
            this.helpers.closeQuietly(preparedStatement);
        }

        return value;
    }

    public synchronized boolean saveData(String key, String value) {
        String existing = this.getDataByName(key);
        boolean isNew = false;

        Connection connection;
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
        catch (SQLException ex) {
            this.logger.severe(String.format("e241d7cd::error in class %s exception %s searchcode was unable save data name %s, this is likely to break all sorts of things, most likely the table has changed or is missing", ex.getClass(), ex.getMessage(), key));
        }
        finally {
            this.helpers.closeQuietly(preparedStatement);
        }

        return isNew;
    }

    public synchronized void createTableIfMissing() {
        Connection connection;
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

            if (Singleton.getHelpers().isNullEmptyOrWhitespace(value)) {
                preparedStatement = connection.prepareStatement("CREATE TABLE \"data\" (\"key\" VARCHAR PRIMARY KEY  NOT NULL , \"value\" VARCHAR)");
                preparedStatement.execute();
            }
        }
        catch (SQLException ex) {
            Singleton.getLogger().severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
            this.logger.severe(String.format("3deb6433::error in class %s exception %s searchcode was unable create the data table, this is likely to break all sorts of things", ex.getClass(), ex.getMessage()));
        }
        finally {
            this.helpers.closeQuietly(resultSet);
            this.helpers.closeQuietly(preparedStatement);
        }
    }
}
