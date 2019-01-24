package com.searchcode.app.jobs;

import com.searchcode.app.jobs.searchcode.ReindexerJob;
import com.searchcode.app.model.searchcode.SearchcodeCodeResult;
import junit.framework.TestCase;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class ReindexerJobTest extends TestCase {

    private final ReindexerJob reindexerJob;

    public ReindexerJobTest() {
        this.reindexerJob = new ReindexerJob();
    }

    public void testConvert() {
        var codeIndexDocument = reindexerJob.convert(new SearchcodeCodeResult(1, 2, 3, 4, 5, "something", "filename", 6));
        assertThat(codeIndexDocument.getId()).isEqualTo(1);
        assertThat(codeIndexDocument.getFileName()).isEqualTo("filename");
        assertThat(codeIndexDocument.getContents()).isEqualTo("something");
    }
}
