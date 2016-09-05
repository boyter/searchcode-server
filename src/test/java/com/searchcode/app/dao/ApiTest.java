package com.searchcode.app.dao;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.searchcode.app.config.InjectorConfig;
import com.searchcode.app.model.ApiResult;
import junit.framework.TestCase;

public class ApiTest extends TestCase {
    public void testMultipleCreateTable() {
        Injector injector = Guice.createInjector(new InjectorConfig());
        Api api = injector.getInstance(Api.class);

        api.createTableIfMissing();
        api.createTableIfMissing();
        api.createTableIfMissing();
        api.createTableIfMissing();
    }

    public void testSaveDelete() {
        Injector injector = Guice.createInjector(new InjectorConfig());
        Api api = injector.getInstance(Api.class);

        api.saveApi(new ApiResult(0, "publicKey", "privateKey", "", ""));
        api.deleteApiByPublicKey("publicKey");
    }

    public void testSaveRetrieve() {
        Injector injector = Guice.createInjector(new InjectorConfig());
        Api api = injector.getInstance(Api.class);

        api.saveApi(new ApiResult(0, "publicKey", "privateKey", "", ""));
        ApiResult apiResult = api.getApiByPublicKey("publicKey");

        assertEquals("publicKey", apiResult.getPublicKey());
        assertEquals("privateKey", apiResult.getPrivateKey());

        api.deleteApiByPublicKey("publicKey");
    }

    public void testMultipleRetrieveCache() {
        Injector injector = Guice.createInjector(new InjectorConfig());
        Api api = injector.getInstance(Api.class);

        api.saveApi(new ApiResult(0, "publicKey", "privateKey", "", ""));

        // Without the cache this is horribly slow
        for(int i=0; i < 50000; i++) {
            ApiResult apiResult = api.getApiByPublicKey("publicKey");

            assertEquals("publicKey", apiResult.getPublicKey());
            assertEquals("privateKey", apiResult.getPrivateKey());
        }

        api.deleteApiByPublicKey("publicKey");
    }

    public void testGetAllApi() {
        Injector injector = Guice.createInjector(new InjectorConfig());
        Api api = injector.getInstance(Api.class);

        api.saveApi(new ApiResult(0, "publicKey1", "privateKey", "", ""));
        api.saveApi(new ApiResult(0, "publicKey2", "privateKey", "", ""));
        assertEquals(2, api.getAllApi().size());
        api.deleteApiByPublicKey("publicKey1");
        api.deleteApiByPublicKey("publicKey2");
    }
}
