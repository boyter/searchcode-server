/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

/**
 * The implementation of the front end for searchcode server using Mithril.js
 */

Date.prototype.addDays = function (days) {
    var dat = new Date(this.valueOf())
    dat.setDate(dat.getDate() + days);
    return dat;
}

var HelperModel = {
    getDateSpan: function(startDate, stopDate) {
        var dateArray = new Array();
        var currentDate = startDate;
        while (currentDate <= stopDate) {
            dateArray.push( new Date (currentDate) )
            currentDate = currentDate.addDays(1);
        }
        return dateArray;
    },
    yearMonthDayToDate: function (yearMonthDay) {
        var year = yearMonthDay.substring(0, 4);
        var month = yearMonthDay.substring(5, 7) - 1; // Months are index so 0 = Jan
        var day = yearMonthDay.substring(8, 10);

        var date = new Date(year, month, day);
        return date;
    },
    yearMonthDayDelimit: function (yearMonthDay) {
        var year = yearMonthDay.substring(0, 4);
        var month = yearMonthDay.substring(4, 6);
        var day = yearMonthDay.substring(6, 8);

        return year + '/' + month + '/' + day;
    },
    humanise: function (diff) {
        var str = '';
        var values = [[' Year', 365], [' Month', 30], [' Day', 1]];

        for (var i=0;i<values.length;i++) {
            var amount = Math.floor(diff / values[i][1]);

            if (amount >= 1) {
                str += amount + values[i][0] + (amount > 1 ? 's' : '') + ' ';
                diff -= amount * values[i][1];
            }
        }

        return str.trim();
    }
}


