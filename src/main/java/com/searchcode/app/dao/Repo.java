/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.2
 */

package com.searchcode.app.dao;

import com.google.inject.Inject;
import com.searchcode.app.config.IDatabaseConfig;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.LoggerWrapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides access to all methods required to get Repo details from the database.
 * Note that we use an in memory cache to avoid hitting the database too much. This was because when hit really hard
 * that there would be timeouts and other database connection issues with the dreaded "Too many connections".
 */
public class Repo implements IRepo {

    private static final LoggerWrapper LOGGER = Singleton.getLogger();
    private AbstractMap<String, RepoResult> cache = Singleton.getRepoCache();
    private IDatabaseConfig dbConfig;
    private AbstractMap<String, Object> genericCache = Singleton.getGenericCache();
    private String repoCountCacheKey = "repo-repo-count";
    private String repoAllRepoCacheKey = "repo-all-repo-cache";

    @Inject
    public Repo(IDatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public synchronized List<RepoResult> getAllRepo() {
        List<RepoResult> repoResults = (ArrayList<RepoResult>)this.genericCache.get(this.repoAllRepoCacheKey);
        if (this.genericCache.containsKey(this.repoAllRepoCacheKey)) {
            return repoResults;
        }

        repoResults = new ArrayList<>();

        try {
            Connection conn = this.dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select rowid,name,scm,url,username,password,source,branch,masks from repo order by rowid desc;");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int rowId = rs.getInt("rowid");
                String repoName = rs.getString("name");
                String repoScm = rs.getString("scm");
                String repoUrl = rs.getString("url");
                String repoUsername = rs.getString("username");
                String repoPassword = rs.getString("password");
                String repoSource = rs.getString("source");
                String repoBranch = rs.getString("branch");
                String repoMasks = rs.getString("masks");

                repoResults.add(new RepoResult(rowId, repoName, repoScm, repoUrl, repoUsername, repoPassword, repoSource,
                        repoBranch, repoMasks));
            }

            stmt.close();
            conn.close();
        }
        catch(SQLException ex) {
            LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        this.genericCache.put(this.repoAllRepoCacheKey, repoResults);
        return repoResults;
    }

    public synchronized List<RepoResult> searchRepo(String searchTerms) {
        List<RepoResult> repoResults = this.getAllRepo();
        List<RepoResult> matchRepoResults = new ArrayList<RepoResult>();

        String[] split = searchTerms.toLowerCase().split(" ");

        for(RepoResult rr: repoResults) {
            boolean isMatch = false;

            for(String term: split) {
                if (rr.toString().toLowerCase().contains(term)) {
                    isMatch = true;
                }
                else {
                    isMatch = false;
                }
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

        try {
            Connection conn = this.dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select rowid,name,scm,url,username,password,source,branch,masks from repo order by rowid desc limit ?, ?;");

            stmt.setInt(1, offset);
            stmt.setInt(2, pageSize);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int rowId = rs.getInt("rowid");
                String repoName = rs.getString("name");
                String repoScm = rs.getString("scm");
                String repoUrl = rs.getString("url");
                String repoUsername = rs.getString("username");
                String repoPassword = rs.getString("password");
                String repoSource = rs.getString("source");
                String repoBranch = rs.getString("branch");
                String repoMasks = rs.getString("masks");

                repoResults.add(new RepoResult(rowId, repoName, repoScm, repoUrl, repoUsername, repoPassword, repoSource,
                        repoBranch, repoMasks));
            }

            stmt.close();
            conn.close();
        }
        catch(SQLException ex) {
            LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        return repoResults;
    }

    public synchronized int getRepoCount() {
        Integer totalcount = (Integer)this.genericCache.get(this.repoCountCacheKey);
        if (totalcount != null) {
            return totalcount;
        }

        totalcount = 0;
        try {
            Connection conn = this.dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select count(rowid) as totalcount from repo;");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                totalcount = rs.getInt("totalcount");
            }

            stmt.close();
            conn.close();
        }
        catch(SQLException ex) {
            LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        this.genericCache.put(this.repoCountCacheKey, totalcount);
        return totalcount;
    }

    public synchronized RepoResult getRepoByName(String repositoryName) {
        RepoResult result = this.cache.get(repositoryName);
        if (result != null) {
            return result;
        }


        try {
            Connection conn = this.dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select rowid,name,scm,url,username,password,source,branch,masks from repo where name=?;");

            stmt.setString(1, repositoryName);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int rowId = rs.getInt("rowid");
                String repoName = rs.getString("name");
                String repoScm = rs.getString("scm");
                String repoUrl = rs.getString("url");
                String repoUsername = rs.getString("username");
                String repoPassword = rs.getString("password");
                String repoSource = rs.getString("source");
                String repoBranch = rs.getString("branch");
                String repoMasks = rs.getString("masks");

                result = new RepoResult(rowId, repoName, repoScm, repoUrl, repoUsername, repoPassword, repoSource,
                        repoBranch, repoMasks);
            }

            stmt.close();
            conn.close();
        }
        catch(SQLException ex) {
            LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        if (result != null) {
            this.cache.put(repositoryName, result);
        }

        return result;
    }

    public synchronized void deleteRepoByName(String repositoryName) {
        try {
            Connection conn = this.dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement("delete from repo where name=?;");

            stmt.setString(1, repositoryName);

            stmt.execute();

            stmt.close();
            conn.close();

            this.cache.remove(repositoryName);
            this.genericCache.remove(this.repoCountCacheKey);
            this.genericCache.remove(this.repoAllRepoCacheKey);
        }
        catch(SQLException ex) {
            LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
    }

    // Anything which updates or saves should be syncronized to avoid
    // SQLite issues with such operations
    // TODO add retry logic here as this can fail and as such should just trigger again
    @Override
    public synchronized boolean saveRepo(RepoResult repoResult) {
        RepoResult existing = this.getRepoByName(repoResult.getName());
        this.cache.remove(repoResult.getName());

        boolean isNew = false;

        if(existing != null) {
            // Update with new details
            try {
                Connection conn = this.dbConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement("UPDATE \"repo\" SET \"name\" = ?, \"scm\" = ?, \"url\" = ?, \"username\" = ?, \"password\" = ?, \"source\" = ?, \"branch\" = ?, \"masks\" = ? WHERE  \"name\" = ?");

                stmt.setString(1, repoResult.getName());
                stmt.setString(2, repoResult.getScm());
                stmt.setString(3, repoResult.getUrl());
                stmt.setString(4, repoResult.getUsername());
                stmt.setString(5, repoResult.getPassword());
                stmt.setString(6, repoResult.getSource());
                stmt.setString(7, repoResult.getBranch());
                stmt.setString(8, repoResult.getMasks());

                // Target the row
                stmt.setString(8, repoResult.getName());

                stmt.execute();

                stmt.close();
                conn.close();
            }
            catch(SQLException ex) {
                LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
            }
        }
        else {
            isNew = true;
            try {
                Connection conn = this.dbConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO repo(\"name\",\"scm\",\"url\", \"username\", \"password\",\"source\",\"branch\",\"masks\") VALUES (?,?,?,?,?,?,?,?)");

                stmt.setString(1, repoResult.getName());
                stmt.setString(2, repoResult.getScm());
                stmt.setString(3, repoResult.getUrl());
                stmt.setString(4, repoResult.getUsername());
                stmt.setString(5, repoResult.getPassword());
                stmt.setString(6, repoResult.getSource());
                stmt.setString(7, repoResult.getBranch());
                stmt.setString(8, repoResult.getMasks());

                stmt.execute();

                stmt.close();
                conn.close();
            }
            catch(SQLException ex) {
                LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
            }
        }

        this.genericCache.remove(this.repoCountCacheKey);
        this.genericCache.remove(this.repoAllRepoCacheKey);
        return isNew;
    }

    // Schema Migrations below
    public void addSourceToTable() {
        try {
            Connection conn = this.dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement("PRAGMA table_info(repo);");

            boolean shouldAlter = true;

            ResultSet rs = stmt.executeQuery();
            String value = "";
            while (rs.next()) {
                value = rs.getString("name");

                if("source".equals(value)) {
                    shouldAlter = false;
                }
            }

            if(shouldAlter) {
                List<String> commands = Arrays.asList(
                        "ALTER TABLE \"repo\" RENAME TO \"oXHFcGcd04oXHFcGcd04_repo\"",
                        "CREATE TABLE \"repo\" (\"name\" VARCHAR PRIMARY KEY  NOT NULL ,\"scm\" VARCHAR,\"url\" VARCHAR,\"username\" VARCHAR,\"password\" VARCHAR, \"source\" VARCHAR, \"masks\" VARCHAR)",
                        "INSERT INTO \"repo\" SELECT \"name\",\"scm\",\"url\",\"username\",\"password\", \"\" FROM \"main\".\"oXHFcGcd04oXHFcGcd04_repo\"",
                        "DROP TABLE \"oXHFcGcd04oXHFcGcd04_repo\"");

                for(String command: commands) {
                    stmt = conn.prepareStatement(command);
                    stmt.execute();
                }
            }

            stmt.close();
            conn.close();
        }
        catch(SQLException ex) {
            LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
    }

    public void addBranchToTable() {
        try {
            Connection conn = this.dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement("PRAGMA table_info(repo);");

            boolean shouldAlter = true;

            ResultSet rs = stmt.executeQuery();
            String value = "";
            while (rs.next()) {
                value = rs.getString("name");

                if("branch".equals(value)) {
                    shouldAlter = false;
                }
            }

            // Python to generate random table name
            // ''.join(random.choice(string.ascii_uppercase + string.digits + string.ascii_lowercase) for _ in range(20))
            if(shouldAlter) {
                List<String> commands = Arrays.asList(
                        "ALTER TABLE \"repo\" RENAME TO \"y6L0VN5j9eQSg65hWtJJ_repo\"",
                        "CREATE TABLE \"repo\" (\"name\" VARCHAR PRIMARY KEY  NOT NULL ,\"scm\" VARCHAR,\"url\" VARCHAR,\"username\" VARCHAR,\"password\" VARCHAR, \"source\", \"branch\" VARCHAR, \"masks\" VARCHAR)",
                        "INSERT INTO \"repo\" SELECT \"name\",\"scm\",\"url\",\"username\",\"password\", \"\", \"master\" FROM \"main\".\"y6L0VN5j9eQSg65hWtJJ_repo\"",
                        "DROP TABLE \"y6L0VN5j9eQSg65hWtJJ_repo\"");

                for(String command: commands) {
                    stmt = conn.prepareStatement(command);
                    stmt.execute();
                }
            }

            stmt.close();
            conn.close();
        }
        catch(SQLException ex) {
            LOGGER.severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
    }
}
