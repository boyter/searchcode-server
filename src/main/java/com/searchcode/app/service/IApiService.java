/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 */

package com.searchcode.app.service;

import com.searchcode.app.model.ApiResult;

import java.util.List;

public interface IApiService {
    boolean validateRequest(String publicKey, String hmac, String query);
    ApiResult createKeys();
    List<ApiResult> getApiResults();
    void deleteKey(String publicKey);
}