// Model that perfoms the search logic and does the actual search
var SearchModel = {
    searchvalue: m.prop(''),
    pathvalue: m.prop(''),
    searchresults: m.prop([]),

    // Used for knowing which filters have been currently selected by the user
    facetfilters: m.prop({}),

    // Text filters for select filters
    langfiltertext: m.prop(''),
    ownerfiltertext: m.prop(''),
    repofiltertext: m.prop(''),
    sourcefiltertext: m.prop(''),

    // Copy of which filters have been selected TODO make this similar to facet filters
    activefacetfilters: m.prop({}),
    activelangfilters: m.prop([]),
    activerepositoryfilters: m.prop([]),
    activeownfilters: m.prop([]),
    activesrcfilters: m.prop([]),

    currentlyloading: m.prop(false),
    currentpage: m.prop(0),

    // User search options
    filterinstantly: m.prop(true),
    compactview: m.prop(false),
    literalview: m.prop(false),

    // Holds the values from the response
    totalhits: m.prop(0),
    altquery: m.prop([]),
    query: m.prop(''),
    pages: m.prop([]),
    coderesults: m.prop([]),
    repofilters: m.prop([]),
    languagefilters: m.prop([]),
    ownerfilters: m.prop([]),
    sourcefilters: m.prop([]),

    clearfilters: function() {
        // Reset all of the applied filters
        SearchModel.facetfilters({});
        SearchModel.pathvalue('');
    },
    toggle_instant: function() {
        if (window.localStorage) {
            localStorage.setItem('toggleinstant', JSON.stringify(!SearchModel.filterinstantly()));
        }
        SearchModel.filterinstantly(!SearchModel.filterinstantly());
    },
    togglecompact: function() {
        if (window.localStorage) {
            localStorage.setItem('togglecompact', JSON.stringify(!SearchModel.compactview()));
        }
        SearchModel.compactview(!SearchModel.compactview());
    },
    toggleliteral: function() {
        if (window.localStorage) {
            localStorage.setItem('toggleliteral', JSON.stringify(!SearchModel.literalview()));
        }
        SearchModel.literalview(!SearchModel.literalview());
    },
    get_string_title: function() {
        var repos = '';
        var langs = '';
        var owns = '';
        var srcs = '';
        var path = '';
        var literal = '';

        if (SearchModel.activerepositoryfilters().length !== 0) {
            var plural = 'repository';
            if (SearchModel.activerepositoryfilters().length >= 2) {
                plural = 'repositories';
            }

            repos = ' filtered by ' + plural + ' "' + SearchModel.activerepositoryfilters().join(', ') + '"';
        }

        if (SearchModel.activelangfilters().length !== 0) {
            var plural = 'language';
            if (SearchModel.activelangfilters().length >= 2) {
                plural = 'languages';
            }

            if (repos === '') {
                langs = ' filtered by ' + plural + ' "';
            }
            else {
                langs = ' and ' + plural + ' "';
            }
            langs = langs + SearchModel.activelangfilters().join(', ') + '"';
        }

        if (SearchModel.activesrcfilters().length !== 0) {
            var plural = 'source';
            if (SearchModel.activesrcfilters().length >= 2) {
                plural = 'sources';
            }

            if (repos === '') {
                srcs = ' filtered by ' + plural + ' "';
            }
            else {
                srcs = ' and ' + plural + ' "';
            }
            srcs = srcs + SearchModel.activesrcfilters().join(', ') + '"';
        }

        if (SearchModel.activeownfilters().length != 0) {
            var plural = 'owner';

            if (SearchModel.activeownfilters().length >= 2) {
                plural = 'owners';
            }

            if (repos === '' && langs === '') {
                owns = ' filtered by ' + plural + ' "';
            }
            else {
                owns = ' and ' + plural + ' "';
            }

            owns = owns + SearchModel.activeownfilters().join(', ') + '"';
        }

        if (SearchModel.pathvalue() !== '') {
            path = ' filtered to the path "/' + SearchModel.pathvalue().replace(/\_/g, '/') + '/"';
        }

        if (SearchModel.literalview()) {
            literal = ' using literal search';
        }

        return '"' + SearchModel.query() + '"' + repos + langs + owns + path + srcs + literal;
    },
    toggle_filter: function (type, name) {
        // Toggles if a filter should be enabled or not
        var filters = SearchModel.facetfilters();

        var filter = [];
        if (type in filters) {
            filter = filters[type];
        }

        if (_.indexOf(filter, name) === -1) {
            filter.push(name);
        }
        else {
            filter = _.without(filter, name);
        }

        filters[type] = filter;
        SearchModel.facetfilters(filters);
    },
    filter_exists: function (type, name) {
        // Checks if a filter exists IE is enabled
        var filters = SearchModel.facetfilters();

        var filter = [];
        if (type in filters) {
            filter = filters[type];
        }

        if (_.indexOf(filter, name) === -1) {
            return false;
        }

        return true;
    },

    geturlfilters: function(seperator, source) {
        var tmp = '';
        var sep = '&' + seperator + '=';

        if (source.length != 0) {
            tmp = sep + _.map(source, function(e) { return encodeURIComponent(e); } ).join(sep);
        }

        return tmp;
    },
    getfacetfilter: function(type) {
        var filters = SearchModel.facetfilters();

        var filter = [];
        if (type in filters) {
            filter = filters[type];
        }

        return filter;
    },
    get_lang_url_filters: function() {
        var filter = SearchModel.getfacetfilter('language');
        return SearchModel.geturlfilters('lan', filter);
    },
    get_repo_url_filters: function() {
        var filter = SearchModel.getfacetfilter('repo');
        return SearchModel.geturlfilters('repo', filter);
    },
    get_own_url_filters: function() {
        var filter = SearchModel.getfacetfilter('owner');
        return SearchModel.geturlfilters('own', filter);
    },
    get_src_url_filters: function() {
        var filter = SearchModel.getfacetfilter('source');
        return SearchModel.geturlfilters('src', filter);
    },
    get_path_url_filters: function() {
        var path = '';

        if (SearchModel.pathvalue().length !== 0) {
            path = '&fl=' + SearchModel.pathvalue();
        }

        return path;
    },
    get_lit_url_filter: function() {
        if (SearchModel.literalview() === true) {
            return '&lit=true';
        }

        return '';
    },
    setstatechange: function(pagequery, isstatechange) {
        // set the state
        if (isstatechange === undefined) {
            history.pushState({
                searchvalue: SearchModel.searchvalue(),
                langfilters: SearchModel.activelangfilters(),
                repofilters: SearchModel.activerepositoryfilters(),
                ownfilters: SearchModel.activeownfilters(),
                srcfilters: SearchModel.activesrcfilters(),
                currentpage: SearchModel.currentpage(),
                pathvalue: SearchModel.pathvalue()
            }, 'search', '?q=' + 
                        encodeURIComponent(SearchModel.searchvalue()) + 
                        SearchModel.get_lang_url_filters() + 
                        SearchModel.get_repo_url_filters() + 
                        SearchModel.get_own_url_filters() + 
                        SearchModel.get_src_url_filters() + 
                        SearchModel.get_path_url_filters() + 
                        SearchModel.get_lit_url_filter() +
                        pagequery);
        }
    },
    get_search_query_string: function(page) {
        // If we have filters append them on
        var lang = SearchModel.get_lang_url_filters();
        var repo = SearchModel.get_repo_url_filters();
        var own = SearchModel.get_own_url_filters();
        var src = SearchModel.get_src_url_filters();
        var pathvalue = SearchModel.get_path_url_filters();

        var searchpage = 0;
        var pagequery = ''

        var searchpage = 0;
        var pagequery = ''

        if (page !== undefined) {
            searchpage = page
            SearchModel.currentpage(page);
            if (searchpage !== 0) {
                pagequery = '&p=' + searchpage;
            }
        }

        // Stringify and parse to create a copy not a reference
        SearchModel.activelangfilters(JSON.parse(JSON.stringify(SearchModel.getfacetfilter('language'))));
        SearchModel.activerepositoryfilters(JSON.parse(JSON.stringify(SearchModel.getfacetfilter('repo'))));
        SearchModel.activeownfilters(JSON.parse(JSON.stringify(SearchModel.getfacetfilter('owner'))));
        SearchModel.activesrcfilters(JSON.parse(JSON.stringify(SearchModel.getfacetfilter('source'))));

        var queryurl = '?q=' + encodeURIComponent(SearchModel.searchvalue()) + lang + repo + own + src + pathvalue + '&p=' + searchpage;
        return queryurl;
    },
    search: function(page, isstatechange) {
        if (SearchModel.currentlyloading()) {
            return;
        }

        SearchModel.currentlyloading(true);
        m.redraw();

        var queryurl = SearchModel.get_search_query_string(page);

        if (SearchModel.literalview() === true) {
            queryurl = '/api/literalcodesearch/' + queryurl;
        }
        else {
            queryurl = '/api/codesearch/' + queryurl;
        }

        var pagequery = '';
        if (page !== undefined) {
            pagequery = '&p=' + page;
        }
        SearchModel.setstatechange(pagequery, isstatechange);

        m.request({ method: 'GET', url: queryurl} ).then(function(e) {
            if (e !== null) {
                SearchModel.totalhits(e.totalHits);
                SearchModel.altquery(e.altQuery);
                SearchModel.query(e.query);
                SearchModel.pages(e.pages);
                SearchModel.currentpage(e.page);

                SearchModel.coderesults(e.codeResultList);
                SearchModel.repofilters(e.repoFacetResults);
                SearchModel.languagefilters(e.languageFacetResults);
                SearchModel.ownerfilters(e.repoOwnerResults);
                SearchModel.sourcefilters(e.codeFacetSources);
            }

            SearchModel.currentlyloading(false);
        });
    }
};

