package com.searchcode.app.util;

import com.searchcode.app.dto.FileClassifierResult;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class FileClassifierResultTest extends TestCase {

    public void testLoadDatabase() {
        FileClassifier fileClassifier = new FileClassifier();
        HashMap<String, FileClassifierResult> database = fileClassifier.getDatabase();
        assertThat(database.size()).isNotZero();
    }

    public void testDatabaseControl() {
        FileClassifier fileClassifier = new FileClassifier(new HashMap<>());
        assertThat(fileClassifier.getDatabase()).isEmpty();
        HashMap<String, FileClassifierResult> database = new HashMap<>();

        database.put("Unknown", new FileClassifierResult(""));
        fileClassifier.setDatabase(database);
        assertThat(fileClassifier.getDatabase().size()).isEqualTo(1);
    }

    public void testIdentifyLanguage() {
        HashMap<String, FileClassifierResult> database = new HashMap<>();
        database.put("boyterlang", new FileClassifierResult("boyter"));
        FileClassifier fileClassifier = new FileClassifier(database);

        String languageGuess = fileClassifier.languageGuesser("test.boyter", new ArrayList<>());
        assertThat(languageGuess).isEqualTo("boyterlang");
    }

//    public void testIdentifyLanguageAdditionalDots() {
//        List<FileClassifierResult> database = new ArrayList<>();
//        database.add(new FileClassifierResult("Typescript", "ts", ""));
//        database.add(new FileClassifierResult("Typings Definition", "d.ts", ""));
//
//        FileClassifier fileClassifier = new FileClassifier(database);
//        String languageGuess = fileClassifier.languageGuesser("test.d.ts", new ArrayList<>());
//
//        assertThat(languageGuess).isEqualTo("Typings Definition");
//    }

    public void testLanguageGuesserText() {
        FileClassifier fileClassifier = new FileClassifier();
        String language = fileClassifier.languageGuesser("test.txt", new ArrayList<>());
        assertEquals("Plain Text", language);
    }

    public void testLanguageGuesserXAML() {
        FileClassifier fileClassifier = new FileClassifier();
        String language = fileClassifier.languageGuesser("test.xml", new ArrayList<>());
        assertEquals("XML", language);
    }

    public void testLanguageGuesserHTML() {
        FileClassifier fileClassifier = new FileClassifier();
        String language = fileClassifier.languageGuesser("test.html", new ArrayList<>());
        assertEquals("HTML", language);
    }

    public void testLanguageGuesserUnknown() {
        FileClassifier fileClassifier = new FileClassifier();
        String language = fileClassifier.languageGuesser("test.shouldnotexist", new ArrayList<>());
        assertEquals("Unknown", language);
    }

    public void testLanguageGuesserNoExtension() {
        FileClassifier fileClassifier = new FileClassifier();
        String language = fileClassifier.languageGuesser("Jenkinsfile", new ArrayList<>());
        assertEquals("Jenkins Buildfile", language);
    }

    public void testLanguageGuesserKotlin() {
        FileClassifier fileClassifier = new FileClassifier();
        String language = fileClassifier.languageGuesser("test.kt", new ArrayList<>());
        assertEquals("Kotlin", language);
    }
}
