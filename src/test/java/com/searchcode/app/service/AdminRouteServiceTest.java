package com.searchcode.app.service;

import com.searchcode.app.dao.Repo;
import com.searchcode.app.service.route.AdminRouteService;
import junit.framework.TestCase;
import org.mockito.Mockito;
import spark.Request;

import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AdminRouteServiceTest extends TestCase {

    public void testCheckIndexStatus() {
        AdminRouteService adminRouteService = new AdminRouteService();

        Request mockRequest = Mockito.mock(Request.class);

        Set<String> returnSet = new HashSet<>();
        returnSet.add("reponame");

        when(mockRequest.queryParams()).thenReturn(returnSet);
        when(mockRequest.queryParams("reponame")).thenReturn("hopefullyarandomnamethatdoesnotexist");

        String result = adminRouteService.CheckIndexStatus(mockRequest, null);
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

            String result = adminRouteService.GetStat(mockRequest, null);
            assertThat(result).as("For value %s", stat).isEmpty();
        }
    }

    public void testGetStatValuesExpectValue() {
        AdminRouteService adminRouteService = new AdminRouteService();
        List<String> statValue = Arrays.asList("memoryusage", "loadaverage", "uptime", "searchcount", "spellingcount", "repocount", "numdocs", "servertime", "deletionqueue");

        for(String stat: statValue) {
            Request mockRequest = Mockito.mock(Request.class);

            Set<String> returnSet = new HashSet<>();
            returnSet.add("statname");

            when(mockRequest.queryParams()).thenReturn(returnSet);
            when(mockRequest.queryParams("statname")).thenReturn(stat);

            String result = adminRouteService.GetStat(mockRequest, null);
            assertThat(result).as("For value %s", stat).isNotEmpty();
        }
    }

    public void testPostRepoRepoNamesEmptyNothing() {
        Repo mockRepo = Mockito.mock(Repo.class);
        AdminRouteService adminRouteService = new AdminRouteService(mockRepo);
        Request mockRequest = Mockito.mock(Request.class);

        when(mockRequest.queryParamsValues("reponame")).thenReturn(new String[0]);
        when(mockRequest.queryParamsValues("reposcm")).thenReturn(new String[0]);
        when(mockRequest.queryParamsValues("repourl")).thenReturn(new String[0]);
        when(mockRequest.queryParamsValues("repousername")).thenReturn(new String[0]);
        when(mockRequest.queryParamsValues("repopassword")).thenReturn(new String[0]);
        when(mockRequest.queryParamsValues("reposource")).thenReturn(new String[0]);
        when(mockRequest.queryParamsValues("repobranch")).thenReturn(new String[0]);

        adminRouteService.PostRepo(mockRequest, null);
    }

    public void testPostRepoMultipleRepo() {
        Repo mockRepo = Mockito.mock(Repo.class);

        when(mockRepo.saveRepo(any())).thenReturn(true);

        AdminRouteService adminRouteService = new AdminRouteService(mockRepo);
        Request mockRequest = Mockito.mock(Request.class);

        when(mockRequest.queryParamsValues("reponame")).thenReturn("name,name".split(","));
        when(mockRequest.queryParamsValues("reposcm")).thenReturn("git,git".split(","));
        when(mockRequest.queryParamsValues("repourl")).thenReturn("url,url".split(","));
        when(mockRequest.queryParamsValues("repousername")).thenReturn("username,username".split(","));
        when(mockRequest.queryParamsValues("repopassword")).thenReturn("password,password".split(","));
        when(mockRequest.queryParamsValues("reposource")).thenReturn("source,source".split(","));
        when(mockRequest.queryParamsValues("repobranch")).thenReturn("master,master".split(","));


        adminRouteService.PostRepo(mockRequest, null);
        verify(mockRepo, times(2)).saveRepo(any());
    }


}
