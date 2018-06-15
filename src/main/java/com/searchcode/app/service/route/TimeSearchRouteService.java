/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.14
 */

package com.searchcode.app.service.route;

import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dto.CodeResult;
import com.searchcode.app.dto.SearchResult;
import com.searchcode.app.service.CodeMatcher;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.service.TimeCodeSearcher;
import com.searchcode.app.util.SearchCodeLib;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains API Route logic
 */
public class TimeSearchRouteService {

    public TimeSearchRouteService() {
    }

    public SearchResult getTimeSearch(Request request, Response response) {
        Data data = Singleton.getData();

        SearchCodeLib scl = Singleton.getSearchcodeLib();
        TimeCodeSearcher cs = new TimeCodeSearcher();
        CodeMatcher cm = new CodeMatcher(data);

        response.header("Content-Encoding", "gzip");

        if (request.queryParams().contains("q") == false || Values.EMPTYSTRING.equals(request.queryParams("q").trim())) {
            return null;
        }

        String query = request.queryParams("q").trim();

        int page = this.getPage(request);

        String[] repos;
        String[] langs;
        String[] owners;
        String[] year;
        String[] yearmonth;
        String[] yearmonthday;
        String[] revisions;
        String[] deleted;

        String reposFilter = Values.EMPTYSTRING;
        String langsFilter = Values.EMPTYSTRING;
        String ownersFilter = Values.EMPTYSTRING;
        String yearFilter = Values.EMPTYSTRING;
        String yearMonthFilter = Values.EMPTYSTRING;
        String yearMonthDayFilter = Values.EMPTYSTRING;
        String revisionsFilter = Values.EMPTYSTRING;
        String deletedFilter = Values.EMPTYSTRING;

        if (request.queryParams().contains("repo")) {
            repos = request.queryParamsValues("repo");
            reposFilter = getRepos(repos, reposFilter);
        }

        if (request.queryParams().contains("lan")) {
            langs = request.queryParamsValues("lan");
            langsFilter = getLanguages(langs, langsFilter);
        }

        if (request.queryParams().contains("own")) {
            owners = request.queryParamsValues("own");
            ownersFilter = getOwners(owners, ownersFilter);
        }

        if (request.queryParams().contains("year")) {
            year = request.queryParamsValues("year");
            yearFilter = this.getYears(year, yearFilter);
        }

        if (request.queryParams().contains("ym")) {
            yearmonth = request.queryParamsValues("ym");
            yearMonthFilter = this.getYearMonths(yearmonth, yearMonthFilter);
        }

        if (request.queryParams().contains("ymd")) {
            yearmonthday = request.queryParamsValues("ymd");
            yearMonthDayFilter = this.getYearMonthDays(yearmonthday, yearMonthDayFilter);
        }

        if (request.queryParams().contains("rev")) {
            revisions = request.queryParamsValues("rev");
            revisionsFilter = this.getRevisions(revisions, revisionsFilter);
        }

        if (request.queryParams().contains("del")) {
            deleted = request.queryParamsValues("del");
            deletedFilter = this.getDeleted(deleted, deletedFilter);
        }

        // split the query escape it and and it together
        String cleanQueryString = scl.formatQueryString(query);

        SearchResult searchResult = cs.search(cleanQueryString + reposFilter + langsFilter + ownersFilter + yearFilter + yearMonthFilter + yearMonthDayFilter + revisionsFilter + deletedFilter, page);
        searchResult.setCodeResultList(cm.formatResults(searchResult.getCodeResultList(), query, true));

        searchResult.setQuery(query);

        this.getAltQueries(scl, query, searchResult);

        // Null out code as it isn't required and there is no point in bloating our ajax requests
        for(CodeResult codeSearchResult: searchResult.getCodeResultList()) {
            codeSearchResult.setCode(null);
        }

        return searchResult;
    }

