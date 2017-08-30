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
import com.searchcode.app.dto.CodeResult;
import com.searchcode.app.dto.SearchResult;
import com.searchcode.app.service.CodeMatcher;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.SearchcodeLib;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SearchRouteService {

    public SearchResult codeSearch(Request request, Response response) {
        CodeMatcher cm = new CodeMatcher();
        SearchcodeLib scl = Singleton.getSearchcodeLib();

        if (request.queryParams().contains("q") && !request.queryParams("q").trim().equals(Values.EMPTYSTRING)) {
            String query = request.queryParams("q").trim();

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

            String[] repos;
            String[] langs;
            String[] owners;
            String reposFilter = Values.EMPTYSTRING;
            String langsFilter = Values.EMPTYSTRING;
            String ownersFilter = Values.EMPTYSTRING;

            if (request.queryParams().contains("repo")) {
                repos = request.queryParamsValues("repo");

                if (repos.length != 0) {
                    List<String> reposList = Arrays.asList(repos).stream()
                            .map((s) -> "reponame:" + QueryParser.escape(s.replace(" ", "_")))
                            .collect(Collectors.toList());

                    reposFilter = " && (" + StringUtils.join(reposList, " || ") + ")";
                }
            }

            if (request.queryParams().contains("lan")) {
                langs = request.queryParamsValues("lan");

                if (langs.length != 0) {
                    List<String> langsList = Arrays.asList(langs).stream()
                            .map((s) -> "languagename:" + QueryParser.escape(s.replace(" ", "_")))
                            .collect(Collectors.toList());

                    langsFilter = " && (" + StringUtils.join(langsList, " || ") + ")";
                }
            }

            if (request.queryParams().contains("own")) {
                owners = request.queryParamsValues("own");

                if (owners.length != 0) {
                    List<String> ownersList = Arrays.asList(owners).stream()
                            .map((s) -> "codeowner:" + QueryParser.escape(s.replace(" ", "_")))
                            .collect(Collectors.toList());

                    ownersFilter = " && (" + StringUtils.join(ownersList, " || ") + ")";
                }
            }

            // split the query escape it and and it together
            String cleanQueryString = scl.formatQueryString(query);

            SearchResult searchResult = Singleton.getIndexService().search(cleanQueryString + reposFilter + langsFilter + ownersFilter, page);
            searchResult.setCodeResultList(cm.formatResults(searchResult.getCodeResultList(), query, true));

            searchResult.setQuery(query);

            for(String altQuery: scl.generateAltQueries(query)) {
                searchResult.addAltQuery(altQuery);
            }

            // Null out code as it isnt required and there is no point in bloating our ajax requests
            for(CodeResult codeSearchResult: searchResult.getCodeResultList()) {
                codeSearchResult.setCode(null);
            }

            return searchResult;
        }

        return null;
    }

    public SearchResult literalCodeSearch(Request request, Response response) {
        CodeMatcher cm = new CodeMatcher();
        SearchcodeLib scl = Singleton.getSearchcodeLib();

        if (request.queryParams().contains("q") && !request.queryParams("q").trim().equals(Values.EMPTYSTRING)) {
            String query = request.queryParams("q").trim();

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

            String[] repos;
            String[] langs;
            String[] owners;
            String reposFilter = Values.EMPTYSTRING;
            String langsFilter = Values.EMPTYSTRING;
            String ownersFilter = Values.EMPTYSTRING;

            if (request.queryParams().contains("repo")) {
                repos = request.queryParamsValues("repo");

                if (repos.length != 0) {
                    List<String> reposList = Arrays.asList(repos).stream()
                            .map((s) -> "reponame:" + QueryParser.escape(s.replace(" ", "_")))
                            .collect(Collectors.toList());

                    reposFilter = " && (" + StringUtils.join(reposList, " || ") + ")";
                }
            }

            if (request.queryParams().contains("lan")) {
                langs = request.queryParamsValues("lan");

                if (langs.length != 0) {
                    List<String> langsList = Arrays.asList(langs).stream()
                            .map((s) -> "languagename:" + QueryParser.escape(s.replace(" ", "_")))
                            .collect(Collectors.toList());

                    langsFilter = " && (" + StringUtils.join(langsList, " || ") + ")";
                }
            }

            if (request.queryParams().contains("own")) {
                owners = request.queryParamsValues("own");

                if (owners.length != 0) {
                    List<String> ownersList = Arrays.asList(owners).stream()
                            .map((s) -> "codeowner:" + QueryParser.escape(s.replace(" ", "_")))
                            .collect(Collectors.toList());

                    ownersFilter = " && (" + StringUtils.join(ownersList, " || ") + ")";
                }
            }

            SearchResult searchResult = Singleton.getIndexService().search(query + reposFilter + langsFilter + ownersFilter, page);

            // TODO this is the difference between them both
            searchResult.setCodeResultList(cm.formatResults(searchResult.getCodeResultList(), query, true));
            searchResult.setQuery(query);

            for(String altQuery: scl.generateAltQueries(query)) {
                searchResult.addAltQuery(altQuery);
            }

            // Null out code as it isnt required and there is no point in bloating our ajax requests
            for(CodeResult codeSearchResult: searchResult.getCodeResultList()) {
                codeSearchResult.setCode(null);
            }

            return searchResult;
        }

        return null;
    }
}
