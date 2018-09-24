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
import com.searchcode.app.model.ApiResult;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Provides access to all methods required to get API details from the database.
 * Note that we use an in memory cache to avoid hitting the database too much. This was because when hit really hard
 * that there would be timeouts and other database connection issues with the dreaded "Too many connections".
 */
public class Api {
    private final Helpers helpers;
    private final IDatabaseConfig dbConfig;

    public Api(){
        this(Singleton.getDatabaseConfig(), Singleton.getHelpers());
    }

    public Api(IDatabaseConfig dbConfig, Helpers helpers) {
        this.dbConfig = dbConfig;
        this.helpers = helpers;
    }

    public synchronized List<ApiResult> getAllApi() {
        List<ApiResult> apiResults = new ArrayList<>();

        Connection connection;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.dbConfig.getConnection();
            preparedStatement = connection.prepareStatement("select rowid,publickey,privatekey,lastused,data from api;");
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int rowId = resultSet.getInt("rowid");
                String d_publicKey = resultSet.getString("publickey");
                String privateKey = resultSet.getString("privatekey");
                String lastUsed = resultSet.getString("lastused");
                String data = resultSet.getString("data");

                apiResults.add(new ApiResult(rowId, d_publicKey, privateKey, lastUsed, data));
            }
        }
        catch (SQLException ex) {
            Singleton.getLogger().severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            this.helpers.closeQuietly(resultSet);
            this.helpers.closeQuietly(preparedStatement);
        }

        return apiResults;
    }

    public synchronized Optional<ApiResult> getApiByPublicKey(String publicKey) {
        Optional<ApiResult> result = Optional.empty();

        Connection connection;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.dbConfig.getConnection();
            preparedStatement = connection.prepareStatement("select rowid,publickey,privatekey,lastused,data from api where publickey=?;");

            preparedStatement.setString(1, publicKey);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int rowId = resultSet.getInt("rowid");
                String d_publicKey = resultSet.getString("publickey");
                String privateKey = resultSet.getString("privatekey");
                String lastUsed = resultSet.getString("lastused");
                String data = resultSet.getString("data");

                result = Optional.of(new ApiResult(rowId, d_publicKey, privateKey, lastUsed, data));
            }
        }
        catch (SQLException ex) {
            Singleton.getLogger().severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            this.helpers.closeQuietly(resultSet);
            this.helpers.closeQuietly(preparedStatement);
        }

        return result;
    }

    public synchronized boolean saveApi(ApiResult apiResult) {
        boolean successful = false;

        Connection connection;
        PreparedStatement preparedStatement = null;

        try {
            connection = this.dbConfig.getConnection();
            preparedStatement = connection.prepareStatement("INSERT INTO \"api\" (\"publickey\",\"privatekey\",\"lastused\",\"data\") VALUES (?,?,?,?)");

            preparedStatement.setString(1, apiResult.getPublicKey());
            preparedStatement.setString(2, apiResult.getPrivateKey());
            preparedStatement.setString(3, apiResult.getLastUsed());
            preparedStatement.setString(4, apiResult.getData());

            preparedStatement.execute();

            successful = true;
        }
        catch (SQLException ex) {
            Singleton.getLogger().severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            this.helpers.closeQuietly(preparedStatement);
        }

        return successful;
    }

    public synchronized void deleteApiByPublicKey(String publicKey) {
        Connection connection;
        PreparedStatement preparedStatement = null;

        try {
            connection = this.dbConfig.getConnection();
            preparedStatement = connection.prepareStatement("delete from api where publickey=?;");

            preparedStatement.setString(1, publicKey);

            preparedStatement.execute();
        }
        catch (SQLException ex) {
            Singleton.getLogger().severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            this.helpers.closeQuietly(preparedStatement);
        }
    }

    // Avoid migrations by creating if its missing
    public synchronized void createTableIfMissing() {
        Connection connection;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.dbConfig.getConnection();
            preparedStatement = connection.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name='api';");

            resultSet = preparedStatement.executeQuery();
            String value = Values.EMPTYSTRING;
            while (resultSet.next()) {
                value = resultSet.getString("name");
            }

            if (Singleton.getHelpers().isNullEmptyOrWhitespace(value)) {
                preparedStatement = connection.prepareStatement("CREATE  TABLE \"main\".\"api\" (\"publickey\" VARCHAR PRIMARY KEY  NOT NULL , \"privatekey\" VARCHAR NOT NULL , \"lastused\" VARCHAR, \"data\" VARCHAR);");
                preparedStatement.execute();
            }
        }
        catch (SQLException ex) {
            Singleton.getLogger().severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            this.helpers.closeQuietly(resultSet);
            this.helpers.closeQuietly(preparedStatement);
        }
    }
}
