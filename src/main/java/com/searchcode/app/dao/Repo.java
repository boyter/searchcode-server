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
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Provides access to all methods required to get Repo details from the database.
 * Note that we use an in memory cache to avoid hitting the database too much. This was because when hit really hard
 * that there would be timeouts and other database connection issues with the dreaded "Too many connections".
 */
public class Repo {

    private final Helpers helpers;
    private final IDatabaseConfig dbConfig;
    private final LoggerWrapper logger;

    public Repo() {
        this(Singleton.getDatabaseConfig(), Singleton.getHelpers(), Singleton.getLogger());
    }


    public Repo(IDatabaseConfig dbConfig, Helpers helpers, LoggerWrapper loggerWrapper) {
        this.dbConfig = dbConfig;
        this.helpers = helpers;
        this.logger = loggerWrapper;
        this.createTableIfMissing();
    }

    public synchronized List<RepoResult> getAllRepo() {
        List<RepoResult> repoResults = new ArrayList<>();
        ConnStmtRs connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("select rowid,name,scm,url,username,password,source,branch,data from repo order by rowid desc;");

            connStmtRs.rs = connStmtRs.stmt.executeQuery();

            while (connStmtRs.rs.next()) {
                int rowId = connStmtRs.rs.getInt("rowid");
                String repoName = connStmtRs.rs.getString("name");
                String repoScm = connStmtRs.rs.getString("scm");
                String repoUrl = connStmtRs.rs.getString("url");
                String repoUsername = connStmtRs.rs.getString("username");
                String repoPassword = connStmtRs.rs.getString("password");
                String repoSource = connStmtRs.rs.getString("source");
                String repoBranch = connStmtRs.rs.getString("branch");
                String repoData = connStmtRs.rs.getString("data");

                repoResults.add(new RepoResult()
                        .setRowId(rowId)
                        .setName(repoName)
                        .setScm(repoScm)
                        .setUrl(repoUrl)
                        .setUsername(repoUsername)
                        .setPassword(repoPassword)
                        .setSource(repoSource)
                        .setBranch(repoBranch)
                        .setData(repoData));
            }
        } catch (SQLException ex) {
            this.logger.severe(String.format("820c9557::error in class %s exception %s searchcode was unable to get the list of all repositories, this is likely nothing will be indexed, most likely the table has changed or is missing", ex.getClass(), ex.getMessage()));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return repoResults;
    }

    public synchronized List<RepoResult> searchRepo(String searchTerms) {
        List<RepoResult> repoResults = this.getAllRepo();
        List<RepoResult> matchRepoResults = new ArrayList<>();

        String[] split = searchTerms.toLowerCase().split(" ");

        for (RepoResult rr : repoResults) {
            boolean isMatch = false;

            for (String term : split) {
                isMatch = rr.toString().toLowerCase().contains(term);
            }

            if (isMatch) {
                matchRepoResults.add(rr);
            }
        }

        if (matchRepoResults.size() > 100) {
            matchRepoResults = matchRepoResults.subList(0, 100);
        }

        return matchRepoResults;
    }

    public synchronized List<RepoResult> getPagedRepo(int offset, int pageSize) {
        List<RepoResult> repoResults = new ArrayList<>();
        ConnStmtRs connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("select rowid,name,scm,url,username,password,source,branch,data from repo order by rowid desc limit ?, ?;");

            connStmtRs.stmt.setInt(1, offset);
            connStmtRs.stmt.setInt(2, pageSize);

            connStmtRs.rs = connStmtRs.stmt.executeQuery();

            while (connStmtRs.rs.next()) {
                int rowId = connStmtRs.rs.getInt("rowid");
                String repoName = connStmtRs.rs.getString("name");
                String repoScm = connStmtRs.rs.getString("scm");
                String repoUrl = connStmtRs.rs.getString("url");
                String repoUsername = connStmtRs.rs.getString("username");
                String repoPassword = connStmtRs.rs.getString("password");
                String repoSource = connStmtRs.rs.getString("source");
                String repoBranch = connStmtRs.rs.getString("branch");
                String repoData = connStmtRs.rs.getString("data");

                repoResults.add(new RepoResult()
                        .setRowId(rowId)
                        .setName(repoName)
                        .setScm(repoScm)
                        .setUrl(repoUrl)
                        .setUsername(repoUsername)
                        .setPassword(repoPassword)
                        .setSource(repoSource)
                        .setBranch(repoBranch)
                        .setData(repoData));
            }
        } catch (SQLException ex) {
            this.logger.severe(String.format("20c36ec2::error in class %s exception %s searchcode was unable to get the paged list of repositories, this is likely nothing will be indexed, most likely the table has changed or is missing", ex.getClass(), ex.getMessage()));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return repoResults;
    }

