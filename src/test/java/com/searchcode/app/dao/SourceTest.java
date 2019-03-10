package com.searchcode.app.dao;

import com.searchcode.app.model.SourceResult;
import com.searchcode.app.service.CacheSingleton;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.Helpers;
import junit.framework.TestCase;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SourceTest extends TestCase {
    private Source source;

    public void setUp() throws Exception {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        super.setUp();
        this.source = new Source(Singleton.getDatabaseConfig(), new Helpers(), Singleton.getLogger(),
                CacheSingleton.getSourceCache());
    }

    public void testGetSourceByNameNonExistent() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        assertThat(this.source.getSourceByName("SHOULDNOTEXIST").isPresent()).isFalse();
    }

    public void testGetSourceByIdNonExistent() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        assertThat(this.source.getSourceById(999999999).isPresent()).isFalse();
    }

    public void testGetSourceByIdFirst100() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        for (int i = 0; i < 100; i++) {
            this.source.getSourceById(i).map(x -> assertThat(this.source.getSourceByName(x.name).isPresent()));
        }
    }

    public void testGetSourceByIdCache() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var cache = CacheSingleton.getSourceCache();
        cache.put("d.s.999", Optional.of(new SourceResult().setId(999).setName("ZeName")));
        var newSource = new Source(Singleton.getDatabaseConfig(), new Helpers(), Singleton.getLogger(), cache);

        var result = newSource.getSourceById(999);

        assertThat(result.get().id).isEqualTo(999);
        assertThat(result.get().name).isEqualTo("ZeName");
    }

    public void testGetSourceByNameCache() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var cache = CacheSingleton.getSourceCache();
        cache.put("d.s.ZeName", Optional.of(new SourceResult().setId(999).setName("ZeName")));
        var newSource = new Source(Singleton.getDatabaseConfig(), new Helpers(), Singleton.getLogger(), cache);

        var result = newSource.getSourceByName("ZeName");

        assertThat(result.get().id).isEqualTo(999);
        assertThat(result.get().name).isEqualTo("ZeName");
    }
}
