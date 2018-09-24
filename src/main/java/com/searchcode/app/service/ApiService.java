/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.service;


import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Api;
import com.searchcode.app.model.ApiResult;
import com.searchcode.app.util.Helpers;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;
import java.util.Optional;

/**
 * Deals exclusively with API methods such as validation, creation of keys, deletion etc...
 */
public class ApiService {

    public enum HmacType { SHA1, SHA512};

    private final Api api;
    private final Helpers helpers;

    public ApiService() {
        this(Singleton.getApi(), Singleton.getHelpers());
    }

    public ApiService(Api api, Helpers helpers) {
        this.api = api;
        this.helpers = helpers;
    }


    /**
     * Validates a request made to the API against the public key supplied, the hmac supplied and the
     * query string itself.
     * http://stackoverflow.com/questions/11830338/web-api-creating-api-keys
     * http://stackoverflow.com/questions/6312544/hmac-sha1-how-to-do-it-properly-in-java
     * http://stackoverflow.com/questions/3208160/how-to-generate-an-hmac-in-java-equivalent-to-a-python-example?rq=1
     *
     */
    public boolean validateRequest(String publicKey, String hmac, String query, HmacType hmacType) {
        Optional<ApiResult> apiResult = this.api.getApiByPublicKey(publicKey);

        if (this.helpers.isNullEmptyOrWhitespace(hmac)) {
            return false;
        }

        String myHmac;

        switch (hmacType) {
            case SHA512:
                myHmac = apiResult.map(x -> HmacUtils.hmacSha512Hex(x.getPrivateKey(), query)).orElse(Values.EMPTYSTRING);
                break;
            default:
                myHmac = apiResult.map(x -> HmacUtils.hmacSha1Hex(x.getPrivateKey(), query)).orElse(Values.EMPTYSTRING);
                break;
        }

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
