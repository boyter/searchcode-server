/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.11
 */

package com.searchcode.app.service.route;

import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Repo;
import com.searchcode.app.dto.ProjectStats;
import com.searchcode.app.dto.api.ApiResponse;
import com.searchcode.app.dto.api.RepoResultApiResponse;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.model.ValidatorResult;
import com.searchcode.app.service.*;
import com.searchcode.app.util.Properties;
import spark.Request;
import spark.Response;

import java.net.URLEncoder;
import java.util.List;

public class ApiRouteService {

    private final IApiService apiService;
    private final IJobService jobService;
    private final DataService dataService;
    private final Repo repo;
    private final ValidatorService validatorService;

    public boolean apiEnabled = Boolean.parseBoolean(Properties.getProperties().getProperty("api_enabled", "false"));
    public boolean apiAuth = Boolean.parseBoolean(Properties.getProperties().getProperty("api_key_authentication", "true"));

    public ApiRouteService() {
        this(Singleton.getApiService(), Singleton.getJobService(), Singleton.getRepo(), Singleton.getDataService(), Singleton.getValidatorService());
    }

    public ApiRouteService(IApiService apiService, IJobService jobService, Repo repo, DataService dataService, ValidatorService validatorService){
        this.apiService = apiService;
        this.jobService = jobService;
        this.repo = repo;
        this.dataService = dataService;
        this.validatorService = validatorService;
    }

    public ApiResponse repositoryReindex(Request request, Response response) {
        if (!this.apiEnabled) {
            return new ApiResponse(false, "API not enabled");
        }

        String publicKey = request.queryParams("pub");
        String signedKey = request.queryParams("sig");
        String hmacTypeString = request.queryParams("hmac");

        hmacTypeString = hmacTypeString == null ? Values.EMPTYSTRING : hmacTypeString;

        if (apiAuth) {
            if (publicKey == null || publicKey.trim().equals(Values.EMPTYSTRING)) {
                return new ApiResponse(false, "pub is a required parameter");
            }

            if (signedKey == null || signedKey.trim().equals(Values.EMPTYSTRING)) {
                return new ApiResponse(false, "sig is a required parameter");
            }

            String toValidate = String.format("pub=%s", URLEncoder.encode(publicKey));

            ApiService.HmacType hmacType = hmacTypeString.toLowerCase().equals("sha512") ? ApiService.HmacType.SHA512 : ApiService.HmacType.SHA1;
            boolean validRequest = apiService.validateRequest(publicKey, signedKey, toValidate, hmacType);

            if (!validRequest) {
                Singleton.getLogger().apiLog("Invalid signed repositoryReindex API call using publicKey=" + publicKey);
                return new ApiResponse(false, "invalid signed url");
            }
        }

        boolean result = this.jobService.rebuildAll();
        if (result) {
            this.jobService.forceEnqueue();
            Singleton.getLogger().apiLog("Valid signed repositoryReindex API call using publicKey=" + publicKey);
            return new ApiResponse(true, "reindex forced");
        }

        return new ApiResponse(false, "was unable to force the index");
    }

    public ApiResponse repositoryIndex(Request request, Response response) {
        if (!this.apiEnabled) {
            return new ApiResponse(false, "API not enabled");
        }

        String repoUrl = request.queryParams("repoUrl");
        RepoResult repoByUrl = this.repo.getRepoByUrl(repoUrl);

        if (repoByUrl != null) {
            this.jobService.forceEnqueue(repoByUrl);
            return new ApiResponse(true, "Enqueued repository " + repoUrl);
        }

        return new ApiResponse(false, "Was unable to find repository " + repoUrl);
    }

    public String getFileCount(Request request, Response response) {
        if (request.queryParams().contains("reponame")) {
            CodeSearcher codeSearcher = new CodeSearcher();
            ProjectStats projectStats = codeSearcher.getProjectStats(request.queryParams("reponame"));
            return Values.EMPTYSTRING + projectStats.getTotalFiles();
        }

        return Values.EMPTYSTRING;
    }

    public String getIndexTime(Request request, Response response) {
        if (request.queryParams().contains("reponame")) {
            RepoResult reponame = this.repo.getRepoByName(request.queryParams("reponame"));
            if (reponame == null) {
                return Values.EMPTYSTRING;
            }

            return Singleton.getHelpers().timeAgo(reponame.getData().jobRunTime);
        }

        return Values.EMPTYSTRING;
    }

