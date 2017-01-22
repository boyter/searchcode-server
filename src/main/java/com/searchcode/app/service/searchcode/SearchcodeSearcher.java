/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.6
 */

package com.searchcode.app.service.searchcode;

import com.searchcode.app.config.SphinxSearchConfig;
import com.searchcode.app.util.Helpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SearchcodeSearcher {

    /**
     * Given a query and what page of results we are on return the matching results for that search
     */
    public List<Integer> search(String queryString, int page) {
        SphinxSearchConfig sphinxSearchConfig = new SphinxSearchConfig();

        List<Integer> results = new ArrayList<>();

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            connection = sphinxSearchConfig.getConnection();

            stmt = connection.prepareStatement("SELECT * FROM codesearchrt1 WHERE MATCH(?);");
            stmt.setString(1, queryString);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                results.add(id);
            }

        } catch (SQLException ex) {
            return results;
        }
        finally {
            Helpers.closeQuietly(resultSet);
            Helpers.closeQuietly(stmt);
            Helpers.closeQuietly(connection);
        }

        return results;
    }

}
