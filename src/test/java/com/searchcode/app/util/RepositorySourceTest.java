package com.searchcode.app.util;

import com.searchcode.app.dto.Source;
import junit.framework.TestCase;

import java.util.HashMap;

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

    public void testGetLink() {
        RepositorySource repositorySource = new RepositorySource();
        String link = repositorySource.getLink("GitHub", new HashMap<String, String>() {{
            put("user", "boyter");
            put("project", "searchcode-server");
            put("branch", "master");
            put("filepath", "fabfile.py");
        }});

        assertThat(link).isEqualTo("https://github.com/boyter/searchcode-server/blob/master/fabfile.py");
    }

    public void testGetLinkMissingRepository() {
        RepositorySource repositorySource = new RepositorySource();
        String link = repositorySource.getLink("DoesNotExist", new HashMap<String, String>() {{
            put("user", "boyter");
            put("project", "searchcode-server");
            put("branch", "master");
            put("filepath", "fabfile.py");
        }});

        assertThat(link).isEqualTo("");
    }
}
