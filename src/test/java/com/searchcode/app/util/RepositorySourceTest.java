package com.searchcode.app.util;

import com.searchcode.app.dto.Source;
import junit.framework.TestCase;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class RepositorySourceTest extends TestCase {

    public void testLoader() {
        RepositorySource repositorySource = new RepositorySource();
        assertThat(repositorySource.getDatabase()).hasAtLeastOneElementOfType(Source.class);
        repositorySource.loadDatabase();
        assertThat(repositorySource.getDatabase()).hasAtLeastOneElementOfType(Source.class);
    }

    public void testGetSourceByName() {
        RepositorySource repositorySource = new RepositorySource();
        Source source = repositorySource.getSourceByName("GitHub").get();
        assertThat(source.getName()).isEqualTo("GitHub");
    }
}
