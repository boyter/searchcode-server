package com.searchcode.app.dto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ConnStmtRs {
    public Connection conn = null;
    public PreparedStatement stmt = null;
    public ResultSet rs = null;
}
