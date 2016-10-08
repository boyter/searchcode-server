/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.4.0
 */

/**
 * The implementation of the front end for searchcode server using Mithril.js
 */

// Model that perfoms the search logic and does the actual search
var SearchModel = {
    searchvalue: m.prop(''),
    searchhistory: m.prop(false),
    searchresults: m.prop([]),

    // Used for knowing which filters have been currently selected
    langfilters: m.prop([]),
    repositoryfilters: m.prop([]),
    ownfilters: m.prop([]),
    yearfilters: m.prop([]),
    yearmonthfilters: m.prop([]),
    yearmonthdayfilters: m.prop([]),
    deletedfilters: m.prop([]),

    activelangfilters: m.prop([]),
    activerepositoryfilters: m.prop([]),
    activeownfilters: m.prop([]),

    pages: m.prop([]),
    currentlyloading: m.prop(false),
    currentpage: m.prop(0),

    filterinstantly: m.prop(true),

    // From the response
    totalhits: m.prop(0),
    altquery: m.prop([]),
    query: m.prop(''),
    coderesults: m.prop([]),
    repofilters: m.prop([]),
    languagefilters: m.prop([]),
    ownerfilters: m.prop([]),

    repoFacetYear: m.prop([]),
    repoFacetYearMonth: m.prop([]),
    repoFacetYearMonthDay: m.prop([]),
    repoFacetRevision: m.prop([]),
    repoFacetDeleted: m.prop([]),

    chart: m.prop(undefined),

    clearfilters: function() {
        SearchModel.langfilters([]);
        SearchModel.repositoryfilters([]);
        SearchModel.ownfilters([]);
    },
    toggleinstant: function() {
        if (window.localStorage) {
            localStorage.setItem('toggleinstant', JSON.stringify(!SearchModel.filterinstantly()));
        }
        SearchModel.filterinstantly(!SearchModel.filterinstantly());
    },
    togglefilter: function (type, name) {
        switch(type) {
            case 'language':
                if (_.indexOf(SearchModel.langfilters(), name) === -1) {
                    SearchModel.langfilters().push(name);
                }
                else {
                    SearchModel.langfilters(_.without(SearchModel.langfilters(), name));
                }
                break;
            case 'repo':
                if (_.indexOf(SearchModel.repositoryfilters(), name) === -1) {
                    SearchModel.repositoryfilters().push(name);
                }
                else {
                    SearchModel.repositoryfilters(_.without(SearchModel.repositoryfilters(), name));
                }
                break;
            case 'owner':
                if (_.indexOf(SearchModel.ownfilters(), name) === -1) {
                    SearchModel.ownfilters().push(name);
                }
                else {
                    SearchModel.ownfilters(_.without(SearchModel.ownfilters(), name));
                }
                break;
        }
    },
    filterexists: function (type, name) {
        switch(type) {
            case 'language':
                if (_.indexOf(SearchModel.langfilters(), name) === -1) {
                    return false;
                }
                break;
            case 'repo':
                if (_.indexOf(SearchModel.repositoryfilters(), name) === -1) {
                    return false;
                }
                break;
            case 'owner':
                if (_.indexOf(SearchModel.ownfilters(), name) === -1) {
                    return false;
                }
            case 'year':
                if (_.indexOf(SearchModel.yearfilters(), name) === -1) {
                    return false;
                }
                break;
            case 'yearmonth':
                if (_.indexOf(SearchModel.yearmonthfilters(), name) === -1) {
                    return false;
                }
                break;
            case 'yearmonthday':
                if (_.indexOf(SearchModel.yearmonthdayfilters(), name) === -1) {
                    return false;
                }
                break;
            case 'deleted':
                if (_.indexOf(SearchModel.deletedfilters(), name) === -1) {
                    return false;
                }
                break;
        }

        return true;
    },
    getlangfilters: function() {
        var lang = '';

        if (SearchModel.langfilters().length != 0) {
            lang = '&lan=' + _.map(SearchModel.langfilters(), function(e) { return encodeURIComponent(e); } ).join('&lan=');
        }

        return lang;
    },
    getrepofilters: function() {
        var repo = '';

        if (SearchModel.repositoryfilters().length != 0) {
            repo = '&repo=' + _.map(SearchModel.repositoryfilters(), function(e) { return encodeURIComponent(e); } ).join('&repo=');
        }

        return repo;
    },
    getownfilters: function() {
        var own = '';

        if (SearchModel.ownfilters().length != 0) {
            own = '&own=' + _.map(SearchModel.ownfilters(), function(e) { return encodeURIComponent(e); } ).join('&own=');
        }

        return own;
    },
    setstatechange: function(pagequery, isstatechange) {
        // set the state
        if (isstatechange === undefined) {
            history.pushState({
                searchvalue: SearchModel.searchvalue(),
                langfilters: SearchModel.activelangfilters(),
                repofilters: SearchModel.activerepositoryfilters(),
                ownfilters: SearchModel.activeownfilters(),
                currentpage: SearchModel.currentpage()
            }, 'search', '?q=' + 
                        encodeURIComponent(SearchModel.searchvalue()) + 
                        SearchModel.getlangfilters() + 
                        SearchModel.getrepofilters() + 
                        SearchModel.getownfilters() + 
                        pagequery);
        }
    },
    search: function(page, isstatechange) {
        if (SearchModel.currentlyloading()) {
            return;
        }

        SearchModel.currentlyloading(true);
        m.redraw();

        // If we have filters append them on
        var lang = SearchModel.getlangfilters();
        var repo = SearchModel.getrepofilters();
        var own = SearchModel.getownfilters();

        var searchpage = 0;
        var pagequery = ''
        if(page !== undefined) {
            searchpage = page
            SearchModel.currentpage(page);
            if (searchpage !== 0) {
                pagequery = '&p=' + searchpage;
            }
        }

        // Stringify and parse to create a copy not a reference
        SearchModel.activelangfilters(JSON.parse(JSON.stringify(SearchModel.langfilters())));
        SearchModel.activerepositoryfilters(JSON.parse(JSON.stringify(SearchModel.repositoryfilters())));
        SearchModel.activeownfilters(JSON.parse(JSON.stringify(SearchModel.ownfilters())));

        SearchModel.setstatechange(pagequery, isstatechange);

        var queryurl = '/api/codesearch/?q=' + encodeURIComponent(SearchModel.searchvalue()) + lang + repo + own + '&p=' + searchpage;
        if (SearchModel.searchhistory() === true ) { 
            queryurl = '/api/timecodesearch/?q=' + encodeURIComponent(SearchModel.searchvalue()) + lang + repo + own + '&p=' + searchpage;
        }

        m.request( { method: 'GET', url: queryurl } ).then( function(e) {

            SearchModel.totalhits(e.totalHits);
            SearchModel.altquery(e.altQuery);
            SearchModel.query(e.query);
            SearchModel.pages(e.pages);
            SearchModel.currentpage(e.page);

            SearchModel.coderesults(e.codeResultList);
            SearchModel.repofilters(e.repoFacetResults);
            SearchModel.languagefilters(e.languageFacetResults);
            SearchModel.ownerfilters(e.repoOwnerResults);

            // History fields
            SearchModel.repoFacetYear(e.repoFacetYear);
            SearchModel.repoFacetYearMonth(e.repoFacetYearMonth);
            SearchModel.repoFacetYearMonthDay(e.repoFacetYearMonthDay);
            SearchModel.repoFacetRevision(e.repoFacetRevision);
            SearchModel.repoFacetDeleted(e.repoFacetDeleted);

            SearchModel.currentlyloading(false);
        }).then( function(e) {
            
            if (SearchModel.chart() !== undefined) {
                SearchModel.chart().destroy();
            }

            var ctx = document.getElementById("timeChart");


            //var facet = SearchModel.repoFacetYearMonthDay();
            var facet = SearchModel.repoFacetYearMonthDay();

            var facets = {};
            var labels = [];
            _.each(SearchModel.repoFacetYearMonthDay(), function(e) { 
                facets[e.yearMonthDay] = e.count; 
                labels.push(e.yearMonthDay);
            });

            labels.sort();


            var data = {
                labels: labels,
                datasets: [
                    {
                        label: "",
                        fill: true,
                        lineTension: 0.1,
                        backgroundColor: "rgba(75,192,192,0.4)",
                        borderColor: "rgba(75,192,192,1)",
                        borderCapStyle: 'butt',
                        borderDash: [],
                        borderDashOffset: 0.0,
                        borderJoinStyle: 'miter',
                        pointBorderColor: "rgba(75,192,192,1)",
                        pointBackgroundColor: "#fff",
                        pointBorderWidth: 1,
                        pointHoverRadius: 5,
                        pointHoverBackgroundColor: "rgba(75,192,192,1)",
                        pointHoverBorderColor: "rgba(220,220,220,1)",
                        pointHoverBorderWidth: 2,
                        pointRadius: 1,
                        pointHitRadius: 10,
                        data: _.map(labels, function(e) { return facets[e]; } ),
                        spanGaps: false,
                    }
                ]
            };

            var myLineChart = new Chart(ctx, {
                type: 'line',
                data: data,
                options: {
                    legend: {
                        display: false
                     },
                     responsiveAnimationDuration: 0
                }
            });

            SearchModel.chart(myLineChart);
            
        });
    }
};

