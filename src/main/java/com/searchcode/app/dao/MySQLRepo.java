package com.searchcode.app.dao;

import com.searchcode.app.config.IDatabaseConfig;
import com.searchcode.app.dto.ConnStmtRs;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MySQLRepo implements IRepo {

    private final IDatabaseConfig dbConfig;
    private final Helpers helpers;
    private final LoggerWrapper logger;

    public MySQLRepo() {
        this(Singleton.getDatabaseConfig(), Singleton.getHelpers(), Singleton.getLogger());
    }

    public MySQLRepo(IDatabaseConfig dbConfig, Helpers helpers, LoggerWrapper loggerWrapper) {
        this.dbConfig = dbConfig;
        this.helpers = helpers;
        this.logger = loggerWrapper;
    }

    @Override
    public boolean saveRepo(RepoResult repoResult) {
        Optional<RepoResult> existing = this.getRepoByUrl(repoResult.getUrl());
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
                connStmtRs.stmt = connStmtRs.conn.prepareStatement("INSERT INTO `repo`(`id`, `name`, `scm`, `url`, `suggestedname`, `sourceurl`, `instructions`, `sourceid`, `spdx`, `username`) VALUES (null,?,?,?,?,?,?,?,?,?);");
            }

            connStmtRs.stmt.setString(1, repoResult.getName());
            connStmtRs.stmt.setString(2, repoResult.getScm());
            connStmtRs.stmt.setString(3, repoResult.getUrl());
            // suggestedd
            // sourcerurl
            // instructions
            // sourceid
            // spdx
            // username

            connStmtRs.stmt.execute();
        } catch (SQLException ex) {
            this.logger.severe(String.format("6b1285b5::error in class %s exception %s searchcode was unable to add repository %s, this is unlikely to break anything but there should be other errors in the logs", ex.getClass(), ex.getMessage(), repoResult.getName()));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return isNew;
    }

    @Override
    public void deleteRepoByName(String repositoryName) {

    }

    @Override
    public Optional<RepoResult> getRepoByUrl(String repositoryUrl) {
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
            this.logger.severe(String.format("fb6e9dfe::error in class %s exception %s searchcode was unable to get repository by url %s, this is likely to cause indexing issues and its likely other issues will be in the logs", ex.getClass(), ex.getMessage(), repositoryUrl));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return result;
    }

    @Override
    public Optional<RepoResult> getRepoByName(String repositoryName) {
        return Optional.empty();
    }

    @Override
    public int getRepoCount() {
        return 0;
    }

    @Override
    public List<RepoResult> getPagedRepo(int offset, int pageSize) {
        return null;
    }

    @Override
    public List<RepoResult> searchRepo(String searchTerms) {
        return null;
    }

    @Override
    public List<RepoResult> getAllRepo() {
        return null;
    }
}
