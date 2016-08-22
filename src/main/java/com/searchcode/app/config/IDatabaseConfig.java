/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 */

package com.searchcode.app.config;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDatabaseConfig {
    public Connection getConnection() throws SQLException;
}