// Main component that does everything
var SearchComponent = {
    view: function(ctrl) {
        return m("div", [
                m.component(SearchOptionsComponent),
                m.component(SearchChartComponent),
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
                        m.component(SearchButtonFilterComponent, {
                            totalhits: SearchModel.totalhits(),
                            clearfilters: SearchModel.clearfilters,
                            search: SearchModel.search,
                            languagefilters: SearchModel.langfilters(),
                            repofilters: SearchModel.repositoryfilters(),
                            ownfilters: SearchModel.ownfilters(),
                            filterinstantly: SearchModel.filterinstantly
                        }),
                        m.component(SearchAlternateFilterComponent, {
                            query: SearchModel.query(),
                            altquery: SearchModel.altquery()
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
                        m.component(SearchOwnersFilterComponent, {
                           ownerfilters: SearchModel.ownerfilters(),
                           search: SearchModel.search,
                           filterinstantly: SearchModel.filterinstantly
                        }),
                        m.component(SearchYearFilterComponent),
                        m.component(SearchYearMonthFilterComponent),
                        m.component(SearchYearMonthDayFilterComponent),
                        m.component(SearchDeletedComponent),
                        m.component(FilterOptionsComponent, {
                            filterinstantly: SearchModel.filterinstantly
                        })
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
    view: function(ctrl, args) {
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

        if (args.totalhits === 0 && (args.languagefilters.length + args.repofilters.length + args.ownfilters.length === 0)) {
            return m('div');
        }

        if (args.totalhits === 0 && (args.languagefilters.length + args.repofilters.length + args.ownfilters.length !== 0)) {
            return m('div', [
                m('h5', 'Filter Results'),
                m('input.btn.btn-xs.btn-success.filter-button', { 
                    type: 'submit', 
                    onclick: function() { args.clearfilters(); args.search(); }, 
                    value: 'Remove' })
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
                    disabled: args.filterinstantly,
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
            togglehistory: function() {
                SearchModel.searchhistory(!SearchModel.searchhistory());
                SearchModel.search();
            }
        }
    },
    view: function(ctrl, args) {
        var inputparams = { type: 'checkbox', onclick: function() { 
            SearchModel.toggleinstant();
        } };
        
        if (args.filterinstantly) {
            inputparams.checked = 'checked'
        }

        return m('div', 
            m('h5', 'Filter Options'),
            m('div', [
                m('div.checkbox', 
                    m('label', [
                        m('input', inputparams),
                        m('span', 'Apply Filters Instantly')
                    ])
                ),
                m('div.checkbox', 
                    m('label', [
                        m('input', { type: 'checkbox', onclick: function() { ctrl.togglehistory(); } }),
                        m('span', 'Search Across History')
                    ])
                )
            ])
        );
    }
}

var SearchRepositoriesFilterComponent = {
    controller: function() {
        
        var showall = false;
        var trimlength = 5;
        var filtervalue = '';

        return {
            trimrepo: function (languagefilters) {
                var toreturn = languagefilters;

                if (filtervalue.length === 0 && !showall) {
                    toreturn = _.first(toreturn, trimlength);
                }

                if (filtervalue.length !== 0) {
                    toreturn = _.filter(toreturn, function (e) { 
                        return e.repoName.toLowerCase().indexOf(filtervalue) !== -1; 
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
                SearchModel.togglefilter('repo', repo);
            },
            filtervalue: function(value) {
                filtervalue = value;
            },
            hasfilter: function() {
                return filtervalue.length !== 0;
            },
            getfiltervalue: function() {
                return filtervalue;
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
                        if (args.filterinstantly) {
                            args.search();
                        }
                    },
                    value: res.repoName,
                    count: res.count,
                    checked: SearchModel.filterexists('repo', res.repoName)
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
        var filtervalue = '';
        
        return {
            trimlanguage: function (languagefilters) {
                var toreturn = languagefilters;

                if (filtervalue.length === 0 && !showall) {
                    toreturn = _.first(toreturn, trimlength);
                }

                if (filtervalue.length !== 0) {
                    toreturn = _.filter(toreturn, function (e) { 
                        return e.languageName.toLowerCase().indexOf(filtervalue) !== -1; 
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
                SearchModel.togglefilter('language', language);
            },
            filtervalue: function(value) {
                filtervalue = value;
            },
            hasfilter: function() {
                return filtervalue.length !== 0;
            },
            getfiltervalue: function() {
                return filtervalue;
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
                        if (args.filterinstantly) {
                            args.search();
                        }
                    },
                    value: res.languageName,
                    count: res.count,
                    checked: SearchModel.filterexists('language', res.languageName)
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
        var filtervalue = '';
        
        return {
            trimlanguage: function (ownerfilters) {
                var toreturn = ownerfilters;

                if (filtervalue.length === 0 && !showall) {
                    toreturn = _.first(toreturn, trimlength);
                }

                if (filtervalue.length !== 0) {
                    toreturn = _.filter(toreturn, function (e) { 
                        return e.owner.toLowerCase().indexOf(filtervalue) !== -1; 
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
                SearchModel.togglefilter('owner', owner);
            },
            filtervalue: function(value) {
                filtervalue = value;
            },
            hasfilter: function() {
                return filtervalue.length !== 0;
            },
            getfiltervalue: function() {
                return filtervalue;
            }
        }
    },
    view: function(ctrl, args) {

        var showmoreless = m('div');

        if (args.ownerfilters === undefined || args.ownerfilters.length == 0) {
            return showmoreless;
        }

        if (!ctrl.hasfilter() && ctrl.trimlength() < args.ownerfilters.length) {
            var morecount = args.ownerfilters.length - ctrl.trimlength();

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
            _.map(ctrl.trimlanguage(args.ownerfilters), function(res, ind) {
                return m.component(FilterCheckboxComponent, {
                    onclick: function() { 
                        ctrl.clickenvent(res.owner); 
                        if (args.filterinstantly) {
                            args.search();
                        }
                    },
                    value: res.owner,
                    count: res.count,
                    checked: SearchModel.filterexists('owner', res.owner)
                });
            }),
            showmoreless
        ]);
    }
}

var SearchYearFilterComponent = {
    controller: function() {

        var showall = false;
        var trimlength = 5;
        var filtervalue = '';
        
        return {
            trimlanguage: function (ownerfilters) {
                var toreturn = ownerfilters;

                if (filtervalue.length === 0 && !showall) {
                    toreturn = _.first(toreturn, trimlength);
                }

                if (filtervalue.length !== 0) {
                    toreturn = _.filter(toreturn, function (e) { 
                        return e.year.toLowerCase().indexOf(filtervalue) !== -1; 
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
                SearchModel.togglefilter('owner', owner);
            },
            filtervalue: function(value) {
                filtervalue = value;
            },
            hasfilter: function() {
                return filtervalue.length !== 0;
            },
            getfiltervalue: function() {
                return filtervalue;
            }
        }
    },
    view: function(ctrl, args) {

        var showmoreless = m('div');

        if (SearchModel.repoFacetYear() === undefined || SearchModel.repoFacetYear().length == 0) {
            return showmoreless;
        }

        if (!ctrl.hasfilter() && ctrl.trimlength() < SearchModel.repoFacetYear.length) {
            var morecount = args.ownerfilters.length - ctrl.trimlength();

            showmoreless =  m('a.green', { onclick: ctrl.toggleshowall }, morecount + ' more dates ', m('span.glyphicon.glyphicon-chevron-down'))

            if (ctrl.showall()) {
                showmoreless = m('a.green', { onclick: ctrl.toggleshowall }, 'less dates ', m('span.glyphicon.glyphicon-chevron-up'))
            }
        }

        return m('div', [
            m('h5', 'Year'),
            m('input.repo-filter', {
                onkeyup: m.withAttr('value', ctrl.filtervalue),
                placeholder: 'Filter Dates',
                value: ctrl.getfiltervalue()
            }),
            _.map(ctrl.trimlanguage(SearchModel.repoFacetYear()), function(res, ind) {
                return m.component(FilterCheckboxComponent, {
                    onclick: function() { 
                        ctrl.clickenvent(res.yearMonthDay); 
                        if (args.filterinstantly) {
                            args.search();
                        }
                    },
                    value: res.year,
                    count: res.count,
                    checked: SearchModel.filterexists('year', res.year)
                });
            }),
            showmoreless
        ]);
    }
}

var SearchYearMonthFilterComponent = {
    controller: function() {

        var showall = false;
        var trimlength = 5;
        var filtervalue = '';
        
        return {
            trimlanguage: function (ownerfilters) {
                var toreturn = ownerfilters;

                if (filtervalue.length === 0 && !showall) {
                    toreturn = _.first(toreturn, trimlength);
                }

                if (filtervalue.length !== 0) {
                    toreturn = _.filter(toreturn, function (e) { 
                        return e.yearMonth.toLowerCase().indexOf(filtervalue) !== -1; 
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
                SearchModel.togglefilter('owner', owner);
            },
            filtervalue: function(value) {
                filtervalue = value;
            },
            hasfilter: function() {
                return filtervalue.length !== 0;
            },
            getfiltervalue: function() {
                return filtervalue;
            }
        }
    },
    view: function(ctrl, args) {

        var showmoreless = m('div');

        if (SearchModel.repoFacetYearMonth() === undefined || SearchModel.repoFacetYearMonth().length == 0) {
            return showmoreless;
        }

        if (!ctrl.hasfilter() && ctrl.trimlength() < SearchModel.repoFacetYearMonth().length) {
            var morecount = args.ownerfilters.length - ctrl.trimlength();

            showmoreless =  m('a.green', { onclick: ctrl.toggleshowall }, morecount + ' more dates ', m('span.glyphicon.glyphicon-chevron-down'))

            if (ctrl.showall()) {
                showmoreless = m('a.green', { onclick: ctrl.toggleshowall }, 'less dates ', m('span.glyphicon.glyphicon-chevron-up'))
            }
        }

        return m('div', [
            m('h5', 'Year/Month'),
            m('input.repo-filter', {
                onkeyup: m.withAttr('value', ctrl.filtervalue),
                placeholder: 'Filter Dates',
                value: ctrl.getfiltervalue()
            }),
            _.map(ctrl.trimlanguage(SearchModel.repoFacetYearMonth()), function(res, ind) {
                return m.component(FilterCheckboxComponent, {
                    onclick: function() { 
                        ctrl.clickenvent(res.yearMonth); 
                        if (args.filterinstantly) {
                            args.search();
                        }
                    },
                    value: res.yearMonth,
                    count: res.count,
                    checked: SearchModel.filterexists('yearmonth', res.yearMonth)
                });
            }),
            showmoreless
        ]);
    }
}

var SearchYearMonthDayFilterComponent = {
    controller: function() {

        var showall = false;
        var trimlength = 5;
        var filtervalue = '';
        
        return {
            trimlanguage: function (ownerfilters) {
                var toreturn = ownerfilters;

                if (filtervalue.length === 0 && !showall) {
                    toreturn = _.first(toreturn, trimlength);
                }

                if (filtervalue.length !== 0) {
                    toreturn = _.filter(toreturn, function (e) { 
                        return e.yearMonthDay.toLowerCase().indexOf(filtervalue) !== -1; 
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
                SearchModel.togglefilter('owner', owner);
            },
            filtervalue: function(value) {
                filtervalue = value;
            },
            hasfilter: function() {
                return filtervalue.length !== 0;
            },
            getfiltervalue: function() {
                return filtervalue;
            }
        }
    },
    view: function(ctrl, args) {

        var showmoreless = m('div');

        if (SearchModel.repoFacetYearMonthDay() === undefined || SearchModel.repoFacetYearMonthDay().length == 0) {
            return showmoreless;
        }

        if (!ctrl.hasfilter() && ctrl.trimlength() < SearchModel.repoFacetYearMonthDay().length) {
            var morecount = SearchModel.repoFacetYearMonthDay().length - ctrl.trimlength();

            showmoreless =  m('a.green', { onclick: ctrl.toggleshowall }, morecount + ' more dates ', m('span.glyphicon.glyphicon-chevron-down'))

            if (ctrl.showall()) {
                showmoreless = m('a.green', { onclick: ctrl.toggleshowall }, 'less dates ', m('span.glyphicon.glyphicon-chevron-up'))
            }
        }

        return m('div', [
            m('h5', 'Year/Month/Day'),
            m('input.repo-filter', {
                onkeyup: m.withAttr('value', ctrl.filtervalue),
                placeholder: 'Filter Dates',
                value: ctrl.getfiltervalue()
            }),
            _.map(ctrl.trimlanguage(SearchModel.repoFacetYearMonthDay()), function(res, ind) {
                return m.component(FilterCheckboxComponent, {
                    onclick: function() { 
                        ctrl.clickenvent(res.yearMonthDay); 
                        if (SearchModel.filterinstantly()) {
                            SearchModel.search();
                        }
                    },
                    value: res.yearMonthDay,
                    count: res.count,
                    checked: SearchModel.filterexists('yearmonth', res.yearMonthDay)
                });
            }),
            showmoreless
        ]);
    }
}

var SearchDeletedComponent = {
    controller: function() {
        return {
            clickenvent: function(owner) {
                SearchModel.togglefilter('deleted', owner);
            }
        }
    },
    view: function(ctrl, args) {
        if (SearchModel.repoFacetDeleted() === undefined || SearchModel.repoFacetDeleted().length == 0) {
            return m('div');
        }

        return m('div', [
            m('h5', 'Added/Removed'),
            _.map(SearchModel.repoFacetDeleted(), function(res, ind) {
                return m.component(FilterCheckboxComponent, {
                    onclick: function() { 
                        ctrl.clickenvent(res.deleted); 
                        if (SearchModel.filterinstantly()) {
                            SearchModel.search();
                        }
                    },
                    value: res.deleted,
                    count: res.count,
                    checked: SearchModel.filterexists('deleted', res.deleted)
                });
            })
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
        if(args.totalhits === undefined) {
            return m('div');
        }


        var repos = '';
        var langs = '';
        var owns = '';

        if (args.repofilters.length !== 0) {
            var plural = 'repository';
            if (args.repofilters.length >= 2) {
                plural = 'repositories';
            }

            repos = ' filtered by ' + plural + ' "' + args.repofilters.join(', ') + '"';
        }

        if (args.languagefilters.length !== 0) {
            var plural = 'language';
            if (args.languagefilters.length >= 2) {
                plural = 'languages';
            }

            if (repos === '') {
                langs = ' filtered by ' + plural + ' "';
            }
            else {
                langs = ' and ' + plural + ' "';
            }
            langs = langs + args.languagefilters.join(', ') + '"';
        }

        if (args.ownerfilters.length != 0) {
            var plural = 'owner';

            if (args.ownerfilters.length >= 2) {
                plural = 'owners';
            }

            if (repos === '' && langs === '') {
                owns = ' filtered by ' + plural + ' "';
            }
            else {
                owns = ' and ' + plural + ' "';
            }

            owns = owns + args.ownerfilters.join(', ') + '"';
        }

        document.title = 'Search for "' + args.query + '"' + repos + langs + owns;

        return m('div.row.search-count', [
            m('b', args.totalhits + ' results: '),
            m('span.grey', '"' + args.query + '"' + repos + langs + owns)
        ]);
    }
}

var SearchChartComponent = {
    controller: function() {
        return {}
    },
    view: function(ctrl, args) {
        // return m('div.row.search-chart', [
        //     _.map(SearchModel.repoFacetYearMonthDay(), function(res) {
        //         return m('span', res.yearMonthDay + ' ' + res.count);
        //     })
        // ]);

        return m('div.row.search-chart', 
            m('canvas', {id: 'timeChart', width: '500', height: '50'})
        );  
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
        if(args.altquery === undefined || args.altquery.length === 0) {
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
            gethreflineno: function(result, lineNumber) {
                return '/file/' + result.codeId + '/' + result.codePath + '#' + lineNumber;
            },
            getatag: function(result) {
                return result.fileName + ' in ' + result.repoName;
            },
            getsmallvalue: function(result){
                var fixedCodePath = '/' + result.codePath.split('/').slice(1,100000).join('/');
                return ' | ' + fixedCodePath +' | ' + result.codeLines + ' lines | ' + result.languageName;
            }
        }
    },
    view: function(ctrl, args) {
        return m('div', [
                _.map(args.coderesults, function(res) {
                    return m('div.code-result', [
                        m('div', 
                            m('h5', [
                                m('a', { href: ctrl.gethref(res) }, ctrl.getatag(res)),
                                m('small', ctrl.getsmallvalue(res))  
                            ])
                        ),
                        m('ol.code-result', [
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
    SearchModel.searchvalue(event.state.searchvalue);
    SearchModel.currentpage(event.state.currentpage);
    SearchModel.activelangfilters(event.state.langfilters);
    SearchModel.langfilters(event.state.langfilters);
    SearchModel.activerepositoryfilters(event.state.repofilters);
    SearchModel.repositoryfilters(event.state.repofilters);
    SearchModel.ownfilters(event.state.ownfilters);
    SearchModel.activeownfilters(event.state.ownfilters);

    SearchModel.search(event.state.currentpage, true);
    popstate = true;
};

// For direct links to search results 
if (preload !== undefined) {
    SearchModel.searchvalue(preload.query);
    SearchModel.currentpage(preload.page);

    SearchModel.activelangfilters(preload.languageFacets);
    SearchModel.langfilters(preload.languageFacets);

    SearchModel.activerepositoryfilters(preload.repositoryFacets);
    SearchModel.repositoryfilters(preload.repositoryFacets);

    SearchModel.ownfilters(preload.ownerFacets);
    SearchModel.activeownfilters(preload.ownerFacets);

    SearchModel.search(preload.page, true);
}