// Main component that does everything
var SearchComponent = {
    view: function(ctrl) {
        return m("div", [
                m.component(SearchOptionsComponent),
                m.component(SearchCountComponent, { 
                    totalhits: SearchModel.totalhits(), 
                    query: SearchModel.query(),
                    repofilters: SearchModel.activerepositoryfilters(),
                    languagefilters: SearchModel.activelangfilters(),
                    ownerfilters: SearchModel.activeownfilters()
                }),
                
                m.component(SearchLoadingComponent, {
                    currentlyloading: SearchModel.currentlyloading()
                }),
                m('div.row', [
                    m('div.col-md-3.search-filters-container.search-filters', [
                        m.component(SearchNextPreviousComponent, {
                            currentpage: SearchModel.currentpage(), 
                            pages: SearchModel.pages(),
                            setpage: SearchModel.setpage,
                            search: SearchModel.search,
                            totalhits: SearchModel.totalhits(),
                        }),
                        m.component(SearchAlternateFilterComponent, {
                            query: SearchModel.query(),
                            altquery: SearchModel.altquery()
                        }),
                        m.component(SearchSourcesFilterComponent, {
                            sourcefilters: SearchModel.sourcefilters(),
                            search: SearchModel.search,
                            filterinstantly: SearchModel.filterinstantly
                        }),
                        m.component(SearchRepositoriesFilterComponent, {
                            repofilters: SearchModel.repofilters(),
                            search: SearchModel.search,
                            filterinstantly: SearchModel.filterinstantly
                        }),
                        m.component(SearchLanguagesFilterComponent, {
                            languagefilters: SearchModel.languagefilters(),
                            search: SearchModel.search,
                            filterinstantly: SearchModel.filterinstantly
                        }),
                        m.component(SearchOwnersFilterComponent),
                        m.component(SearchPathFilterComponent),
                        m.component(SearchButtonFilterComponent, {
                            totalhits: SearchModel.totalhits(),
                            clearfilters: SearchModel.clearfilters,
                            search: SearchModel.search,
                            languagefilters: SearchModel.getfacetfilter('language'),
                            repofilters: SearchModel.getfacetfilter('repository'),
                            ownfilters: SearchModel.getfacetfilter('owner'),
                            sourcefilters: SearchModel.getfacetfilter('source'),
                            filterinstantly: SearchModel.filterinstantly
                        }),
                        m.component(FilterOptionsComponent, {
                            filterinstantly: SearchModel.filterinstantly
                        }),
                        m.component(RSSComponent)
                    ]),
                    m('div.col-md-9.search-results', [
                        m.component(SearchNoResultsComponent, {
                            totalhits: SearchModel.totalhits(),
                            query: SearchModel.query(),
                            altquery: SearchModel.altquery(),
                        }),
                        m.component(SearchResultsComponent, { 
                            coderesults: SearchModel.coderesults()
                        })
                    ]),
                    m.component(SearchPagesComponent, { 
                        currentpage: SearchModel.currentpage(),
                        pages: SearchModel.pages(),
                        search: SearchModel.search
                    })
                ])
            ]);
    }
}


