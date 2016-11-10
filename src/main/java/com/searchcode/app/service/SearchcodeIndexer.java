package com.searchcode.app.service;


import com.searchcode.app.config.SphinxSearchConfig;
import com.searchcode.app.model.SearchcodeCodeResult;
import com.searchcode.app.util.SearchcodeLib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class SearchcodeIndexer {

    public static synchronized void indexDocuments(List<SearchcodeCodeResult> codeResultList) {
        SearchcodeLib scl = Singleton.getSearchCodeLib();


        // Need to connect to each sphinx config eventually
        SphinxSearchConfig sphinxSearchConfig = new SphinxSearchConfig();
        Connection connection = null;
        try {
             connection = sphinxSearchConfig.getConnection();
        } catch (SQLException ex) {}

        for(SearchcodeCodeResult codeResult: codeResultList) {
            PreparedStatement stmt = null;

            try {
                stmt = connection.prepareStatement("INSERT INTO codesearchrt1 VALUES(?,?,?,?,?,?,?,?);");
                stmt.setInt(1, codeResult.getId());
                stmt.setInt(2, codeResult.getRepoid());
                stmt.setInt(3, codeResult.getLangugeid());
                stmt.setInt(4, codeResult.getSourceid());
                stmt.setString(5, codeResult.getContent());
                stmt.setString(6, codeResult.getFilename());
                stmt.setInt(7, 0); //CCR
                stmt.setInt(8, codeResult.getLinescount());
                stmt.execute();
            } catch (SQLException ex) {}
        }
    }
}
