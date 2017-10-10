package com.searchcode.app.service;

import com.searchcode.app.dto.CodeIndexDocument;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SphinxIndexServiceTest extends TestCase {
    public void testSearch() {
        SphinxIndexService sphinxIndexService = new SphinxIndexService();

        List<String> someList = new ArrayList<>();

        for (int i=0; i < 100; i++) {
            someList.add("" + i);
        }

        someList.parallelStream()
                .forEach(x -> {
                    sphinxIndexService.search("test", 0);
                });
    }

    private CodeIndexDocument codeIndexDocument = new CodeIndexDocument("repoLocationRepoNameLocationFilename",
            "this is a repositoryname",
            "fileName",
            "fileLocation",
            "fileLocationFilename",
            "md5hash",
            "language name",
            100,
            "this is some content to search on test",
            "repoRemoteLocation",
            "owner",
            "mydisplaylocation");

//    public void testIndexDocumentEndToEnd() throws IOException {
//        SphinxIndexService sphinxIndexService = new SphinxIndexService();
//
//        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
//        queue.add(this.codeIndexDocument);
//        sphinxIndexService.indexDocument(queue);
//    }
}
