package com.searchcode.app.dao;

import com.searchcode.app.config.IDatabaseConfig;
import com.searchcode.app.dto.ConnStmtRs;
import com.searchcode.app.model.SourceResult;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Source {

    private final Helpers helpers;
    private final IDatabaseConfig dbConfig;
    private final LoggerWrapper logger;

    public Source() {
        this(Singleton.getDatabaseConfig(), Singleton.getHelpers(), Singleton.getLogger());
    }

    public Source(IDatabaseConfig dbConfig, Helpers helpers, LoggerWrapper logger) {
        this.dbConfig = dbConfig;
        this.helpers = helpers;
        this.logger = logger;
    }

    public Optional<SourceResult> getSourceByName(String sourceName) {
        Optional<SourceResult> result = Optional.empty();
        var connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("select id,name from source where name=?;");

            connStmtRs.stmt.setString(1, sourceName);
            connStmtRs.rs = connStmtRs.stmt.executeQuery();

            while (connStmtRs.rs.next()) {
                result = getSourceResult(connStmtRs);
            }
        } catch (SQLException ex) {
            this.logger.severe(String.format("8c537eed::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return result;
    }

    public Optional<SourceResult> getSourceById(int sourceId) {
        Optional<SourceResult> result = Optional.empty();
        var connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("select id,name from source where id=?;");

            connStmtRs.stmt.setInt(1, sourceId);
            connStmtRs.rs = connStmtRs.stmt.executeQuery();

            while (connStmtRs.rs.next()) {
                result = getSourceResult(connStmtRs);
            }
        } catch (SQLException ex) {
            this.logger.severe(String.format("8c537eed::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return result;
    }

    private Optional<SourceResult> getSourceResult(ConnStmtRs connStmtRs) throws SQLException {
        var id = connStmtRs.rs.getInt("id");
        var name = connStmtRs.rs.getString("name");

        var sourceResult = new SourceResult()
                .setId(id)
                .setName(name);

        return Optional.of(sourceResult);
    }
}
