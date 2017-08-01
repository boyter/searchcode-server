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


import com.searchcode.app.model.ApiResult;

import java.util.List;
import java.util.Optional;

public interface IApi {
    Optional<ApiResult> getApiByPublicKey(String publicKey);
    List<ApiResult> getAllApi();
    boolean saveApi(ApiResult apiResult);
    void deleteApiByPublicKey(String publicKey);
    void createTableIfMissing();
}
