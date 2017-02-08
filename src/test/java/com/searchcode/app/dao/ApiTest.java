package com.searchcode.app.dao;

import com.searchcode.app.model.ApiResult;
import com.searchcode.app.service.Singleton;
import junit.framework.TestCase;

public class ApiTest extends TestCase {

    public ApiTest() {
        // Tests need to bootstrap themselves
        Api api = Singleton.getApi();
        api.createTableIfMissing();
    }

    public void testMultipleCreateTable() {
        Api api = Singleton.getApi();

        api.createTableIfMissing();
        api.createTableIfMissing();
        api.createTableIfMissing();
        api.createTableIfMissing();
    }

    public void testSaveDelete() {
        Api api = Singleton.getApi();

        api.saveApi(new ApiResult(0, "publicKey", "privateKey", "", ""));
        api.deleteApiByPublicKey("publicKey");
    }

    public void testSaveRetrieve() {
        Api api = Singleton.getApi();

        api.saveApi(new ApiResult(0, "publicKey", "privateKey", "", ""));
        ApiResult apiResult = api.getApiByPublicKey("publicKey");

        assertEquals("publicKey", apiResult.getPublicKey());
        assertEquals("privateKey", apiResult.getPrivateKey());

        api.deleteApiByPublicKey("publicKey");
    }

    public void testMultipleRetrieveCache() {
        Api api = Singleton.getApi();

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
        Api api = Singleton.getApi();

        api.saveApi(new ApiResult(0, "publicKey1", "privateKey", "", ""));
        api.saveApi(new ApiResult(0, "publicKey2", "privateKey", "", ""));
        assertEquals(2, api.getAllApi().size());
        api.deleteApiByPublicKey("publicKey1");
        api.deleteApiByPublicKey("publicKey2");
    }
}
