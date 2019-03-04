package com.searchcode.app.dao;

import com.searchcode.app.config.IDatabaseConfig;
import com.searchcode.app.config.MySQLDatabaseConfig;
import com.searchcode.app.dto.ConnStmtRs;
import com.searchcode.app.dto.LanguageTypeDTO;
import com.searchcode.app.service.CacheSingleton;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;
import org.cache2k.Cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Generally only used when Sphinx is set as the indexing engine and used to store language
 * name/id in the database
 */
public class LanguageType {

    private final Helpers helpers;
    private final IDatabaseConfig dbConfig;
    private final LoggerWrapper logger;

    // Gets its own typeCache as this is likely to be one of the harder hit endpoints and we want to typeCache it for a long time
    private final Cache<String, Optional<LanguageTypeDTO>> typeCache;
    private final String CachePrefix = "d.l.";

    public LanguageType() {
        this(
                new MySQLDatabaseConfig(),
                Singleton.getHelpers(),
                Singleton.getLogger(),
                CacheSingleton.getLanguageTypeCache()
        );
    }

    public LanguageType(IDatabaseConfig dbConfig, Helpers helpers, LoggerWrapper logger, Cache<String, Optional<LanguageTypeDTO>> typeCache) {
        this.dbConfig = dbConfig;
        this.helpers = helpers;
        this.logger = logger;
        this.typeCache = typeCache;
    }

    public synchronized Optional<LanguageTypeDTO> getByType(String type) {
        var cacheKey = CachePrefix + type;
        var cacheResult = this.typeCache.peekEntry(cacheKey);
        if (cacheResult != null) {
            return cacheResult.getValue();
        }

        Optional<LanguageTypeDTO> optional = Optional.empty();
        var connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("SELECT id, type FROM languagetype WHERE type = ? LIMIT 1;");
            connStmtRs.stmt.setString(1, type);
            connStmtRs.rs = connStmtRs.stmt.executeQuery();

            while (connStmtRs.rs.next()) {
                var id = connStmtRs.rs.getInt("id");
                type = connStmtRs.rs.getString("type");
                optional = Optional.of(new LanguageTypeDTO(id, type));
            }
        } catch (SQLException ex) {
            this.logger.severe(String.format("5d3921d2::error in class %s exception %s searchcode was unable to get language by type %s, this is likely to break all sorts of things, most likely the table has changed or is missing", ex.getClass(), ex.getMessage(), type));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        if (optional.isPresent()) {
            this.typeCache.put(cacheKey, optional);
        }

        return optional;
    }

    public synchronized Optional<LanguageTypeDTO> getById(int id) {
        var cacheKey = CachePrefix + id;
        var cacheResult = this.typeCache.peekEntry(cacheKey);
        if (cacheResult != null) {
            return cacheResult.getValue();
        }

        Optional<LanguageTypeDTO> optional = Optional.empty();
        var connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("SELECT id, type FROM languagetype WHERE id = ? LIMIT 1;");
            connStmtRs.stmt.setInt(1, id);
            connStmtRs.rs = connStmtRs.stmt.executeQuery();

            while (connStmtRs.rs.next()) {
                id = connStmtRs.rs.getInt("id");
                var type = connStmtRs.rs.getString("type");
                optional = Optional.of(new LanguageTypeDTO(id, type));
            }
        } catch (SQLException ex) {
            this.logger.severe(String.format("af0acf72::error in class %s exception %s searchcode was unable to get language by id %s, this is likely to break all sorts of things, most likely the table has changed or is missing", ex.getClass(), ex.getMessage(), id));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        if (optional.isPresent()) {
            this.typeCache.put(cacheKey, optional);
        }

        return optional;
    }

    /**
     * Create a new language type in the database and return an optional containing it.
     * If given a type that already exists will return that one rather than create a new one.
     */
    public synchronized Optional<LanguageTypeDTO> createLanguageType(String type) {
        var byType = this.getByType(type);
        if (byType.isPresent()) {
            return byType;
        }

        var connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("INSERT INTO `languagetype` (`type`) VALUES (?);", Statement.RETURN_GENERATED_KEYS);
            connStmtRs.stmt.setString(1, type);
            connStmtRs.stmt.execute();

            ResultSet tableKeys = connStmtRs.stmt.getGeneratedKeys();
            tableKeys.next();
            var autoGeneratedID = tableKeys.getInt(1);
            byType = Optional.of(new LanguageTypeDTO(autoGeneratedID, type));
        } catch (SQLException ex) {
            this.logger.severe(String.format("5e49d36c::error in class %s exception %s searchcode was unable to create language by type %s, this is likely to break all sorts of things, most likely the table has changed or is missing", ex.getClass(), ex.getMessage(), type));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return byType;
    }
}
