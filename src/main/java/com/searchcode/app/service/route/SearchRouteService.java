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
import com.searchcode.app.dto.SearchResult;
import com.searchcode.app.service.CodeMatcher;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.SearchcodeLib;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.HashMap;
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

            HashMap<String, String[]> facets = new HashMap<>();

            String[] repos;
            String[] langs;
            String[] owners;

            String filelocationFilter = Values.EMPTYSTRING;

            if (request.queryParams().contains("repo")) {
                repos = request.queryParamsValues("repo");
                facets.put("repo", repos);
            }

            if (request.queryParams().contains("lan")) {
                langs = request.queryParamsValues("lan");
                facets.put("lan", langs);
            }

            if (request.queryParams().contains("own")) {
                owners = request.queryParamsValues("own");
                facets.put("own", owners);
            }

            // TODO determine if possible to move this into search
            if (request.queryParams().contains("fl")) {
                filelocationFilter = " && (fl:" + Singleton.getHelpers().replaceNonAlphanumeric(request.queryParams("fl"), "_") + "*)";
            }

            String cleanQueryString = scl.formatQueryString(query);
            SearchResult searchResult;

            if (query.trim().startsWith("/") && query.trim().endsWith("/")) {
                isLiteral = true;
            }

            if (isLiteral) {
                searchResult = Singleton.getIndexService().search(query + filelocationFilter, facets, page);
            }
            else {
                searchResult = Singleton.getIndexService().search(cleanQueryString + filelocationFilter, facets, page);
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
