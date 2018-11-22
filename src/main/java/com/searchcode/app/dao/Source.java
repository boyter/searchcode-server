package com.searchcode.app.dao;

import com.searchcode.app.config.IDatabaseConfig;
import com.searchcode.app.dto.ConnStmtRs;
import com.searchcode.app.model.SourceResult;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;
import org.cache2k.Cache;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Source {

    private final Helpers helpers;
    private final IDatabaseConfig dbConfig;
    private final LoggerWrapper logger;
    private final Cache<String, Object> cache;
    private final String CachePrefix = "dao.source.";

    public Source() {
        this(Singleton.getDatabaseConfig(), Singleton.getHelpers(), Singleton.getLogger(), Singleton.getGenericCache());
    }

    public Source(IDatabaseConfig dbConfig, Helpers helpers, LoggerWrapper logger, Cache<String, Object> cache) {
        this.dbConfig = dbConfig;
        this.helpers = helpers;
        this.logger = logger;
        this.cache = cache;

    }

    public Optional<SourceResult> getSourceByName(String sourceName) {
        var cacheResult = this.cache.peekEntry(CachePrefix + sourceName);
        if (cacheResult != null) {
            return (Optional<SourceResult>) cacheResult.getValue();
        }

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

        if (result.isPresent()) {
            this.cache.put(CachePrefix + sourceName, result);
        }

        return result;
    }

    public Optional<SourceResult> getSourceById(int sourceId) {
        var cacheResult = this.cache.peekEntry(CachePrefix + sourceId);
        if (cacheResult != null) {
            return (Optional<SourceResult>) cacheResult.getValue();
        }

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
            this.logger.severe(String.format("f4ab169b::error in class %s exception %s", ex.getClass(), ex.getMessage()));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        if (result.isPresent()) {
            this.cache.put(CachePrefix + sourceId, result);
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
