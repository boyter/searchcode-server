/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.searchcode.app.service.route;

import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Repo;
import com.searchcode.app.dto.ProjectStats;
import com.searchcode.app.dto.SearchResult;
import com.searchcode.app.dto.api.ApiResponse;
import com.searchcode.app.dto.api.RepoResultApiResponse;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.model.ValidatorResult;
import com.searchcode.app.service.*;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.Properties;
import spark.Request;
import spark.Response;

import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;

public class ApiRouteService {

    private final ApiService apiService;
    private final JobService jobService;
    private final DataService dataService;
    private final Repo repo;
    private final ValidatorService validatorService;
    private final IIndexService indexService;
    private final Helpers helpers;

    public boolean apiEnabled = Boolean.parseBoolean(Properties.getProperties().getProperty("api_enabled", "false"));
    public boolean apiAuth = Boolean.parseBoolean(Properties.getProperties().getProperty("api_key_authentication", "true"));

    public ApiRouteService() {
        this(Singleton.getApiService(), Singleton.getJobService(), Singleton.getRepo(), Singleton.getDataService(), Singleton.getValidatorService(), Singleton.getIndexService(), Singleton.getHelpers());
    }

    public ApiRouteService(ApiService apiService, JobService jobService, Repo repo, DataService dataService, ValidatorService validatorService, IIndexService indexService, Helpers helpers) {
        this.apiService = apiService;
        this.jobService = jobService;
        this.repo = repo;
        this.dataService = dataService;
        this.validatorService = validatorService;
        this.indexService = indexService;
        this.helpers = helpers;
    }

    public ApiResponse repositoryReindex(Request request, Response response) {
        if (!this.apiEnabled) {
            return new ApiResponse(false, "API not enabled");
        }

        String publicKey = request.queryParams(Values.PUB);
        String signedKey = request.queryParams(Values.SIG);
        String hmacTypeString = request.queryParams(Values.HMAC);

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

        this.indexService.reindexAll();
        Singleton.getLogger().apiLog("Valid signed repositoryReindex API call using publicKey=" + publicKey);
        return new ApiResponse(true, "reindex forced");
    }

    public ApiResponse repositoryIndex(Request request, Response response) {
        if (!this.apiEnabled) {
            return new ApiResponse(false, "API not enabled");
        }

        String repoUrl = request.queryParams("repoUrl");
        Optional<RepoResult> repoByUrl = this.repo.getRepoByUrl(repoUrl);

        if (repoByUrl.isPresent()) {
            this.jobService.forceEnqueue(repoByUrl.get());
            return new ApiResponse(true, "Enqueued repository " + repoUrl);
        }

        return new ApiResponse(false, "Was unable to find repository " + repoUrl);
    }

    public String getFileCount(Request request, Response response) {
        if (request.queryParams().contains("reponame")) {
            ProjectStats projectStats = Singleton.getIndexService().getProjectStats(request.queryParams("reponame"));
            return Values.EMPTYSTRING + projectStats.getTotalFiles();
        }

        return Values.EMPTYSTRING;
    }

    public String getIndexTime(Request request, Response response) {
        String indexTime = Values.EMPTYSTRING;

        if (request.queryParams().contains("reponame")) {
            Optional<RepoResult> repoResult = this.repo.getRepoByName(request.queryParams("reponame"));
            indexTime = repoResult.map(x -> Singleton.getHelpers().timeAgo(x.getData().jobRunTime)).orElse(Values.EMPTYSTRING);
        }

        return indexTime;
    }

    public String getAverageIndexTimeSeconds(Request request, Response response) {
        String averageIndexTimeSeconds = Values.EMPTYSTRING;

        if (request.queryParams().contains("reponame")) {
            Optional<RepoResult> repoResult = this.repo.getRepoByName(request.queryParams("reponame"));
            averageIndexTimeSeconds = repoResult.map(x -> Values.EMPTYSTRING + (x.getData().averageIndexTimeSeconds + 1)).orElse(Values.EMPTYSTRING);
        }

        return averageIndexTimeSeconds;
    }

    public RepoResult getRepo(Request request, Response response) {
        RepoResult repoResult = null;

        if (request.queryParams().contains("reponame")) {
            Optional<RepoResult> reponame = Singleton.getRepo().getRepoByName(request.queryParams("reponame"));

            repoResult = reponame.map(x -> {
                x.setUsername(null);
                x.setPassword(null);
                return x;
            }).orElse(null);
        }

        return repoResult;
    }

    public SearchResult repoTree(Request request, Response response) {

        if (request.queryParams().contains("reponame")) {
            return this.indexService.getProjectFileTree(request.queryParams("reponame"));
        }

        return null;
    }

