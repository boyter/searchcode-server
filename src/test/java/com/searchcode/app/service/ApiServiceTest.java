package com.searchcode.app.service;

import com.searchcode.app.dao.Api;
import com.searchcode.app.model.ApiResult;
import com.searchcode.app.util.Helpers;
import junit.framework.TestCase;

import java.util.Optional;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApiServiceTest extends TestCase {

    public void testValidateRequestNoMatchingKeyExpectFalse() {
        var apiMock = mock(Api.class);
        when(apiMock.getApiByPublicKey("publicKey")).thenReturn(Optional.empty());

        var service = new ApiService(apiMock, new Helpers());

        var actual = service.validateRequest("publicKey", "", "", ApiService.HmacType.SHA1);
        assertFalse(actual);
    }

    public void testValidateRequestCorrectHmac() {
        var apiMock = mock(Api.class);
        when(apiMock.getApiByPublicKey("publicKey")).thenReturn(Optional.of(new ApiResult(1, "publicKey", "privateKey", "", "")));

        var service = new ApiService(apiMock, new Helpers());

        var actual = service.validateRequest("publicKey", "3eb4cb7c8a30ac3814bbfae935cbe3c1f4f2acce", "stringtohmac", ApiService.HmacType.SHA1);
        assertTrue(actual);
    }

    public void testValidateRequestInCorrectHmac() {
        var apiMock = mock(Api.class);
        when(apiMock.getApiByPublicKey("publicKey")).thenReturn(Optional.of(new ApiResult(1, "publicKey", "privateKey", "", "")));

        var service = new ApiService(apiMock, new Helpers());

        var actual = service.validateRequest("publicKey", "incorrecthmac", "stringtohmac", ApiService.HmacType.SHA1);
        assertFalse(actual);
    }

    public void testCase1() {
        var apiMock = mock(Api.class);
        when(apiMock.getApiByPublicKey("publicKey")).thenReturn(Optional.of(new ApiResult(1, "publicKey", "privateKey", "", "")));

        var service = new ApiService(apiMock, new Helpers());

        var actual = service.validateRequest("publicKey", "e15db69d711f0f25ce07a9c11ebebe821e6fc312", "", ApiService.HmacType.SHA1);
        assertTrue(actual);
    }

    public void testCase2() {
        var apiMock = mock(Api.class);
        when(apiMock.getApiByPublicKey("publicKey")).thenReturn(Optional.of(new ApiResult(1, "publicKey", "privateKey", "", "")));

        var service = new ApiService(apiMock, new Helpers());

        var actual = service.validateRequest("publicKey", "1577b8c8f5781bf2817a45bfb47ded066c579c37", "testmessage1", ApiService.HmacType.SHA1);
        assertTrue(actual);
    }

    public void testCase3() {
        var apiMock = mock(Api.class);
        when(apiMock.getApiByPublicKey("publicKey")).thenReturn(Optional.of(new ApiResult(1, "publicKey", "privateKey", "", "")));

        var service = new ApiService(apiMock, new Helpers());

        var actual = service.validateRequest("publicKey", "0cb1ae7ab0db51dd82c4d29000523e643d8a1fcb", "?pub=publicKey&reponame=test&repourl=http://github.com/&reposource=&repobranch=master", ApiService.HmacType.SHA1);
        assertTrue(actual);
    }

    public void testValidateRequestCorrectHmacMethod() {
        var apiMock = mock(Api.class);
        when(apiMock.getApiByPublicKey("publicKey")).thenReturn(Optional.of(new ApiResult(1, "publicKey", "privateKey", "", "")));

        var service = new ApiService(apiMock, new Helpers());

        var actual = service.validateRequest("publicKey", "3eb4cb7c8a30ac3814bbfae935cbe3c1f4f2acce", "stringtohmac", ApiService.HmacType.SHA1);
        assertTrue(actual);
    }

    public void testValidateRequestCorrectHmacMethodSha1() {
        var apiMock = mock(Api.class);
        when(apiMock.getApiByPublicKey("publicKey")).thenReturn(Optional.of(new ApiResult(1, "publicKey", "privateKey", "", "")));

        var service = new ApiService(apiMock, new Helpers());

        var actual = service.validateRequest("publicKey", "3eb4cb7c8a30ac3814bbfae935cbe3c1f4f2acce", "stringtohmac", ApiService.HmacType.SHA1);
        assertTrue(actual);
    }

    public void testValidateRequestCorrectHmacMethodSha512() {
        var apiMock = mock(Api.class);
        when(apiMock.getApiByPublicKey("publicKey")).thenReturn(Optional.of(new ApiResult(1, "publicKey", "privateKey", "", "")));

        var service = new ApiService(apiMock, new Helpers());

        var actual = service.validateRequest("publicKey", "8d8219101eecb1ae62e025c79379872d7461cf201a737893afc172aa9c98c505c7d1f4d864d9adbc17f8e2694fb0287fb7533e942c34589fc2daefc068e0cad3", "stringtohmac", ApiService.HmacType.SHA512);
        assertTrue(actual);
    }

    public void testValidateRequestInCorrectHmacMethodSha512() {
        var apiMock = mock(Api.class);
        when(apiMock.getApiByPublicKey("publicKey")).thenReturn(Optional.of(new ApiResult(1, "publicKey", "privateKey", "", "")));

        var service = new ApiService(apiMock, new Helpers());

        var actual = service.validateRequest("publicKey", "thisiswrong", "stringtohmac", ApiService.HmacType.SHA512);
        assertFalse(actual);
    }

    public void testCreateKeys() {
        var apiMock = mock(Api.class);
        when(apiMock.saveApi(anyObject())).thenReturn(true);

        var service = new ApiService(apiMock, new Helpers());

        var actual = service.createKeys();
        assertNotNull(actual);
        assertEquals(-1, actual.getRowId());
        assertTrue(actual.getPublicKey().startsWith("APIK-"));
        assertEquals(32, actual.getPublicKey().length());
        assertEquals(32, actual.getPrivateKey().length());
        assertEquals("", actual.getData());
        assertEquals("", actual.getLastUsed());
    }
}
