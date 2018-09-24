package com.searchcode.app.util;

import junit.framework.TestCase;

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
        String result = this.fileClassifier.languageGuesser("something.c");
        assertThat(result).isEqualTo("C");
    }

    public void testLanguageGuesserMultiple() {
        String result = this.fileClassifier.languageGuesser("test.d.ts");
        assertThat(result).isEqualTo("TypeScript Typings");
    }

    public void testLanguageGuesserFullname() {
        String result = this.fileClassifier.languageGuesser(".gitignore");
        assertThat(result).isEqualTo("gitignore");
    }

    public void testLanguageGuesserCssBug() {
        String result = this.fileClassifier.languageGuesser("kimbie.dark.css");
        assertThat(result).isEqualTo("CSS");
    }
}
