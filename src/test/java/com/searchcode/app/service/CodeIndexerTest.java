package com.searchcode.app.service;

import com.searchcode.app.dto.CodeIndexDocument;
import junit.framework.TestCase;
import org.eclipse.jetty.util.ConcurrentArrayQueue;

import java.io.IOException;
import java.util.Queue;

public class CodeIndexerTest extends TestCase {
    public CodeIndexDocument codeIndexDocument = new CodeIndexDocument("repoLocationRepoNameLocationFilename", "repoName", "fileName", "fileLocation", "fileLocationFilename", "md5hash", "languageName", 100, "contents", "repoRemoteLocation", "codeOwner");

    public void testIndexDocument() throws IOException {
        CodeIndexer.indexDocument(codeIndexDocument);
    }

    // TODO actually assert something in here
    public void testIndexDocuments() throws IOException {
        Queue<CodeIndexDocument> queue = new ConcurrentArrayQueue<>();
        queue.add(codeIndexDocument);
        CodeIndexer.indexDocuments(queue);
    }

    // TODO actually assert something in here
    public void testDeleteByRepoName() throws IOException {
        CodeIndexer.deleteByReponame("repoName");
    }

    // TODO actually assert something in here
    public void testDeleteByFileLocationFilename() throws IOException {
        CodeIndexer.deleteByFileLocationFilename("fileLocationFilename");
    }

    // TODO fix the assert rather then programming by exception
    public void testIndexDocumentsEmptyIssue() {
        try {
            CodeIndexDocument cid = new CodeIndexDocument("repoLocationRepoNameLocationFilename", "", "fileName", "fileLocation", "fileLocationFilename", "md5hash", "languageName", 0, null, "repoRemoteLocation", "codeOwner");

            Queue queue = new ConcurrentArrayQueue<CodeIndexDocument>();
            queue.add(cid);

            CodeIndexer.indexDocuments(queue);
        }
        catch(Exception ex) {
            assertTrue(false);
        }
    }
}
