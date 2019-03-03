package com.searchcode.app.util;

import com.searchcode.app.dto.FileClassifierResult;
import junit.framework.TestCase;
import org.assertj.core.api.AssertionsForClassTypes;

import java.util.HashMap;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class FileClassifierTest extends TestCase {
    private final FileClassifier fileClassifier;

    public FileClassifierTest() {
        this.fileClassifier = new FileClassifier();
    }

    public void testGetExtension() {
        String result = this.fileClassifier.getExtension("something.c");
        assertThat(result).isEqualTo("c");
    }

    public void testGetExtensionNoExtension() {
        String result = this.fileClassifier.getExtension("something");
        assertThat(result).isEqualTo("something");
    }

    public void testGetExtensionLeadingDot() {
        String result = this.fileClassifier.getExtension(".gitignore");
        assertThat(result).isEqualTo(".gitignore");
    }

    public void testGetExtensionMultiple() {
        String result = this.fileClassifier.getExtension("test.d.ts");
        assertThat(result).isEqualTo("d.ts");
    }

    public void testGetExtensionSecondPass() {
        String result = this.fileClassifier.getExtension("test.d.ts");
        result = this.fileClassifier.getExtension(result);
        assertThat(result).isEqualTo("ts");
    }

    public void testLanguageGuesser() {
        String result = this.fileClassifier.languageGuesser("something.c", "");
        assertThat(result).isEqualTo("C");
    }

    public void testLanguageGuesserMultiple() {
        String result = this.fileClassifier.languageGuesser("test.d.ts", "");
        assertThat(result).isEqualTo("TypeScript Typings");
    }

    public void testLanguageGuesserFullname() {
        String result = this.fileClassifier.languageGuesser(".gitignore", "");
        assertThat(result).isEqualTo("gitignore");
    }

    public void testLanguageGuesserCssBug() {
        String result = this.fileClassifier.languageGuesser("kimbie.dark.css", "");
        assertThat(result).isEqualTo("CSS");
    }

    public void testLoadDatabase() {
        FileClassifier fileClassifier = new FileClassifier();
        HashMap<String, FileClassifierResult> database = fileClassifier.getDatabase();
        AssertionsForClassTypes.assertThat(database.size()).isNotZero();
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
        var database = new HashMap<String, FileClassifierResult>();
        database.put("boyterlang", new FileClassifierResult("boyter"));
        FileClassifier fileClassifier = new FileClassifier(database);

        String languageGuess = fileClassifier.languageGuesser("test.boyter", "");
        assertThat(languageGuess).isEqualTo("boyterlang");
    }

    public void testIdentifyLanguageAdditionalDots() {
        HashMap<String, FileClassifierResult> database = new HashMap<>();
        database.put("Typescript", new FileClassifierResult("ts"));
        database.put("Typings Definition", new FileClassifierResult("d.ts"));

        FileClassifier fileClassifier = new FileClassifier(database);
        String languageGuess = fileClassifier.languageGuesser("test.d.ts", "");

        assertThat(languageGuess).isEqualTo("Typings Definition");
    }

    public void testLanguageGuesserText() {
        FileClassifier fileClassifier = new FileClassifier();
        String language = fileClassifier.languageGuesser("test.txt", "");
        assertEquals("Plain Text", language);
    }

    public void testLanguageGuesserXAML() {
        FileClassifier fileClassifier = new FileClassifier();
        String language = fileClassifier.languageGuesser("test.xml", "");
        assertEquals("XML", language);
    }

    public void testLanguageGuesserHTML() {
        FileClassifier fileClassifier = new FileClassifier();
        String language = fileClassifier.languageGuesser("test.html", "");
        assertEquals("HTML", language);
    }

    public void testLanguageGuesserUnknown() {
        FileClassifier fileClassifier = new FileClassifier();
        String language = fileClassifier.languageGuesser("test.shouldnotexist", "");
        assertEquals("Unknown", language);
    }

    public void testLanguageGuesserNoExtension() {
        FileClassifier fileClassifier = new FileClassifier();
        String language = fileClassifier.languageGuesser("Jenkinsfile", "");
        assertEquals("Jenkins Buildfile", language);
    }

    public void testLanguageGuesserKotlin() {
        FileClassifier fileClassifier = new FileClassifier();
        String language = fileClassifier.languageGuesser("test.kt", "");
        assertEquals("Kotlin", language);
    }

    public void testIdentifyLanguageMultipleExt() {
        var database = new HashMap<String, FileClassifierResult>();
        database.put("V", new FileClassifierResult("v", "Require,Hypothesis,Inductive,Remark,Lemma,Proof,Definition,Theorem"));
        database.put("Coq", new FileClassifierResult("v", "fn,const,println,import,struct,module"));
        var fileClassifier = new FileClassifier(database);

        var languageGuess = fileClassifier.languageGuesser("test.v", "fn,const,println,import,struct,module");
        assertThat(languageGuess).isEqualTo("V");
        languageGuess = fileClassifier.languageGuesser("test.v", "Require,Hypothesis,Inductive,Remark,Lemma,Proof,Definition,Theorem");
        assertThat(languageGuess).isEqualTo("Coq");
    }
}
