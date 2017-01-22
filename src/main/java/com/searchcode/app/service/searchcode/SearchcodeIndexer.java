package com.searchcode.app.service.searchcode;


import com.searchcode.app.config.SphinxSearchConfig;
import com.searchcode.app.model.searchcode.SearchcodeCodeResult;
import com.searchcode.app.service.CodeIndexer;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class SearchcodeIndexer {

    public static synchronized void indexDocuments(List<SearchcodeCodeResult> codeResultList) {
        // Need to connect to each sphinx config eventually
        SphinxSearchConfig sphinxSearchConfig = new SphinxSearchConfig();
        Connection connection = null;
        PreparedStatement stmt = null;

        try {
             connection = sphinxSearchConfig.getConnection();
        } catch (SQLException ex) {
            return;
        }
        finally {
            Helpers.closeQuietly(connection);
        }

        try {
            for (SearchcodeCodeResult codeResult : codeResultList) {
                try {
                    stmt = connection.prepareStatement("REPLACE INTO codesearchrt1 VALUES(?,?,?,?,?,?,?,?,?)");

                    stmt.setInt(1, codeResult.getId());
                    stmt.setString(2, CodeIndexer.runCodeIndexPipeline(Singleton.getSearchCodeLib(), codeResult.getContent()));
                    stmt.setString(3, codeResult.getFilename());
                    stmt.setInt(4, codeResult.getRepoid());
                    stmt.setInt(5, codeResult.getFiletypeid());
                    stmt.setInt(6, codeResult.getLangugeid());
                    stmt.setInt(7, codeResult.getSourceid());
                    stmt.setInt(8, 0); //CCR
                    stmt.setInt(9, codeResult.getLinescount());
                    stmt.execute();
                } catch (SQLException ex) {
                    Singleton.getLogger().warning(ex.toString());
                }
            }
        }
        finally {
            Helpers.closeQuietly(stmt);
            Helpers.closeQuietly(connection);
        }
    }
}
