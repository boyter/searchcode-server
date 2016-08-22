/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 */


package com.searchcode.app.dao;

import com.searchcode.app.model.RepoResult;

import java.util.List;

public interface IData {
    String getDataByName(String name);
    boolean saveData(String key, String value);
    void createTableIfMissing();
}
