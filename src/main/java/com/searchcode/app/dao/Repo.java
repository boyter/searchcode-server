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

        Connection connection;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.dbConfig.getConnection();
            preparedStatement = connection.prepareStatement("select rowid,name,scm,url,username,password,source,branch,data from repo order by rowid desc;");

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int rowId = resultSet.getInt("rowid");
                String repoName = resultSet.getString("name");
                String repoScm = resultSet.getString("scm");
                String repoUrl = resultSet.getString("url");
                String repoUsername = resultSet.getString("username");
                String repoPassword = resultSet.getString("password");
                String repoSource = resultSet.getString("source");
                String repoBranch = resultSet.getString("branch");
                String repoData = resultSet.getString("data");

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
        }
        catch (SQLException ex) {
            this.logger.severe(String.format("820c9557::error in class %s exception %s searchcode was unable to get the list of all repositories, this is likely nothing will be indexed, most likely the table has changed or is missing", ex.getClass(), ex.getMessage()));
        }
        finally {
            this.helpers.closeQuietly(resultSet);
            this.helpers.closeQuietly(preparedStatement);
        }

        return repoResults;
    }

    public synchronized List<RepoResult> searchRepo(String searchTerms) {
        List<RepoResult> repoResults = this.getAllRepo();
        List<RepoResult> matchRepoResults = new ArrayList<>();

        String[] split = searchTerms.toLowerCase().split(" ");

        for(RepoResult rr: repoResults) {
            boolean isMatch = false;

            for (String term: split) {
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

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = this.dbConfig.getConnection();
            stmt = conn.prepareStatement("select rowid,name,scm,url,username,password,source,branch,data from repo order by rowid desc limit ?, ?;");

            stmt.setInt(1, offset);
            stmt.setInt(2, pageSize);

            rs = stmt.executeQuery();

            while (rs.next()) {
                int rowId = rs.getInt("rowid");
                String repoName = rs.getString("name");
                String repoScm = rs.getString("scm");
                String repoUrl = rs.getString("url");
                String repoUsername = rs.getString("username");
                String repoPassword = rs.getString("password");
                String repoSource = rs.getString("source");
                String repoBranch = rs.getString("branch");
                String repoData = rs.getString("data");

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
        }
        catch (SQLException ex) {
            this.logger.severe(String.format("20c36ec2::error in class %s exception %s searchcode was unable to get the paged list of repositories, this is likely nothing will be indexed, most likely the table has changed or is missing", ex.getClass(), ex.getMessage()));
        }
        finally {
            this.helpers.closeQuietly(rs);
            this.helpers.closeQuietly(stmt);
        }

        return repoResults;
    }

    public synchronized int getRepoCount() {
        int totalCount = 0;

        Connection connection;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.dbConfig.getConnection();
            preparedStatement = connection.prepareStatement("select count(rowid) as totalcount from repo;");

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                totalCount = resultSet.getInt("totalcount");
            }
        }
        catch (SQLException ex) {
            this.logger.severe(String.format("4e403331::error in class %s exception %s searchcode was unable to get the count of repositories, this is unlikely to affect anything but there are likely to be other issues in the logs", ex.getClass(), ex.getMessage()));
        }
        finally {
            this.helpers.closeQuietly(resultSet);
            this.helpers.closeQuietly(preparedStatement);
        }

        return totalCount;
    }

    public synchronized Optional<RepoResult> getRepoByName(String repositoryName) {
        if (repositoryName == null) {
            return Optional.empty();
        }

        Optional<RepoResult> result = Optional.empty();

        Connection connection;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.dbConfig.getConnection();
            preparedStatement = connection.prepareStatement("select rowid,name,scm,url,username,password,source,branch,data from repo where name=?;");

            preparedStatement.setString(1, repositoryName);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int rowId = resultSet.getInt("rowid");
                String repoName = resultSet.getString("name");
                String repoScm = resultSet.getString("scm");
                String repoUrl = resultSet.getString("url");
                String repoUsername = resultSet.getString("username");
                String repoPassword = resultSet.getString("password");
                String repoSource = resultSet.getString("source");
                String repoBranch = resultSet.getString("branch");
                String repoData = resultSet.getString("data");

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
        }
        catch (SQLException ex) {
            this.logger.severe(String.format("359a0566::error in class %s exception %s searchcode was unable to get repository by name %s, this is likely to cause indexing issues and its likely other issues will be in the logs", ex.getClass(), ex.getMessage(), repositoryName));
        }
        finally {
            this.helpers.closeQuietly(resultSet);
            this.helpers.closeQuietly(preparedStatement);
        }

        return result;
    }

    public synchronized Optional<RepoResult> getRepoByUrl(String repositoryUrl) {
        if (repositoryUrl == null) {
            return Optional.empty();
        }

        Connection connection;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        Optional<RepoResult> result = Optional.empty();

        try {
            connection = this.dbConfig.getConnection();
            preparedStatement = connection.prepareStatement("select rowid,name,scm,url,username,password,source,branch,data from repo where url=?;");
            preparedStatement.setString(1, repositoryUrl);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int rowId = resultSet.getInt("rowid");
                String repoName = resultSet.getString("name");
                String repoScm = resultSet.getString("scm");
                String repoUrl = resultSet.getString("url");
                String repoUsername = resultSet.getString("username");
                String repoPassword = resultSet.getString("password");
                String repoSource = resultSet.getString("source");
                String repoBranch = resultSet.getString("branch");
                String repoData = resultSet.getString("data");

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
        }
        catch (SQLException ex) {
            this.logger.severe(String.format("afd625ce::error in class %s exception %s searchcode was unable to get repository by url %s, this is likely to cause indexing issues and its likely other issues will be in the logs", ex.getClass(), ex.getMessage(), repositoryUrl));
        }
        finally {
            this.helpers.closeQuietly(resultSet);
            this.helpers.closeQuietly(preparedStatement);
        }

        return result;
    }

    public synchronized void deleteRepoByName(String repositoryName) {
        Connection connection;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.dbConfig.getConnection();
            preparedStatement = connection.prepareStatement("delete from repo where name=?;");
            preparedStatement.setString(1, repositoryName);
            preparedStatement.execute();
        }
        catch (SQLException ex) {
            this.logger.severe(String.format("8f05a49c::error in class %s exception %s searchcode was unable to delete repository by name %s, this is unlikely to break anything but there should be other errors in the logs", ex.getClass(), ex.getMessage(), repositoryName));
        }
        finally {
            this.helpers.closeQuietly(resultSet);
            this.helpers.closeQuietly(preparedStatement);
        }
    }

    public synchronized boolean saveRepo(RepoResult repoResult) {
        Optional<RepoResult> existing = this.getRepoByName(repoResult.getName());

        boolean isNew = false;

        Connection connection;
        PreparedStatement preparedStatement = null;

        // Update with new details
        try {
            connection = this.dbConfig.getConnection();
            if (existing.isPresent()) {
                preparedStatement = connection.prepareStatement("UPDATE \"repo\" SET \"name\" = ?, \"scm\" = ?, \"url\" = ?, \"username\" = ?, \"password\" = ?, \"source\" = ?, \"branch\" = ?, \"data\" = ? WHERE  \"name\" = ?");
                preparedStatement.setString(9, repoResult.getName());
            }
            else {
                isNew = true;
                preparedStatement = connection.prepareStatement("INSERT INTO repo(\"name\",\"scm\",\"url\", \"username\", \"password\",\"source\",\"branch\",\"data\") VALUES (?,?,?,?,?,?,?,?)");
            }

            preparedStatement.setString(1, repoResult.getName());
            preparedStatement.setString(2, repoResult.getScm());
            preparedStatement.setString(3, repoResult.getUrl());
            preparedStatement.setString(4, repoResult.getUsername());
            preparedStatement.setString(5, repoResult.getPassword());
            preparedStatement.setString(6, repoResult.getSource());
            preparedStatement.setString(7, repoResult.getBranch());
            preparedStatement.setString(8, repoResult.getDataAsJson());

            preparedStatement.execute();
        }
        catch (SQLException ex) {
            this.logger.severe(String.format("653b7384::error in class %s exception %s searchcode was unable to add repository %s, this is unlikely to break anything but there should be other errors in the logs", ex.getClass(), ex.getMessage(), repoResult.getName()));
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
            preparedStatement = connection.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name='repo';");

            resultSet = preparedStatement.executeQuery();
            String value = Values.EMPTYSTRING;
            while (resultSet.next()) {
                value = resultSet.getString("name");
            }

            if (Singleton.getHelpers().isNullEmptyOrWhitespace(value)) {
                preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS \"repo\" (\"name\" VARCHAR PRIMARY KEY  NOT NULL ,\"scm\" VARCHAR,\"url\" VARCHAR,\"username\" VARCHAR,\"password\" VARCHAR, \"source\", \"branch\" VARCHAR, data text);");
                preparedStatement.execute();
            }
        }
        catch (SQLException ex) {
            this.logger.severe(String.format("5ec972ce::error in class %s exception %s searchcode was to create the api key table, so api calls will fail", ex.getClass(), ex.getMessage()));
        }
        finally {
            this.helpers.closeQuietly(resultSet);
            this.helpers.closeQuietly(preparedStatement);
        }
    }
}
