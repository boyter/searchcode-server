package com.searchcode.app.service;

import com.searchcode.app.dto.api.ApiResponse;
import junit.framework.TestCase;
import org.mockito.Mockito;
import spark.Request;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

public class ApiRouteServiceTest extends TestCase {
    public void testRepositoryReindexApiNotEnabled() {
        ApiRouteService apiRouteService = new ApiRouteService();
        apiRouteService.apiEnabled = false;

        ApiResponse apiResponse = apiRouteService.repositoryReindex(null, null);

        assertThat(apiResponse.getMessage()).isEqualTo("API not enabled");
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepositoryReindexApiAuthNotEnabledRebuildAllWorks() {
        JobService mockJobService = Mockito.mock(JobService.class);
        Request mockRequest = Mockito.mock(Request.class);

        when(mockJobService.rebuildAll()).thenReturn(true);

        ApiRouteService apiRouteService = new ApiRouteService(null, mockJobService);
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = false;

        ApiResponse apiResponse = apiRouteService.repositoryReindex(mockRequest, null);
        assertThat(apiResponse.getMessage()).isEqualTo("reindex forced");
        assertThat(apiResponse.isSucessful()).isEqualTo(true);
        verify(mockJobService, times(1)).rebuildAll();
        verify(mockJobService, times(1)).forceEnqueue();
    }

    public void testRepositoryReindexApiAuthNotEnabledRebuildAllFails() {
        JobService mockJobService = Mockito.mock(JobService.class);
        Request mockRequest = Mockito.mock(Request.class);

        when(mockJobService.rebuildAll()).thenReturn(false);

        ApiRouteService apiRouteService = new ApiRouteService(null, mockJobService);
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = false;

        ApiResponse apiResponse = apiRouteService.repositoryReindex(mockRequest, null);
        assertThat(apiResponse.getMessage()).isEqualTo("was unable to force the index");
        assertThat(apiResponse.isSucessful()).isEqualTo(false);

        verify(mockJobService, times(1)).rebuildAll();
        verify(mockJobService, times(0)).forceEnqueue();
    }

    public void testRepositoryReindexApiAuthEnabledPubMissing() {
        JobService mockJobService = Mockito.mock(JobService.class);
        Request mockRequest = Mockito.mock(Request.class);

        when(mockJobService.rebuildAll()).thenReturn(true);

        ApiRouteService apiRouteService = new ApiRouteService(null, mockJobService);
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        ApiResponse apiResponse = apiRouteService.repositoryReindex(mockRequest, null);
        assertThat(apiResponse.getMessage()).isEqualTo("pub is a required parameter");
        assertThat(apiResponse.isSucessful()).isEqualTo(false);
    }

    public void testRepositoryReindexApiAuthEnabledSigMissing() {
        JobService mockJobService = Mockito.mock(JobService.class);
        Request mockRequest = Mockito.mock(Request.class);

        when(mockRequest.queryParams("pub")).thenReturn("test");
        when(mockJobService.rebuildAll()).thenReturn(true);

        ApiRouteService apiRouteService = new ApiRouteService(null, mockJobService);
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        ApiResponse apiResponse = apiRouteService.repositoryReindex(mockRequest, null);
        assertThat(apiResponse.getMessage()).isEqualTo("sig is a required parameter");
        assertThat(apiResponse.isSucessful()).isEqualTo(false);
    }

    public void testRepositoryReindexApiAuthEnabledInvalidSig() {
        JobService mockJobService = Mockito.mock(JobService.class);
        ApiService mockApiService = Mockito.mock(ApiService.class);

        Request mockRequest = Mockito.mock(Request.class);

        when(mockRequest.queryParams("pub")).thenReturn("test");
        when(mockRequest.queryParams("sig")).thenReturn("test");
        when(mockJobService.rebuildAll()).thenReturn(true);
        when(mockApiService.validateRequest("test", "test", "pub=test", ApiService.HmacType.SHA1)).thenReturn(false);

        ApiRouteService apiRouteService = new ApiRouteService(mockApiService, mockJobService);
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        ApiResponse apiResponse = apiRouteService.repositoryReindex(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("invalid signed url");
        assertThat(apiResponse.isSucessful()).isEqualTo(false);
    }

    public void testRepositoryReindexApiAuthEnabledValidSig() {
        JobService mockJobService = Mockito.mock(JobService.class);
        ApiService mockApiService = Mockito.mock(ApiService.class);

        Request mockRequest = Mockito.mock(Request.class);

        when(mockRequest.queryParams("pub")).thenReturn("test");
        when(mockRequest.queryParams("sig")).thenReturn("test");
        when(mockJobService.rebuildAll()).thenReturn(true);
        when(mockApiService.validateRequest("test", "test", "pub=test", ApiService.HmacType.SHA1)).thenReturn(true);

        ApiRouteService apiRouteService = new ApiRouteService(mockApiService, mockJobService);
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        ApiResponse apiResponse = apiRouteService.repositoryReindex(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("reindex forced");
        assertThat(apiResponse.isSucessful()).isEqualTo(true);
    }
}