var SearchNoResultsComponent = {
    controller: function() {
        return {
            doaltquery: function(altquery) {
                SearchModel.searchvalue(altquery);
                SearchModel.search();
            }
        }
    },
    view: function(ctrl, args) {
        if (args.totalhits !== 0) {
            return m('div');
        }

        var suggestion = m('h5', 'Try searching with fewer and more general keywords or if you have filters remove them.');

        if (args.altquery.length !== 0) {

            var message = 'Try one of the following searches instead';
            if (args.altquery.length === 1) {
                message = 'Try the following search instead';
            }
            
            suggestion = m('div', [
                m('h5', message),
                m('ul', { style: { 'list-style-type': 'none' } },
                    _.map(args.altquery, function (e) { 
                        return m('li', m('a', { href: '', onclick: function () { ctrl.doaltquery(e); } }, e)); 
                    } )
                )
            ]);
        }

        return m('div', [
            m('h4', 'No results found for ',  m('i.grey', args.query)),
            suggestion
        ]);
    }
}

var SearchNextPreviousComponent = {
    controller: function() {
    },
    view: function(ctrl, args) {

        if (args.pages === undefined || args.totalhits === undefined || args.totalhits === 0) {
            return m('div');
        }

        var previouspageoptions = '';
        var nextpageoptions = '';

        if (SearchModel.currentpage() == 0) {
            previouspageoptions = 'disabled';
        }

        if ((SearchModel.currentpage() + 1) >= args.pages.length) {
            nextpageoptions = 'disabled';
        }

        return m('div', [
            m('h5', 'Page ' +  (SearchModel.currentpage() + 1) + ' of ' + (args.pages.length == 0 ? 1 : args.pages.length)),
            m('div.center',
                m('input.btn.btn-xs.btn-success.filter-button', { 
                    type: 'submit', 
                    disabled: previouspageoptions,
                    onclick: function() { args.search((SearchModel.currentpage() - 1)); }, 
                    value: '◀ Previous' }
                ),
                m('span', m.trust('&nbsp;')),
                m('input.btn.btn-xs.btn-success.filter-button', { 
                    type: 'submit', 
                    disabled: nextpageoptions,
                    onclick: function() { args.search((SearchModel.currentpage() + 1)); }, 
                    value: 'Next ▶' }
                )
            )
        ]);
    }
}

var SearchLoadingComponent = {
    view: function() {
        var style = {};

        if (SearchModel.currentlyloading() === false) {
            style = { style: { display: 'none' } };
        }

        return m('div.search-loading', style, [
            m('img', { src: '/img/loading.gif' }),
            m('h5', 'Loading...')
        ]);
    }
}

var SearchPagesComponent = {
    controller: function() {
    },
    view: function(ctrl, args) {
        return m('div.search-pagination', 
            m('ul.pagination', [
                _.map(args.pages, function (res) {
                    return m('li', { class: res == SearchModel.currentpage() ? 'active' : '' },
                        m('a', { onclick: function() { 
                            args.search(res); 
                            window.scrollTo(0, 0);
                        } }, res + 1)
                    )
                })
            ])
        );
    }
}

var SearchButtonFilterComponent = {
    controller: function() {
    },
    view: function(ctrl, args) {
        if (args.totalhits === undefined) {
            return m('div');
        }

        if (args.totalhits === 0) {
            return m('div', [
                m('h5', 'Filter Results'),
                m('div.center', 
                    m('input.btn.btn-xs.btn-success.filter-button', { 
                        type: 'submit', 
                        onclick: function() { args.clearfilters(); args.search(); }, 
                        value: 'Remove' }),
                    m('span', m.trust('&nbsp;')),
                    m('span.filter-button', {'style': {'height': '1px', 'float': 'right'}}, '')
                )
            ]);
        }

        return m('div', [
            m('h5', 'Filter Results'),
            m('div.center', 
                m('input.btn.btn-xs.btn-success.filter-button', { 
                    type: 'submit', 
                    onclick: function() { args.clearfilters(); args.search(); }, 
                    value: 'Remove' }
                ),
                m('span', m.trust('&nbsp;')),
                m('input.btn.btn-xs.btn-success.filter-button', { 
                    type: 'submit',
                    disabled: SearchModel.filterinstantly(),
                    onclick: function() { args.search() }, 
                    value: 'Apply' }
                )
            )
        ]);
    }
}

var FilterOptionsComponent = {
    controller: function() {
        return {
            toggle_instant: function() {
                SearchModel.toggle_instant();
            },
            toggle_compact: function() {
                SearchModel.togglecompact();
            },
            toggle_literal: function() {
                SearchModel.toggleliteral();
                SearchModel.search();
            }
        }
    },
    view: function(ctrl, args) {
        var instantparams = { type: 'checkbox', onclick: ctrl.toggle_instant };
        var compactparams = { type: 'checkbox', onclick: ctrl.toggle_compact };
        var literalparams = { type: 'checkbox', onclick: ctrl.toggle_literal };
        
        if (SearchModel.filterinstantly()) {
            instantparams.checked = 'checked'
        }

        if (SearchModel.compactview()) {
            compactparams.checked = 'checked'
        }

        if (SearchModel.literalview()) {
            literalparams.checked = 'checked'
        }


        return m('div', 
            m('h5', 'Search Options'),
            m('div', [
                m('div.checkbox', 
                    m('label', [
                        m('input', instantparams),
                        m('span', 'Apply Filters Instantly')
                    ])
                ),
                m('div.checkbox', 
                    m('label', [
                        m('input', compactparams),
                        m('span', 'Compact View')
                    ])
                ),
                m('div.checkbox', 
                    m('label', [
                        m('input', literalparams),
                        m('span', [
                            m('span', 'Literal Search '),
                            m('small', 
                                m('a', {href: '/documentation/#literal'}, '(help)')
                            )
                        ])
                    ])
                )
            ])
        );
    }
}

