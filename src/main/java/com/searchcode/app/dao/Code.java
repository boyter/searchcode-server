/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.11
 */


package com.searchcode.app.dao;

import com.searchcode.app.config.IDatabaseConfig;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Code {
    private final Helpers helpers;
    private final IDatabaseConfig dbConfig;
    private final LoggerWrapper logger;

    public Code() {
        this(Singleton.getDatabaseConfig(), Singleton.getHelpers(), Singleton.getLogger());
    }

    public Code(IDatabaseConfig dbConfig, Helpers helpers, LoggerWrapper logger) {
        this.dbConfig = dbConfig;
        this.helpers = helpers;
        this.logger = logger;
        this.createTableIfMissing();
    }

    private synchronized void createTableIfMissing() {

        Connection connection;
        PreparedStatement preparedStatement = null;

        try {
            connection = this.dbConfig.getConnection();
            preparedStatement = connection.prepareStatement("CREATE  TABLE  IF NOT EXISTS \"main\".\"code\" (\"reponame\" VARCHAR, \"filename\" VARCHAR, \"filelocation\" VARCHAR, \"filelocationfilename\" VARCHAR, \"md5hash\" VARCHAR, \"languagename\" VARCHAR, \"codelines\" INTEGER, \"repolocation\" VARCHAR, \"codeowner\" VARCHAR, \"codeid\" VARCHAR, \"schash\" VARCHAR);");
            preparedStatement.execute();
        }
        catch(SQLException ex) {
            this.logger.severe("ERROR - caught a " + ex.getClass() + "\n with message: " + ex.getMessage());
        }
        finally {
            this.helpers.closeQuietly(preparedStatement);
        }
    }
}
