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
        assertThat(repositorySource.getSourceByName("GitHub").get().getName()).isEqualTo("GitHub");
        assertThat(repositorySource.getSourceByName("HOPEFULLYNEVEREXISTS").isPresent()).isFalse();
    }
}