var RSSComponent = {
    view: function(ctrl, args) {
        return m('div', 
            m('div', [
                m('div.checkbox', 
                    m('label', [
                        m('a', {'href': '/api/codesearch/rss/' + SearchModel.get_search_query_string() }, 'RSS Feed of Search')
                    ])
                )
            ])
        );
    }
}

var SearchSourcesFilterComponent = {
    controller: function() {
        
        var showall = false;
        var trimlength = 5;

        return {
            trimrepo: function (languagefilters) {
                var toreturn = languagefilters;

                if (SearchModel.sourcefiltertext().length === 0 && !showall) {
                    toreturn = _.first(toreturn, trimlength);
                }

                if (SearchModel.sourcefiltertext().length !== 0) {
                    toreturn = _.filter(toreturn, function (e) { 
                        return e.source.toLowerCase().indexOf(SearchModel.sourcefiltertext().toLowerCase()) !== -1; 
                    } );
                }

                return toreturn;
            },
            toggleshowall: function() {
                showall = !showall;
            },
            showall: function () {
                return showall;
            },
            trimlength: function () {
                return trimlength;
            },
            clickenvent: function(source) {
                SearchModel.toggle_filter('source', source);
            },
            filtervalue: function(value) {
                SearchModel.sourcefiltertext(value);
            },
            hasfilter: function() {
                return SearchModel.sourcefiltertext().length !== 0;
            },
            getfiltervalue: function() {
                return SearchModel.sourcefiltertext();
            }
        }
    },
    view: function(ctrl, args) {
        var showmoreless = m('div');

        return showmoreless;
        if (args.sourcefilters === undefined || args.sourcefilters.length == 0) {
            return showmoreless;
        }

        if (!ctrl.hasfilter() && ctrl.trimlength() < args.sourcefilters.length) {
            var morecount = args.sourcefilters.length - ctrl.trimlength();

            showmoreless =  m('a.green', { onclick: ctrl.toggleshowall }, morecount + ' more sources ', m('span.glyphicon.glyphicon-chevron-down'))

            if (ctrl.showall()) {
                showmoreless = m('a.green', { onclick: ctrl.toggleshowall }, 'less sources ', m('span.glyphicon.glyphicon-chevron-up'))
            }
        }

        return m('div', [
            m('h5', 'Sources'),
            m('input.repo-filter', {
                onkeyup: m.withAttr('value', ctrl.filtervalue),
                placeholder: 'Filter Sources',
                value: ctrl.getfiltervalue()
            }),
            _.map(ctrl.trimrepo(args.sourcefilters), function(res, ind) {
                return m.component(FilterCheckboxComponent, {
                    onclick: function() { 
                        ctrl.clickenvent(res.source);
                        if (SearchModel.filterinstantly()) {
                            args.search();
                        }
                    },
                    value: res.source,
                    count: res.count,
                    checked: SearchModel.filter_exists('source', res.source)
                });
            }),
            showmoreless
        ]);
    }
}

