/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.14
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
            this.logger.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
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
            this.logger.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
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
            this.logger.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
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
            this.logger.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
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
            this.logger.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
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
            this.logger.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            this.helpers.closeQuietly(resultSet);
            this.helpers.closeQuietly(preparedStatement);
        }
    }

    // TODO add retry logic here as this can fail and as such should just trigger again
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
            this.logger.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            this.helpers.closeQuietly(preparedStatement);
        }

        return isNew;
    }


    // Schema Migrations below
    public void addSourceToTable() {
        Connection connection;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.dbConfig.getConnection();
            preparedStatement = connection.prepareStatement("PRAGMA table_info(repo);");

            boolean shouldAlter = true;

            resultSet = preparedStatement.executeQuery();
            String value;

            while (resultSet.next()) {
                value = resultSet.getString("name");

                if ("source".equals(value)) {
                    shouldAlter = false;
                }
            }

            if (shouldAlter) {
                List<String> commands = Arrays.asList(
                        "ALTER TABLE \"repo\" RENAME TO \"oXHFcGcd04oXHFcGcd04_repo\"",
                        "CREATE TABLE \"repo\" (\"name\" VARCHAR PRIMARY KEY  NOT NULL ,\"scm\" VARCHAR,\"url\" VARCHAR,\"username\" VARCHAR,\"password\" VARCHAR, \"source\" VARCHAR)",
                        "INSERT INTO \"repo\" SELECT \"name\",\"scm\",\"url\",\"username\",\"password\", \"\" FROM \"main\".\"oXHFcGcd04oXHFcGcd04_repo\"",
                        "DROP TABLE \"oXHFcGcd04oXHFcGcd04_repo\"");

                for (String command: commands) {
                    preparedStatement = connection.prepareStatement(command);
                    preparedStatement.execute();
                }
            }
        }
        catch (SQLException ex) {
            this.logger.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            this.helpers.closeQuietly(resultSet);
            this.helpers.closeQuietly(preparedStatement);
        }
    }

    public void addBranchToTable() {
        Connection connection;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.dbConfig.getConnection();
            preparedStatement = connection.prepareStatement("PRAGMA table_info(repo);");

            boolean shouldAlter = true;

            resultSet = preparedStatement.executeQuery();
            String value;

            while (resultSet.next()) {
                value = resultSet.getString("name");

                if ("branch".equals(value)) {
                    shouldAlter = false;
                }
            }

            // Python to generate random table name
            // ''.join(random.choice(string.ascii_uppercase + string.digits + string.ascii_lowercase) for _ in range(20))
            if (shouldAlter) {
                List<String> commands = Arrays.asList(
                        "ALTER TABLE \"repo\" RENAME TO \"y6L0VN5j9eQSg65hWtJJ_repo\"",
                        "CREATE TABLE \"repo\" (\"name\" VARCHAR PRIMARY KEY  NOT NULL ,\"scm\" VARCHAR,\"url\" VARCHAR,\"username\" VARCHAR,\"password\" VARCHAR, \"source\", \"branch\" VARCHAR)",
                        "INSERT INTO \"repo\" SELECT \"name\",\"scm\",\"url\",\"username\",\"password\", \"\", \"master\" FROM \"main\".\"y6L0VN5j9eQSg65hWtJJ_repo\"",
                        "DROP TABLE \"y6L0VN5j9eQSg65hWtJJ_repo\"");

                for (String command: commands) {
                    preparedStatement = connection.prepareStatement(command);
                    preparedStatement.execute();
                }
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

    public synchronized void createTableIfMissing() {

        Connection connection;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.dbConfig.getConnection();
            preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS \"repo\" (\"name\" VARCHAR PRIMARY KEY  NOT NULL ,\"scm\" VARCHAR,\"url\" VARCHAR,\"username\" VARCHAR,\"password\" VARCHAR, \"source\", \"branch\" VARCHAR, data text);");
            preparedStatement.execute();
        }
        catch (SQLException ex) {
            Singleton.getLogger().severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            this.helpers.closeQuietly(resultSet);
            this.helpers.closeQuietly(preparedStatement);
        }
    }

    public void addDataToTable() {
        Connection connection;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = this.dbConfig.getConnection();
            preparedStatement = connection.prepareStatement("PRAGMA table_info(repo);");

            boolean shouldAlter = true;

            resultSet = preparedStatement.executeQuery();
            String value;

            while (resultSet.next()) {
                value = resultSet.getString("name");

                if ("data".equals(value)) {
                    shouldAlter = false;
                }
            }

            if (shouldAlter) {
                preparedStatement = connection.prepareStatement("ALTER TABLE repo ADD COLUMN data text;");
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
