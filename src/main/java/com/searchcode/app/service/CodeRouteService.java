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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.searchcode.app.App;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dao.Repo;
import com.searchcode.app.dto.CodeResult;
import com.searchcode.app.dto.OWASPMatchingResult;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.util.Cocomo2;
import com.searchcode.app.util.OWASPClassifier;
import com.searchcode.app.util.Properties;
import com.searchcode.app.util.SearchcodeLib;
import org.apache.commons.lang3.StringEscapeUtils;
import spark.Request;
import spark.Response;

import static spark.Spark.halt;

public class CodeRouteService {

    public Map<String, Object> getCode(Request request, Response response) {
        Map<String, Object> map = new HashMap<>();
        Repo repo = Singleton.getRepo();
        Data data = Singleton.getData();
        SearchcodeLib scl = Singleton.getSearchcodeLib(data);
        OWASPClassifier owaspClassifier = new OWASPClassifier();

        CodeSearcher cs = new CodeSearcher();
        Cocomo2 coco = new Cocomo2();

        String fileName = Values.EMPTYSTRING;
        if (request.splat().length != 0) {
            fileName = request.splat()[0];
        }

        String codeId = request.params(":codeid");
        CodeResult codeResult = cs.getByCodeId(codeId);

        if (codeResult == null) {
            response.redirect("/404/");
            halt();
        }

        List<String> codeLines = codeResult.code;
        StringBuilder code = new StringBuilder();
        StringBuilder lineNos = new StringBuilder();
        String padStr = "";
        for (int total = codeLines.size() / 10; total > 0; total = total / 10) {
            padStr += " ";
        }
        for (int i=1, d=10, len=codeLines.size(); i<=len; i++) {
            if (i/d > 0)
            {
                d *= 10;
                padStr = padStr.substring(0, padStr.length()-1);  // Del last char
            }
            code.append("<span id=\"")
                    .append(i)
                    .append("\"></span>")
                    .append(StringEscapeUtils.escapeHtml4(codeLines.get(i - 1)))
                    .append("\n");
            lineNos.append(padStr)
                    .append("<a href=\"#")
                    .append(i)
                    .append("\">")
                    .append(i)
                    .append("</a>")
                    .append("\n");
        }

        List<OWASPMatchingResult> owaspResults = new ArrayList<OWASPMatchingResult>();
        if (CommonRouteService.owaspAdvisoriesEnabled()) {
            if (!codeResult.languageName.equals("Text") && !codeResult.languageName.equals("Unknown")) {
                owaspResults = owaspClassifier.classifyCode(codeLines, codeResult.languageName);
            }
        }

        int limit = Integer.parseInt(
                Properties.getProperties().getProperty(
                        Values.HIGHLIGHT_LINE_LIMIT, Values.DEFAULT_HIGHLIGHT_LINE_LIMIT));
        boolean highlight = Integer.parseInt(codeResult.codeLines) <= limit;

        RepoResult repoResult = repo.getRepoByName(codeResult.repoName);

        if (repoResult != null) {
            map.put("source", repoResult.getSource());
        }

        map.put("fileName", codeResult.fileName);

        // TODO fix this properly code path includes the repo name and should be removed
        String codePath = codeResult.codePath.substring(codeResult.codePath.indexOf('/'), codeResult.codePath.length());
        if (!codePath.startsWith("/")) {
            codePath = "/" + codePath;
        }
        map.put("codePath", codePath);
        map.put("codeLength", codeResult.codeLines);

        map.put("linenos", lineNos.toString());

        map.put("languageName", codeResult.languageName);
        map.put("md5Hash", codeResult.md5hash);
        map.put("repoName", codeResult.repoName);
        map.put("highlight", highlight);
        map.put("repoLocation", codeResult.getRepoLocation());

        map.put("codeValue", code.toString());
        map.put("highligher", CommonRouteService.getSyntaxHighlighter());
        map.put("codeOwner", codeResult.getCodeOwner());
        map.put("owaspResults", owaspResults);

        double estimatedEffort = coco.estimateEffort(scl.countFilteredLines(codeResult.getCode()));
        int estimatedCost = (int)coco.estimateCost(estimatedEffort, CommonRouteService.getAverageSalary());
        if (estimatedCost != 0 && !scl.languageCostIgnore(codeResult.getLanguageName())) {
            map.put("estimatedCost", estimatedCost);
        }

        map.put("logoImage", CommonRouteService.getLogo());
        map.put("isCommunity", App.ISCOMMUNITY);

        return map;
    }
}
