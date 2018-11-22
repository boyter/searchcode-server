package com.searchcode.app.dao;

import com.searchcode.app.config.MySQLDatabaseConfig;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.model.SourceResult;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import junit.framework.TestCase;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SourceTest extends TestCase {
    private Source source;

    public void setUp() throws Exception {
        super.setUp();
        this.source = new Source(new MySQLDatabaseConfig(), new Helpers(), Singleton.getLogger(), Singleton.getGenericCache());
    }

    public void testGetSourceByNameNonExistent() {
        assertThat(this.source.getSourceByName("SHOULDNOTEXIST").isPresent()).isFalse();
    }

    public void testGetSourceByIdNonExistent() {
        assertThat(this.source.getSourceById(999999999).isPresent()).isFalse();
    }

    public void testGetSourceByIdFirst100() {
        for (int i = 0; i < 100; i++) {
            this.source.getSourceById(i).map(x -> assertThat(this.source.getSourceByName(x.name).isPresent()));
        }
    }

    public void testGetSourceByIdCache() {
        var cache = Singleton.getGenericCache();
        cache.put("dao.source.999", Optional.of(new SourceResult().setId(999).setName("ZeName")));
        var newSource = new Source(new MySQLDatabaseConfig(), new Helpers(), Singleton.getLogger(), cache);

        var result = newSource.getSourceById(999);

        assertThat(result.get().id).isEqualTo(999);
        assertThat(result.get().name).isEqualTo("ZeName");
    }

    public void testGetSourceByNameCache() {
        var cache = Singleton.getGenericCache();
        cache.put("dao.source.ZeName", Optional.of(new SourceResult().setId(999).setName("ZeName")));
        var newSource = new Source(new MySQLDatabaseConfig(), new Helpers(), Singleton.getLogger(), cache);

        var result = newSource.getSourceByName("ZeName");

        assertThat(result.get().id).isEqualTo(999);
        assertThat(result.get().name).isEqualTo("ZeName");
    }
}