    public RepoResultApiResponse repoList(Request request, Response response) {
        if (!this.apiEnabled) {
            return new RepoResultApiResponse(false, "API not enabled", null);
        }

        String publicKey = request.queryParams(Values.PUB);
        String signedKey = request.queryParams(Values.SIG);
        String hmacTypeString = request.queryParams(Values.HMAC);
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

        String publicKey = request.queryParams(Values.PUB);
        String signedKey = request.queryParams(Values.SIG);
        String reponames = request.queryParams(Values.REPONAME);
        String hmacTypeString = request.queryParams(Values.HMAC);
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

        Optional<RepoResult> repoResult = this.repo.getRepoByName(reponames);
        if (!repoResult.isPresent()) {
            return new ApiResponse(false, "repository already deleted");
        }

        repoResult.ifPresent(x -> this.dataService.addToPersistentDelete(x.getName()));

        Singleton.getLogger().apiLog("Valid signed repoDelete API call using publicKey=" + publicKey);
        return new ApiResponse(true, "repository queued for deletion");
    }

    public ApiResponse repoAdd(Request request, Response response) {
        if (!this.apiEnabled) {
            return new ApiResponse(false, "API not enabled");
        }

        Optional<String> publicKey = Optional.ofNullable(request.queryParams(Values.PUB));
        Optional<String> signedKey =  Optional.ofNullable(request.queryParams(Values.SIG));
        Optional<String> reponames =  Optional.ofNullable(request.queryParams(Values.REPONAME));
        Optional<String> repourls =  Optional.ofNullable(request.queryParams("repourl"));
        Optional<String> repotype =  Optional.ofNullable(request.queryParams("repotype"));
        Optional<String> repousername =  Optional.ofNullable(request.queryParams("repousername"));
        Optional<String> repopassword =  Optional.ofNullable(request.queryParams("repopassword"));
        Optional<String> reposource =  Optional.ofNullable(request.queryParams("reposource"));
        Optional<String> repobranch =  Optional.ofNullable(request.queryParams("repobranch"));
        Optional<String> hmacTypeString =  Optional.ofNullable(request.queryParams(Values.HMAC));

        // Optional
        Optional<String> source =  Optional.ofNullable(request.queryParams("source"));
        Optional<String> sourceuser =  Optional.ofNullable(request.queryParams("sourceuser"));
        Optional<String> sourceproject =  Optional.ofNullable(request.queryParams("sourceproject"));


        if (this.helpers.isNullEmptyOrWhitespace(reponames.orElse(Values.EMPTYSTRING))) {
            return new ApiResponse(false, "reponame is a required parameter");
        }

        if (this.helpers.isNullEmptyOrWhitespace(repourls.orElse(Values.EMPTYSTRING))) {
            return new ApiResponse(false, "repourl is a required parameter");
        }

        if (!repotype.isPresent()) {
            return new ApiResponse(false, "repotype is a required parameter");
        }

        if (!repousername.isPresent()) {
            return new ApiResponse(false, "repousername is a required parameter");
        }

        if (!repopassword.isPresent()) {
            return new ApiResponse(false, "repopassword is a required parameter");
        }

        if (!reposource.isPresent()) {
            return new ApiResponse(false, "reposource is a required parameter");
        }

        if (!repobranch.isPresent()) {
            return new ApiResponse(false, "repobranch is a required parameter");
        }

        if (this.apiAuth) {
            if (!publicKey.isPresent() || this.helpers.isNullEmptyOrWhitespace(publicKey.get())) {
                return new ApiResponse(false, "pub is a required parameter");
            }

            if (!signedKey.isPresent() || this.helpers.isNullEmptyOrWhitespace(signedKey.get())) {
                return new ApiResponse(false, "sig is a required parameter");
            }

            String toValidate1 = String.format("pub=%s&reponame=%s&repourl=%s&repotype=%s&repousername=%s&repopassword=%s&reposource=%s&repobranch=%s",
                    URLEncoder.encode(publicKey.orElse(Values.EMPTYSTRING)),
                    URLEncoder.encode(reponames.orElse(Values.EMPTYSTRING)),
                    URLEncoder.encode(repourls.orElse(Values.EMPTYSTRING)),
                    URLEncoder.encode(repotype.orElse(Values.EMPTYSTRING)),
                    URLEncoder.encode(repousername.orElse(Values.EMPTYSTRING)),
                    URLEncoder.encode(repopassword.orElse(Values.EMPTYSTRING)),
                    URLEncoder.encode(reposource.orElse(Values.EMPTYSTRING)),
                    URLEncoder.encode(repobranch.orElse(Values.EMPTYSTRING)));

            String toValidate2 = String.format("pub=%s&reponame=%s&repourl=%s&repotype=%s&repousername=%s&repopassword=%s&reposource=%s&repobranch=%s&source=%s&sourceuser=%s&sourceproject=%s",
                    URLEncoder.encode(publicKey.orElse(Values.EMPTYSTRING)),
                    URLEncoder.encode(reponames.orElse(Values.EMPTYSTRING)),
                    URLEncoder.encode(repourls.orElse(Values.EMPTYSTRING)),
                    URLEncoder.encode(repotype.orElse(Values.EMPTYSTRING)),
                    URLEncoder.encode(repousername.orElse(Values.EMPTYSTRING)),
                    URLEncoder.encode(repopassword.orElse(Values.EMPTYSTRING)),
                    URLEncoder.encode(reposource.orElse(Values.EMPTYSTRING)),
                    URLEncoder.encode(repobranch.orElse(Values.EMPTYSTRING)),
                    URLEncoder.encode(source.orElse(Values.EMPTYSTRING)),
                    URLEncoder.encode(sourceuser.orElse(Values.EMPTYSTRING)),
                    URLEncoder.encode(sourceproject.orElse(Values.EMPTYSTRING)));

            ApiService.HmacType hmacType = hmacTypeString.orElse(Values.EMPTYSTRING).toLowerCase().equals("sha512") ? ApiService.HmacType.SHA512 : ApiService.HmacType.SHA1;

            boolean validRequest = this.validateRequest(publicKey.orElse(Values.EMPTYSTRING), signedKey.orElse(Values.EMPTYSTRING), toValidate1, hmacType);

            if (!validRequest) {
                validRequest = this.validateRequest(publicKey.orElse(Values.EMPTYSTRING), signedKey.orElse(Values.EMPTYSTRING), toValidate2, hmacType);
            }

            if (!validRequest) {
                Singleton.getLogger().apiLog("Invalid signed repoAdd API call using publicKey=" + publicKey);
                return new ApiResponse(false, "invalid signed url");
            }
        }


        if (repobranch.orElse(Values.EMPTYSTRING).trim().equals(Values.EMPTYSTRING)) {
            repobranch = Optional.of("master");
        }

        String repoType = repotype.orElse(Values.EMPTYSTRING).trim().toLowerCase();
        if (!Values.GIT.equals(repoType) && !Values.SVN.equals(repoType) && !Values.FILE.equals(repoType)) {
            repotype = Optional.of(Values.GIT);
        }

        Optional<RepoResult> repoResult = this.repo.getRepoByName(reponames.orElse(Values.EMPTYSTRING));

        if (repoResult.isPresent()) {
            return new ApiResponse(false, "repository name already exists");
        }

        RepoResult newRepoResult = new RepoResult()
                .setRowId(-1)
                .setName(reponames.orElse(Values.EMPTYSTRING))
                .setScm(repotype.orElse(Values.EMPTYSTRING))
                .setUrl(repourls.orElse(Values.EMPTYSTRING))
                .setUsername(repousername.orElse(Values.EMPTYSTRING))
                .setPassword(repopassword.orElse(Values.EMPTYSTRING))
                .setSource(reposource.orElse(Values.EMPTYSTRING))
                .setBranch(repobranch.orElse(Values.EMPTYSTRING))
                .setData("{}");

        // Set optional fields
        newRepoResult.getData().source = source.orElse(Values.EMPTYSTRING);
        newRepoResult.getData().project = sourceproject.orElse(Values.EMPTYSTRING);
        newRepoResult.getData().user = sourceuser.orElse(Values.EMPTYSTRING);

        ValidatorResult validate = this.validatorService.validate(newRepoResult, false);

        if (!validate.isValid) {
            return new ApiResponse(false, validate.reason);
        }

        this.repo.saveRepo(newRepoResult);

        Singleton.getLogger().apiLog("Valid signed repoAdd API call using publicKey=" + publicKey);
        return new ApiResponse(true, "added repository successfully");
    }

    private boolean validateRequest(String publicKey, String signedKey, String toValidate, ApiService.HmacType hmacType) {
        boolean validRequest;

        validRequest = this.apiService.validateRequest(publicKey, signedKey, toValidate, hmacType);

        // Check both types of encoding of spaces even though one is wrong
        // https://github.com/boyter/searchcode-server/issues/134
        if (!validRequest) {
            toValidate = toValidate.replace("+", "%20");
            validRequest = this.apiService.validateRequest(publicKey, signedKey, toValidate, hmacType);
        }

        return validRequest;
    }
}