    public String getAverageIndexTimeSeconds(Request request, Response response) {
        if (request.queryParams().contains("reponame")) {
            RepoResult reponame = this.repo.getRepoByName(request.queryParams("reponame"));
            if (reponame == null) {
                return Values.EMPTYSTRING;
            }

            return Values.EMPTYSTRING + reponame.getData().averageIndexTimeSeconds;
        }

        return Values.EMPTYSTRING;
    }

    public RepoResult getRepo(Request request, Response response) {
        if (request.queryParams().contains("reponame")) {
            RepoResult reponame = Singleton.getRepo().getRepoByName(request.queryParams("reponame"));
            if (reponame == null) {
                return null;
            }

            reponame.setUsername(null);
            reponame.setPassword(null);

            return reponame;
        }

        return null;
    }

    public RepoResultApiResponse repoList(Request request, Response response) {
        if (!this.apiEnabled) {
            return new RepoResultApiResponse(false, "API not enabled", null);
        }

        String publicKey = request.queryParams("pub");
        String signedKey = request.queryParams("sig");
        String hmacTypeString = request.queryParams("hmac");
        hmacTypeString = hmacTypeString == null ? Values.EMPTYSTRING : hmacTypeString;

        if (apiAuth) {
            if (publicKey == null || publicKey.trim().equals(Values.EMPTYSTRING)) {
                return new RepoResultApiResponse(false, "pub is a required parameter", null);
            }

            if (signedKey == null || signedKey.trim().equals(Values.EMPTYSTRING)) {
                return new RepoResultApiResponse(false, "sig is a required parameter", null);
            }

            String toValidate = String.format("pub=%s",
                    URLEncoder.encode(publicKey));

            ApiService.HmacType hmacType = hmacTypeString.toLowerCase().equals("sha512") ? ApiService.HmacType.SHA512 : ApiService.HmacType.SHA1;
            boolean validRequest = apiService.validateRequest(publicKey, signedKey, toValidate, hmacType);

            if (!validRequest) {
                Singleton.getLogger().apiLog("Invalid signed repoList API call using publicKey=" + publicKey);
                return new RepoResultApiResponse(false, "invalid signed url", null);
            }
        }

        List<RepoResult> repoResultList = repo.getAllRepo();

        Singleton.getLogger().apiLog("Valid signed repoList API call using publicKey=" + publicKey);
        return new RepoResultApiResponse(true, Values.EMPTYSTRING, repoResultList);
    }

    public ApiResponse repoDelete(Request request, Response response) {
        if (!apiEnabled) {
            return new ApiResponse(false, "API not enabled");
        }

        String publicKey = request.queryParams("pub");
        String signedKey = request.queryParams("sig");
        String reponames = request.queryParams("reponame");
        String hmacTypeString = request.queryParams("hmac");
        hmacTypeString = hmacTypeString == null ? Values.EMPTYSTRING : hmacTypeString;

        if (reponames == null || reponames.trim().equals(Values.EMPTYSTRING)) {
            return new ApiResponse(false, "reponame is a required parameter");
        }

        if (apiAuth) {
            if (publicKey == null || publicKey.trim().equals(Values.EMPTYSTRING)) {
                return new ApiResponse(false, "pub is a required parameter");
            }

            if (signedKey == null || signedKey.trim().equals(Values.EMPTYSTRING)) {
                return new ApiResponse(false, "sig is a required parameter");
            }

            String toValidate = String.format("pub=%s&reponame=%s",
                    URLEncoder.encode(publicKey),
                    URLEncoder.encode(reponames));

            ApiService.HmacType hmacType = hmacTypeString.toLowerCase().equals("sha512") ? ApiService.HmacType.SHA512 : ApiService.HmacType.SHA1;
            boolean validRequest = apiService.validateRequest(publicKey, signedKey, toValidate, hmacType);

            if (!validRequest) {
                Singleton.getLogger().apiLog("Invalid signed repoDelete API call using publicKey=" + publicKey);
                return new ApiResponse(false, "invalid signed url");
            }
        }

        RepoResult rr = this.repo.getRepoByName(reponames);
        if (rr == null) {
            return new ApiResponse(false, "repository already deleted");
        }

        this.dataService.addToPersistentDelete(rr.getName());

        Singleton.getLogger().apiLog("Valid signed repoDelete API call using publicKey=" + publicKey);
        return new ApiResponse(true, "repository queued for deletion");
    }

