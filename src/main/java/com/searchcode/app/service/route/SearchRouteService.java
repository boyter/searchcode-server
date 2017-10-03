/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.12
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
        return this.getSearchResult(request, false);
    }

    public SearchResult literalCodeSearch(Request request, Response response) {
        return this.getSearchResult(request, true);
    }

    private SearchResult getSearchResult(Request request, boolean isLiteral) {
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
                catch (NumberFormatException ex) {
                    page = 0;
                }
            }

            String[] repos;
            String[] langs;
            String[] owners;
            String reposFilter = Values.EMPTYSTRING;
            String langsFilter = Values.EMPTYSTRING;
            String ownersFilter = Values.EMPTYSTRING;
            String filelocationFilter = Values.EMPTYSTRING;

            if (request.queryParams().contains("repo")) {
                repos = request.queryParamsValues("repo");

                if (repos.length != 0) {
                    List<String> reposList = Arrays.asList(repos).stream()
                            .map((s) -> Values.REPO_NAME_LITERAL + ":" + QueryParser.escape(Singleton.getHelpers().replaceForIndex(s)))
                            .collect(Collectors.toList());

                    reposFilter = " && (" + StringUtils.join(reposList, " || ") + ")";
                }
            }

            if (request.queryParams().contains("lan")) {
                langs = request.queryParamsValues("lan");

                if (langs.length != 0) {
                    List<String> langsList = Arrays.asList(langs).stream()
                            .map((s) -> Values.LANGUAGE_NAME_LITERAL + ":" + QueryParser.escape(Singleton.getHelpers().replaceForIndex(s)))
                            .collect(Collectors.toList());

                    langsFilter = " && (" + StringUtils.join(langsList, " || ") + ")";
                }
            }

            if (request.queryParams().contains("own")) {
                owners = request.queryParamsValues("own");

                if (owners.length != 0) {
                    List<String> ownersList = Arrays.asList(owners).stream()
                            .map((s) -> Values.OWNER_NAME_LITERAL + ":" + QueryParser.escape(Singleton.getHelpers().replaceForIndex(s)))
                            .collect(Collectors.toList());

                    ownersFilter = " && (" + StringUtils.join(ownersList, " || ") + ")";
                }
            }

            if (request.queryParams().contains("fl")) {
                filelocationFilter = " && (fl:" + Singleton.getHelpers().replaceNonAlphanumeric(request.queryParams("fl"), "_") + "*)";
            }

            String cleanQueryString = scl.formatQueryString(query);
            SearchResult searchResult;

            if (query.trim().startsWith("/") && query.trim().endsWith("/")) {
                isLiteral = true;
            }

            if (isLiteral) {
                searchResult = Singleton.getIndexService().search(query + reposFilter + langsFilter + ownersFilter + filelocationFilter, page);
            }
            else {
                searchResult = Singleton.getIndexService().search(cleanQueryString + reposFilter + langsFilter + ownersFilter + filelocationFilter, page);
            }

            searchResult.setCodeResultList(cm.formatResults(searchResult.getCodeResultList(), query, true));
            searchResult.setQuery(query);

            for (String altQuery: scl.generateAltQueries(query)) {
                searchResult.addAltQuery(altQuery);
            }

            // Null out code as it isnt required and there is no point in bloating our ajax requests
            searchResult.getCodeResultList().forEach(x -> x.setCode(null));

            return searchResult;
        }

        return null;
    }
}
