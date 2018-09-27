package com.searchcode.app.service;

import com.searchcode.app.dao.Repo;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.model.ValidatorResult;
import com.searchcode.app.service.route.AdminRouteService;
import junit.framework.TestCase;
import org.mockito.Mockito;
import spark.Request;

import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class AdminRouteServiceTest extends TestCase {

    public void testCheckIndexStatus() {
        AdminRouteService adminRouteService = new AdminRouteService();

        Request mockRequest = Mockito.mock(Request.class);

        Set<String> returnSet = new HashSet<>();
        returnSet.add("reponame");

        when(mockRequest.queryParams()).thenReturn(returnSet);
        when(mockRequest.queryParams("reponame")).thenReturn("hopefullyarandomnamethatdoesnotexist");

        String result = adminRouteService.checkIndexStatus(mockRequest, null);
        assertThat(result).isEqualTo("Queued");
    }


    public void testGetStatValuesExpectEmpty() {
        AdminRouteService adminRouteService = new AdminRouteService();
        Singleton.getLogger().clearAllLogs();
        List<String> statValue = Arrays.asList(null, "", "alllogs", "infologs", "warninglogs", "severelogs", "searchlogs");

        for(String stat: statValue) {
            Request mockRequest = Mockito.mock(Request.class);

            Set<String> returnSet = new HashSet<>();
            returnSet.add("statname");

            when(mockRequest.queryParams()).thenReturn(returnSet);
            when(mockRequest.queryParams("statname")).thenReturn(stat);

            String result = adminRouteService.getStat(mockRequest, null);
            assertThat(result).as("For value %s", stat).isEmpty();
        }
    }

    public void testGetStatValuesExpectNbspBecauseIntercoolerJS() {
        AdminRouteService adminRouteService = new AdminRouteService();
        Singleton.getLogger().clearAllLogs();

        Request mockRequest = mock(Request.class);

        Set<String> returnSet = new HashSet<>();
        returnSet.add("statname");

        when(mockRequest.queryParams()).thenReturn(returnSet);
        when(mockRequest.queryParams("statname")).thenReturn("runningjobs");

        String result = adminRouteService.getStat(mockRequest, null);
        assertThat(result).as("For value runningjobs").isEqualTo("&nbsp;");
    }

    public void testGetStatValuesExpectValue() {
        Repo repoMock = mock(Repo.class);
        StatsService statsServiceMock = mock(StatsService.class);
        IndexService indexServiceMock = mock(IndexService.class);

        when(statsServiceMock.getMemoryUsage(any())).thenReturn("Yep");
        when(statsServiceMock.getLoadAverage()).thenReturn("Yep");
        when(statsServiceMock.getUpTime()).thenReturn("Yep");
        when(repoMock.getRepoCount()).thenReturn(1);

        when(indexServiceMock.getIndexedDocumentCount()).thenReturn(100);
        when(indexServiceMock.shouldPause(IIndexService.JobType.REPO_PARSER)).thenReturn(false);

        AdminRouteService adminRouteService = new AdminRouteService(repoMock, null, null, null, indexServiceMock, statsServiceMock, null, null, Singleton.getLogger());
        List<String> statValue = Arrays.asList("memoryusage", "loadaverage", "uptime", "searchcount", "spellingcount", "repocount", "numdocs", "servertime", "deletionqueue");

        for (String stat: statValue) {
            Request mockRequest = Mockito.mock(Request.class);

            Set<String> returnSet = new HashSet<>();
            returnSet.add("statname");

            when(mockRequest.queryParams()).thenReturn(returnSet);
            when(mockRequest.queryParams("statname")).thenReturn(stat);

            String result = adminRouteService.getStat(mockRequest, null);
            assertThat(result).as("For value %s", stat).isNotEmpty();
        }
    }

    public void testGetStatLogs() {
        AdminRouteService adminRouteService = new AdminRouteService();

        Singleton.getLogger().apiLog("test");
        Singleton.getLogger().searchLog("test");
        Singleton.getLogger().severe("ignore this severe message");
        Singleton.getLogger().info("test");

        List<String> statValue = Arrays.asList("alllogs", "infologs", "severelogs", "searchlogs", "apilogs");
        for (String stat: statValue) {
            Request mockRequest = Mockito.mock(Request.class);

            Set<String> returnSet = new HashSet<>();
            returnSet.add("statname");

            when(mockRequest.queryParams()).thenReturn(returnSet);
            when(mockRequest.queryParams("statname")).thenReturn(stat);

            String result = adminRouteService.getStat(mockRequest, null);
            assertThat(result).as("For value %s", stat).isNotEmpty();
        }
    }

    public void testPostRepoRepoNamesEmptyNothing() {
        Repo mockRepo = Mockito.mock(Repo.class);
        JobService mockJobService = Mockito.mock(JobService.class);

        AdminRouteService adminRouteService = new AdminRouteService(mockRepo, null, mockJobService, null, null, null, null, null, Singleton.getLogger());
        Request mockRequest = Mockito.mock(Request.class);

        when(mockRequest.queryParamsValues("reponame")).thenReturn(new String[0]);
        when(mockRequest.queryParamsValues("reposcm")).thenReturn(new String[0]);
        when(mockRequest.queryParamsValues("repourl")).thenReturn(new String[0]);
        when(mockRequest.queryParamsValues("repousername")).thenReturn(new String[0]);
        when(mockRequest.queryParamsValues("repopassword")).thenReturn(new String[0]);
        when(mockRequest.queryParamsValues("reposource")).thenReturn(new String[0]);
        when(mockRequest.queryParamsValues("repobranch")).thenReturn(new String[0]);

        adminRouteService.postRepo(mockRequest, null, true);
    }

    public void testPostRepoMultipleRepo() {
        Repo mockRepo = mock(Repo.class);
        JobService mockJobService = mock(JobService.class);
        ValidatorService mockValidatorService = mock(ValidatorService.class);

        when(mockRepo.saveRepo(any())).thenReturn(true);
        when(mockValidatorService.validate(any(), anyBoolean())).thenReturn(new ValidatorResult(true, ""));
        when(mockRepo.getRepoByUrl(any())).thenReturn(Optional.of(new RepoResult()));

        AdminRouteService adminRouteService = new AdminRouteService(mockRepo, null, mockJobService, null, null, null, mockValidatorService, null, Singleton.getLogger());
        Request mockRequest = mock(Request.class);

        when(mockRequest.queryParamsValues("reponame")).thenReturn("name,name".split(","));
        when(mockRequest.queryParamsValues("reposcm")).thenReturn("git,git".split(","));
        when(mockRequest.queryParamsValues("repourl")).thenReturn("url,url".split(","));
        when(mockRequest.queryParamsValues("repousername")).thenReturn("username,username".split(","));
        when(mockRequest.queryParamsValues("repopassword")).thenReturn("password,password".split(","));
        when(mockRequest.queryParamsValues("reposource")).thenReturn("source,source".split(","));
        when(mockRequest.queryParamsValues("repobranch")).thenReturn("source,source".split(","));
        when(mockRequest.queryParamsValues("source")).thenReturn("source,source".split(","));
        when(mockRequest.queryParamsValues("sourceuser")).thenReturn("master,master".split(","));
        when(mockRequest.queryParamsValues("sourceproject")).thenReturn("master,master".split(","));

        adminRouteService.postRepo(mockRequest, null, false);
        verify(mockRepo, times(2)).saveRepo(any());
        verify(mockJobService, times(2)).forceEnqueue(any());
        verify(mockValidatorService, times(2)).validate(any(), anyBoolean());
    }

    public void testDeleteRepo() {
        Repo mockRepo = Mockito.mock(Repo.class);
        JobService mockJobService = Mockito.mock(JobService.class);
        DataService mockDataService = Mockito.mock(DataService.class);

        AdminRouteService adminRouteService = new AdminRouteService(mockRepo, null, mockJobService, mockDataService, null, null, null, null, Singleton.getLogger());
        Request mockRequest = Mockito.mock(Request.class);

        when(mockRequest.queryParams("repoName")).thenReturn("myRepo");
        when(mockRepo.getRepoByName("myRepo")).thenReturn(Optional.of(new RepoResult()));


        adminRouteService.deleteRepo(mockRequest, null);
        verify(mockDataService, times(1)).addToPersistentDelete("");
    }
}