    public ApiResponse repoAdd(Request request, Response response) {
        if (!this.apiEnabled) {
            return new ApiResponse(false, "API not enabled");
        }

        String publicKey = request.queryParams("pub");
        String signedKey = request.queryParams("sig");
        String reponames = request.queryParams("reponame");
        String repourls = request.queryParams("repourl");
        String repotype = request.queryParams("repotype");
        String repousername = request.queryParams("repousername");
        String repopassword = request.queryParams("repopassword");
        String reposource = request.queryParams("reposource");
        String repobranch = request.queryParams("repobranch");
        String hmacTypeString = request.queryParams("hmac");
        hmacTypeString = hmacTypeString == null ? Values.EMPTYSTRING : hmacTypeString;

        if (reponames == null || reponames.trim().equals(Values.EMPTYSTRING)) {
            return new ApiResponse(false, "reponame is a required parameter");
        }

        if (repourls == null || repourls.trim().equals(Values.EMPTYSTRING)) {
            return new ApiResponse(false, "repourl is a required parameter");
        }

        if (repotype == null) {
            return new ApiResponse(false, "repotype is a required parameter");
        }

        if (repousername == null) {
            return new ApiResponse(false, "repousername is a required parameter");
        }

        if (repopassword == null) {
            return new ApiResponse(false, "repopassword is a required parameter");
        }

        if (reposource == null) {
            return new ApiResponse(false, "reposource is a required parameter");
        }

        if (repobranch == null) {
            return new ApiResponse(false, "repobranch is a required parameter");
        }

        if (apiAuth) {
            if (publicKey == null || publicKey.trim().equals(Values.EMPTYSTRING)) {
                return new ApiResponse(false, "pub is a required parameter");
            }

            if (signedKey == null || signedKey.trim().equals(Values.EMPTYSTRING)) {
                return new ApiResponse(false, "sig is a required parameter");
            }

            String toValidate = String.format("pub=%s&reponame=%s&repourl=%s&repotype=%s&repousername=%s&repopassword=%s&reposource=%s&repobranch=%s",
                    URLEncoder.encode(publicKey),
                    URLEncoder.encode(reponames),
                    URLEncoder.encode(repourls),
                    URLEncoder.encode(repotype),
                    URLEncoder.encode(repousername),
                    URLEncoder.encode(repopassword),
                    URLEncoder.encode(reposource),
                    URLEncoder.encode(repobranch));

            ApiService.HmacType hmacType = hmacTypeString.toLowerCase().equals("sha512") ? ApiService.HmacType.SHA512 : ApiService.HmacType.SHA1;
            boolean validRequest = apiService.validateRequest(publicKey, signedKey, toValidate, hmacType);

            if (!validRequest) {
                Singleton.getLogger().apiLog("Invalid signed repoAdd API call using publicKey=" + publicKey);
                return new ApiResponse(false, "invalid signed url");
            }
        }


        if (repobranch.trim().equals(Values.EMPTYSTRING)) {
            repobranch = "master";
        }

        repotype = repotype.trim().toLowerCase();
        if (!"git".equals(repotype) && !"svn".equals(repotype) && !"file".equals(repotype)) {
            repotype = "git";
        }

        RepoResult repoResult = this.repo.getRepoByName(reponames);

        if (repoResult != null) {
            return new ApiResponse(false, "repository name already exists");
        }

        RepoResult newRepoResult = new RepoResult(-1, reponames, repotype, repourls, repousername, repopassword, reposource, repobranch, "{}");

        ValidatorResult validate = this.validatorService.validate(newRepoResult);

        if (!validate.isValid) {
            return new ApiResponse(false, validate.reason);
        }

        this.repo.saveRepo(newRepoResult);

        Singleton.getLogger().apiLog("Valid signed repoAdd API call using publicKey=" + publicKey);
        return new ApiResponse(true, "added repository successfully");
    }
}
