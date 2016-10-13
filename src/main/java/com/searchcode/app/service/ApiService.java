/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.3
 */

package com.searchcode.app.service;


import com.google.inject.Inject;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.IApi;
import com.searchcode.app.model.ApiResult;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;

/**
 * Deals exclusively with API methods such as validation, creation of keys, deletion etc...
 */
public class ApiService implements IApiService {

    private IApi api = null;

    @Inject
    public ApiService(IApi api) {
        this.api = api;
    }

    /**
     * Validates a request made to the API against the public key supplied, the hmac supplied and the
     * query string itself.
     * http://stackoverflow.com/questions/11830338/web-api-creating-api-keys
     * http://stackoverflow.com/questions/6312544/hmac-sha1-how-to-do-it-properly-in-java
     * http://stackoverflow.com/questions/3208160/how-to-generate-an-hmac-in-java-equivalent-to-a-python-example?rq=1
     *
     */
    public boolean validateRequest(String publicKey, String hmac, String query) {
        ApiResult apiResult = this.api.getApiByPublicKey(publicKey);

        if (apiResult == null) {
            return false;
        }

        String myHmac = HmacUtils.hmacSha1Hex(apiResult.getPrivateKey(), query);
        return myHmac.equals(hmac);
    }

    /**
     * Creates a public and private key with access to the API
     * TODO allow creation of permissions for keys
     */
    public ApiResult createKeys() {
        String publicKey = "APIK-" + RandomStringUtils.randomAlphanumeric(27);
        String privateKey = RandomStringUtils.randomAlphanumeric(32);

        ApiResult apiResult = new ApiResult(-1, publicKey, privateKey, Values.EMPTYSTRING, Values.EMPTYSTRING);

        this.api.saveApi(apiResult);
        return apiResult;
    }

    /**
     * Returns all of the current API keys
     */
    public List<ApiResult> getApiResults() {
        return this.api.getAllApi();
    }

    /**
     * Deletes the public and private key from the system given the public key
     */
    public void deleteKey(String publicKey) {
        this.api.deleteApiByPublicKey(publicKey);
    }
}
