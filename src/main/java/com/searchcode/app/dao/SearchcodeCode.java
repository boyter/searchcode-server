/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.4
 */

package com.searchcode.app.dao;

import com.searchcode.app.config.IDatabaseConfig;
import com.searchcode.app.config.MySQLDatabaseConfig;
import com.searchcode.app.model.SearchcodeCodeResult;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SearchcodeCode {

    private IDatabaseConfig dbConfig;

    public SearchcodeCode() {
        this.dbConfig = new MySQLDatabaseConfig();
    }

    public SearchcodeCode(IDatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public synchronized int getMaxId() {
        int maxId = 0;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = this.dbConfig.getConnection();
            stmt = conn.prepareStatement("select id from code order by id desc limit 1;");
            rs = stmt.executeQuery();

            while (rs.next()) {
                maxId = rs.getInt(1);
            }
        }
        catch(SQLException ex) {
            Singleton.getLogger().severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            Helpers.closeQuietly(rs);
            Helpers.closeQuietly(stmt);
            Helpers.closeQuietly(conn);
        }

        return maxId;
    }

    public synchronized List<SearchcodeCodeResult> getByids(List<Integer> codeIds) {
        List<SearchcodeCodeResult> codeResultList = new ArrayList<>();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = this.dbConfig.getConnection();
            stmt = conn.prepareStatement("select c.id,c.filename,c.location,\n" +
                    "        substring(uncompress(c.content),1,500000) as content,\n" +
                    "        c.hash, r.name, c.simhash, c.linescount,\n" +
                    "                lt.type as languagetype,\n" +
                    "        s.name, r.sourceurl, r.url, c.blank, c.comment, r.username\n" +
                    "        from code c\n" +
                    "        join repo r ON c.repoid = r.id\n" +
                    "        join languagetype lt ON c.languagename = lt.id\n" +
                    "        join source s ON s.id = r.sourceid\n" +
                    "        where c.id in (?) order by field(c.id, ?);");

            StringBuffer stringBuffer = new StringBuffer();
            for (Integer codeId: codeIds) {
                stringBuffer.append(codeId).append(",");
            }
            String codeIdsString = StringUtils.substring(stringBuffer.toString(), 0, -1);

            stmt.setString(1, codeIdsString);
            stmt.setString(2, codeIdsString);

            rs = stmt.executeQuery();

            while (rs.next()) {
                codeResultList.add(new SearchcodeCodeResult(
                        rs.getInt("id"),
                        rs.getInt("repoid"),
                        rs.getInt("filetypeid"),
                        rs.getInt("languagename"),
                        rs.getInt("sourceid"),
                        rs.getString("content"),
                        rs.getString("filename"),
                        rs.getInt("linescount")
                ));
            }
        }
        catch(SQLException ex) {
            Singleton.getLogger().severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            Helpers.closeQuietly(rs);
            Helpers.closeQuietly(stmt);
            Helpers.closeQuietly(conn);
        }

        return codeResultList;
    }

    public synchronized List<SearchcodeCodeResult> getCodeBetween(int start, int end) {

        List<SearchcodeCodeResult> codeResultList = new ArrayList<>(end - start);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = this.dbConfig.getConnection();
            stmt = conn.prepareStatement("SELECT c.id, c.repoid, c.filetypeid, c.languagename, r.sourceid, " +
                    "UNCOMPRESS(c.content) AS content, " +
                    "c.filename, " +
                    "c.linescount " +
                    "FROM code c " +
                    "JOIN repo r ON r.id = c.repoid " +
                    "WHERE c.id >= ? AND c.id <= ? AND c.deleted = 0 " +
                    "AND c.languagename not in (select id from languagetype where type in ('text', 'Unknown', 'xml', 'xaml', 'css', 'MSBuild scripts'))");
            stmt.setInt(1, start);
            stmt.setInt(2, end);

            rs = stmt.executeQuery();

            while (rs.next()) {
                codeResultList.add(new SearchcodeCodeResult(
                        rs.getInt("id"),
                        rs.getInt("repoid"),
                        rs.getInt("filetypeid"),
                        rs.getInt("languagename"),
                        rs.getInt("sourceid"),
                        rs.getString("content"),
                        rs.getString("filename"),
                        rs.getInt("linescount")
                ));
            }
        }
        catch(SQLException ex) {
            Singleton.getLogger().severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            Helpers.closeQuietly(rs);
            Helpers.closeQuietly(stmt);
            Helpers.closeQuietly(conn);
        }

        return codeResultList;
    }

}
