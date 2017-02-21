package com.searchcode.app.util;


import com.searchcode.app.dto.FileClassifierResult;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class FileFileClassifierResultTest extends TestCase {
    public void testSomething() {
        FileClassifier fileClassifier = new FileClassifier(new ArrayList<>());

        assertThat(fileClassifier.getDatabase()).isEmpty();
        List<FileClassifierResult> database = new ArrayList<>();

        database.add(new FileClassifierResult("", "", ""));
        fileClassifier.setDatabase(database);
        assertThat(fileClassifier.getDatabase().size()).isEqualTo(1);
    }
}
