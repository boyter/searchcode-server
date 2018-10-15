package com.searchcode.app.dao;

import com.searchcode.app.config.IDatabaseConfig;
import com.searchcode.app.config.MySQLDatabaseConfig;
import com.searchcode.app.config.Values;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.dto.ConnStmtRs;
import com.searchcode.app.dto.LanguageTypeDTO;
import com.searchcode.app.dto.SourceCodeDTO;
import com.searchcode.app.model.CodeResult;
import com.searchcode.app.model.searchcode.SearchcodeCodeResult;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SourceCode {
    private final Helpers helpers;
    private final IDatabaseConfig dbConfig;
    private final LanguageType languageType;
    private final LoggerWrapper logger;

    public SourceCode() {
        this(new MySQLDatabaseConfig(), Singleton.getHelpers(), Singleton.getLanguageType(), Singleton.getLogger());
    }

    public SourceCode(IDatabaseConfig dbConfig, Helpers helpers, LanguageType languageType, LoggerWrapper logger) {
        this.dbConfig = dbConfig;
        this.helpers = helpers;
        this.languageType = languageType;
        this.logger = logger;
    }

    public synchronized int getMaxId() {
        int maxId = 0;
        ConnStmtRs connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("select id from code order by id desc limit 1;");
            connStmtRs.rs = connStmtRs.stmt.executeQuery();

            while (connStmtRs.rs.next()) {
                maxId = connStmtRs.rs.getInt(1);
            }
        } catch (SQLException ex) {
            this.logger.severe(String.format("d6941483::error in class %s exception %s searchcode unable to get the maxid, this is not likely to affect anything other than the display", ex.getClass(), ex.getMessage()));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return maxId;
    }

    public synchronized List<CodeResult> getByIds(List<Integer> codeIds) {
        List<CodeResult> codeResultList = new ArrayList<>();
        ConnStmtRs connStmtRs = new ConnStmtRs();

        StringBuffer stringBuffer = new StringBuffer();
        for (Integer codeId : codeIds) {
            stringBuffer.append(codeId).append(",");
        }
        String codeIdsString = StringUtils.substring(stringBuffer.toString(), 0, -1);

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("select c.id,c.filename,c.location," +
                    "substring(uncompress(c.content),1,500000) as content," +
                    "c.hash, r.name as reponame, c.simhash, c.linescount," +
                    "lt.type as languagetype," +
                    "s.name as sourcename, r.sourceurl, r.url, c.blank, c.comment, r.username " +
                    "from code c " +
                    "join repo r ON c.repoid = r.id " +
                    "join languagetype lt ON c.languagename = lt.id " +
                    "join source s ON s.id = r.sourceid " +
                    "where c.id in (" +
                    codeIdsString +
                    ") order by field(c.id, " +
                    codeIdsString +
                    ");");

            connStmtRs.rs = connStmtRs.stmt.executeQuery();

//            while (rs.next()) {
//                codeResultList.add(new SearchcodeSearchResult(
//                        rs.getInt("id"),
//                        rs.getString("filename"),
//                        rs.getString("location"),
//                        rs.getString("content"),
//                        rs.getString("hash"),
//                        rs.getString("reponame"),
//                        rs.getString("simhash"),
//                        rs.getInt("linescount"),
//                        rs.getString("languagetype"),
//                        rs.getString("sourcename"),
//                        rs.getString("sourceurl"),
//                        rs.getString("url"),
//                        rs.getInt("blank"),
//                        rs.getInt("comment"),
//                        rs.getString("username")
//                ));
//            }
        } catch (SQLException ex) {
            this.logger.severe(String.format("1eb33289::error in class %s exception %s searchcode unable to get code by ids %s", ex.getClass(), ex.getMessage(), String.join(", ", codeIds.stream().map(x -> x.toString()).collect(Collectors.toList()))));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return codeResultList;
    }

    public synchronized List<SearchcodeCodeResult> getCodeBetween(int start, int end) {
        List<SearchcodeCodeResult> codeResultList = new ArrayList<>(end - start);
        ConnStmtRs connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();
            connStmtRs.stmt = connStmtRs.conn.prepareStatement("SELECT c.id, c.repoid, c.filetypeid, c.languagename, r.sourceid, " +
                    "UNCOMPRESS(c.content) AS content, " +
                    "c.filename, " +
                    "c.linescount " +
                    "FROM code c " +
                    "JOIN repo r ON r.id = c.repoid " +
                    "WHERE c.id >= ? AND c.id <= ? AND c.deleted = 0 " +
                    "AND c.languagename not in (select id from languagetype where type in ('text', 'Unknown', 'xml', 'xaml', 'css', 'MSBuild scripts'))");
            connStmtRs.stmt.setInt(1, start);
            connStmtRs.stmt.setInt(2, end);

            connStmtRs.rs = connStmtRs.stmt.executeQuery();

            while (connStmtRs.rs.next()) {
                codeResultList.add(new SearchcodeCodeResult(
                        connStmtRs.rs.getInt("id"),
                        connStmtRs.rs.getInt("repoid"),
                        connStmtRs.rs.getInt("filetypeid"),
                        connStmtRs.rs.getInt("languagename"),
                        connStmtRs.rs.getInt("sourceid"),
                        connStmtRs.rs.getString("content"),
                        connStmtRs.rs.getString("filename"),
                        connStmtRs.rs.getInt("linescount")
                ));
            }
        } catch (SQLException ex) {
            this.logger.severe(String.format("e3492faa::error in class %s exception %s searchcode unable to get code between ids %d - %d", ex.getClass(), ex.getMessage(), start, end));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return codeResultList;
    }

    public Optional<SourceCodeDTO> getByCodeIndexDocument(CodeIndexDocument codeIndexDocument) {
        Optional<SourceCodeDTO> result = Optional.empty();
        ConnStmtRs connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();

            String query = "SELECT sourcecode.id, repoid, languageid, sourceid, ownerid, licenseid, location, filename, UNCOMPRESS(content) AS content, hash, simhash, linescount, data, languagetype.type as languagename" +
                    " FROM sourcecode" +
                    " INNER JOIN languagetype ON languagetype.id = sourcecode.languageid" +
                    //"repoid=? AND location=? AND filename=?";
                    " WHERE location=? AND filename=? LIMIT 1;";

            connStmtRs.stmt = connStmtRs.conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            connStmtRs.stmt.setString(1, this.getLocation(codeIndexDocument));
            connStmtRs.stmt.setString(2, codeIndexDocument.getFileName());

            connStmtRs.rs = connStmtRs.stmt.executeQuery();
            while (connStmtRs.rs.next()) {
                result = Optional.of(new SourceCodeDTO(
                        connStmtRs.rs.getInt("id"),
                        connStmtRs.rs.getInt("repoid"),
                        connStmtRs.rs.getInt("languageid"),
                        connStmtRs.rs.getString("languagename"),
                        connStmtRs.rs.getInt("sourceid"),
                        connStmtRs.rs.getInt("ownerid"),
                        connStmtRs.rs.getInt("licenseid"),
                        connStmtRs.rs.getString("location"),
                        connStmtRs.rs.getString("filename"),
                        connStmtRs.rs.getString("content"),
                        connStmtRs.rs.getString("hash"),
                        connStmtRs.rs.getString("simhash"),
                        connStmtRs.rs.getInt("linescount"),
                        connStmtRs.rs.getString("data")
                ));
            }

        } catch (SQLException ex) {
            this.logger.severe(String.format("f176335f::error in class %s exception %s searchcode unable to get code by index document location %s filename %s", ex.getClass(), ex.getMessage(), this.getLocation(codeIndexDocument), codeIndexDocument.getFileName()));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return result;
    }

    public Optional<SourceCodeDTO> getById(int id) {
        Optional<SourceCodeDTO> result = Optional.empty();
        ConnStmtRs connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();

            String query = "SELECT sourcecode.id, repoid, languageid, sourceid, ownerid, licenseid, location, filename, UNCOMPRESS(content) AS content, hash, simhash, linescount, data, languagetype.type as languagename" +
                    " FROM sourcecode" +
                    " INNER JOIN languagetype ON languagetype.id = sourcecode.languageid" +
                    " WHERE sourcecode.id=? LIMIT 1;";

            connStmtRs.stmt = connStmtRs.conn.prepareStatement(query);
            connStmtRs.stmt.setInt(1, id);

            connStmtRs.rs = connStmtRs.stmt.executeQuery();
            while (connStmtRs.rs.next()) {
                result = Optional.of(new SourceCodeDTO(
                        connStmtRs.rs.getInt("id"),
                        connStmtRs.rs.getInt("repoid"),
                        connStmtRs.rs.getInt("languageid"),
                        connStmtRs.rs.getString("languagename"),
                        connStmtRs.rs.getInt("sourceid"),
                        connStmtRs.rs.getInt("ownerid"),
                        connStmtRs.rs.getInt("licenseid"),
                        connStmtRs.rs.getString("location"),
                        connStmtRs.rs.getString("filename"),
                        connStmtRs.rs.getString("content"),
                        connStmtRs.rs.getString("hash"),
                        connStmtRs.rs.getString("simhash"),
                        connStmtRs.rs.getInt("linescount"),
                        connStmtRs.rs.getString("data")
                ));
            }

        } catch (SQLException ex) {
            this.logger.severe(String.format("a8ea57fb::error in class %s exception %s searchcode unable to get code by id %d", ex.getClass(), ex.getMessage(), id));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return result;
    }

    public SourceCodeDTO saveCode(CodeIndexDocument codeIndexDocument) {
        Optional<SourceCodeDTO> existing = this.getByCodeIndexDocument(codeIndexDocument);
        ConnStmtRs connStmtRs = new ConnStmtRs();

        try {
            connStmtRs.conn = this.dbConfig.getConnection();

            // If the language does not exist then create it
            Optional<LanguageTypeDTO> languageType = this.languageType.createLanguageType(codeIndexDocument.getLanguageName());

            String query = "INSERT INTO `sourcecode` (`id`, `repoid`, `languageid`, `sourceid`, `ownerid`, `licenseid`, `location`, `filename`, `content`, `hash`, `simhash`, `linescount`, `data`) VALUES " +
                    "(NULL, ?, ?, ?, ?, ?, ?, ?, COMPRESS(?), ?, ?, ?, ?)";

            // Why is this here and not above??
            if (existing.isPresent()) {
                return existing.get();
            }

            connStmtRs.stmt = connStmtRs.conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            connStmtRs.stmt.setInt(1, 31337);
            connStmtRs.stmt.setInt(2, languageType.get().getId());
            connStmtRs.stmt.setInt(3, 31337);
            connStmtRs.stmt.setInt(4, 31337);
            connStmtRs.stmt.setInt(5, 31337);
            connStmtRs.stmt.setString(6, this.getLocation(codeIndexDocument));
            connStmtRs.stmt.setString(7, codeIndexDocument.getFileName());
            connStmtRs.stmt.setString(8, codeIndexDocument.getContents());
            connStmtRs.stmt.setString(9, codeIndexDocument.getHash());
            connStmtRs.stmt.setString(10, "simhash");
            connStmtRs.stmt.setInt(11, codeIndexDocument.getLines());
            connStmtRs.stmt.setString(12, "{}");

            connStmtRs.stmt.execute();
            ResultSet tableKeys = connStmtRs.stmt.getGeneratedKeys();
            tableKeys.next();
            int autoGeneratedID = tableKeys.getInt(1);
            return this.getById(autoGeneratedID).get();
        } catch (SQLException ex) {
            this.logger.severe(String.format("4a1aa86d::error in class %s exception %s searchcode save code with name %s", ex.getClass(), ex.getMessage(), codeIndexDocument.getFileName()));
        } finally {
            this.helpers.closeQuietly(connStmtRs, this.dbConfig.closeConnection());
        }

        return null;
    }

    public String getLocation(CodeIndexDocument codeIndexDocument) {
        if (codeIndexDocument == null || codeIndexDocument.getDisplayLocation() == null || codeIndexDocument.getFileName() == null) {
            return Values.EMPTYSTRING;
        }

        return codeIndexDocument.getDisplayLocation().substring(0, codeIndexDocument.getDisplayLocation().length() - codeIndexDocument.getFileName().length());
    }
}
