package com.searchcode.app.service;


import com.searchcode.app.config.SphinxSearchConfig;
import com.searchcode.app.model.SearchcodeCodeResult;
import com.searchcode.app.util.SearchcodeLib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SearchcodeIndexer {

    public static synchronized void indexDocuments(List<SearchcodeCodeResult> codeResultList) {
        // Need to connect to each sphinx config eventually
        SphinxSearchConfig sphinxSearchConfig = new SphinxSearchConfig();
        Connection connection = null;
        try {
             connection = sphinxSearchConfig.getConnection();
        } catch (SQLException ex) {}

        for(SearchcodeCodeResult codeResult: codeResultList) {
            PreparedStatement stmt = null;

            try {
                stmt = connection.prepareStatement("REPLACE INTO codesearchrt1 VALUES(?,?,?,?,?,?,?,?,?)");

                //CodeIndexer.getIndexContents(Singleton.getSearchCodeLib(), codeResult.getContent());

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
                System.out.println(ex);
            }
        }
    }
}
