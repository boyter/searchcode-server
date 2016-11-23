package com.searchcode.app.service;

import com.searchcode.app.App;
import junit.framework.TestCase;
import org.mockito.Mockito;
import spark.ModelAndView;
import spark.Request;

import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class CodeRouteServiceTest extends TestCase {
    public void testRootNoQueryString() {
        CodeRouteService codeRouteService = new CodeRouteService();

        Request request = Mockito.mock(Request.class);
        ModelAndView modelAndView = codeRouteService.root(request, null);

        Map<String, Object> model = (Map<String, Object>)modelAndView.getModel();
        String viewName = modelAndView.getViewName();

        assertThat(model.get("photoId")).isInstanceOf(Integer.class);
        assertThat(model.get("numDocs")).isInstanceOf(Integer.class);
        assertThat(model.get("logoImage")).isNotNull();
        assertThat(model.get("isCommunity")).isEqualTo(App.ISCOMMUNITY);

        assertThat(viewName).isEqualTo("index.ftl");
    }
}
