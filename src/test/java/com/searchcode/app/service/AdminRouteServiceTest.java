package com.searchcode.app.service;

import com.searchcode.app.service.route.AdminRouteService;
import junit.framework.TestCase;
import org.mockito.Mockito;
import spark.Request;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.when;

public class AdminRouteServiceTest extends TestCase {
    public void testRepositoryReindexApiNotEnabled() {
        AdminRouteService adminRouteService = new AdminRouteService();

        Request mockRequest = Mockito.mock(Request.class);

        Set<String> returnSet = new HashSet<>();
        returnSet.add("reponame");

        when(mockRequest.queryParams()).thenReturn(returnSet);
        when(mockRequest.queryParams("reponame")).thenReturn("pystacks");

        adminRouteService.CheckIndexStatus(mockRequest, null);
    }
}
