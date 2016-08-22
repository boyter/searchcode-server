package com.searchcode.app.service;

import com.searchcode.app.dao.Api;
import com.searchcode.app.model.ApiResult;
import junit.framework.TestCase;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApiServiceTest extends TestCase {

    public void testValidateRequestNoMatchingKeyExpectFalse() {
        Api apiMock = mock(Api.class);
        when(apiMock.getApiByPublicKey("publicKey")).thenReturn(null);

        ApiService service = new ApiService(apiMock);

        boolean actual = service.validateRequest("publicKey", "", "");
        assertFalse(actual);
    }

    public void testValidateRequestCorrectHmac() {
        Api apiMock = mock(Api.class);
        when(apiMock.getApiByPublicKey("publicKey")).thenReturn(new ApiResult(1, "publicKey", "privateKey", "", ""));

        ApiService service = new ApiService(apiMock);

        boolean actual = service.validateRequest("publicKey", "3eb4cb7c8a30ac3814bbfae935cbe3c1f4f2acce", "stringtohmac");
        assertTrue(actual);
    }

    public void testValidateRequestInCorrectHmac() {
        Api apiMock = mock(Api.class);
        when(apiMock.getApiByPublicKey("publicKey")).thenReturn(new ApiResult(1, "publicKey", "privateKey", "", ""));

        ApiService service = new ApiService(apiMock);

        boolean actual = service.validateRequest("publicKey", "incorrecthmac", "stringtohmac");
        assertFalse(actual);
    }

    public void testCase1() {
        Api apiMock = mock(Api.class);
        when(apiMock.getApiByPublicKey("publicKey")).thenReturn(new ApiResult(1, "publicKey", "privateKey", "", ""));

        ApiService service = new ApiService(apiMock);

        boolean actual = service.validateRequest("publicKey", "e15db69d711f0f25ce07a9c11ebebe821e6fc312", "");
        assertTrue(actual);
    }

    public void testCase2() {
        Api apiMock = mock(Api.class);
        when(apiMock.getApiByPublicKey("publicKey")).thenReturn(new ApiResult(1, "publicKey", "privateKey", "", ""));

        ApiService service = new ApiService(apiMock);

        boolean actual = service.validateRequest("publicKey", "1577b8c8f5781bf2817a45bfb47ded066c579c37", "testmessage1");
        assertTrue(actual);
    }

    public void testCase3() {
        Api apiMock = mock(Api.class);
        when(apiMock.getApiByPublicKey("publicKey")).thenReturn(new ApiResult(1, "publicKey", "privateKey", "", ""));

        ApiService service = new ApiService(apiMock);

        boolean actual = service.validateRequest("publicKey", "0cb1ae7ab0db51dd82c4d29000523e643d8a1fcb", "?pub=publicKey&reponame=test&repourl=http://github.com/&reposource=&repobranch=master");
        assertTrue(actual);
    }

    public void testCreateKeys() {
        Api apiMock = mock(Api.class);
        when(apiMock.saveApi(anyObject())).thenReturn(true);

        ApiService service = new ApiService(apiMock);

        ApiResult actual = service.createKeys();
        assertNotNull(actual);
        assertEquals(-1, actual.getRowId());
        assertTrue(actual.getPublicKey().startsWith("APIK-"));
        assertEquals(32, actual.getPublicKey().length());
        assertEquals(32, actual.getPrivateKey().length());
        assertEquals("", actual.getData());
        assertEquals("", actual.getLastUsed());
    }
}