var SearchRepositoriesFilterComponent = {
    controller: function() {
        
        var showall = false;
        var trimlength = 5;

        return {
            trimrepo: function (languagefilters) {
                var toreturn = languagefilters;

                if (SearchModel.repofiltertext().length === 0 && !showall) {
                    toreturn = _.first(toreturn, trimlength);
                }

                if (SearchModel.repofiltertext().length !== 0) {
                    toreturn = _.filter(toreturn, function (e) { 
                        return e.repoName.toLowerCase().indexOf(SearchModel.repofiltertext().toLowerCase()) !== -1; 
                    } );
                }

                return toreturn;
            },
            toggleshowall: function() {
                showall = !showall;
            },
            showall: function () {
                return showall;
            },
            trimlength: function () {
                return trimlength;
            },
            clickenvent: function(repo) {
                SearchModel.toggle_filter('repo', repo);
            },
            filtervalue: function(value) {
                SearchModel.repofiltertext(value);
            },
            hasfilter: function() {
                return SearchModel.repofiltertext().length !== 0;
            },
            getfiltervalue: function() {
                return SearchModel.repofiltertext();
            }
        }
    },
    view: function(ctrl, args) {
        var showmoreless = m('div');

        if (args.repofilters === undefined || args.repofilters.length == 0) {
            return showmoreless;
        }

        if (!ctrl.hasfilter() && ctrl.trimlength() < args.repofilters.length) {
            var morecount = args.repofilters.length - ctrl.trimlength();

            showmoreless =  m('a.green', { onclick: ctrl.toggleshowall }, morecount + ' more repositories ', m('span.glyphicon.glyphicon-chevron-down'))

            if (ctrl.showall()) {
                showmoreless = m('a.green', { onclick: ctrl.toggleshowall }, 'less repositories ', m('span.glyphicon.glyphicon-chevron-up'))
            }
        }

        return m('div', [
            m('h5', 'Repositories'),
            m('input.repo-filter', {
                onkeyup: m.withAttr('value', ctrl.filtervalue),
                placeholder: 'Filter Repositories',
                value: ctrl.getfiltervalue()
            }),
            _.map(ctrl.trimrepo(args.repofilters), function(res, ind) {
                return m.component(FilterCheckboxComponent, {
                    onclick: function() { 
                        ctrl.clickenvent(res.repoName);
                        if (SearchModel.filterinstantly()) {
                            args.search();
                        }
                    },
                    value: res.repoName,
                    count: res.count,
                    checked: SearchModel.filter_exists('repo', res.repoName)
                });
            }),
            showmoreless
        ]);
    }
}

var SearchLanguagesFilterComponent = {
    controller: function() {

        var showall = false;
        var trimlength = 5;
        
        return {
            trimlanguage: function (languagefilters) {
                var toreturn = languagefilters;

                if (SearchModel.langfiltertext().length === 0 && !showall) {
                    toreturn = _.first(toreturn, trimlength);
                }

                if (SearchModel.langfiltertext().length !== 0) {
                    toreturn = _.filter(toreturn, function (e) { 
                        return e.languageName.toLowerCase().indexOf(SearchModel.langfiltertext().toLowerCase()) !== -1; 
                    });
                }

                return toreturn;
            },
            toggleshowall: function() {
                showall = !showall;
            },
            showall: function () {
                return showall;
            },
            trimlength: function () {
                return trimlength;
            },
            clickenvent: function(language) {
                SearchModel.toggle_filter('language', language);
            },
            filtervalue: function(value) {
                SearchModel.langfiltertext(value);
            },
            hasfilter: function() {
                return SearchModel.langfiltertext().length !== 0;
            },
            getfiltervalue: function() {
                return SearchModel.langfiltertext();
            }
        }
    },
    view: function(ctrl, args) {

        var showmoreless = m('div');

        if (args.languagefilters === undefined || args.languagefilters.length == 0) {
            return showmoreless;
        }

        if (!ctrl.hasfilter() && ctrl.trimlength() < args.languagefilters.length) {
            var morecount = args.languagefilters.length - ctrl.trimlength();

            showmoreless =  m('a.green', { onclick: ctrl.toggleshowall }, morecount + ' more languages ', m('span.glyphicon.glyphicon-chevron-down'))

            if (ctrl.showall()) {
                showmoreless = m('a.green', { onclick: ctrl.toggleshowall }, 'less languages ', m('span.glyphicon.glyphicon-chevron-up'))
            }
        }

        return m('div', [
            m('h5', 'Languages'),
            m('input.repo-filter', {
                onkeyup: m.withAttr('value', ctrl.filtervalue),
                placeholder: 'Filter Languages',
                value: ctrl.getfiltervalue()
            }),
            _.map(ctrl.trimlanguage(args.languagefilters), function(res, ind) {
                return m.component(FilterCheckboxComponent, {
                    onclick: function() { 
                        ctrl.clickenvent(res.languageName); 
                        if (SearchModel.filterinstantly()) {
                            args.search();
                        }
                    },
                    value: res.languageName,
                    count: res.count,
                    checked: SearchModel.filter_exists('language', res.languageName)
                });
            }),
            showmoreless
        ]);
    }
}

