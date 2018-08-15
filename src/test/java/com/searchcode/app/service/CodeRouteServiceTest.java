package com.searchcode.app.service;

import com.searchcode.app.App;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dto.CodeMatchResult;
import com.searchcode.app.dto.CodeResult;
import com.searchcode.app.service.route.CodeRouteService;
import junit.framework.TestCase;
import org.mockito.Mockito;
import spark.HaltException;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

public class CodeRouteServiceTest extends TestCase {

    public void testRootNoQueryString() {
        CodeRouteService codeRouteService = new CodeRouteService();

        Request request = Mockito.mock(Request.class);
        Data mockData = Mockito.mock(Data.class);

        when(mockData.getDataByName(Values.LOGO, Values.EMPTYSTRING)).thenReturn(Values.EMPTYSTRING);

        Singleton.setData(mockData);

        ModelAndView modelAndView = codeRouteService.root(request, null);

        Map<String, Object> model = (Map<String, Object>)modelAndView.getModel();
        String viewName = modelAndView.getViewName();

        assertThat(model.get("photoId")).isInstanceOf(Integer.class);
        assertThat((int)model.get("photoId")).isGreaterThanOrEqualTo(0);
        assertThat((int)model.get("photoId")).isLessThanOrEqualTo(42);
        assertThat(model.get("numDocs")).isInstanceOf(Integer.class);
        assertThat((int)model.get("numDocs")).isGreaterThanOrEqualTo(0);
        assertThat(model.get("logoImage")).isNotNull();
        assertThat(model.get("isCommunity")).isEqualTo(App.ISCOMMUNITY);

        assertThat(viewName).isEqualTo("index.ftl");
    }

    public void testRootQueryString() {
        CodeRouteService codeRouteService = new CodeRouteService();

        Request request = Mockito.mock(Request.class);

        Set<String> hashSet = new HashSet<>();
        hashSet.add("q");
        when(request.queryParams()).thenReturn(hashSet);
        when(request.queryParams("q")).thenReturn("test");

        ModelAndView modelAndView = codeRouteService.root(request, null);

        Map<String, Object> model = (Map<String, Object>)modelAndView.getModel();
        String viewName = modelAndView.getViewName();

        assertThat(model.get("searchValue")).isEqualTo("test");
        assertThat(model.get("searchResultJson")).isNotNull();
        assertThat(model.get("logoImage")).isNotNull();
        assertThat(model.get("isCommunity")).isEqualTo(App.ISCOMMUNITY);

        assertThat(viewName).isEqualTo("search_ajax.ftl");
    }

    public void testHtmlNoQueryString() {
        CodeRouteService codeRouteService = new CodeRouteService();

        Request request = Mockito.mock(Request.class);
        ModelAndView modelAndView = codeRouteService.html(request, null);

        Map<String, Object> model = (Map<String, Object>)modelAndView.getModel();
        String viewName = modelAndView.getViewName();

        assertThat(model.get("photoId")).isInstanceOf(Integer.class);
        assertThat((int)model.get("photoId")).isGreaterThanOrEqualTo(0);
        assertThat((int)model.get("photoId")).isLessThanOrEqualTo(42);
        assertThat(model.get("numDocs")).isInstanceOf(Integer.class);
        assertThat((int)model.get("numDocs")).isGreaterThanOrEqualTo(0);
        assertThat(model.get("logoImage")).isNotNull();
        assertThat(model.get("isCommunity")).isEqualTo(App.ISCOMMUNITY);

        assertThat(viewName).isEqualTo("index.ftl");
    }

