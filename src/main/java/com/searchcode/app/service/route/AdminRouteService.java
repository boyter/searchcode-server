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


import com.google.gson.Gson;
import com.searchcode.app.App;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Api;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dao.Repo;
import com.searchcode.app.dto.Source;
import com.searchcode.app.dto.Version;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.model.ValidatorResult;
import com.searchcode.app.service.*;
import com.searchcode.app.util.LoggerWrapper;
import com.searchcode.app.util.Properties;

import com.searchcode.app.util.RepositorySource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class AdminRouteService {

    private final Repo repo;
    private final Data data;
    private final JobService jobService;
    private final DataService dataService;
    private final IIndexService indexService;
    private final StatsService statsService;
    private final ValidatorService validatorService;
    private final RepositorySource repositorySource;
    private final LoggerWrapper loggerWrapper;

    public AdminRouteService() {
        this(Singleton.getRepo(),
             Singleton.getData(),
             Singleton.getJobService(),
             Singleton.getDataService(),
             Singleton.getIndexService(),
             Singleton.getStatsService(),
             Singleton.getValidatorService(),
             Singleton.getRepositorySource(),
             Singleton.getLogger());
    }


    public AdminRouteService(Repo repo, Data data, JobService jobService, DataService dataService, IIndexService indexService, StatsService statsService, ValidatorService validatorService, RepositorySource repositorySource, LoggerWrapper loggerWrapper) {
        this.repo = repo;
        this.data = data;
        this.jobService = jobService;
        this.dataService = dataService;
        this.indexService = indexService;
        this.statsService = statsService;
        this.validatorService = validatorService;
        this.repositorySource = repositorySource;
        this.loggerWrapper = loggerWrapper;
    }

    public String getStat(Request request, Response response) {
        if (request.queryParams().contains("statname")) {
            String statName = request.queryParams("statname");
            return this.getStat(statName);
        }

        return Values.EMPTYSTRING;
    }

    public String checkIndexStatus(Request request, Response response) {
        if (request.queryParams().contains("reponame")) {
            String reponame = request.queryParams("reponame");

            Optional<RepoResult> repoResult = Singleton.getRepo().getRepoByName(reponame);
            String indexStatus = repoResult.map(x -> x.getData().indexStatus).orElse(Values.EMPTYSTRING);

            if ("success".equals(indexStatus)) {
                return "Indexed ✓";
            }

            if (Singleton.getDataService().getPersistentDelete().contains(reponame)) {
                return "Pending Delete";
            }

            if (Singleton.getRunningIndexRepoJobs().keySet().contains(reponame)) {
                return "Indexing...";
            }

            return "Queued";
        }

        return Values.EMPTYSTRING;
    }

    public Map<String, Object> adminPage(Request request, Response response) {
        Map<String, Object> map = new HashMap<>();

        // Put all properties here
        map.put(Values.SQLITE_FILE, Properties.getProperties().getProperty(Values.SQLITE_FILE, Values.DEFAULT_SQLITE_FILE));
        map.put(Values.SERVER_PORT, Properties.getProperties().getProperty(Values.SERVER_PORT, Values.DEFAULT_SERVER_PORT));
        map.put(Values.REPOSITORYLOCATION, Properties.getProperties().getProperty(Values.REPOSITORYLOCATION, Values.DEFAULTREPOSITORYLOCATION));
        map.put(Values.INDEXLOCATION, Properties.getProperties().getProperty(Values.INDEXLOCATION, Values.DEFAULTINDEXLOCATION));
        map.put(Values.FACETSLOCATION, Properties.getProperties().getProperty(Values.FACETSLOCATION, Values.DEFAULTFACETSLOCATION));
        map.put(Values.CHECKREPOCHANGES, Properties.getProperties().getProperty(Values.CHECKREPOCHANGES, Values.DEFAULTCHECKREPOCHANGES));
        map.put(Values.CHECKFILEREPOCHANGES, Properties.getProperties().getProperty(Values.CHECKFILEREPOCHANGES, Values.DEFAULTCHECKFILEREPOCHANGES));
        map.put(Values.ONLYLOCALHOST, Properties.getProperties().getProperty(Values.ONLYLOCALHOST, Values.DEFAULTONLYLOCALHOST));
        map.put(Values.LOWMEMORY, Properties.getProperties().getProperty(Values.LOWMEMORY, Values.DEFAULTLOWMEMORY));
        map.put(Values.SPELLINGCORRECTORSIZE, Properties.getProperties().getProperty(Values.SPELLINGCORRECTORSIZE, Values.DEFAULTSPELLINGCORRECTORSIZE));
        map.put(Values.USESYSTEMGIT, Properties.getProperties().getProperty(Values.USESYSTEMGIT, Values.DEFAULTUSESYSTEMGIT));
        map.put(Values.GITBINARYPATH, Properties.getProperties().getProperty(Values.GITBINARYPATH, Values.DEFAULTGITBINARYPATH));
        map.put(Values.APIENABLED, Properties.getProperties().getProperty(Values.APIENABLED, Values.DEFAULTAPIENABLED));
        map.put(Values.APIKEYAUTH, Properties.getProperties().getProperty(Values.APIKEYAUTH, Values.DEFAULTAPIKEYAUTH));
        map.put(Values.SVNBINARYPATH, Properties.getProperties().getProperty(Values.SVNBINARYPATH, Values.DEFAULTSVNBINARYPATH));
        map.put(Values.SVNENABLED, Properties.getProperties().getProperty(Values.SVNENABLED, Values.DEFAULTSVNENABLED));
        map.put(Values.MAXDOCUMENTQUEUESIZE, Properties.getProperties().getProperty(Values.MAXDOCUMENTQUEUESIZE, Values.DEFAULTMAXDOCUMENTQUEUESIZE));
        map.put(Values.MAXDOCUMENTQUEUELINESIZE, Properties.getProperties().getProperty(Values.MAXDOCUMENTQUEUELINESIZE, Values.DEFAULTMAXDOCUMENTQUEUELINESIZE));
        map.put(Values.MAXFILELINEDEPTH, Properties.getProperties().getProperty(Values.MAXFILELINEDEPTH, Values.DEFAULTMAXFILELINEDEPTH));
        map.put(Values.OWASPDATABASELOCATION, Properties.getProperties().getProperty(Values.OWASPDATABASELOCATION, Values.DEFAULTOWASPDATABASELOCATION));
        map.put(Values.HIGHLIGHT_LINE_LIMIT, Properties.getProperties().getProperty(Values.HIGHLIGHT_LINE_LIMIT, Values.DEFAULT_HIGHLIGHT_LINE_LIMIT));
        map.put(Values.BINARY_WHITE_LIST, Properties.getProperties().getProperty(Values.BINARY_WHITE_LIST, Values.DEFAULT_BINARY_WHITE_LIST));
        map.put(Values.BINARY_BLACK_LIST, Properties.getProperties().getProperty(Values.BINARY_BLACK_LIST, Values.DEFAULT_BINARY_BLACK_LIST));
        map.put(Values.DIRECTORY_BLACK_LIST, Properties.getProperties().getProperty(Values.DIRECTORY_BLACK_LIST, Values.DEFAULT_DIRECTORY_BLACK_LIST));
        map.put(Values.NUMBER_GIT_PROCESSORS, Properties.getProperties().getProperty(Values.NUMBER_GIT_PROCESSORS, Values.DEFAULT_NUMBER_GIT_PROCESSORS));
        map.put(Values.NUMBER_SVN_PROCESSORS, Properties.getProperties().getProperty(Values.NUMBER_SVN_PROCESSORS, Values.DEFAULT_NUMBER_SVN_PROCESSORS));
        map.put(Values.NUMBER_FILE_PROCESSORS, Properties.getProperties().getProperty(Values.NUMBER_FILE_PROCESSORS, Values.DEFAULT_NUMBER_FILE_PROCESSORS));
        map.put(Values.AND_MATCH, Properties.getProperties().getProperty(Values.AND_MATCH, Values.DEFAULT_AND_MATCH));
        map.put(Values.LOG_INDEXED, Properties.getProperties().getProperty(Values.LOG_INDEXED, Values.DEFAULT_LOG_INDEXED));
        map.put(Values.TRASH_LOCATION, Properties.getProperties().getProperty(Values.TRASH_LOCATION, Values.DEFAULT_TRASH_LOCATION));
        map.put(Values.FOLLOW_LINKS, Properties.getProperties().getProperty(Values.FOLLOW_LINKS, Values.DEFAULT_FOLLOW_LINKS));
        map.put(Values.DEEP_GUESS_FILES, Properties.getProperties().getProperty(Values.DEEP_GUESS_FILES, Values.DEFAULT_DEEP_GUESS_FILES));
        map.put(Values.HOST_NAME, Properties.getProperties().getProperty(Values.HOST_NAME, Values.DEFAULT_HOST_NAME));
        map.put(Values.INDEX_ALL_FIELDS, Properties.getProperties().getProperty(Values.INDEX_ALL_FIELDS, Values.DEFAULT_INDEX_ALL_FIELDS));
        map.put(Values.INDEXTIME, Properties.getProperties().getProperty(Values.INDEXTIME, Values.DEFAULTINDEXTIME));

        map.put("repoCount", this.getStat("repoCount"));
        map.put("numDocs", this.getStat("numDocs"));
        map.put("numSearches", this.getStat("searchcount"));
        map.put("uptime", this.getStat("uptime"));
        map.put("loadAverage", this.getStat("loadAverage"));
        map.put("memoryUsage", this.getStat("memoryUsage"));
        map.put("currentdatetime", this.getStat("servertime"));
        map.put("spellingCount", this.getStat("spellingCount"));
        map.put("runningJobs", this.getStat("runningJobs"));
        map.put("threads", this.getStat("threads"));
        map.put("paused", this.getStat("paused"));
        map.put("repoQueueSize", this.getStat("repoqueuesize"));

        map.put("dataValues", this.data.getAllData());

        map.put("sysArch", this.statsService.getArch());
        map.put("sysVersion", this.statsService.getOsVersion());
        map.put("processorCount", this.statsService.getProcessorCount());
        map.put("deletionQueue", Singleton.getDataService().getPersistentDelete().size());
        map.put("version", App.VERSION);
        map.put("logoImage", CommonRouteService.getLogo());
        map.put("isCommunity", App.ISCOMMUNITY);
        map.put("index_paused", this.indexService.shouldPause(IIndexService.JobType.REPO_PARSER) ? "paused" : "running");
        map.put(Values.EMBED, Singleton.getData().getDataByName(Values.EMBED, Values.EMPTYSTRING));

        return map;
    }

    public Map<String, Object> adminRepo(Request request, Response response) {
        Map<String, Object> map = new HashMap<>();

        Repo repo = Singleton.getRepo();

        int repoCount = repo.getRepoCount();
        String offSet = request.queryParams("offset");
        String searchQuery = request.queryParams("q");
        int indexOffset = 0;

        if (offSet != null) {
            try {
                indexOffset = Integer.parseInt(offSet);
                if (indexOffset > repoCount || indexOffset < 0) {
                    indexOffset = 0;
                }
            }
            catch (NumberFormatException ex) {
                indexOffset = 0;
            }
        }

        if (searchQuery != null) {
            map.put("repoResults", repo.searchRepo(searchQuery));
        }
        else {
            map.put("repoResults", repo.getPagedRepo(indexOffset, 100));
        }

        map.put("searchQuery", searchQuery);
        map.put("hasPrevious", indexOffset > 0);
        map.put("hasNext", (indexOffset + 100) < repoCount);
        map.put("previousOffset", "" + (indexOffset - 100));
        map.put("nextOffset", "" + (indexOffset + 100));
        map.put("repoCount", this.getStat("repoCount"));

        map.put("logoImage", CommonRouteService.getLogo());
        map.put("isCommunity", App.ISCOMMUNITY);
        map.put(Values.EMBED, Singleton.getData().getDataByName(Values.EMBED, Values.EMPTYSTRING));

        map.put("repositorySource", this.repositorySource.loadDatabase().stream().map(Source::getName).collect(Collectors.toList()));

        return map;
    }

    public Map<String, Object> adminGetRepo(Request request, Response response) {
        Map<String, Object> map = new HashMap<>();
        String repoName = request.params(":reponame");
        Optional<RepoResult> repository = Singleton.getRepo().getRepoByName(repoName);

        repository.ifPresent(x -> map.put("repoResult", x));
        map.put("logoImage", CommonRouteService.getLogo());
        map.put("isCommunity", App.ISCOMMUNITY);
        map.put("repoCount", this.getStat("repoCount"));
        map.put(Values.EMBED, Singleton.getData().getDataByName(Values.EMBED, Values.EMPTYSTRING));
        map.put("repositorySource", this.repositorySource.loadDatabase().stream().map(Source::getName).collect(Collectors.toList()));

        return map;
    }

    public Map<String, Object> adminApi(Request request, Response response) {
        Map<String, Object> map = new HashMap<>();

        Api api = Singleton.getApi();

        map.put("apiKeys", api.getAllApi());

        boolean apiEnabled = Boolean.parseBoolean(Properties.getProperties().getProperty("api_enabled", "false"));
        boolean apiAuth = Boolean.parseBoolean(Properties.getProperties().getProperty("api_key_authentication", "true"));

        map.put("apiAuthentication", apiEnabled && apiAuth);
        map.put("logoImage", CommonRouteService.getLogo());
        map.put("isCommunity", App.ISCOMMUNITY);
        map.put(Values.EMBED, Singleton.getData().getDataByName(Values.EMBED, Values.EMPTYSTRING));
        map.put("repoCount", this.getStat("repoCount"));

        return map;
    }

    public Map<String, Object> adminSettings(Request request, Response response) {
        String[] highlighters = "agate,androidstudio,arta,ascetic,atelier-cave.dark,atelier-cave.light,atelier-dune.dark,atelier-dune.light,atelier-estuary.dark,atelier-estuary.light,atelier-forest.dark,atelier-forest.light,atelier-heath.dark,atelier-heath.light,atelier-lakeside.dark,atelier-lakeside.light,atelier-plateau.dark,atelier-plateau.light,atelier-savanna.dark,atelier-savanna.light,atelier-seaside.dark,atelier-seaside.light,atelier-sulphurpool.dark,atelier-sulphurpool.light,brown_paper,codepen-embed,color-brewer,dark,darkula,default,docco,far,foundation,github-gist,github,googlecode,grayscale,hopscotch,hybrid,idea,ir_black,kimbie.dark,kimbie.light,magula,mono-blue,monokai,monokai_sublime,obsidian,paraiso.dark,paraiso.light,pojoaque,railscasts,rainbow,school_book,solarized_dark,solarized_light,sunburst,tomorrow-night-blue,tomorrow-night-bright,tomorrow-night-eighties,tomorrow-night,tomorrow,vs,xcode,zenburn".split(",");

        Map<String, Object> map = new HashMap<>();

        map.put("logoImage", CommonRouteService.getLogo());
        map.put("syntaxHighlighter", CommonRouteService.getSyntaxHighlighter());
        map.put("averageSalary", Values.EMPTYSTRING + (int) CommonRouteService.getAverageSalary());
        map.put("matchLines", Values.EMPTYSTRING + (int) CommonRouteService.getMatchLines());
        map.put("maxLineDepth", Values.EMPTYSTRING + (int) CommonRouteService.getMaxLineDepth());
        map.put("minifiedLength", Values.EMPTYSTRING + (int) CommonRouteService.getMinifiedLength());
        map.put("owaspenabled", CommonRouteService.owaspAdvisoriesEnabled());
        map.put("backoffValue", CommonRouteService.getBackoffValue());
        map.put("embed", CommonRouteService.getEmbed());
        map.put("isCommunity", App.ISCOMMUNITY);
        map.put("highlighters", highlighters);
        map.put("repoCount", this.getStat("repoCount"));

        return map;
    }

    public Map<String, Object> adminLogs(Request request, Response response) {
        Map<String, Object> map = new HashMap<>();
        String level = Properties.getProperties().getOrDefault("log_level", "SEVERE").toString().toUpperCase();

        if (request.queryParams().contains("level") && !request.queryParams("level").trim().equals("")) {
            level = request.queryParams("level").trim().toUpperCase();
        }

        String logs;
        switch (level) {
            case "INFO":
                logs = this.getStat("infologs");
                break;
            case "WARNING":
                logs = this.getStat("warninglogs");
                break;
            case "ALL":
                logs = this.getStat("alllogs");
                break;
            case "SEARCH":
                logs = this.getStat("searchlogs");
                break;
            case "API":
                logs = this.getStat("apiLogs");
                break;
            case "FINE":
                logs = this.getStat("finelogs");
                break;
            case "SEVERE":
            default:
                logs = this.getStat("severelogs");
                break;
        }

        map.put("level", level);
        map.put("logs", logs);

        map.put("logoImage", CommonRouteService.getLogo());
        map.put("isCommunity", App.ISCOMMUNITY);
        map.put(Values.EMBED, Singleton.getData().getDataByName(Values.EMBED, Values.EMPTYSTRING));
        map.put("repoCount", this.getStat("repoCount"));

        return map;
    }

    public void postSettings(Request request, Response response) {
        Data data = Singleton.getData();

        String logo = request.queryParams("logo").trim();
        String syntaxHighlighter = request.queryParams("syntaxhighligher");
        String embed = request.queryParams("embed").trim();

        try {
            double averageSalary = Double.parseDouble(request.queryParams("averagesalary"));
            data.saveData(Values.AVERAGESALARY, "" + (int)averageSalary);
        }
        catch(NumberFormatException ex) {
            data.saveData(Values.AVERAGESALARY, Values.DEFAULTAVERAGESALARY);
        }

        try {
            double averageSalary = Double.parseDouble(request.queryParams("matchlines"));
            data.saveData(Values.MATCHLINES, "" + (int)averageSalary);
        }
        catch(NumberFormatException ex) {
            data.saveData(Values.MATCHLINES, Values.DEFAULTMATCHLINES);
        }

        try {
            double averageSalary = Double.parseDouble(request.queryParams("maxlinedepth"));
            data.saveData(Values.MAXLINEDEPTH, "" + (int)averageSalary);
        }
        catch(NumberFormatException ex) {
            data.saveData(Values.MAXLINEDEPTH, Values.DEFAULTMAXLINEDEPTH);
        }

        try {
            double minifiedlength = Double.parseDouble(request.queryParams("minifiedlength"));
            data.saveData(Values.MINIFIEDLENGTH, "" + (int)minifiedlength);
        }
        catch(NumberFormatException ex) {
            data.saveData(Values.MINIFIEDLENGTH, Values.DEFAULTMINIFIEDLENGTH);
        }

        try {
            double backoffValue = Double.parseDouble(request.queryParams("backoffValue"));
            data.saveData(Values.BACKOFFVALUE, "" + backoffValue);
        }
        catch(NumberFormatException ex) {
            data.saveData(Values.BACKOFFVALUE, Values.DEFAULTBACKOFFVALUE);
        }

        boolean owaspadvisories = Boolean.parseBoolean(request.queryParams("owaspadvisories"));
        data.saveData(Values.OWASPENABLED, "" + owaspadvisories);

        data.saveData(Values.LOGO, logo);
        data.saveData(Values.SYNTAXHIGHLIGHTER, syntaxHighlighter);
        data.saveData(Values.EMBED, embed);
    }

    public List<ValidatorResult> postBulk(Request request, Response response) {
        String repos = request.queryParams("repos");
        String repolines[] = repos.split("\\r?\\n");
        Repo repo = Singleton.getRepo();

        List<ValidatorResult> validatorResults = new ArrayList<>();

        for(String line: repolines) {
            String[] repoparams = line.split(",", -1);

            if (repoparams.length == 7) {
                String branch = repoparams[6].trim();
                if (branch.equals(Values.EMPTYSTRING)) {
                    branch = "master";
                }

                String scm = repoparams[1].trim().toLowerCase();
                if (scm.equals(Values.EMPTYSTRING)) {
                    scm = "git";
                }

                RepoResult repoResult = new RepoResult()
                        .setRowId(-1)
                        .setName(repoparams[0])
                        .setScm(scm)
                        .setUrl(repoparams[2])
                        .setUsername(repoparams[3])
                        .setPassword(repoparams[4])
                        .setSource(repoparams[5])
                        .setBranch(branch)
                        .setData("{}");

                ValidatorResult validate = this.validatorService.validate(repoResult, false);

                if (validate.isValid) {
                    repo.saveRepo(repoResult);
                    this.jobService.forceEnqueue(repoResult);
                }
                else {
                    validate.setLine(line);
                    validatorResults.add(validate);
                }
            }
            else {
                ValidatorResult validate = new ValidatorResult(false, "Incorrect number of elements: " + line);
                validate.setLine(line);
                validatorResults.add(validate);
            }
        }

        return validatorResults;
    }

    // TODO split out so we don't need the ignoreDuplicates param
    public ValidatorResult postRepo(Request request, Response response, boolean ignoreDuplicates) {
        String[] reponames = request.queryParamsValues("reponame");
        String[] reposcms = request.queryParamsValues("reposcm");
        String[] repourls = request.queryParamsValues("repourl");
        String[] repousername = request.queryParamsValues("repousername");
        String[] repopassword = request.queryParamsValues("repopassword");
        String[] reposource = request.queryParamsValues("reposource");
        String[] repobranch = request.queryParamsValues("repobranch");

        // Additional
        String[] source = request.queryParamsValues("source");
        String[] sourceuser = request.queryParamsValues("sourceuser");
        String[] sourceproject = request.queryParamsValues("sourceproject");


        ValidatorResult validate = new ValidatorResult(true, Values.EMPTYSTRING);

        for(int i = 0; i < reponames.length; i++) {

            String branch = repobranch[i].trim();
            if (branch.equals(Values.EMPTYSTRING)) {
                branch = "master";
            }

            RepoResult repoResult = new RepoResult()
                    .setRowId(-1)
                    .setName(reponames[i])
                    .setScm(reposcms[i])
                    .setUrl(repourls[i])
                    .setUsername(repousername[i])
                    .setPassword(repopassword[i])
                    .setSource(reposource[i])
                    .setBranch(branch)
                    .setData("{}");

            validate = this.validatorService.validate(repoResult, ignoreDuplicates);

            repoResult.getData().source = source[i];
            repoResult.getData().user = sourceuser[i];
            repoResult.getData().project = sourceproject[i];

            if (!validate.isValid) {
                validate.setRepoResult(repoResult);
                return validate;
            }

            this.repo.saveRepo(repoResult);
            Optional<RepoResult> repoByUrl = this.repo.getRepoByUrl(repourls[i]);
            repoByUrl.ifPresent(this.jobService::forceEnqueue);
        }

        return validate;
    }

    public void deleteRepo(Request request, Response response) {
        String repoName = request.queryParams("repoName");
        Optional<RepoResult> repoResult = this.repo.getRepoByName(repoName);

        repoResult.ifPresent(x -> this.dataService.addToPersistentDelete(x.getName()));
    }

    public void reindexRepo(Request request, Response response) {
        String repoName = request.queryParams("repoName");
        Optional<RepoResult> repoResult = this.repo.getRepoByName(repoName);

        repoResult.ifPresent(x ->  {
            x.getData().jobRunTime = Instant.parse("1800-01-01T00:00:00.000Z");
            this.loggerWrapper.info("Resetting Job Run Time due to reindex request repoName:" + x.getName());
            this.repo.saveRepo(x);
            this.jobService.forceEnqueue(x);
        });
    }

    public String checkVersion() {
        Version version;
        try {
            Gson gson = new Gson();
            String downloaded = IOUtils.toString(new URL("https://searchcodeserver.com/version.json"));
            version = gson.fromJson(downloaded, Version.class);
        }
        catch(Exception ex) {
            return "Unable to determine if running the latest version. Check https://searchcodeserver.com/pricing.html for the latest release.";
        }

        if (App.VERSION.equals(version.getVersion())) {
            return "Your searchcode server version " + App.VERSION + " is the latest.";
        }
        else {
            return "Your searchcode server version " + App.VERSION + " instance is out of date. The latest version is " + version.getVersion() + ".";
        }
    }

    public String getStat(String statName) {
        if (statName == null) {
            return Values.EMPTYSTRING;
        }

        switch (statName.toLowerCase()) {
            case "memoryusage":
                return this.statsService.getMemoryUsage("<br>");
            case "loadaverage":
                return this.statsService.getLoadAverage();
            case "uptime":
                return this.statsService.getUpTime();
            case "searchcount":
                return Values.EMPTYSTRING + this.statsService.getSearchCount();
            case "runningjobs":
                String collect = Singleton.getRunningIndexRepoJobs().keySet().stream()
                        .filter(x -> Singleton.getRunningIndexRepoJobs().get(x) != null)
                        .map(x -> x + " <small>(" + (Singleton.getHelpers().getCurrentTimeSeconds() - Singleton.getRunningIndexRepoJobs().get(x).startTime) + "s)</small>")
                        .collect(Collectors.joining("<br>"));
                return collect + "&nbsp;";
            case "spellingcount":
                return Values.EMPTYSTRING + Singleton.getSpellingCorrector().getWordCount();
            case "repocount":
                return Values.EMPTYSTRING + this.repo.getRepoCount();
            case "numdocs":
                return Values.EMPTYSTRING + this.indexService.getIndexedDocumentCount();
            case "servertime":
                return new Date().toString();
            case "deletionqueue":
                return Values.EMPTYSTRING + Singleton.getDataService().getPersistentDelete().size();
            case "alllogs":
                return StringUtils.join(this.loggerWrapper.getAllLogs(), System.lineSeparator());
            case "infologs":
                return StringUtils.join(this.loggerWrapper.getInfoLogs(), System.lineSeparator());
            case "warninglogs":
                return StringUtils.join(this.loggerWrapper.getWarningLogs(), System.lineSeparator());
            case "finelogs":
                return StringUtils.join(this.loggerWrapper.getFineLogs(), System.lineSeparator());
            case "severelogs":
                return StringUtils.join(this.loggerWrapper.getSevereLogs(), System.lineSeparator());
            case "searchlogs":
                return StringUtils.join(this.loggerWrapper.getSearchLogs(), System.lineSeparator());
            case "apilogs":
                return StringUtils.join(this.loggerWrapper.getApiLogs(), System.lineSeparator());
            case "threads":
                return Values.EMPTYSTRING + java.lang.Thread.activeCount();
            case "repoqueuesize":
                return Values.EMPTYSTRING + (Singleton.getUniqueGitRepoQueue().size() + Singleton.getUniqueSvnRepoQueue().size() + Singleton.getUniqueFileRepoQueue().size());
            case "parserpaused":
                return this.indexService.shouldPause(IIndexService.JobType.REPO_PARSER) ? Values.PAUSED : Values.RUNNING;
            case "adderpaused":
                return this.indexService.shouldPause(IIndexService.JobType.REPO_ADDER) ? Values.PAUSED : Values.RUNNING;
            case "indexreadlocation":
                return this.indexService.getProperty("index_read_location");
            case "indexwritelocation":
                return this.indexService.getProperty("index_write_location");
            case "facetwritelocation":
                return this.indexService.getProperty("facet_write_location  ");
            case "codeindexlinescount":
                return Values.EMPTYSTRING + this.indexService.getCodeIndexLinesCount();
            case "codeindexqueuesize":
                return Values.EMPTYSTRING + Singleton.getCodeIndexQueue().size();
        }

        return Values.EMPTYSTRING;
    }
}