var SearchOwnersFilterComponent = {
    controller: function() {

        var showall = false;
        var trimlength = 5;
        
        return {
            trimlanguage: function (ownerfilters) {
                var toreturn = ownerfilters;

                if (SearchModel.ownerfiltertext().length === 0 && !showall) {
                    toreturn = _.first(toreturn, trimlength);
                }

                if (SearchModel.ownerfiltertext().length !== 0) {
                    toreturn = _.filter(toreturn, function (e) { 
                        return e.owner.toLowerCase().indexOf(SearchModel.ownerfiltertext().toLowerCase()) !== -1; 
                    });
                }

                return toreturn;
            },
            toggleshowall: function() {
                showall = !showall;
            },
            showall: function () {
                return showall;
            },
            trimlength: function () {
                return trimlength;
            },
            clickenvent: function(owner) {
                SearchModel.toggle_filter('owner', owner);
                if (SearchModel.filterinstantly()) {
                    SearchModel.search();
                }
            },
            filtervalue: function(value) {
                SearchModel.ownerfiltertext(value);
            },
            hasfilter: function() {
                return SearchModel.ownerfiltertext().length !== 0;
            },
            getfiltervalue: function() {
                return SearchModel.ownerfiltertext();
            }
        }
    },
    view: function(ctrl) {
        var showmoreless = m('div');

        if (SearchModel.ownerfilters() === undefined || SearchModel.ownerfilters().length == 0) {
            return showmoreless;
        }

        if (!ctrl.hasfilter() && ctrl.trimlength() < SearchModel.ownerfilters().length) {
            var morecount = SearchModel.ownerfilters().length - ctrl.trimlength();

            showmoreless =  m('a.green', { onclick: ctrl.toggleshowall }, morecount + ' more owners ', m('span.glyphicon.glyphicon-chevron-down'))

            if (ctrl.showall()) {
                showmoreless = m('a.green', { onclick: ctrl.toggleshowall }, 'less owners ', m('span.glyphicon.glyphicon-chevron-up'))
            }
        }

        return m('div', [
            m('h5', 'Owners'),
            m('input.repo-filter', {
                onkeyup: m.withAttr('value', ctrl.filtervalue),
                placeholder: 'Filter Owners',
                value: ctrl.getfiltervalue()
            }),
            _.map(ctrl.trimlanguage(SearchModel.ownerfilters()), function(res, ind) {
                return m.component(FilterCheckboxComponent, {
                    onclick: function() { 
                        ctrl.clickenvent(res.owner); 
                    },
                    value: res.owner,
                    count: res.count,
                    checked: SearchModel.filter_exists('owner', res.owner)
                });
            }),
            showmoreless
        ]);
    }
}

var SearchPathFilterComponent = {
    view: function(ctrl, args) {
        return m('div', [
            m('h5', 'Path Filter'),
            m('div.center', 
                m('input.btn.btn-xs.btn-success.filter-button', { 
                    type: 'submit', 
                    disabled: SearchModel.pathvalue() === '',
                    onclick: function() { SearchModel.pathvalue(''); SearchModel.search(); }, 
                    value: 'Clear' }
                ),
                m('span', m.trust('&nbsp;')),
                m('span.filter-button', {'style': {'height': '1px', 'float': 'right'}}, '')
            )
        ]);
    }
}



var FilterCheckboxComponent = {
    view: function(ctrl, args) {
        var inputparams = { type: 'checkbox', onclick: args.onclick };
        
        if (args.checked) {
            inputparams.checked = 'checked'
        }

        return m('div.checkbox', 
            m('label', [
                m('input', inputparams),
                m('span', args.value),
                m('span', { class: 'badge pull-right' }, args.count)
            ])
        )
    }
}

var SearchOptionsComponent = {
    controller: function() {
        return {
            dosearch: function() {
                SearchModel.search();
            }
        }
    },
    view: function(ctrl, args) {
        return m('div', { class: 'search-options'}, 
            m('form', { onsubmit: function(event) { ctrl.dosearch(); return false; } },
                m('div.form-inline', [
                    m('div.form-group', 
                        m('input.form-control', {
                            autocapitalize: 'off', 
                            autocorrect: 'off',
                            autocomplete: 'off',
                            spellcheck: 'false',
                            size:'50', 
                            placeholder: 'Search Expression',
                            onkeyup: m.withAttr('value', SearchModel.searchvalue),
                            value: SearchModel.searchvalue(),
                            type: 'search',
                            id: 'searchbox'})
                    ),
                    m('input.btn.btn-success', { value: 'search', type: 'submit', onclick: ctrl.dosearch.bind(this) })
                ])
            )
        );
    }
}

var SearchCountComponent = {
    controller: function() {
        return {}
    },
    view: function(ctrl, args) {
        if (args.totalhits === undefined) {
            return m('div');
        }

        var stringTitle = SearchModel.get_string_title();
        document.title = 'Search for ' + stringTitle;

        return m('div.row.search-count', [
            m('b', SearchModel.totalhits() + ' results: '),
            m('span.grey', stringTitle)
        ]);
    }
}


var SearchAlternateFilterComponent = {
    controller: function() {
        // helper functions would go here
        return {
            doaltquery: function(altquery) {
                SearchModel.searchvalue(altquery);
                SearchModel.search();
            }
        }
    },
    view: function(ctrl, args) {
        if (args.altquery === undefined || args.altquery.length === 0) {
            return m('div');
        }

        return m('div', [
            m('h5', 'Alternate Searches'),
            m('div', [
                _.map(args.altquery, function(res) {
                    return m('div.checkbox', 
                        m('label', [
                            m('a', { onclick: function () { ctrl.doaltquery(res)} }, res)
                        ])
                    )
                })
            ])
        ]);
    }
}