    public void testHtmlQueryString() {
        CodeRouteService codeRouteService = new CodeRouteService();

        Request request = Mockito.mock(Request.class);

        Set<String> hashSet = new HashSet<>();
        hashSet.add("q");
        when(request.queryParams()).thenReturn(hashSet);
        when(request.queryParams("q")).thenReturn("test");

        ModelAndView modelAndView = codeRouteService.html(request, null);

        Map<String, Object> model = (Map<String, Object>)modelAndView.getModel();
        String viewName = modelAndView.getViewName();

        assertThat(model.get("searchValue")).isEqualTo("test");
        assertThat(model.get("searchResult")).isNotNull();
        assertThat(model.get("reposQueryString")).isNotNull();
        assertThat(model.get("langsQueryString")).isNotNull();
        assertThat(model.get("ownsQueryString")).isNotNull();
        assertThat(model.get("altQuery")).isNotNull();
        assertThat((int)model.get("totalPages")).isGreaterThanOrEqualTo(0);
        assertThat((boolean)model.get("isHtml")).isTrue();
        assertThat(model.get("logoImage")).isNotNull();
        assertThat(model.get("isCommunity")).isEqualTo(App.ISCOMMUNITY);

        assertThat(viewName).isEqualTo("searchresults.ftl");
    }

    public void testGetCodeNoParams() {
        CodeRouteService codeRouteService = new CodeRouteService();
        Request request = Mockito.mock(Request.class);
        Response response = Mockito.mock(Response.class);

        try {
            codeRouteService.getCode(request, response);
        }
        catch(HaltException ex) {}

        verify(response, times(1)).redirect("/404/");
    }

    public void testGetCodeWithParamsNoMatch() {
        CodeRouteService codeRouteService = new CodeRouteService();
        Request request = Mockito.mock(Request.class);
        Response response = Mockito.mock(Response.class);

        when(request.params(":codeid")).thenReturn("NOTHING-SHOULD-MATCH-THIS-EVER");

        try {
            codeRouteService.getCode(request, response);
        }
        catch(HaltException ex) {}

        verify(response, times(1)).redirect("/404/");
    }

    public void testGetCodeWithParamsWithMatch() {
        Request request = mock(Request.class);
        Response response = mock(Response.class);
        IndexService indexService = mock(IndexService.class);

        CodeRouteService codeRouteService = new CodeRouteService(indexService);

        CodeResult codeResult = new CodeResult(new ArrayList<>(), new ArrayList<>());
        codeResult.setLines("100");
        codeResult.setLanguageName("LanguageName");
        codeResult.setMd5hash("md5hash");
        codeResult.setRepoName("myRepo");
        codeResult.setRepoLocation("repoLocation");
        codeResult.setCodeOwner("codeOwner");
        codeResult.setDisplayLocation("myDisplayLocation");

        when(request.params(":codeid")).thenReturn("MATCH-MOCK");
        when(indexService.getCodeResultByCodeId("MATCH-MOCK")).thenReturn(codeResult);

        Map<String, Object> map = codeRouteService.getCode(request, response);

        assertThat(map.get("codePath")).isEqualTo("myDisplayLocation");
        assertThat(map.get("codeLength")).isEqualTo("100");
        assertThat(map.get("languageName")).isEqualTo("LanguageName");
        assertThat(map.get("md5Hash")).isEqualTo("md5hash");
        assertThat(map.get("repoName")).isEqualTo("myRepo");
        assertThat(map.get("highlight")).isEqualTo(true);
        assertThat(map.get("repoLocation")).isEqualTo("repoLocation");
        assertThat(map.get("codeValue")).isEqualTo("");
        assertThat(map.get("highligher")).isNotNull();
        assertThat(map.get("codeOwner")).isEqualTo("codeOwner");
        assertThat(map.get("owaspResults")).isNotNull();
        assertThat(map.get("logoImage")).isNotNull();
        assertThat(map.get("isCommunity")).isEqualTo(App.ISCOMMUNITY);
        assertThat(map.get("estimatedCost")).isNull();
    }

    public void testGetProject() {
        CodeRouteService codeRouteService = new CodeRouteService();
        Request request = Mockito.mock(Request.class);
        Response response = Mockito.mock(Response.class);

        try {
            codeRouteService.getProject(request, response);
        }
        catch(HaltException ex) {}

        verify(response, times(1)).redirect("/404/");
    }
}