    public synchronized int getRepoCount() {
        int totalCount = 0;
        ConnStmtRs connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("select count(rowid) as totalcount from repo;");

            connStmtRs.rs = connStmtRs.stmt.executeQuery();

            while (connStmtRs.rs.next()) {
                totalCount = connStmtRs.rs.getInt("totalcount");
            }
        } catch (SQLException ex) {
            this.logger.severe(String.format("4e403331::error in class %s exception %s searchcode was unable to get the count of repositories, this is unlikely to affect anything but there are likely to be other issues in the logs", ex.getClass(), ex.getMessage()));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return totalCount;
    }

    public synchronized Optional<RepoResult> getRepoByName(String repositoryName) {
        if (repositoryName == null) {
            return Optional.empty();
        }

        Optional<RepoResult> result = Optional.empty();
        ConnStmtRs connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("select rowid,name,scm,url,username,password,source,branch,data from repo where name=?;");

            connStmtRs.stmt.setString(1, repositoryName);
            connStmtRs.rs = connStmtRs.stmt.executeQuery();

            while (connStmtRs.rs.next()) {
                int rowId = connStmtRs.rs.getInt("rowid");
                String repoName = connStmtRs.rs.getString("name");
                String repoScm = connStmtRs.rs.getString("scm");
                String repoUrl = connStmtRs.rs.getString("url");
                String repoUsername = connStmtRs.rs.getString("username");
                String repoPassword = connStmtRs.rs.getString("password");
                String repoSource = connStmtRs.rs.getString("source");
                String repoBranch = connStmtRs.rs.getString("branch");
                String repoData = connStmtRs.rs.getString("data");

                RepoResult repoResult = new RepoResult()
                        .setRowId(rowId)
                        .setName(repoName)
                        .setScm(repoScm)
                        .setUrl(repoUrl)
                        .setUsername(repoUsername)
                        .setPassword(repoPassword)
                        .setSource(repoSource)
                        .setBranch(repoBranch)
                        .setData(repoData);

                result = Optional.of(repoResult);
            }
        } catch (SQLException ex) {
            this.logger.severe(String.format("359a0566::error in class %s exception %s searchcode was unable to get repository by name %s, this is likely to cause indexing issues and its likely other issues will be in the logs", ex.getClass(), ex.getMessage(), repositoryName));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return result;
    }

    public synchronized Optional<RepoResult> getRepoByUrl(String repositoryUrl) {
        if (repositoryUrl == null) {
            return Optional.empty();
        }

        Optional<RepoResult> result = Optional.empty();
        ConnStmtRs connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("select rowid,name,scm,url,username,password,source,branch,data from repo where url=?;");
            connStmtRs.stmt.setString(1, repositoryUrl);
            connStmtRs.rs = connStmtRs.stmt.executeQuery();

            while (connStmtRs.rs.next()) {
                int rowId = connStmtRs.rs.getInt("rowid");
                String repoName = connStmtRs.rs.getString("name");
                String repoScm = connStmtRs.rs.getString("scm");
                String repoUrl = connStmtRs.rs.getString("url");
                String repoUsername = connStmtRs.rs.getString("username");
                String repoPassword = connStmtRs.rs.getString("password");
                String repoSource = connStmtRs.rs.getString("source");
                String repoBranch = connStmtRs.rs.getString("branch");
                String repoData = connStmtRs.rs.getString("data");

                RepoResult repoResult = new RepoResult()
                        .setRowId(rowId)
                        .setName(repoName)
                        .setScm(repoScm)
                        .setUrl(repoUrl)
                        .setUsername(repoUsername)
                        .setPassword(repoPassword)
                        .setSource(repoSource)
                        .setBranch(repoBranch)
                        .setData(repoData);

                result = Optional.of(repoResult);
            }
        } catch (SQLException ex) {
            this.logger.severe(String.format("afd625ce::error in class %s exception %s searchcode was unable to get repository by url %s, this is likely to cause indexing issues and its likely other issues will be in the logs", ex.getClass(), ex.getMessage(), repositoryUrl));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return result;
    }