var SearchResultsComponent = {
    controller: function() {
        return {
            gethref: function(result) {
                return '/file/' + result.codeId + '/' + result.codePath;
            },
            getrepositoryhref: function(result) {
                return '/repository/overview/' + encodeURIComponent(result.repoName) + '/';
            },
            gethreflineno: function(result, lineNumber) {
                return '/file/' + result.codeId + '/' + result.codePath + '#' + lineNumber;
            },
            getatag: function(result) {
                return result.fileName;
            },
            getsmallvalue: function(result){
                return ' | ' + result.codeLines + ' lines | ' + result.languageName;
            },
            getlinkvalue: function(result) {
                var split = result.displayLocation.split('/');

                var link = [];
                var running = '';

                for (var i = 0; i < split.length; i++) {
                    if (running !== '') {
                        running += '_'
                    }

                    running += split[i]; 
                  
                    link.push({
                        'display': split[i],
                        'value': running,
                        'last': i === (split.length - 1)
                    });
                }

                return _.map(link, function(res) {
                    return res['last'] ? m('span', '/' + res['display']) : m('span', [
                        m('span', '/'),
                        m('a', { onclick: function () { 
                            SearchModel.pathvalue(res['value']);
                            SearchModel.search();
                        }}, res['display'])
                    ]);
                });
            }
        }
    },
    view: function(ctrl, args) {
        return m('div', [
                _.map(args.coderesults, function(res) {
                    return m('div.code-result', [
                        m('div', 
                            m('h5', [
                                m('div', [
                                    m('a', { href: ctrl.gethref(res) }, ctrl.getatag(res)),
                                    m('span', ' in '),
                                    m('a', { href: ctrl.getrepositoryhref(res) }, res.repoName),
                                    m('small', [
                                        m('span', ' '),
                                        ctrl.getlinkvalue(res),
                                        ctrl.getsmallvalue(res)
                                    ])
                                ]),
                                
                            ])
                        ),
                        SearchModel.compactview() ? m('div') : m('ol.code-result', [
                            _.map(res.matchingResults, function(line) {
                                return m('li', { value: line.lineNumber }, 
                                    m('a', { 'href':  ctrl.gethreflineno(res, line.lineNumber) },
                                        m('pre', m.trust(line.line))
                                    )
                                );
                            })
                        ]),
                        m('hr', {class: 'spacer'})
                    ]);
                })
        ]);
    }
}


//Initialize the application
m.mount(document.getElementsByClassName('container')[0], m.component(SearchComponent));

// For when someone hits the back button in the browser
window.onpopstate = function(event) {
    if (event && event.state) {
        SearchModel.searchvalue(event.state.searchvalue);
        SearchModel.currentpage(event.state.currentpage);
        SearchModel.activelangfilters(event.state.langfilters);
        SearchModel.langfilters(event.state.langfilters);
        SearchModel.activerepositoryfilters(event.state.repofilters);
        SearchModel.repositoryfilters(event.state.repofilters);
        SearchModel.ownfilters(event.state.ownfilters);
        SearchModel.activeownfilters(event.state.ownfilters);
        SearchModel.pathvalue(event.state.pathvalue);

        SearchModel.search(event.state.currentpage, true);
        popstate = true;
    }
};

// For direct links to search results 
if (typeof preload !== 'undefined') {
    SearchModel.searchvalue(preload.query);
    SearchModel.currentpage(preload.page);

    SearchModel.activelangfilters(preload.languageFacets);
    _.each(preload.languageFacets, function(e) { 
        SearchModel.toggle_filter('language', e);
    });

    SearchModel.activerepositoryfilters(preload.repositoryFacets);
    _.each(preload.repositoryFacets, function(e) { 
        SearchModel.toggle_filter('repo', e);
    });

    SearchModel.activeownfilters(preload.ownerFacets);
    _.each(preload.ownerFacets, function(e) { 
        SearchModel.toggle_filter('owner', e);
    });

    SearchModel.activesrcfilters(preload.srcFacets);
    _.each(preload.srcFacets, function(e) { 
        SearchModel.toggle_filter('source', e);
    });

    SearchModel.pathvalue(preload.pathValue);
    SearchModel.literalview(preload.isLiteral);

    SearchModel.search(preload.page, true);
}


// For things such as if literal search, compact view or instant search set the values
// based on what is in the users localstorage
if (window.localStorage) {
    var tmp = JSON.parse(localStorage.getItem('toggleinstant'));
    tmp !== null ? SearchModel.filterinstantly(tmp) : SearchModel.filterinstantly(true);

    if (SearchModel.literalview() === true) {
        localStorage.setItem('toggleliteral', JSON.stringify(SearchModel.literalview()));
    }

    tmp = JSON.parse(localStorage.getItem('toggleliteral'));
    tmp !== null ? SearchModel.literalview(tmp) : SearchModel.literalview(false);

    tmp = JSON.parse(localStorage.getItem('togglecompact'));
    tmp !== null ? SearchModel.compactview(tmp) : SearchModel.compactview(false);
}
else {
    SearchModel.filterinstantly(true);
    SearchModel.literalview(false);
    SearchModel.compactview(false);
}
