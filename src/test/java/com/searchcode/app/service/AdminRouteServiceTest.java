package com.searchcode.app.service;

import com.searchcode.app.dao.Repo;
import com.searchcode.app.model.RepoResult;
import com.searchcode.app.service.route.AdminRouteService;
import junit.framework.TestCase;
import org.mockito.Mockito;
import spark.Request;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        assertThat(result).isEmpty();
    }


    public void testGetStatValuesExpectEmpty() {
        AdminRouteService adminRouteService = new AdminRouteService();
        Singleton.getLogger().clearAllLogs();
        List<String> statValue = Arrays.asList(null,  "", "runningjobs","alllogs", "infologs", "warninglogs", "severelogs", "searchlogs");

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

    public void testGetStatValuesExpectValue() {
        SharedService sharedServiceMock = Mockito.mock(SharedService.class);
        StatsService statsServiceMock = Mockito.mock(StatsService.class);

        when(statsServiceMock.getMemoryUsage(any())).thenReturn("Yep");
        when(statsServiceMock.getLoadAverage()).thenReturn("Yep");
        when(statsServiceMock.getUptime()).thenReturn("Yep");

        AdminRouteService adminRouteService = new AdminRouteService(null, null, null, sharedServiceMock, statsServiceMock);
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
        Singleton.getLogger().warning("test");
        Singleton.getLogger().searchLog("test");
        Singleton.getLogger().severe("test");
        Singleton.getLogger().info("test");

        List<String> statValue = Arrays.asList("alllogs", "infologs", "warninglogs", "severelogs", "searchlogs", "apilogs");
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

        AdminRouteService adminRouteService = new AdminRouteService(mockRepo, mockJobService, null, null, null);
        Request mockRequest = Mockito.mock(Request.class);

        when(mockRequest.queryParamsValues("reponame")).thenReturn(new String[0]);
        when(mockRequest.queryParamsValues("reposcm")).thenReturn(new String[0]);
        when(mockRequest.queryParamsValues("repourl")).thenReturn(new String[0]);
        when(mockRequest.queryParamsValues("repousername")).thenReturn(new String[0]);
        when(mockRequest.queryParamsValues("repopassword")).thenReturn(new String[0]);
        when(mockRequest.queryParamsValues("reposource")).thenReturn(new String[0]);
        when(mockRequest.queryParamsValues("repobranch")).thenReturn(new String[0]);

        adminRouteService.postRepo(mockRequest, null);
    }

    public void testPostRepoMultipleRepo() {
        Repo mockRepo = Mockito.mock(Repo.class);
        JobService mockJobService = Mockito.mock(JobService.class);

        when(mockRepo.saveRepo(any())).thenReturn(true);

        AdminRouteService adminRouteService = new AdminRouteService(mockRepo, mockJobService, null, null, null);
        Request mockRequest = Mockito.mock(Request.class);

        when(mockRequest.queryParamsValues("reponame")).thenReturn("name,name".split(","));
        when(mockRequest.queryParamsValues("reposcm")).thenReturn("git,git".split(","));
        when(mockRequest.queryParamsValues("repourl")).thenReturn("url,url".split(","));
        when(mockRequest.queryParamsValues("repousername")).thenReturn("username,username".split(","));
        when(mockRequest.queryParamsValues("repopassword")).thenReturn("password,password".split(","));
        when(mockRequest.queryParamsValues("reposource")).thenReturn("source,source".split(","));
        when(mockRequest.queryParamsValues("repobranch")).thenReturn("master,master".split(","));


        adminRouteService.postRepo(mockRequest, null);
        verify(mockRepo, times(2)).saveRepo(any());
        verify(mockJobService, times(2)).forceEnqueue(any());
    }

    public void testDeleteRepo() {
        Repo mockRepo = Mockito.mock(Repo.class);
        JobService mockJobService = Mockito.mock(JobService.class);
        DataService mockDataService = Mockito.mock(DataService.class);

        AdminRouteService adminRouteService = new AdminRouteService(mockRepo, mockJobService, mockDataService, null, null);
        Request mockRequest = Mockito.mock(Request.class);

        when(mockRequest.queryParams("repoName")).thenReturn("myRepo");
        when(mockRepo.getRepoByName("myRepo")).thenReturn(new RepoResult());


        adminRouteService.deleteRepo(mockRequest, null);
        verify(mockDataService, times(1)).addToPersistentDelete("");
    }
}
