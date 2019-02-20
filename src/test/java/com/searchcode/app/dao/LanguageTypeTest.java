package com.searchcode.app.dao;

import com.searchcode.app.TestHelpers;
import com.searchcode.app.config.MySQLDatabaseConfig;
import com.searchcode.app.dto.LanguageTypeDTO;
import com.searchcode.app.service.Singleton;
import junit.framework.TestCase;
import org.cache2k.Cache2kBuilder;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class LanguageTypeTest extends TestCase {
    private LanguageType languageType;

    public void setUp() throws Exception {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        super.setUp();
        this.languageType = new LanguageType(
                new MySQLDatabaseConfig(),
                Singleton.getHelpers(),
                Singleton.getLogger(),
                new Cache2kBuilder<String, Optional<LanguageTypeDTO>>() {
                }
                        .name(TestHelpers.getRandomAlphanumeric())
                        .expireAfterWrite(1, TimeUnit.NANOSECONDS)
                        .entryCapacity(10000)
                        .build()
        );
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

    public void testGetLanguageTypeShouldNotExist() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var result = this.languageType.getByType(TestHelpers.getRandomAlphanumeric());
        assertThat(result.isEmpty()).isTrue();
    }

    public void testGetLanguageTypeCachedExists() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var cache = new Cache2kBuilder<String, Optional<LanguageTypeDTO>>() {}
                .name(TestHelpers.getRandomAlphanumeric())
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .entryCapacity(10000)
                .build();

        this.languageType = new LanguageType(
                new MySQLDatabaseConfig(),
                Singleton.getHelpers(),
                Singleton.getLogger(),
                cache
        );

        this.languageType.createLanguageType("kwyjibo");
        this.languageType.getByType("kwyjibo");
        var result = this.languageType.getByType("kwyjibo");

        assertThat(result.get().getType()).isEqualTo("kwyjibo");

        assertThat(cache.get("dao.languagetype.kwyjibo")).isNotNull();
    }

    public void testGetLanguageTypeCachedNotExists() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        this.languageType = new LanguageType(
                new MySQLDatabaseConfig(),
                Singleton.getHelpers(),
                Singleton.getLogger(),
                new Cache2kBuilder<String, Optional<LanguageTypeDTO>>() {}
                        .name(TestHelpers.getRandomAlphanumeric())
                        .expireAfterWrite(1, TimeUnit.MINUTES)
                        .entryCapacity(10000)
                        .build()
        );

        var result = this.languageType.getByType(TestHelpers.getRandomAlphanumeric());

        assertThat(result.isEmpty()).isTrue();
    }

    public void testGetLanguageTypeById() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var result = this.languageType.createLanguageType("kwyjibo");
        result = this.languageType.getById(result.get().getId());

        assertThat(result.get().getType()).isEqualTo("kwyjibo");
    }

    public void testGetLanguageTypeByIdCached() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var cache = new Cache2kBuilder<String, Optional<LanguageTypeDTO>>() {}
                .name(TestHelpers.getRandomAlphanumeric())
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .entryCapacity(10000)
                .build();

        this.languageType = new LanguageType(
                new MySQLDatabaseConfig(),
                Singleton.getHelpers(),
                Singleton.getLogger(),
                cache
        );

        var result = this.languageType.createLanguageType("kwyjibo");

        // Force it into the cache
        this.languageType.getById(result.get().getId());
        result = this.languageType.getById(result.get().getId());

        assertThat(result.get().getType()).isEqualTo("kwyjibo");
        assertThat(cache.get("dao.languagetype." + result.get().getId())).isNotNull();
    }

    public void testGetLanguageTypeParallel() {
        if (Singleton.getHelpers().isStandaloneInstance()) return;

        var stringTypes = new ArrayList<String>();
        var integerTypes = new ArrayList<Integer>();
        for (int i = 0; i < 100; i++) {
            stringTypes.add(TestHelpers.getRandomAscii());
            integerTypes.add(i);
        }

        stringTypes.parallelStream().forEach(x -> this.languageType.getByType(x));
        integerTypes.parallelStream().forEach(x -> this.languageType.getById(x));
    }
}
