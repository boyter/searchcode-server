package com.searchcode.app.service;

import com.searchcode.app.App;
import junit.framework.TestCase;
import org.mockito.Mockito;
import spark.ModelAndView;
import spark.Request;

import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

public class CodeRouteServiceTest extends TestCase {
    public void testRootNoQueryString() {
        CodeRouteService codeRouteService = new CodeRouteService();

        Request request = Mockito.mock(Request.class);
        ModelAndView modelAndView = codeRouteService.root(request, null);

        Map<String, Object> model = (Map<String, Object>)modelAndView.getModel();
        String viewName = modelAndView.getViewName();

        assertThat(model.get("photoId")).isInstanceOf(Integer.class);
        assertThat((int)model.get("photoId")).isGreaterThanOrEqualTo(0);
        assertThat((int)model.get("photoId")).isLessThanOrEqualTo(4);
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

        assertThat(viewName).isEqualTo("search_test.ftl");
    }

    public void testHtmlNoQueryString() {
        CodeRouteService codeRouteService = new CodeRouteService();

        Request request = Mockito.mock(Request.class);
        ModelAndView modelAndView = codeRouteService.html(request, null);

        Map<String, Object> model = (Map<String, Object>)modelAndView.getModel();
        String viewName = modelAndView.getViewName();

        assertThat(model.get("photoId")).isInstanceOf(Integer.class);
        assertThat((int)model.get("photoId")).isGreaterThanOrEqualTo(0);
        assertThat((int)model.get("photoId")).isLessThanOrEqualTo(4);
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
}