    public synchronized void deleteRepoByName(String repositoryName) {
        ConnStmtRs connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("delete from repo where name=?;");
            connStmtRs.stmt.setString(1, repositoryName);
            connStmtRs.stmt.execute();
        } catch (SQLException ex) {
            this.logger.severe(String.format("8f05a49c::error in class %s exception %s searchcode was unable to delete repository by name %s, this is unlikely to break anything but there should be other errors in the logs", ex.getClass(), ex.getMessage(), repositoryName));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }
    }

    public synchronized boolean saveRepo(RepoResult repoResult) {
        Optional<RepoResult> existing = this.getRepoByName(repoResult.getName());
        ConnStmtRs connStmtRs = new ConnStmtRs();
        boolean isNew = false;

        // Update with new details
        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            if (existing.isPresent()) {
                connStmtRs.stmt = connStmtRs.conn.prepareStatement("UPDATE \"repo\" SET \"name\" = ?, \"scm\" = ?, \"url\" = ?, \"username\" = ?, \"password\" = ?, \"source\" = ?, \"branch\" = ?, \"data\" = ? WHERE  \"name\" = ?");
                connStmtRs.stmt.setString(9, repoResult.getName());
            } else {
                isNew = true;
                connStmtRs.stmt = connStmtRs.conn.prepareStatement("INSERT INTO repo(\"name\",\"scm\",\"url\", \"username\", \"password\",\"source\",\"branch\",\"data\") VALUES (?,?,?,?,?,?,?,?)");
            }

            connStmtRs.stmt.setString(1, repoResult.getName());
            connStmtRs.stmt.setString(2, repoResult.getScm());
            connStmtRs.stmt.setString(3, repoResult.getUrl());
            connStmtRs.stmt.setString(4, repoResult.getUsername());
            connStmtRs.stmt.setString(5, repoResult.getPassword());
            connStmtRs.stmt.setString(6, repoResult.getSource());
            connStmtRs.stmt.setString(7, repoResult.getBranch());
            connStmtRs.stmt.setString(8, repoResult.getDataAsJson());

            connStmtRs.stmt.execute();
        } catch (SQLException ex) {
            this.logger.severe(String.format("653b7384::error in class %s exception %s searchcode was unable to add repository %s, this is unlikely to break anything but there should be other errors in the logs", ex.getClass(), ex.getMessage(), repoResult.getName()));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return isNew;
    }

    public synchronized void createTableIfMissing() {
        ConnStmtRs connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name='repo';");

            connStmtRs.rs = connStmtRs.stmt.executeQuery();
            String value = Values.EMPTYSTRING;
            while (connStmtRs.rs.next()) {
                value = connStmtRs.rs.getString("name");
            }

            if (Singleton.getHelpers().isNullEmptyOrWhitespace(value)) {
                connStmtRs.stmt = connStmtRs.conn.prepareStatement("CREATE TABLE IF NOT EXISTS \"repo\" (\"name\" VARCHAR PRIMARY KEY  NOT NULL ,\"scm\" VARCHAR,\"url\" VARCHAR,\"username\" VARCHAR,\"password\" VARCHAR, \"source\", \"branch\" VARCHAR, data text);");
                connStmtRs.stmt.execute();
            }
        } catch (SQLException ex) {
            this.logger.severe(String.format("5ec972ce::error in class %s exception %s searchcode was to create the api key table, so api calls will fail", ex.getClass(), ex.getMessage()));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }
    }
}