    private int getPage(Request request) {
        int page = 0;

        if (request.queryParams().contains("p")) {
            try {
                page = Integer.parseInt(request.queryParams("p"));
                page = page > 19 ? 19 : page;
            }
            catch(NumberFormatException ex) {
                page = 0;
            }
        }
        return page;
    }

    private void getAltQueries(SearchCodeLib scl, String query, SearchResult searchResult) {
        for(String altQuery: scl.generateAltQueries(query)) {
            searchResult.addAltQuery(altQuery);
        }
    }

    private String getOwners(String[] owners, String ownersFilter) {
        if (owners.length != 0) {
            List<String> ownersList = Arrays.asList(owners).stream()
                    .map((s) -> Values.CODEOWNER + ":" + QueryParser.escape(s))
                    .collect(Collectors.toList());

            ownersFilter = " && (" + StringUtils.join(ownersList, " || ") + ")";
        }
        return ownersFilter;
    }

    private String getLanguages(String[] langs, String langsFilter) {
        if (langs.length != 0) {
            List<String> langsList = Arrays.asList(langs).stream()
                    .map((s) -> Values.LANGUAGENAME + ":" + QueryParser.escape(s))
                    .collect(Collectors.toList());

            langsFilter = " && (" + StringUtils.join(langsList, " || ") + ")";
        }
        return langsFilter;
    }

    private String getRepos(String[] repos, String reposFilter) {
        if (repos.length != 0) {
            List<String> reposList = Arrays.asList(repos).stream()
                    .map((s) -> Values.REPONAME + ":" + QueryParser.escape(s))
                    .collect(Collectors.toList());

            reposFilter = " && (" + StringUtils.join(reposList, " || ") + ")";
        }
        return reposFilter;
    }

    private String getYearMonthDays(String[] yearmonthday, String yearMonthDayFilter) {
        if (yearmonthday.length != 0) {
            List<String> reposList = Arrays.asList(yearmonthday).stream()
                    .map((s) -> Values.DATEYEARMONTHDAY + ":" + QueryParser.escape(s))
                    .collect(Collectors.toList());

            yearMonthDayFilter = " && (" + StringUtils.join(reposList, " || ") + ")";
        }
        return yearMonthDayFilter;
    }

    private String getYearMonths(String[] yearMonth, String yearMonthFilter) {
        if (yearMonth.length != 0) {
            List<String> reposList = Arrays.asList(yearMonth).stream()
                    .map((s) -> Values.DATEYEARMONTH + ":" + QueryParser.escape(s))
                    .collect(Collectors.toList());

            yearMonthFilter = " && (" + StringUtils.join(reposList, " || ") + ")";
        }
        return yearMonthFilter;
    }

    private String getYears(String[] year, String yearFilter) {
        if (year.length != 0) {
            List<String> reposList = Arrays.asList(year).stream()
                    .map((s) -> Values.DATEYEAR + ":" + QueryParser.escape(s))
                    .collect(Collectors.toList());

            yearFilter = " && (" + StringUtils.join(reposList, " || ") + ")";
        }
        return yearFilter;
    }

    private String getRevisions(String[] revisions, String revisionsFilter) {
        if (revisions.length != 0) {
            List<String> reposList = Arrays.asList(revisions).stream()
                    .map((s) -> Values.REVISION + ":" + QueryParser.escape(s))
                    .collect(Collectors.toList());

            revisionsFilter = " && (" + StringUtils.join(reposList, " || ") + ")";
        }
        return revisionsFilter;
    }

    private String getDeleted(String[] deleted, String deletedFilter) {
        if (deleted.length != 0) {
            List<String> reposList = Arrays.asList(deleted).stream()
                    .map((s) -> Values.DELETED + ":" + QueryParser.escape(s))
                    .collect(Collectors.toList());

            deletedFilter = " && (" + StringUtils.join(reposList, " || ") + ")";
        }
        return deletedFilter;
    }
}
