/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.5
 */

package com.searchcode.app.service;

import com.searchcode.app.config.Values;
import com.searchcode.app.dto.api.ApiResponse;
import com.searchcode.app.util.Properties;
import spark.Request;
import spark.Response;

import java.net.URLEncoder;

public class ApiRouteService {

    public ApiResponse RepositoryReindex(Request request, Response response) {
        boolean apiEnabled = Boolean.parseBoolean(Properties.getProperties().getProperty("api_enabled", "false"));
        boolean apiAuth = Boolean.parseBoolean(Properties.getProperties().getProperty("api_key_authentication", "true"));
        ApiService apiService = Singleton.getApiService();
        JobService jobService = Singleton.getJobService();

        if (!apiEnabled) {
            return new ApiResponse(false, "API not enabled");
        }

        String publicKey = request.queryParams("pub");
        String signedKey = request.queryParams("sig");

        if (apiAuth) {
            if (publicKey == null || publicKey.trim().equals(Values.EMPTYSTRING)) {
                return new ApiResponse(false, "pub is a required parameter");
            }

            if (signedKey == null || signedKey.trim().equals(Values.EMPTYSTRING)) {
                return new ApiResponse(false, "sig is a required parameter");
            }

            String toValidate = String.format("pub=%s",
                    URLEncoder.encode(publicKey));

            boolean validRequest = apiService.validateRequest(publicKey, signedKey, toValidate);

            if (!validRequest) {
                return new ApiResponse(false, "invalid signed url");
            }
        }

        boolean result = jobService.rebuildAll();
        if (result) {
            jobService.forceEnqueue();
        }

        return new ApiResponse(result, "reindex forced");
    }

}
