package com.searchcode.app.jobs.repository;

import com.searchcode.app.dao.Data;
import com.searchcode.app.dto.CodeLinesReturn;
import com.searchcode.app.dto.IsMinifiedReturn;
import com.searchcode.app.dto.RepoData;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.Singleton;
import junit.framework.TestCase;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SearchcodeFleVisitorTest extends TestCase {
    public void testWalkFileTree() throws IOException {
        IndexBaseRepoJob mockIndexBase = mock(IndexFileRepoJob.class);
        when(mockIndexBase.isUpdated(any(), any())).thenReturn(true);
        RepoResult repoResult = mock(RepoResult.class);
        RepoData repoData = new RepoData();
        repoData.jobRunTime = Instant.MIN;

        when(repoResult.getData()).thenReturn(repoData);

        when(mockIndexBase.getCodeLines(any(), any())).thenReturn(new CodeLinesReturn(new ArrayList<String>(), null, false));
        when(mockIndexBase.getIsMinified(any(), any(), any())).thenReturn(new IsMinifiedReturn(false, null));
        when(mockIndexBase.getFileLocationFilename(any(), any())).thenReturn("");

        SearchcodeFileVisitor<Path> searchcodeFileVisitor = new SearchcodeFileVisitor<>(mockIndexBase, repoResult, "./", "");
        Files.walkFileTree( Paths.get("./"), EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, searchcodeFileVisitor);
    }
}
