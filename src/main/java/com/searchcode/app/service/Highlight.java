package com.searchcode.app.service;

import com.google.gson.Gson;
import com.searchcode.app.dto.CodeResult;
import com.searchcode.app.dto.HighlighterRequest;
import com.searchcode.app.dto.HighlighterResponse;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;
import org.apache.commons.lang3.StringEscapeUtils;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Highlight {

    private final Helpers helpers;
    private final Gson gson;

    private final Cache<String, HighlighterResponse> highlightCache;
    private final String CachePrefix = "highlight.";
    private final LoggerWrapper logger;

    public Highlight() {
        this.helpers = Singleton.getHelpers();
        this.gson = new Gson();
        this.highlightCache = new Cache2kBuilder<String, HighlighterResponse>() {}
                .name("highlight")
                .expireAfterWrite(1, TimeUnit.DAYS)
                .entryCapacity(100000)
                .build();
        this.logger = Singleton.getLogger();
    }

    // TODO test this method quite a lot
    public HashMap<String, Object> highlightCodeResult(CodeResult codeResult) {
        var map = new HashMap<String, Object>();

        // If set to remote highlighter then post to it to render
        if (!this.helpers.isStandaloneInstance()) {
            try {
                highlightExternal(codeResult, map);
            } catch (Exception ex) {
                this.logger.severe(String.format("cf3292f9::error in class %s exception %s falling back to other highlighter", ex.getClass(), ex.getMessage()));
                highlightInternal(codeResult, map);
            }
        } else {
            highlightInternal(codeResult, map);
        }

        return map;
    }

    public void highlightInternal(CodeResult codeResult, HashMap<String, Object> map) {
        var codeLines = codeResult.code;
        var code = new StringBuilder();
        var lineNos = new StringBuilder();
        var padStr = new StringBuilder();

        for (int total = codeLines.size() / 10; total > 0; total = total / 10) {
            padStr.append(" ");
        }
        for (int i = 1, d = 10, len = codeLines.size(); i <= len; i++) {
            if (i / d > 0) {
                d *= 10;
                padStr = new StringBuilder(padStr.substring(0, padStr.length() - 1));  // Del last char
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

        map.put("linenos", lineNos.toString());
        map.put("codeValue", code.toString());
    }

    /**
     * Calls out to external application to produce the highlighted code
     * External application is https://github.com/boyter/searchcode-server-highlighter
     */
    public void highlightExternal(CodeResult codeResult, HashMap<String, Object> map) throws Exception {
        var cacheKey = CachePrefix + codeResult.getCodeId();
        var cacheResult = this.highlightCache.peekEntry(cacheKey);
        HighlighterResponse highlighter;

        if (cacheResult != null) {
            highlighter = cacheResult.getValue();
        } else {
            var highlighterRequest = this.gson.toJson(new HighlighterRequest()
                    .setContent(String.join("\n", codeResult.code))
                    .setFileName(codeResult.fileName)
                    .setStyle("monokai"));

            var res = this.helpers.sendPost("http://localhost:8089/v1/highlight/", highlighterRequest);
            highlighter = this.gson.fromJson(res, HighlighterResponse.class);
            this.highlightCache.put(cacheKey, highlighter);
        }

        map.put("chromaCss", highlighter.css);
        map.put("chromaHtml", highlighter.html);
    }
}
