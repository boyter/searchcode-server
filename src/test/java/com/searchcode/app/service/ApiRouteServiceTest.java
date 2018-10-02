package com.searchcode.app.service;

import com.searchcode.app.config.SQLiteMemoryDatabaseConfig;
import com.searchcode.app.dao.Repo;
import com.searchcode.app.dto.api.ApiResponse;
import com.searchcode.app.dto.api.RepoResultApiResponse;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.model.ValidatorResult;
import com.searchcode.app.service.route.ApiRouteService;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.LoggerWrapper;
import com.searchcode.app.util.UniqueRepoQueue;
import junit.framework.TestCase;
import org.mockito.Matchers;
import spark.Request;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class ApiRouteServiceTest extends TestCase {

    public void testRepositoryReindexApiNotEnabled() {
        ApiRouteService apiRouteService = new ApiRouteService();
        apiRouteService.apiEnabled = false;

        ApiResponse apiResponse = apiRouteService.repositoryReindex(null, null);

        assertThat(apiResponse.getMessage()).isEqualTo("API not enabled");
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepositoryReindexApiAuthNotEnabledRebuildAllWorks() {
        Request mockRequest = mock(Request.class);
        IndexService mockIndexService = mock(IndexService.class);

        ApiRouteService apiRouteService = new ApiRouteService(null, null, null, null, null, mockIndexService, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = false;

        ApiResponse apiResponse = apiRouteService.repositoryReindex(mockRequest, null);
        assertThat(apiResponse.getMessage()).isEqualTo("reindex forced");
        assertThat(apiResponse.isSucessful()).isEqualTo(true);

        verify(mockIndexService, times(1)).reindexAll();
    }

    public void testRepositoryReindexApiAuthNotEnabledRebuildAllFails() {
        Request mockRequest = mock(Request.class);
        IndexService mockIndexService = mock(IndexService.class);


        ApiRouteService apiRouteService = new ApiRouteService(null, null, null, null, null, mockIndexService, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = false;

        ApiResponse apiResponse = apiRouteService.repositoryReindex(mockRequest, null);
        assertThat(apiResponse.getMessage()).isEqualTo("reindex forced");
        assertThat(apiResponse.isSucessful()).isEqualTo(true);

        verify(mockIndexService, times(1)).reindexAll();
    }

    public void testRepositoryReindexApiAuthEnabledPubMissing() {
        JobService mockJobService = mock(JobService.class);
        Request mockRequest = mock(Request.class);

        ApiRouteService apiRouteService = new ApiRouteService(null, mockJobService, null, null, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        ApiResponse apiResponse = apiRouteService.repositoryReindex(mockRequest, null);
        assertThat(apiResponse.getMessage()).isEqualTo("pub is a required parameter");
        assertThat(apiResponse.isSucessful()).isEqualTo(false);
    }

    public void testRepositoryReindexApiAuthEnabledSigMissing() {
        JobService mockJobService = mock(JobService.class);
        Request mockRequest = mock(Request.class);

        when(mockRequest.queryParams("pub")).thenReturn("test");

        ApiRouteService apiRouteService = new ApiRouteService(null, mockJobService, null, null, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        ApiResponse apiResponse = apiRouteService.repositoryReindex(mockRequest, null);
        assertThat(apiResponse.getMessage()).isEqualTo("sig is a required parameter");
        assertThat(apiResponse.isSucessful()).isEqualTo(false);
    }

    public void testRepositoryReindexApiAuthEnabledInvalidSig() {
        JobService mockJobService = mock(JobService.class);
        ApiService mockApiService = mock(ApiService.class);

        Request mockRequest = mock(Request.class);

        when(mockRequest.queryParams("pub")).thenReturn("test");
        when(mockRequest.queryParams("sig")).thenReturn("test");

        when(mockApiService.validateRequest("test", "test", "pub=test", ApiService.HmacType.SHA1)).thenReturn(false);

        ApiRouteService apiRouteService = new ApiRouteService(mockApiService, mockJobService, null, null, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        ApiResponse apiResponse = apiRouteService.repositoryReindex(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("invalid signed url");
        assertThat(apiResponse.isSucessful()).isEqualTo(false);
    }

    public void testRepositoryReindexApiAuthEnabledValidSig() {
        JobService mockJobService = mock(JobService.class);
        ApiService mockApiService = mock(ApiService.class);
        IndexService mockIndexService = mock(IndexService.class);

        Request mockRequest = mock(Request.class);

        when(mockRequest.queryParams("pub")).thenReturn("test");
        when(mockRequest.queryParams("sig")).thenReturn("test");

        when(mockApiService.validateRequest("test", "test", "pub=test", ApiService.HmacType.SHA1)).thenReturn(true);

        ApiRouteService apiRouteService = new ApiRouteService(mockApiService, mockJobService, null, null, null, mockIndexService, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        ApiResponse apiResponse = apiRouteService.repositoryReindex(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("reindex forced");
        assertThat(apiResponse.isSucessful()).isEqualTo(true);
    }

    /////////////////////////////////////////////////////////////////////

    public void testRepositoryIndexApiNotEnabled() {
        ApiRouteService apiRouteService = new ApiRouteService();
        apiRouteService.apiEnabled = false;

        ApiResponse apiResponse = apiRouteService.repositoryIndex(null, null);

        assertThat(apiResponse.getMessage()).isEqualTo("API not enabled");
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepositoryIndexApiNoRepositorySupplied() {
        JobService mockJobService = mock(JobService.class);
        Request mockRequest = mock(Request.class);
        Repo mockRepo = mock(Repo.class);

        when(mockJobService.forceEnqueue(Matchers.anyObject())).thenReturn(true);
        when(mockRepo.getRepoByUrl(anyString())).thenReturn(Optional.empty());

        ApiRouteService apiRouteService = new ApiRouteService(null, mockJobService, mockRepo, null, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;

        ApiResponse apiResponse = apiRouteService.repositoryIndex(mockRequest, null);
        assertThat(apiResponse.getMessage()).isEqualTo("Was unable to find repository null");
        assertThat(apiResponse.isSucessful()).isEqualTo(false);
    }

    public void testRepositoryIndexApiNoMatchingRepo() {
        JobService mockJobService = mock(JobService.class);
        Request mockRequest = mock(Request.class);
        Repo mockRepo = mock(Repo.class);

        when(mockJobService.forceEnqueue(Matchers.anyObject())).thenReturn(true);
        when(mockRequest.queryParams("repoUrl")).thenReturn("test");
        when(mockRepo.getRepoByUrl(any())).thenReturn(Optional.empty());

        ApiRouteService apiRouteService = new ApiRouteService(null, mockJobService, mockRepo, null, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;

        ApiResponse apiResponse = apiRouteService.repositoryIndex(mockRequest, null);
        assertThat(apiResponse.getMessage()).isEqualTo("Was unable to find repository test");
        assertThat(apiResponse.isSucessful()).isEqualTo(false);
    }

    public void testRepositoryIndexMatchingRepo() {
        JobService mockJobService = mock(JobService.class);
        Request mockRequest = mock(Request.class);
        Repo mockRepo = mock(Repo.class);

        when(mockJobService.forceEnqueue(Matchers.<RepoResult>anyObject())).thenReturn(true);
        when(mockRequest.queryParams("repoUrl")).thenReturn("http://test/");
        when(mockRepo.getRepoByUrl("http://test/")).thenReturn(Optional.of(new RepoResult()));

        ApiRouteService apiRouteService = new ApiRouteService(null, mockJobService, mockRepo, null, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;

        ApiResponse apiResponse = apiRouteService.repositoryIndex(mockRequest, null);
        assertThat(apiResponse.getMessage()).isEqualTo("Enqueued repository http://test/");
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
        Request mockRequest = mock(Request.class);
        Repo repo = new Repo(new SQLiteMemoryDatabaseConfig(), new Helpers(), Singleton.getLogger());
        repo.createTableIfMissing();

        ApiRouteService apiRouteService = new ApiRouteService(null, null, repo, null, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = false;

        RepoResultApiResponse apiResponse = apiRouteService.repoList(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEmpty();
        assertThat(apiResponse.getRepoResultList()).hasSize(0);
        assertThat(apiResponse.isSucessful()).isTrue();
    }

    public void testRepoListApiEnabledAuthMissingPub() {
        Request mockRequest = mock(Request.class);

        ApiRouteService apiRouteService = new ApiRouteService();
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        RepoResultApiResponse apiResponse = apiRouteService.repoList(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("pub is a required parameter");
        assertThat(apiResponse.getRepoResultList()).isNull();
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepoListApiEnabledAuthMissingSig() {
        Request mockRequest = mock(Request.class);

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
        Request mockRequest = mock(Request.class);
        ApiService mockApiService = mock(ApiService.class);

        when(mockApiService.validateRequest("test", "test", "pub=test", ApiService.HmacType.SHA1)).thenReturn(false);

        ApiRouteService apiRouteService = new ApiRouteService(mockApiService, null, null, null, null, null, new Helpers(), new LoggerWrapper());
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
        Request mockRequest = mock(Request.class);
        ApiService mockApiService = mock(ApiService.class);
        Repo mockRepo = mock(Repo.class);

        when(mockApiService.validateRequest("test", "test", "pub=test", ApiService.HmacType.SHA1)).thenReturn(true);

        ApiRouteService apiRouteService = new ApiRouteService(mockApiService, null, mockRepo, null, null, null, new Helpers(), new LoggerWrapper());
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
        Request mockRequest = mock(Request.class);

        ApiRouteService apiRouteService = new ApiRouteService();
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = false;

        ApiResponse apiResponse = apiRouteService.repoDelete(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("reponame is a required parameter");
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepoDeleteNoAuthReponame() {
        Request mockRequest = mock(Request.class);
        Repo mockRepo = mock(Repo.class);
        DataService dataServiceMock = mock(DataService.class);

        when(mockRepo.getRepoByName("unit-test")).thenReturn(Optional.of(new RepoResult()));

        ApiRouteService apiRouteService = new ApiRouteService(null, null, mockRepo, dataServiceMock, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = false;

        when(mockRequest.queryParams("reponame")).thenReturn("unit-test");

        ApiResponse apiResponse = apiRouteService.repoDelete(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("repository queued for deletion");
        assertThat(apiResponse.isSucessful()).isTrue();
        verify(dataServiceMock, times(1)).addToPersistentDelete("");
    }

    public void testRepoDeleteAuthReponameNoPub() {
        Request mockRequest = mock(Request.class);
        Repo mockRepo = mock(Repo.class);
        UniqueRepoQueue uniqueRepoQueue = new UniqueRepoQueue(new ConcurrentLinkedQueue<>());

        when(mockRepo.getRepoByName("unit-test")).thenReturn(Optional.of(new RepoResult()));

        ApiRouteService apiRouteService = new ApiRouteService(null, null, mockRepo, null, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        when(mockRequest.queryParams("reponame")).thenReturn("unit-test");

        ApiResponse apiResponse = apiRouteService.repoDelete(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("pub is a required parameter");
        assertThat(apiResponse.isSucessful()).isFalse();
        assertThat(uniqueRepoQueue.size()).isEqualTo(0);
    }

    public void testRepoDeleteAuthReponameNoSig() {
        Request mockRequest = mock(Request.class);
        Repo mockRepo = mock(Repo.class);
        UniqueRepoQueue uniqueRepoQueue = new UniqueRepoQueue(new ConcurrentLinkedQueue<>());

        when(mockRepo.getRepoByName("unit-test")).thenReturn(Optional.of(new RepoResult()));

        ApiRouteService apiRouteService = new ApiRouteService(null, null, mockRepo, null, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        when(mockRequest.queryParams("pub")).thenReturn("test");
        when(mockRequest.queryParams("reponame")).thenReturn("unit-test");

        ApiResponse apiResponse = apiRouteService.repoDelete(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("sig is a required parameter");
        assertThat(apiResponse.isSucessful()).isFalse();
        assertThat(uniqueRepoQueue.size()).isEqualTo(0);
    }

    public void testRepoDeleteAuthReponameFailedAuth() {
        Request mockRequest = mock(Request.class);
        Repo mockRepo = mock(Repo.class);
        UniqueRepoQueue uniqueRepoQueue = new UniqueRepoQueue(new ConcurrentLinkedQueue<>());
        ApiService mockApiService = mock(ApiService.class);

        when(mockApiService.validateRequest("test", "test", "pub=test", ApiService.HmacType.SHA1)).thenReturn(false);
        when(mockRepo.getRepoByName("unit-test")).thenReturn(Optional.of(new RepoResult()));

        ApiRouteService apiRouteService = new ApiRouteService(mockApiService, null, mockRepo, null, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        when(mockRequest.queryParams("pub")).thenReturn("test");
        when(mockRequest.queryParams("sig")).thenReturn("test");
        when(mockRequest.queryParams("reponame")).thenReturn("unit-test");

        ApiResponse apiResponse = apiRouteService.repoDelete(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("invalid signed url");
        assertThat(apiResponse.isSucessful()).isFalse();
        assertThat(uniqueRepoQueue.size()).isEqualTo(0);
    }

    public void testRepoDeleteAuthReponameAuth() {
        Request mockRequest = mock(Request.class);
        Repo mockRepo = mock(Repo.class);
        DataService dataServiceMock = mock(DataService.class);
        ApiService mockApiService = mock(ApiService.class);

        when(mockApiService.validateRequest("test", "test", "pub=test&reponame=unit-test", ApiService.HmacType.SHA1)).thenReturn(true);
        when(mockRepo.getRepoByName("unit-test")).thenReturn(Optional.of(new RepoResult()));

        ApiRouteService apiRouteService = new ApiRouteService(mockApiService, null, mockRepo, dataServiceMock, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        when(mockRequest.queryParams("pub")).thenReturn("test");
        when(mockRequest.queryParams("sig")).thenReturn("test");
        when(mockRequest.queryParams("reponame")).thenReturn("unit-test");

        ApiResponse apiResponse = apiRouteService.repoDelete(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("repository queued for deletion");
        assertThat(apiResponse.isSucessful()).isTrue();
        verify(dataServiceMock, times(1)).addToPersistentDelete("");

    }

    public void testGetAverageIndexTimeSeconds() {
        Request mockRequest = mock(Request.class);
        Repo repoMock = mock(Repo.class);

        when(mockRequest.queryParams("reponame")).thenReturn("somename");
        when(mockRequest.queryParams()).thenReturn(
            (new HashMap<String, String>() {{
                put("reponame", "reponame");
            }}).keySet()
        );

        when(repoMock.getRepoByName("somename")).thenReturn(
            Optional.of(new RepoResult()
                    .setRowId(0)
                    .setName("name")
                    .setScm("scm")
                    .setUrl("url")
                    .setUsername("username")
                    .setPassword("password")
                    .setSource("source")
                    .setBranch("branch")
                    .setData("{\"averageIndexTimeSeconds\":1}"))
        );

        ApiRouteService apiRouteService = new ApiRouteService(null, null, repoMock, null, null, null, new Helpers(), new LoggerWrapper());
        String averageIndexTimeSeconds = apiRouteService.getAverageIndexTimeSeconds(mockRequest, null);
        assertThat(averageIndexTimeSeconds).isEqualTo("2");
    }

    public void testGetIndexTime() {
        Request mockRequest = mock(Request.class);
        Repo repoMock = mock(Repo.class);

        when(mockRequest.queryParams("reponame")).thenReturn("somename");
        when(mockRequest.queryParams()).thenReturn(
                (new HashMap<String, String>() {{
                    put("reponame", "reponame");
                }}).keySet()
        );

        when(repoMock.getRepoByName("somename")).thenReturn(
                Optional.of(new RepoResult()
                        .setRowId(0)
                        .setName("name")
                        .setScm("scm")
                        .setUrl("url")
                        .setUsername("username")
                        .setPassword("password")
                        .setSource("source")
                        .setBranch("branch")
                        .setData("{\"rowId\":1,\"name\":\"test\",\"scm\":\"git\",\"url\":\"/test/\",\"username\":\"\",\"password\":\"\",\"source\":\"\",\"branch\":\"master\",\"data\":{\"averageIndexTimeSeconds\":9,\"indexStatus\":\"success\",\"jobRunTime\":{\"seconds\":1496356541,\"nanos\":188000000}}}"))

        );

        ApiRouteService apiRouteService = new ApiRouteService(null, null, repoMock, null, null, null, new Helpers(), new LoggerWrapper());
        String averageIndexTimeSeconds = apiRouteService.getIndexTime(mockRequest, null);
        assertThat(averageIndexTimeSeconds).contains("years ago");
    }

    /////////////////////////////////////////////////////////////////////
    // TODO expand on the below tests they do not hit all code paths
    /////////////////////////////////////////////////////////////////////

    public void testRepoAddApiNotEnabled() {
        ApiRouteService apiRouteService = new ApiRouteService();
        apiRouteService.apiEnabled = false;

        ApiResponse apiResponse = apiRouteService.repoAdd(null, null);

        assertThat(apiResponse.getMessage()).isEqualTo("API not enabled");
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepoAddMissingRepoName() {
        Request mockRequest = mock(Request.class);
        Repo mockRepo = mock(Repo.class);

        ApiRouteService apiRouteService = new ApiRouteService(null, null, mockRepo, null, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = false;


        ApiResponse apiResponse = apiRouteService.repoAdd(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("reponame is a required parameter");
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepoAddMissingRepoUrl() {
        Request mockRequest = mock(Request.class);
        Repo mockRepo = mock(Repo.class);

        ApiRouteService apiRouteService = new ApiRouteService(null, null, mockRepo, null, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = false;

        when(mockRequest.queryParams("reponame")).thenReturn("test");

        ApiResponse apiResponse = apiRouteService.repoAdd(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("repourl is a required parameter");
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepoAddMissingRepotype() {
        Request mockRequest = mock(Request.class);
        Repo mockRepo = mock(Repo.class);

        ApiRouteService apiRouteService = new ApiRouteService(null, null, mockRepo, null, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = false;

        when(mockRequest.queryParams("reponame")).thenReturn("test");
        when(mockRequest.queryParams("repourl")).thenReturn("test");

        ApiResponse apiResponse = apiRouteService.repoAdd(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("repotype is a required parameter");
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepoAddMissingRepoUsername() {
        Request mockRequest = mock(Request.class);
        Repo mockRepo = mock(Repo.class);

        ApiRouteService apiRouteService = new ApiRouteService(null, null, mockRepo, null, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = false;

        when(mockRequest.queryParams("reponame")).thenReturn("test");
        when(mockRequest.queryParams("repourl")).thenReturn("test");
        when(mockRequest.queryParams("repotype")).thenReturn("test");

        ApiResponse apiResponse = apiRouteService.repoAdd(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("repousername is a required parameter");
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepoAddMissingRepoPassword() {
        Request mockRequest = mock(Request.class);
        Repo mockRepo = mock(Repo.class);

        ApiRouteService apiRouteService = new ApiRouteService(null, null, mockRepo, null, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = false;

        when(mockRequest.queryParams("reponame")).thenReturn("test");
        when(mockRequest.queryParams("repourl")).thenReturn("test");
        when(mockRequest.queryParams("repotype")).thenReturn("test");
        when(mockRequest.queryParams("repousername")).thenReturn("test");

        ApiResponse apiResponse = apiRouteService.repoAdd(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("repopassword is a required parameter");
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepoAddMissingRepoSource() {
        Request mockRequest = mock(Request.class);
        Repo mockRepo = mock(Repo.class);

        ApiRouteService apiRouteService = new ApiRouteService(null, null, mockRepo, null, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = false;

        when(mockRequest.queryParams("reponame")).thenReturn("test");
        when(mockRequest.queryParams("repourl")).thenReturn("test");
        when(mockRequest.queryParams("repotype")).thenReturn("test");
        when(mockRequest.queryParams("repousername")).thenReturn("test");
        when(mockRequest.queryParams("repopassword")).thenReturn("test");

        ApiResponse apiResponse = apiRouteService.repoAdd(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("reposource is a required parameter");
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepoAddMissingRepoBranch() {
        Request mockRequest = mock(Request.class);
        Repo mockRepo = mock(Repo.class);

        ApiRouteService apiRouteService = new ApiRouteService(null, null, mockRepo, null, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = false;

        when(mockRequest.queryParams("reponame")).thenReturn("test");
        when(mockRequest.queryParams("repourl")).thenReturn("test");
        when(mockRequest.queryParams("repotype")).thenReturn("test");
        when(mockRequest.queryParams("repousername")).thenReturn("test");
        when(mockRequest.queryParams("repopassword")).thenReturn("test");
        when(mockRequest.queryParams("reposource")).thenReturn("test");

        ApiResponse apiResponse = apiRouteService.repoAdd(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("repobranch is a required parameter");
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepoAddNoAuthSucessful() {
        Request mockRequest = mock(Request.class);
        Repo mockRepo = mock(Repo.class);

        ValidatorService mockValidatorService = mock(ValidatorService.class);
        when(mockValidatorService.validate(any(), anyBoolean())).thenReturn(new ValidatorResult(true, ""));
        when(mockRepo.getRepoByName(anyString())).thenReturn(Optional.empty());

        ApiRouteService apiRouteService = new ApiRouteService(null, null, mockRepo, null, mockValidatorService, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = false;

        when(mockRequest.queryParams("reponame")).thenReturn("test");
        when(mockRequest.queryParams("repourl")).thenReturn("test");
        when(mockRequest.queryParams("repotype")).thenReturn("test");
        when(mockRequest.queryParams("repousername")).thenReturn("test");
        when(mockRequest.queryParams("repopassword")).thenReturn("test");
        when(mockRequest.queryParams("reposource")).thenReturn("test");
        when(mockRequest.queryParams("repobranch")).thenReturn("test");

        ApiResponse apiResponse = apiRouteService.repoAdd(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("added repository successfully");
        assertThat(apiResponse.isSucessful()).isTrue();
        verify(mockRepo, times(1)).saveRepo(Matchers.anyObject());
    }

    public void testRepoAddAuthPubMissing() {
        Request mockRequest = mock(Request.class);
        Repo mockRepo = mock(Repo.class);
        ApiService mockApiService = mock(ApiService.class);

        when(mockApiService.validateRequest("test", "test", "pub=test", ApiService.HmacType.SHA1)).thenReturn(false);

        ApiRouteService apiRouteService = new ApiRouteService(mockApiService, null, mockRepo, null, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        when(mockRequest.queryParams("reponame")).thenReturn("test");
        when(mockRequest.queryParams("repourl")).thenReturn("test");
        when(mockRequest.queryParams("repotype")).thenReturn("test");
        when(mockRequest.queryParams("repousername")).thenReturn("test");
        when(mockRequest.queryParams("repopassword")).thenReturn("test");
        when(mockRequest.queryParams("reposource")).thenReturn("test");
        when(mockRequest.queryParams("repobranch")).thenReturn("test");

        ApiResponse apiResponse = apiRouteService.repoAdd(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("pub is a required parameter");
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepoAddAuthSigMissing() {
        Request mockRequest = mock(Request.class);
        Repo mockRepo = mock(Repo.class);
        ApiService mockApiService = mock(ApiService.class);

        when(mockApiService.validateRequest("test", "test", "pub=test", ApiService.HmacType.SHA1)).thenReturn(false);

        ApiRouteService apiRouteService = new ApiRouteService(mockApiService, null, mockRepo, null, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        when(mockRequest.queryParams("reponame")).thenReturn("test");
        when(mockRequest.queryParams("repourl")).thenReturn("test");
        when(mockRequest.queryParams("repotype")).thenReturn("test");
        when(mockRequest.queryParams("repousername")).thenReturn("test");
        when(mockRequest.queryParams("repopassword")).thenReturn("test");
        when(mockRequest.queryParams("reposource")).thenReturn("test");
        when(mockRequest.queryParams("repobranch")).thenReturn("test");
        when(mockRequest.queryParams("pub")).thenReturn("test");

        ApiResponse apiResponse = apiRouteService.repoAdd(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("sig is a required parameter");
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepoAddAuthInvalidSigned() {
        Request mockRequest = mock(Request.class);
        Repo mockRepo = mock(Repo.class);
        ApiService mockApiService = mock(ApiService.class);

        when(mockApiService.validateRequest("test", "test", "pub=test&reponame=test&repourl=test&repotype=test&repousername=test&repopassword=test&reposource=test&repobranch=test", ApiService.HmacType.SHA1)).thenReturn(false);

        ApiRouteService apiRouteService = new ApiRouteService(mockApiService, null, mockRepo, null, null, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        when(mockRequest.queryParams("reponame")).thenReturn("test");
        when(mockRequest.queryParams("repourl")).thenReturn("test");
        when(mockRequest.queryParams("repotype")).thenReturn("test");
        when(mockRequest.queryParams("repousername")).thenReturn("test");
        when(mockRequest.queryParams("repopassword")).thenReturn("test");
        when(mockRequest.queryParams("reposource")).thenReturn("test");
        when(mockRequest.queryParams("repobranch")).thenReturn("test");
        when(mockRequest.queryParams("pub")).thenReturn("test");
        when(mockRequest.queryParams("sig")).thenReturn("test");

        ApiResponse apiResponse = apiRouteService.repoAdd(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("invalid signed url");
        assertThat(apiResponse.isSucessful()).isFalse();
    }

    public void testRepoAddAuthValidSigned() {
        Request mockRequest = mock(Request.class);
        Repo mockRepo = mock(Repo.class);
        ApiService mockApiService = mock(ApiService.class);
        ValidatorService mockValidatorService = mock(ValidatorService.class);

        when(mockApiService.validateRequest("test", "test", "pub=test&reponame=test&repourl=test&repotype=test&repousername=test&repopassword=test&reposource=test&repobranch=test", ApiService.HmacType.SHA1)).thenReturn(true);
        when(mockValidatorService.validate(any(), anyBoolean())).thenReturn(new ValidatorResult(true, ""));
        when(mockRepo.getRepoByName(anyString())).thenReturn(Optional.empty());

        ApiRouteService apiRouteService = new ApiRouteService(mockApiService, null, mockRepo, null, mockValidatorService, null, new Helpers(), new LoggerWrapper());
        apiRouteService.apiEnabled = true;
        apiRouteService.apiAuth = true;

        when(mockRequest.queryParams("reponame")).thenReturn("test");
        when(mockRequest.queryParams("repourl")).thenReturn("test");
        when(mockRequest.queryParams("repotype")).thenReturn("test");
        when(mockRequest.queryParams("repousername")).thenReturn("test");
        when(mockRequest.queryParams("repopassword")).thenReturn("test");
        when(mockRequest.queryParams("reposource")).thenReturn("test");
        when(mockRequest.queryParams("repobranch")).thenReturn("test");
        when(mockRequest.queryParams("pub")).thenReturn("test");
        when(mockRequest.queryParams("sig")).thenReturn("test");

        ApiResponse apiResponse = apiRouteService.repoAdd(mockRequest, null);

        assertThat(apiResponse.getMessage()).isEqualTo("added repository successfully");
        assertThat(apiResponse.isSucessful()).isTrue();
        verify(mockRepo, times(1)).saveRepo(Matchers.anyObject());
    }
}
