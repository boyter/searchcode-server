package com.searchcode.app.service.index;

import com.searchcode.app.config.Values;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.LoggerWrapper;
import com.searchcode.app.util.Properties;
import com.searchcode.app.util.SearchCodeLib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class IndexBaseService implements IIndexService {

    protected SearchCodeLib searchcodeLib;
    protected LoggerWrapper logger;
    protected List<String> indexAllFields; // Contains the fields that should be added to the all portion of the index
    protected final int PAGE_LIMIT, NO_PAGES_LIMIT;

    public IndexBaseService() {
        this.logger = Singleton.getLogger();
        this.indexAllFields = Arrays.asList(Properties.getProperties().getProperty(Values.INDEX_ALL_FIELDS, Values.DEFAULT_INDEX_ALL_FIELDS).split(","));
        this.searchcodeLib = Singleton.getSearchCodeLib();
        this.PAGE_LIMIT = 20;
        this.NO_PAGES_LIMIT = 20;
    }

    public IndexBaseService(SearchCodeLib searchCodeLib, LoggerWrapper logger) {
        this();
        this.searchcodeLib = searchCodeLib;
        this.logger = logger;
    }

    public String indexContentPipeline(CodeIndexDocument codeIndexDocument) {
        // This is the main pipeline for making code searchable and probably the most important
        // part of the indexer codebase
        StringBuilder indexBuilder = new StringBuilder();

        if (this.indexAllFields.contains("filename")) {
            indexBuilder.append(this.searchcodeLib.codeCleanPipeline(codeIndexDocument.getFileName())).append(" ");
        }
        if (this.indexAllFields.contains("filenamereverse")) {
            indexBuilder.append(new StringBuilder(codeIndexDocument.getFileName()).reverse().toString()).append(" ");
        }
        if (this.indexAllFields.contains("path")) {
            indexBuilder.append(this.searchcodeLib.splitKeywords(codeIndexDocument.getFileName(), true)).append(" ");
            indexBuilder.append(codeIndexDocument.getFileLocationFilename()).append(" ");
            indexBuilder.append(codeIndexDocument.getFileLocation()).append(" ");
        }
        if (this.indexAllFields.contains("content")) {
            indexBuilder.append(this.searchcodeLib.splitKeywords(codeIndexDocument.getContents(), true)).append(" ");
            indexBuilder.append( this.searchcodeLib.codeCleanPipeline(codeIndexDocument.getContents())).append(" ");
        }
        if (this.indexAllFields.contains("interesting")) {
            indexBuilder.append(this.searchcodeLib.findInterestingKeywords(codeIndexDocument.getContents())).append(" ");
            indexBuilder.append(this.searchcodeLib.findInterestingCharacters(codeIndexDocument.getContents()));
        }

        return indexBuilder.toString();
    }

    /**
     * Calculate the number of pages which can be searched through
     * TODO this needs more attention and testing on it as the results seem bizzare
     */
    protected List<Integer> calculatePages(int numTotalHits, int noPages) {
        var pages = new ArrayList<Integer>();
        if (numTotalHits != 0) {
            // Account for off by 1 errors
            if (numTotalHits % 10 == 0) {
                noPages -= 1;
            }

            for (var i = 0; i <= noPages; i++) {
                pages.add(i);
            }
        }
        return pages;
    }
}
