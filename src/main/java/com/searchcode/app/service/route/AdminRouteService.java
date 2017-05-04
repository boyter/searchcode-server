/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.10
 */

package com.searchcode.app.service.route;


import com.searchcode.app.App;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Api;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dao.Repo;
import com.searchcode.app.dto.RunningIndexJob;
import com.searchcode.app.jobs.repository.IndexBaseRepoJob;
import com.searchcode.app.jobs.repository.IndexFileRepoJob;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.*;
import com.searchcode.app.util.Properties;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AdminRouteService {

    private final Repo repo;
    private final JobService jobService;
    private final DataService dataService;

    public AdminRouteService() {
        this.repo = Singleton.getRepo();
        this.jobService = Singleton.getJobService();
        this.dataService = Singleton.getDataService();
    }

    public AdminRouteService(Repo repo, JobService jobService, DataService dataService) {
        this.repo = repo;
        this.jobService = jobService;
        this.dataService = dataService;
    }

    public String getStat(Request request, Response response) {
        if (request.queryParams().contains("statname")) {
            String statname = request.queryParams("statname");
            return this.getStat(statname);
        }

        return Values.EMPTYSTRING;
    }

    public String checkIndexStatus(Request request, Response response) {
        if (request.queryParams().contains("reponame")) {
            String reponame = request.queryParams("reponame");
            String reposLocation = Properties.getProperties().getProperty(Values.REPOSITORYLOCATION, Values.DEFAULTREPOSITORYLOCATION);

            IndexBaseRepoJob indexBaseRepoJob = new IndexFileRepoJob();

            RepoResult repoResult = Singleton.getRepo().getRepoByName(reponame);
            String indexStatus = Values.EMPTYSTRING;
            if (repoResult != null) {
                indexStatus = repoResult.getData().indexStatus;
            }

            if (indexBaseRepoJob.checkIndexSucess(reposLocation + "/" + reponame) || "success".equals(indexStatus)) {
                return "Indexed ✓";
            }

            if("indexing".equals(indexStatus)) {
                return "Indexing...";
            }
        }

        return Values.EMPTYSTRING;
    }

    public Map<String, Object> adminPage(Request request, Response response) {
        Map<String, Object> map = new HashMap<>();
        StatsService statsService = Singleton.getStatsService();

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


        map.put("sysArch", statsService.getArch());
        map.put("sysVersion", statsService.getOsVersion());
        map.put("processorCount", statsService.getProcessorCount());
        map.put("deletionQueue", Singleton.getDataService().getPersistentDelete().size());
        map.put("version", App.VERSION);
        map.put("logoImage", CommonRouteService.getLogo());
        map.put("isCommunity", App.ISCOMMUNITY);
        map.put("index_paused", Singleton.getPauseBackgroundJobs() ? "paused" : "running");

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
            catch(NumberFormatException ex) {
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

        map.put("logoImage", CommonRouteService.getLogo());
        map.put("isCommunity", App.ISCOMMUNITY);

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
        map.put("isCommunity", App.ISCOMMUNITY);
        map.put("highlighters", highlighters);

        return map;
    }

    public Map<String, Object> adminLogs(Request request, Response response) {
        Map<String, Object> map = new HashMap<>();
        String level = Properties.getProperties().getOrDefault("log_level", "SEVERE").toString().toUpperCase();

        if (request.queryParams().contains("level") && !request.queryParams("level").trim().equals("")) {
            level = request.queryParams("level").trim().toUpperCase();
        }

        String logs;
        switch(level) {
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
            case "SEVERE":
            default:
                logs = this.getStat("severelogs");
                break;
        }

        map.put("level", level);
        map.put("logs", logs);

        map.put("logoImage", CommonRouteService.getLogo());
        map.put("isCommunity", App.ISCOMMUNITY);

        return map;
    }

    public void postSettings(Request request, Response response) {
        Data data = Singleton.getData();

        String logo = request.queryParams("logo").trim();
        String syntaxHighlighter = request.queryParams("syntaxhighligher");

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

        // Redo anything that requires updates at this point
        Singleton.getSearchcodeLib(data);
    }

    public void postBulk(Request request, Response response) {
        String repos = request.queryParams("repos");
        String repolines[] = repos.split("\\r?\\n");
        Repo repo = Singleton.getRepo();

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

                RepoResult rr = repo.getRepoByName(repoparams[0]);

                if (rr == null) {
                    repo.saveRepo(new RepoResult(-1, repoparams[0], scm, repoparams[2], repoparams[3], repoparams[4], repoparams[5], branch, "{}"));
                    this.jobService.forceEnqueue(this.repo.getRepoByUrl(repoparams[3]));
                }
            }
        }
    }

    public void postRepo(Request request, Response response) {
        String[] reponames = request.queryParamsValues("reponame");
        String[] reposcms = request.queryParamsValues("reposcm");
        String[] repourls = request.queryParamsValues("repourl");
        String[] repousername = request.queryParamsValues("repousername");
        String[] repopassword = request.queryParamsValues("repopassword");
        String[] reposource = request.queryParamsValues("reposource");
        String[] repobranch = request.queryParamsValues("repobranch");

        for(int i=0;i<reponames.length; i++) {
            if (reponames[i].trim().length() != 0) {

                String branch = repobranch[i].trim();
                if (branch.equals(Values.EMPTYSTRING)) {
                    branch = "master";
                }

                this.repo.saveRepo(new RepoResult(-1, reponames[i], reposcms[i], repourls[i], repousername[i], repopassword[i], reposource[i], branch, "{}"));
                this.jobService.forceEnqueue(this.repo.getRepoByUrl(repourls[i]));
            }
        }
    }

    public void deleteRepo(Request request, Response response) {
        String repoName = request.queryParams("repoName");
        RepoResult rr = this.repo.getRepoByName(repoName);

        if (rr != null) {
            this.dataService.addToPersistentDelete(rr.getName());
        }
    }

    public String checkVersion() {
        String version;
        try {
            version = IOUtils.toString(new URL("https://searchcode.com/product/version/")).replace("\"", Values.EMPTYSTRING);
        }
        catch(IOException ex) {
            return "Unable to determine if running the latest version. Check https://searchcode.com/product/download/ for the latest release.";
        }

        if (App.VERSION.equals(version)) {
            return "Your searchcode server version " + version + " is the latest.";
        }
        else {
            return "Your searchcode server version " + App.VERSION + " instance is out of date. The latest version is " + version + ".";
        }
    }

    private String getStat(String statname) {
        if (statname == null) {
            return Values.EMPTYSTRING;
        }

        switch (statname.toLowerCase()) {
            case "memoryusage":
                return Singleton.getStatsService().getMemoryUsage("<br>");
            case "loadaverage":
                return Singleton.getStatsService().getLoadAverage();
            case "uptime":
                return Singleton.getStatsService().getUptime();
            case "searchcount":
                return Values.EMPTYSTRING + Singleton.getStatsService().getSearchCount();
            case "runningjobs":
                StringBuilder stringBuffer = new StringBuilder();
                for ( String key : Singleton.getRunningIndexRepoJobs().keySet() ) {
                    RunningIndexJob indexJob = Singleton.getRunningIndexRepoJobs().get(key);
                    if (indexJob != null) {
                        int runningTime = Singleton.getHelpers().getCurrentTimeSeconds() - indexJob.startTime;
                        stringBuffer.append(key).append(" <small>(").append(runningTime).append(" seconds)</small>").append(" ");
                    }
                    else {
                        stringBuffer.append(key).append(" ");
                    }
                }
                return stringBuffer.toString();
            case "spellingcount":
                return Values.EMPTYSTRING + Singleton.getSpellingCorrector().getWordCount();
            case "repocount":
                return Values.EMPTYSTRING + Singleton.getRepo().getRepoCount();
            case "numdocs":
                CodeSearcher codeSearcher = new CodeSearcher();
                return Values.EMPTYSTRING + codeSearcher.getTotalNumberDocumentsIndexed();
            case "servertime":
                return new Date().toString();
            case "deletionqueue":
                return Values.EMPTYSTRING + Singleton.getDataService().getPersistentDelete().size();
            case "alllogs":
                return StringUtils.join(Singleton.getLogger().getAllLogs(), System.lineSeparator());
            case "infologs":
                return StringUtils.join(Singleton.getLogger().getInfoLogs(), System.lineSeparator());
            case "warninglogs":
                return StringUtils.join(Singleton.getLogger().getWarningLogs(), System.lineSeparator());
            case "severelogs":
                return StringUtils.join(Singleton.getLogger().getSevereLogs(), System.lineSeparator());
            case "searchlogs":
                return StringUtils.join(Singleton.getLogger().getSearchLogs(), System.lineSeparator());
            case "threads":
                return "" + java.lang.Thread.activeCount();
            case "paused":
                return Singleton.getPauseBackgroundJobs() ? "paused": "running";
        }

        return Values.EMPTYSTRING;
    }
}
