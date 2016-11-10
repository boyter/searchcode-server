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

        try {
            Connection conn = this.dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement("select id from code order by id desc limit 1;");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                maxId = rs.getInt(1);
            }

            stmt.close();
            conn.close();
        }
        catch(SQLException ex) {
            Singleton.getLogger().severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        return maxId;
    }

    public synchronized List<SearchcodeCodeResult> getCodeBetween(int start, int end) {

        List<SearchcodeCodeResult> codeResultList = new ArrayList<>(end - start);

        try {
            Connection conn = this.dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT c.id, c.repoid, c.filetypeid, c.languagename, r.sourceid, " +
                    "UNCOMPRESS(c.content) AS content, " +
                    "c.filename, " +
                    "c.linescount " +
                    "FROM code c " +
                    "JOIN repo r ON r.id = c.repoid " +
                    "WHERE c.id >= ? AND c.id <= ? AND c.deleted = 0 " +
                    "AND c.languagename not in (select id from languagetype where type in ('text', 'Unknown', 'xml', 'xaml', 'css', 'MSBuild scripts'))");
            stmt.setInt(1, start);
            stmt.setInt(2, end);

            ResultSet rs = stmt.executeQuery();

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

            stmt.close();
            conn.close();
        }
        catch(SQLException ex) {
            Singleton.getLogger().severe(" caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }

        return codeResultList;
    }

}
