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
import com.searchcode.app.model.ApiResult;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;

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
    private final LoggerWrapper logger;

    public Api() {
        this(Singleton.getDatabaseConfig(), Singleton.getHelpers(), Singleton.getLogger());
    }

    public Api(IDatabaseConfig dbConfig, Helpers helpers, LoggerWrapper logger) {
        this.dbConfig = dbConfig;
        this.helpers = helpers;
        this.logger = logger;
        this.createTableIfMissing();
    }

    public synchronized List<ApiResult> getAllApi() {
        var apiResults = new ArrayList<ApiResult>();
        var connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("select rowid,publickey,privatekey,lastused,data from api;");
            connStmtRs.rs = connStmtRs.stmt.executeQuery();

            while (connStmtRs.rs.next()) {
                var rowId = connStmtRs.rs.getInt("rowid");
                var d_publicKey = connStmtRs.rs.getString("publickey");
                var privateKey = connStmtRs.rs.getString("privatekey");
                var lastUsed = connStmtRs.rs.getString("lastused");
                var data = connStmtRs.rs.getString("data");

                apiResults.add(new ApiResult(rowId, d_publicKey, privateKey, lastUsed, data));
            }
        } catch (SQLException ex) {
            this.logger.severe(String.format("c58e8a00::error in class %s exception %s searchcode was unable to pull the api keys from the database api calls will fail, most likely the table has changed or is missing", ex.getClass(), ex.getMessage()));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return apiResults;
    }

    public synchronized Optional<ApiResult> getApiByPublicKey(String publicKey) {
        Optional<ApiResult> result = Optional.empty();
        var connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("select rowid,publickey,privatekey,lastused,data from api where publickey=?;");

            connStmtRs.stmt.setString(1, publicKey);

            connStmtRs.rs = connStmtRs.stmt.executeQuery();

            while (connStmtRs.rs.next()) {
                var rowId = connStmtRs.rs.getInt("rowid");
                var d_publicKey = connStmtRs.rs.getString("publickey");
                var privateKey = connStmtRs.rs.getString("privatekey");
                var lastUsed = connStmtRs.rs.getString("lastused");
                var data = connStmtRs.rs.getString("data");

                result = Optional.of(new ApiResult(rowId, d_publicKey, privateKey, lastUsed, data));
            }
        } catch (SQLException ex) {
            this.logger.severe(String.format("c81a4390::error in class %s exception %s searchcode was unable to pull the api keys from the database api calls will fail, most likely the table has changed or is missing", ex.getClass(), ex.getMessage()));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return result;
    }

    public synchronized boolean saveApi(ApiResult apiResult) {
        var successful = false;
        var connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("INSERT INTO \"api\" (\"publickey\",\"privatekey\",\"lastused\",\"data\") VALUES (?,?,?,?)");

            connStmtRs.stmt.setString(1, apiResult.getPublicKey());
            connStmtRs.stmt.setString(2, apiResult.getPrivateKey());
            connStmtRs.stmt.setString(3, apiResult.getLastUsed());
            connStmtRs.stmt.setString(4, apiResult.getData());

            connStmtRs.stmt.execute();

            successful = true;
        } catch (SQLException ex) {
            this.logger.severe(String.format("d06c2e67::error in class %s exception %s searchcode was unable to save a new api key to the database, most likely the table has changed or is missing", ex.getClass(), ex.getMessage()));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return successful;
    }

    public synchronized void deleteApiByPublicKey(String publicKey) {
        var connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("delete from api where publickey=?;");
            connStmtRs.stmt.setString(1, publicKey);
            connStmtRs.stmt.execute();
        } catch (SQLException ex) {
            this.logger.severe(String.format("eab0bb55::error in class %s exception %s searchcode was unable to delete an api key by its public key %s", ex.getClass(), ex.getMessage(), publicKey));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }
    }

    public synchronized void createTableIfMissing() {
        var connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name='api';");

            connStmtRs.rs = connStmtRs.stmt.executeQuery();
            var value = Values.EMPTYSTRING;
            while (connStmtRs.rs.next()) {
                value = connStmtRs.rs.getString("name");
            }

            if (Singleton.getHelpers().isNullEmptyOrWhitespace(value)) {
                connStmtRs.stmt = connStmtRs.conn.prepareStatement("CREATE  TABLE \"main\".\"api\" (\"publickey\" VARCHAR PRIMARY KEY  NOT NULL , \"privatekey\" VARCHAR NOT NULL , \"lastused\" VARCHAR, \"data\" VARCHAR);");
                connStmtRs.stmt.execute();
            }
        } catch (SQLException ex) {
            this.logger.severe(String.format("5e666e82::error in class %s exception %s searchcode was to create the api key table, so api calls will fail", ex.getClass(), ex.getMessage()));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }
    }
}
