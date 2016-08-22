/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 */


package com.searchcode.app.dao;


import com.searchcode.app.model.ApiResult;

import java.util.List;

public interface IApi {
    ApiResult getApiByPublicKey(String publicKey);
    List<ApiResult> getAllApi();
    boolean saveApi(ApiResult apiResult);
    void deleteApiByPublicKey(String publicKey);
    void createTableIfMissing();
}
