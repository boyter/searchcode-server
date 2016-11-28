package com.searchcode.app.service;

import com.searchcode.app.dao.Repo;
import com.searchcode.app.dto.api.ApiResponse;
import com.searchcode.app.dto.api.RepoResultApiResponse;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.util.UniqueRepoQueue;
import junit.framework.TestCase;
import org.mockito.Mockito;
import spark.Request;

import java.util.concurrent.ConcurrentLinkedQueue;

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

        ApiRouteService apiRouteService = new ApiRouteService(null, mockJobService, null, null);
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

        ApiRouteService apiRouteService = new ApiRouteService(null, mockJobService, null, null);
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

        ApiRouteService apiRouteService = new ApiRouteService(null, mockJobService, null, null);
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

        ApiRouteService apiRouteService = new ApiRouteService(null, mockJobService, null, null);
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

        ApiRouteService apiRouteService = new ApiRouteService(mockApiService, mockJobService, null, null);
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

        ApiRouteService apiRouteService = new ApiRouteService(mockApiService, mockJobService, null, null);
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        ApiResponse apiResponse = apiRouteService.repositoryReindex(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("reindex forced");
        assertThat(apiResponse.isSucessful()).isEqualTo(true);
    }

    /////////////////////////////////////////////////////////////////////

    public void testRepoListApiNotEnabled() {
        ApiRouteService apiRouteService = new ApiRouteService();
        apiRouteService.apiEnabled = false;

        RepoResultApiResponse apiResponse = apiRouteService.repoList(null, null);

        assertThat(apiResponse.getMessage()).isEqualTo("API not enabled");
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepoListApiEnabledNoAuth() {
        Request mockRequest = Mockito.mock(Request.class);

        ApiRouteService apiRouteService = new ApiRouteService();
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = false;

        RepoResultApiResponse apiResponse = apiRouteService.repoList(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("");
        assertThat(apiResponse.getRepoResultList()).hasSize(0);
        assertThat(apiResponse.isSucessful()).isTrue();
    }

    public void testRepoListApiEnabledAuthMissingPub() {
        Request mockRequest = Mockito.mock(Request.class);

        ApiRouteService apiRouteService = new ApiRouteService();
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        RepoResultApiResponse apiResponse = apiRouteService.repoList(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("pub is a required parameter");
        assertThat(apiResponse.getRepoResultList()).isNull();
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepoListApiEnabledAuthMissingSig() {
        Request mockRequest = Mockito.mock(Request.class);

        ApiRouteService apiRouteService = new ApiRouteService();
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        when(mockRequest.queryParams("pub")).thenReturn("test");

        RepoResultApiResponse apiResponse = apiRouteService.repoList(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("sig is a required parameter");
        assertThat(apiResponse.getRepoResultList()).isNull();
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepoListApiEnabledAuthInvalidSigned() {
        Request mockRequest = Mockito.mock(Request.class);
        ApiService mockApiService = Mockito.mock(ApiService.class);

        when(mockApiService.validateRequest("test", "test", "pub=test", ApiService.HmacType.SHA1)).thenReturn(false);

        ApiRouteService apiRouteService = new ApiRouteService(mockApiService, null, null, null);
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        when(mockRequest.queryParams("pub")).thenReturn("test");
        when(mockRequest.queryParams("sig")).thenReturn("test");

        RepoResultApiResponse apiResponse = apiRouteService.repoList(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("invalid signed url");
        assertThat(apiResponse.getRepoResultList()).isNull();
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepoListApiEnabledAuthValid() {
        Request mockRequest = Mockito.mock(Request.class);
        ApiService mockApiService = Mockito.mock(ApiService.class);
        Repo mockRepo = Mockito.mock(Repo.class);

        when(mockApiService.validateRequest("test", "test", "pub=test", ApiService.HmacType.SHA1)).thenReturn(true);

        ApiRouteService apiRouteService = new ApiRouteService(mockApiService, null, mockRepo, null);
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        when(mockRequest.queryParams("pub")).thenReturn("test");
        when(mockRequest.queryParams("sig")).thenReturn("test");

        RepoResultApiResponse apiResponse = apiRouteService.repoList(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("");
        assertThat(apiResponse.getRepoResultList()).hasSize(0);
        assertThat(apiResponse.isSucessful()).isTrue();
    }

    /////////////////////////////////////////////////////////////////////

    public void testRepoDeleteApiNotEnabled() {
        ApiRouteService apiRouteService = new ApiRouteService();
        apiRouteService.apiEnabled = false;

        ApiResponse apiResponse = apiRouteService.repoDelete(null, null);

        assertThat(apiResponse.getMessage()).isEqualTo("API not enabled");
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepoDeleteNoAuthNoReponame() {
        Request mockRequest = Mockito.mock(Request.class);

        ApiRouteService apiRouteService = new ApiRouteService();
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = false;

        ApiResponse apiResponse = apiRouteService.repoDelete(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("reponame is a required parameter");
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepoDeleteNoAuthReponame() {
        Request mockRequest = Mockito.mock(Request.class);
        Repo mockRepo = Mockito.mock(Repo.class);
        UniqueRepoQueue uniqueRepoQueue = new UniqueRepoQueue(new ConcurrentLinkedQueue<>());

        when(mockRepo.getRepoByName("unit-test")).thenReturn(new RepoResult());

        ApiRouteService apiRouteService = new ApiRouteService(null, null, mockRepo, uniqueRepoQueue);
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = false;

        when(mockRequest.queryParams("reponame")).thenReturn("unit-test");

        ApiResponse apiResponse = apiRouteService.repoDelete(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("repository queued for deletion");
        assertThat(apiResponse.isSucessful()).isTrue();
        assertThat(uniqueRepoQueue.size()).isEqualTo(1);
    }
}
