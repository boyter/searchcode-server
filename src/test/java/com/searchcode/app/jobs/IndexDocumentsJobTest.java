package com.searchcode.app.jobs;

import com.searchcode.app.jobs.repository.IndexDocumentsJob;
import junit.framework.TestCase;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class IndexDocumentsJobTest extends TestCase {

    public void testExecute() {
        IndexDocumentsJob indexDocumentsJob = new IndexDocumentsJob();
        assertThat(true).isFalse();
    }
}
