package com.searchcode.app.dao;

import com.searchcode.app.TestHelpers;
import com.searchcode.app.config.MySQLDatabaseConfig;
import com.searchcode.app.service.Singleton;
import junit.framework.TestCase;
import org.cache2k.Cache2kBuilder;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class LanguageTypeTest extends TestCase {
    private LanguageType languageType;

    public void setUp() throws Exception {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        super.setUp();
        this.languageType = new LanguageType(new MySQLDatabaseConfig(),
                Singleton.getHelpers(),
                Singleton.getLogger(),
                new Cache2kBuilder<String, Object>() {}
                        .name(TestHelpers.getRandomAlphanumeric())
                        .expireAfterWrite(1, TimeUnit.NANOSECONDS)
                        .entryCapacity(10000)
                        .build());
    }

    public void testCreateLanguageType() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var result = this.languageType.createLanguageType("kwyjibo");
        assertThat(result.isPresent()).isTrue();
    }

    public void testGetLanguageType() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        this.languageType.createLanguageType("kwyjibo");
        var result = this.languageType.getByType("kwyjibo");

        assertThat(result.get().getType()).isEqualTo("kwyjibo");
    }
}
