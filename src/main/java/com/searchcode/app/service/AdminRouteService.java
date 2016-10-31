/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.3
 */

package com.searchcode.app.service;


import com.google.inject.Guice;
import com.google.inject.Injector;
import com.searchcode.app.App;
import com.searchcode.app.config.InjectorConfig;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;
import com.searchcode.app.util.Properties;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

public class AdminRouteService {
    private final Injector injector;

    public AdminRouteService() {
        injector = Guice.createInjector(new InjectorConfig());
    }

    public Map<String, Object> AdminPage(Request request, Response response) {
        Map<String, Object> map = new HashMap<>();

        // Put all properties here
        map.put(Values.SQLITEFILE, Properties.getProperties().getProperty(Values.SQLITEFILE, Values.DEFAULTSQLITEFILE));
        map.put(Values.SERVERPORT, Properties.getProperties().getProperty(Values.SERVERPORT, Values.DEFAULTSERVERPORT));
        map.put(Values.REPOSITORYLOCATION, Properties.getProperties().getProperty(Values.REPOSITORYLOCATION, Values.DEFAULTREPOSITORYLOCATION));
        map.put(Values.INDEXLOCATION, Properties.getProperties().getProperty(Values.INDEXLOCATION, Values.DEFAULTINDEXLOCATION));
        map.put(Values.FACETSLOCATION, Properties.getProperties().getProperty(Values.FACETSLOCATION, Values.DEFAULTFACETSLOCATION));
        map.put(Values.CHECKREPOCHANGES, Properties.getProperties().getProperty(Values.CHECKREPOCHANGES, Values.DEFAULTCHECKREPOCHANGES));
        map.put(Values.CHECKFILEREPOCHANGES, Properties.getProperties().getProperty(Values.CHECKFILEREPOCHANGES, Values.DEFAULTCHECKFILEREPOCHANGES));
        map.put(Values.ONLYLOCALHOST, Properties.getProperties().getProperty(Values.ONLYLOCALHOST, Values.DEFAULTONLYLOCALHOST));
        map.put(Values.LOWMEMORY, Properties.getProperties().getProperty(Values.LOWMEMORY, Values.DEFAULTLOWMEMORY));
        map.put(Values.SPELLINGCORRECTORSIZE, Properties.getProperties().getProperty(Values.SPELLINGCORRECTORSIZE, Values.DEFAULTSPELLINGCORRECTORSIZE));
        map.put(Values.USESYSTEMGIT, Properties.getProperties().getProperty(Values.USESYSTEMGIT, Values.DEFAULTUSESYSTEMGIT));
        map.put(Values.GITBINARYPATH, Properties.getProperties().getProperty(Values.GITBINARYPATH, Values.DEFAULTGITBINARYPATH));
        map.put(Values.APIENABLED, Properties.getProperties().getProperty(Values.APIENABLED, Values.DEFAULTAPIENABLED));
        map.put(Values.APIKEYAUTH, Properties.getProperties().getProperty(Values.APIKEYAUTH, Values.DEFAULTAPIKEYAUTH));
        map.put(Values.SVNBINARYPATH, Properties.getProperties().getProperty(Values.SVNBINARYPATH, Values.DEFAULTSVNBINARYPATH));
        map.put(Values.SVNENABLED, Properties.getProperties().getProperty(Values.SVNENABLED, Values.DEFAULTSVNENABLED));
        map.put(Values.MAXDOCUMENTQUEUESIZE, Properties.getProperties().getProperty(Values.MAXDOCUMENTQUEUESIZE, Values.DEFAULTMAXDOCUMENTQUEUESIZE));
        map.put(Values.MAXDOCUMENTQUEUELINESIZE, Properties.getProperties().getProperty(Values.MAXDOCUMENTQUEUELINESIZE, Values.DEFAULTMAXDOCUMENTQUEUELINESIZE));
        map.put(Values.MAXFILELINEDEPTH, Properties.getProperties().getProperty(Values.MAXFILELINEDEPTH, Values.DEFAULTMAXFILELINEDEPTH));
        map.put(Values.OWASPDATABASELOCATION, Properties.getProperties().getProperty(Values.OWASPDATABASELOCATION, Values.DEFAULTOWASPDATABASELOCATION));
        map.put(Values.HIGHLIGHT_LINE_LIMIT, Properties.getProperties().getProperty(Values.HIGHLIGHT_LINE_LIMIT, Values.DEFAULT_HIGHLIGHT_LINE_LIMIT));
        map.put(Values.BINARY_WHITE_LIST, Properties.getProperties().getProperty(Values.BINARY_WHITE_LIST, Values.DEFAULT_BINARY_WHITE_LIST));
        map.put(Values.BINARY_BLACK_LIST, Properties.getProperties().getProperty(Values.BINARY_BLACK_LIST, Values.DEFAULT_BINARY_BLACK_LIST));
        map.put(Values.NUMBER_GIT_PROCESSORS, Properties.getProperties().getProperty(Values.NUMBER_GIT_PROCESSORS, Values.DEFAULT_NUMBER_GIT_PROCESSORS));
        map.put(Values.NUMBER_SVN_PROCESSORS, Properties.getProperties().getProperty(Values.NUMBER_SVN_PROCESSORS, Values.DEFAULT_NUMBER_SVN_PROCESSORS));
        map.put(Values.NUMBER_FILE_PROCESSORS, Properties.getProperties().getProperty(Values.NUMBER_FILE_PROCESSORS, Values.DEFAULT_NUMBER_FILE_PROCESSORS));
        map.put(Values.AND_MATCH, Properties.getProperties().getProperty(Values.AND_MATCH, Values.DEFAULT_AND_MATCH));
        map.put(Values.LOG_INDEXED, Properties.getProperties().getProperty(Values.LOG_INDEXED, Values.DEFAULT_LOG_INDEXED));

        map.put("index_paused", Singleton.getPauseBackgroundJobs() ? "paused" : "running");

        return map;
    }

//    public Map<String, Object> SettingsPage(Request request, Response response) {
//        Map<String, Object> map = new HashMap<>();
//
//        String[] highlighters = "agate,androidstudio,arta,ascetic,atelier-cave.dark,atelier-cave.light,atelier-dune.dark,atelier-dune.light,atelier-estuary.dark,atelier-estuary.light,atelier-forest.dark,atelier-forest.light,atelier-heath.dark,atelier-heath.light,atelier-lakeside.dark,atelier-lakeside.light,atelier-plateau.dark,atelier-plateau.light,atelier-savanna.dark,atelier-savanna.light,atelier-seaside.dark,atelier-seaside.light,atelier-sulphurpool.dark,atelier-sulphurpool.light,brown_paper,codepen-embed,color-brewer,dark,darkula,default,docco,far,foundation,github-gist,github,googlecode,grayscale,hopscotch,hybrid,idea,ir_black,kimbie.dark,kimbie.light,magula,mono-blue,monokai,monokai_sublime,obsidian,paraiso.dark,paraiso.light,pojoaque,railscasts,rainbow,school_book,solarized_dark,solarized_light,sunburst,tomorrow-night-blue,tomorrow-night-bright,tomorrow-night-eighties,tomorrow-night,tomorrow,vs,xcode,zenburn".split(",");
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("logoImage", getLogo());
//        map.put("syntaxHighlighter", getSyntaxHighlighter());
//        map.put("highlighters", highlighters);
//        map.put("averageSalary", "" + (int)getAverageSalary());
//        map.put("matchLines", "" + (int)getMatchLines());
//        map.put("maxLineDepth", "" + (int)getMaxLineDepth());
//        map.put("minifiedLength", "" + (int)getMinifiedLength());
//        map.put("owaspenabled", owaspAdvisoriesEnabled());
//        map.put("isCommunity", ISCOMMUNITY);
//    }
//
//    public double getMatchLines() {
//        if(App.ISCOMMUNITY) {
//            return Double.parseDouble(Values.DEFAULTMATCHLINES);
//        }
//
//        Data data = injector.getInstance(Data.class);
//        String matchLines = data.getDataByName(Values.MATCHLINES);
//
//        if(matchLines == null) {
//            data.saveData(Values.MATCHLINES, Values.DEFAULTMATCHLINES);
//            matchLines = Values.DEFAULTMATCHLINES;
//        }
//
//        return Double.parseDouble(matchLines);
//    }
}
