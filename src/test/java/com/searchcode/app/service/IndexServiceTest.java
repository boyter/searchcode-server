package com.searchcode.app.service;

import com.searchcode.app.config.Values;
import com.searchcode.app.dto.CodeIndexDocument;
import com.searchcode.app.dto.CodeResult;
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
    private CodeIndexDocument codeIndexDocument = new CodeIndexDocument("repoLocationRepoNameLocationFilename", "repoName", "fileName", "fileLocation", "fileLocationFilename", "md5hash", "languageName", 100, "contents", "repoRemoteLocation", "codeOwner");

    public void testIndexDocumentEndToEnd() throws IOException {
        this.indexService = new IndexService();

        Queue<CodeIndexDocument> queue = new ConcurrentLinkedQueue<>();
        queue.add(this.codeIndexDocument);

        this.indexService.indexDocument(queue);

        String codeId = "b9cc3f33794cad323047b4e982e8b3849b7422a8";
        CodeResult codeResult = this.indexService.getCodeResultByCodeId(codeId);
        assertThat(codeResult.getCodeId()).isEqualTo(codeId);
        this.indexService.deleteByCodeId(codeId);

        codeResult = this.indexService.getCodeResultByCodeId(codeId);
        assertThat(codeResult).isNull();
    }


    public void testDeleteByCodeId() {
        this.indexService = new IndexService();
        try {
            this.indexService.deleteByCodeId("this should not do anything but not blow up either");
        } catch (IOException ex) {
            assertThat(true).isFalse();
        }
    }

    public void testDeleteByRepoName() {
        this.indexService = new IndexService();
        try {
            this.indexService.deleteByRepo(new RepoResult());
        } catch (IOException ex) {
            assertThat(true).isFalse();
        }
    }


    public void testBuildDocument() {
        this.indexService = new IndexService();
        Document indexableFields = this.indexService.buildDocument(new CodeIndexDocument(
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

        AssertionsForClassTypes.assertThat(indexableFields.getFields().size()).isEqualTo(16);

        IndexableField[] fields = indexableFields.getFields(Values.REPONAME);
        AssertionsForClassTypes.assertThat(fields[0].stringValue()).isEqualTo("repo_Name");

        fields = indexableFields.getFields(Values.LANGUAGENAME);
        AssertionsForClassTypes.assertThat(fields[0].stringValue()).isEqualTo("language_Name");

        fields = indexableFields.getFields(Values.CODEOWNER);
        AssertionsForClassTypes.assertThat(fields[0].stringValue()).isEqualTo("code_Owner");

        // Verifies that we ran through the pipeline
        fields = indexableFields.getFields(Values.CONTENTS);
        AssertionsForClassTypes.assertThat(fields[0].stringValue()).isEqualTo(" filename filename filename filename filename filename  file name filelocationfilename filelocation contents contents contents contents contents contents");
    }
}
