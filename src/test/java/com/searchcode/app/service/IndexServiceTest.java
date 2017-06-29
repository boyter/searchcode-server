package com.searchcode.app.service;

import com.searchcode.app.config.Values;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.dto.CodeResult;
import com.searchcode.app.dto.SearchResult;
import com.searchcode.app.model.RepoResult;
import junit.framework.TestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.assertj.core.api.AssertionsForClassTypes;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class IndexServiceTest extends TestCase {

    private IndexService indexService = null;
    private String codeId = "b9cc3f33794cad323047b4e982e8b3849b7422a8";
    private String contents = "06e3e59f51894adea03c343910c26282";
    private String repoName = "b89bb20026ff426dae30ab92e1e59b19";
    private CodeIndexDocument codeIndexDocument = new CodeIndexDocument("repoLocationRepoNameLocationFilename",
            this.repoName,
            "fileName",
            "fileLocation",
            "fileLocationFilename",
            "md5hash",
            "languageName",
            100,
            this.contents,
            "repoRemoteLocation",
            "codeOwner");

    public void testIndexDocumentEndToEnd() throws IOException {
        this.indexService = new IndexService();


        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(this.codeIndexDocument);
        this.indexService.indexDocument(queue);

        CodeResult codeResult = this.indexService.getCodeResultByCodeId(this.codeId);
        assertThat(codeResult.getCodeId()).isEqualTo(this.codeId);

        this.indexService.deleteByCodeId(this.codeId);
        codeResult = this.indexService.getCodeResultByCodeId(this.codeId);
        assertThat(codeResult).isNull();
    }


    public void testDeleteByCodeId() throws IOException {
        this.indexService = new IndexService();
        this.indexService.deleteByCodeId("this should not do anything but not blow up either");
    }

    public void testDeleteByRepoName() throws IOException {
        this.indexService = new IndexService();

        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(this.codeIndexDocument);
        this.indexService.indexDocument(queue);

        CodeResult codeResult = this.indexService.getCodeResultByCodeId(this.codeId);
        assertThat(codeResult.getCodeId()).isEqualTo(this.codeId);

        RepoResult repoResult = new RepoResult();
        repoResult.setName(this.repoName);

        this.indexService.deleteByRepo(repoResult);

        codeResult = this.indexService.getCodeResultByCodeId(this.codeId);
        assertThat(codeResult).isNull();
    }

    public void testDeleteAll() throws IOException {
        this.indexService = new IndexService();

        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(this.codeIndexDocument);
        this.indexService.indexDocument(queue);

        CodeResult codeResult = this.indexService.getCodeResultByCodeId(this.codeId);
        assertThat(codeResult.getCodeId()).isEqualTo(this.codeId);

        this.indexService.deleteAll();

        codeResult = this.indexService.getCodeResultByCodeId(this.codeId);
        assertThat(codeResult).isNull();
    }


    public void testBuildDocument() {
        this.indexService = new IndexService();
        Document indexFields = this.indexService.buildDocument(new CodeIndexDocument(
                "repoLocationRepoNameLocationFilename",
                "repo Name",
                "fileName",
                "fileLocation",
                "fileLocationFilename",
                "md5hash",
                "language Name",
                10,
                "contents",
                "repoRemoteLocation",
                "code Owner"
        ));

        AssertionsForClassTypes.assertThat(indexFields.getFields().size()).isEqualTo(17);

        IndexableField[] fields = indexFields.getFields(Values.REPONAME);
        AssertionsForClassTypes.assertThat(fields[0].stringValue()).isEqualTo("repo_Name");

        fields = indexFields.getFields(Values.LANGUAGENAME);
        AssertionsForClassTypes.assertThat(fields[0].stringValue()).isEqualTo("language_Name");

        fields = indexFields.getFields(Values.CODEOWNER);
        AssertionsForClassTypes.assertThat(fields[0].stringValue()).isEqualTo("code_Owner");

        // Verifies that we ran through the pipeline
        fields = indexFields.getFields(Values.CONTENTS);
        AssertionsForClassTypes.assertThat(fields[0].stringValue()).isEqualTo(" filename filename filename filename filename filename  file name filelocationfilename filelocation contents contents contents contents contents contents");
    }

    public void testSearch() throws IOException {
        this.indexService = new IndexService();

        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(this.codeIndexDocument);
        this.indexService.indexDocument(queue);

        SearchResult contents = this.indexService.search(this.contents, 0);
        assertThat(contents.getTotalHits()).isNotZero();

        this.indexService.deleteByCodeId(this.codeId);
    }

    public void testSearchRepo() throws IOException {
        this.indexService = new IndexService();

        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(this.codeIndexDocument);
        this.indexService.indexDocument(queue);

        SearchResult contents = this.indexService.search("reponame:" + this.repoName, 0);
        assertThat(contents.getTotalHits()).isNotZero();
        assertThat(contents.getCodeResultList().get(0).codeId).isEqualTo(this.codeId);

        this.indexService.deleteByCodeId(this.codeId);
    }
}
