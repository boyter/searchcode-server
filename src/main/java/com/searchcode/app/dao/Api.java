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
import com.searchcode.app.model.ApiResult;
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
 * Provides access to all methods required to get API details from the database.
 * Note that we use an in memory cache to avoid hitting the database too much. This was because when hit really hard
 * that there would be timeouts and other database connection issues with the dreaded "Too many connections".
 */
public class Api implements IApi {
    private static final LoggerWrapper LOGGER = Singleton.getLogger();

    private IDatabaseConfig dbConfig;

    private AbstractMap<String, ApiResult> cache = Singleton.getApiCache();
    private AbstractMap<String, Object> genericCache = Singleton.getGenericCache();
    private String apiAllApiCacheKey = "api-all-api-cache";

    public Api(IDatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public synchronized List<ApiResult> getAllApi() {
        List<ApiResult> apiResults = (ArrayList<ApiResult>)this.genericCache.get(this.apiAllApiCacheKey);

        if (apiResults != null) {
            return apiResults;
        }

        apiResults = new ArrayList<>();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = this.dbConfig.getConnection();
            stmt = conn.prepareStatement("select rowid,publickey,privatekey,lastused,data from api;");
            rs = stmt.executeQuery();

            while (rs.next()) {
                int rowId = rs.getInt("rowid");
                String d_publicKey = rs.getString("publickey");
                String privateKey = rs.getString("privatekey");
                String lastUsed = rs.getString("lastused");
                String data = rs.getString("data");

                apiResults.add(new ApiResult(rowId, d_publicKey, privateKey, lastUsed, data));
            }
        }
        catch(SQLException ex) {
            LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            Helpers.closeQuietly(rs);
            Helpers.closeQuietly(stmt);
            Helpers.closeQuietly(conn);
        }

        this.genericCache.put(this.apiAllApiCacheKey, apiResults);
        return apiResults;
    }

    public synchronized ApiResult getApiByPublicKey(String publicKey) {
        ApiResult result = this.cache.get(publicKey);
        if (result != null) {
            return result;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = this.dbConfig.getConnection();
            stmt = conn.prepareStatement("select rowid,publickey,privatekey,lastused,data from api where publickey=?;");

            stmt.setString(1, publicKey);

            rs = stmt.executeQuery();

            while (rs.next()) {
                int rowId = rs.getInt("rowid");
                String d_publicKey = rs.getString("publickey");
                String privateKey = rs.getString("privatekey");
                String lastUsed = rs.getString("lastused");
                String data = rs.getString("data");

                result = new ApiResult(rowId, d_publicKey, privateKey, lastUsed, data);
            }
        }
        catch(SQLException ex) {
            LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            Helpers.closeQuietly(rs);
            Helpers.closeQuietly(stmt);
            Helpers.closeQuietly(conn);
        }

        if (result != null) {
            this.cache.put(publicKey, result);
        }

        return result;
    }

    public synchronized boolean saveApi(ApiResult apiResult) {
        boolean successful = false;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = this.dbConfig.getConnection();
            stmt = conn.prepareStatement("INSERT INTO \"api\" (\"publickey\",\"privatekey\",\"lastused\",\"data\") VALUES (?,?,?,?)");

            stmt.setString(1, apiResult.getPublicKey());
            stmt.setString(2, apiResult.getPrivateKey());
            stmt.setString(3, apiResult.getLastUsed());
            stmt.setString(4, apiResult.getData());

            stmt.execute();

            successful = true;

            this.cache.remove(apiResult.getPublicKey());
            this.genericCache.remove(apiAllApiCacheKey);
        }
        catch(SQLException ex) {
            successful = false;
            LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            Helpers.closeQuietly(rs);
            Helpers.closeQuietly(stmt);
            Helpers.closeQuietly(conn);
        }


        return successful;
    }

    public synchronized void deleteApiByPublicKey(String publicKey) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = this.dbConfig.getConnection();
            stmt = conn.prepareStatement("delete from api where publickey=?;");

            stmt.setString(1, publicKey);

            stmt.execute();

            this.cache.remove(publicKey);
            this.genericCache.remove(apiAllApiCacheKey);

        }
        catch(SQLException ex) {
            LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            Helpers.closeQuietly(rs);
            Helpers.closeQuietly(stmt);
            Helpers.closeQuietly(conn);
        }
    }

    // Avoid migrations by creating if its missing
    public synchronized void createTableIfMissing() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = this.dbConfig.getConnection();
            stmt = conn.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name='api';");

            rs = stmt.executeQuery();
            String value = "";
            while (rs.next()) {
                value = rs.getString("name");
            }

            if (value.equals("")) {
                stmt = conn.prepareStatement("CREATE  TABLE \"main\".\"api\" (\"publickey\" VARCHAR PRIMARY KEY  NOT NULL , \"privatekey\" VARCHAR NOT NULL , \"lastused\" VARCHAR, \"data\" VARCHAR);");
                stmt.execute();
            }
        }
        catch(SQLException ex) {
            LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            Helpers.closeQuietly(rs);
            Helpers.closeQuietly(stmt);
            Helpers.closeQuietly(conn);
        }
    }
}
