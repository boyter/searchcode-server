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

// LRU cache in local storage for keeping result sets when paging back and forth
var lruAppCache = new Cache(-1, false, new Cache.LocalStorageCacheStorage('searchcode'));

// Model that perfoms the search logic and does the actual search
var SearchModel = {
    searchvalue: m.prop(''),
    searchhistory: m.prop(false),
    searchresults: m.prop([]),

    langfilters: m.prop([]),
    repositoryfilters: m.prop([]),
    ownfilters: m.prop([]),

    activelangfilters: m.prop([]),
    activerepositoryfilters: m.prop([]),
    activeownfilters: m.prop([]),

    pages: m.prop([]),
    currentlyloading: m.prop(false),
    currentpage: m.prop(0),

    filterinstantly: m.prop(true),

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
                    SearchModel.repositoryfilters(_.without(vm.repositoryfilters, name));
                }
                break;
            case 'owner':
                if (_.indexOf(SearchModel.ownfilters(), name) === -1) {
                    SearchModel.ownfilters().push(name);
                }
                else {
                    SearchModel.ownfilters(_.without(vm.ownfilters, name));
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
                break;
        }

        return true;
    },
    search: function(page, isstatechange) {
        if (SearchModel.currentlyloading()) {
            return;
        }

        // Start loading indicator
        SearchModel.currentlyloading(true);
        m.redraw();

        // If we have filters append them on
        var lang = '';
        var repo = '';
        var own = '';

        if (vm.langfilters.length != 0) {
            lang = '&lan=' + _.map(vm.langfilters, function(e) { return encodeURIComponent(e); } ).join('&lan=');
        }
        if (vm.repositoryfilters.length != 0) {
            repo = '&repo=' + _.map(vm.repositoryfilters, function(e) { return encodeURIComponent(e); } ).join('&repo=');
        }
        if (vm.ownfilters.length != 0) {
            own = '&own=' + _.map(vm.ownfilters, function(e) { return encodeURIComponent(e); } ).join('&own=');
        }

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
        SearchModel.activelangfilters(JSON.parse(JSON.stringify(vm.langfilters)));
        SearchModel.activerepositoryfilters(JSON.parse(JSON.stringify(vm.repositoryfilters)));
        SearchModel.activeownfilters(JSON.parse(JSON.stringify(vm.ownfilters)));

        // set the state
        if (isstatechange === undefined) {
            history.pushState({
                searchvalue: SearchModel.searchvalue(),
                langfilters: SearchModel.activelangfilters(),
                repofilters: SearchModel.activerepositoryfilters(),
                ownfilters: SearchModel.activeownfilters(),
                currentpage: SearchModel.currentpage()
            }, 'search', '?q=' + encodeURIComponent(SearchModel.searchvalue()) + lang + repo + own + pagequery);
        }

        var queryurl = '/api/codesearch/?q=' + encodeURIComponent(vm.searchvalue()) + lang + repo + own + '&p=' + searchpage;
        var cacheHit = lruAppCache.getItem(queryurl);

        var processResult = function(e) {
            vm.coderesults = new testing.CodeResultList();
            
            // Facets/Filters
            vm.repofilters = new testing.RepoFilterList();
            vm.languagefilters = new testing.LanguageFilterList();
            vm.ownerfilters = new testing.OwnerFilterList();

            vm.totalhits = e.totalHits;
            vm.altquery = e.altQuery;
            vm.query = e.query;
            vm.pages = e.pages;
            vm.currentpage(e.page);

            _.each(e.codeResultList, function(res) {
                vm.coderesults.push(new testing.CodeResult(res));
            });

            _.each(e.repoFacetResults, function(res) {
                vm.repofilters.push(new testing.RepoFilter(res));
            });

            _.each(e.languageFacetResults, function(res) {
                vm.languagefilters.push(new testing.LanguageFilter(res));
            });

            _.each(e.repoOwnerResults, function(res) {
                vm.ownerfilters.push(new testing.OwnerFilter(res));
            });

            vm.currentlyloading(false);
            m.redraw();
        };

        if (cacheHit !== null) {
            processResult(cacheHit);
        }
        else {
            m.request({method: 'GET', background: true, url: queryurl })
            .then(function(e) {

                lruAppCache.setItem(queryurl, e, {
                    expirationAbsolute: null,
                    expirationSliding: 10, // Very low, just for paging back and forth
                    priority: Cache.Priority.HIGH
                });

                processResult(e);
            });
        }
    }
};


var testing = {
    CodeResultList: Array,
    RepoFilterList: Array,
    LanguageFilterList: Array,
    OwnerFilterList: Array,

    CodeResult: function(data) {
        this.filename = m.prop(data.fileName);
        this.reponame = m.prop(data.repoName);
        this.matchingresults = m.prop(data.matchingResults);
        this.repolocation = m.prop(data.repoLocation);
        this.documentid = m.prop(data.documentId);
        this.codeid = m.prop(data.codeId);
        this.filelocation = m.prop(data.fileLocation);
        this.codepath = m.prop(data.codePath);
        this.languagename = m.prop(data.languageName);
        this.codelines = m.prop(data.codeLines);
    },

    RepoFilter: function(data) {
        this.count = m.prop(data.count);
        this.source = m.prop(data.repoName);
        this.selected = m.prop(data.selected);
    },

    LanguageFilter: function(data) {
        this.count = m.prop(data.count);
        this.language = m.prop(data.languageName);
        this.selected = m.prop(data.selected);
    },

    OwnerFilter: function(data) {
        this.count = m.prop(data.count);
        this.owner = m.prop(data.owner);
        this.selected = m.prop(data.selected);
    },

    vm: (function() {
        var vm = {}
        vm.init = function() {
            vm.searchvalue = m.prop(''); // set search to nothing initally 
            
            vm.langfilters = [];
            vm.repositoryfilters = [];
            vm.ownfilters = [];

            vm.activelangfilters = [];
            vm.activerepositoryfilters = [];
            vm.activeownfilters = [];

            vm.pages = [];
            vm.currentlyloading = m.prop(false);
            vm.currentpage = m.prop(0);

            if (window.localStorage) {
                var tmp = JSON.parse(localStorage.getItem('toggleinstant'));
                if (tmp !== null) {
                    vm.filterinstantly = tmp;
                }
                else {
                    vm.filterinstantly = true;
                }
            }
            else {
                vm.filterinstantly = true;
            }

            vm.clearfilters = function() {
                vm.langfilters = [];
                vm.repositoryfilters = [];
                vm.ownfilters = [];
            };

            vm.toggleinstant = function() {
                if (window.localStorage) {
                    localStorage.setItem('toggleinstant', JSON.stringify(!vm.filterinstantly));
                }
                vm.filterinstantly = !vm.filterinstantly;
            };

            vm.togglefilter = function (type, name) {
                switch(type) {
                    case 'language':
                        if (_.indexOf(vm.langfilters, name) === -1) {
                            vm.langfilters.push(name);
                        }
                        else {
                            vm.langfilters = _.without(vm.langfilters, name)
                        }
                        break;
                    case 'repo':
                        if (_.indexOf(vm.repositoryfilters, name) === -1) {
                            vm.repositoryfilters.push(name);
                        }
                        else {
                            vm.repositoryfilters = _.without(vm.repositoryfilters, name)
                        }
                        break;
                    case 'owner':
                        if (_.indexOf(vm.ownfilters, name) === -1) {
                            vm.ownfilters.push(name);
                        }
                        else {
                            vm.ownfilters = _.without(vm.ownfilters, name)
                        }
                        break;
                }

            };

            vm.filterexists = function (type, name) {
                switch(type) {
                    case 'language':
                        if (_.indexOf(vm.langfilters, name) === -1) {
                            return false;
                        }
                        break;
                    case 'repo':
                        if (_.indexOf(vm.repositoryfilters, name) === -1) {
                            return false;
                        }
                        break;
                    case 'owner':
                        if (_.indexOf(vm.ownfilters, name) === -1) {
                            return false;
                        }
                        break;
                }

                return true;
            };

            vm.search = function(page, isstatechange) {

                if (vm.currentlyloading()) {
                    return;
                }

                // Start loading indicator
                vm.currentlyloading(true);
                m.redraw();

                // If we have filters append them on
                var lang = '';
                var repo = '';
                var own = '';
                if (vm.langfilters.length != 0) {
                    lang = '&lan=' + _.map(vm.langfilters, function(e) { return encodeURIComponent(e); } ).join('&lan=');
                }
                if (vm.repositoryfilters.length != 0) {
                    repo = '&repo=' + _.map(vm.repositoryfilters, function(e) { return encodeURIComponent(e); } ).join('&repo=');
                }
                if (vm.ownfilters.length != 0) {
                    own = '&own=' + _.map(vm.ownfilters, function(e) { return encodeURIComponent(e); } ).join('&own=');
                }

                var searchpage = 0;
                var pagequery = ''
                if(page !== undefined) {
                    searchpage = page
                    vm.currentpage(page);
                    if (searchpage !== 0) {
                        pagequery = '&p=' + searchpage;
                    }
                }

                vm.activelangfilters = JSON.parse(JSON.stringify(vm.langfilters));
                vm.activerepositoryfilters = JSON.parse(JSON.stringify(vm.repositoryfilters));
                vm.activeownfilters = JSON.parse(JSON.stringify(vm.ownfilters));

                // set the state
                if (isstatechange === undefined) {
                    history.pushState({
                        searchvalue: vm.searchvalue(),
                        langfilters: vm.activelangfilters,
                        repofilters: vm.activerepositoryfilters,
                        ownfilters: vm.activeownfilters,
                        currentpage: vm.currentpage()
                    }, 'search', '?q=' + encodeURIComponent(vm.searchvalue()) + lang + repo + own + pagequery);
                }

                var queryurl = '/api/codesearch/?q=' + encodeURIComponent(vm.searchvalue()) + lang + repo + own + '&p=' + searchpage;
                var cacheHit = lruAppCache.getItem(queryurl);

                var processResult = function(e) {
                    vm.coderesults = new testing.CodeResultList();
                    
                    // Facets/Filters
                    vm.repofilters = new testing.RepoFilterList();
                    vm.languagefilters = new testing.LanguageFilterList();
                    vm.ownerfilters = new testing.OwnerFilterList();

                    vm.totalhits = e.totalHits;
                    vm.altquery = e.altQuery;
                    vm.query = e.query;
                    vm.pages = e.pages;
                    vm.currentpage(e.page);

                    _.each(e.codeResultList, function(res) {
                        vm.coderesults.push(new testing.CodeResult(res));
                    });

                    _.each(e.repoFacetResults, function(res) {
                        vm.repofilters.push(new testing.RepoFilter(res));
                    });

                    _.each(e.languageFacetResults, function(res) {
                        vm.languagefilters.push(new testing.LanguageFilter(res));
                    });

                    _.each(e.repoOwnerResults, function(res) {
                        vm.ownerfilters.push(new testing.OwnerFilter(res));
                    });

                    vm.currentlyloading(false);
                    m.redraw();
                };

                if (cacheHit !== null) {
                    processResult(cacheHit);
                }
                else {
                    m.request({method: 'GET', background: true, url: queryurl })
                    .then(function(e) {

                        lruAppCache.setItem(queryurl, e, {
                            expirationAbsolute: null,
                            expirationSliding: 10, // Very low, just for paging back and forth
                            priority: Cache.Priority.HIGH
                        });

                        processResult(e);
                    });
                }
            };
        }
        return vm;
    }()),
    controller: function() {
        testing.vm.init()
    },
    view: function() {
        return m("div", [
                m.component(SearchOptionsComponent),
                m.component(SearchCountComponent, { 
                    totalhits: testing.vm.totalhits, 
                    query: testing.vm.query,
                    repofilters: testing.vm.activerepositoryfilters,
                    languagefilters: testing.vm.activelangfilters,
                    ownerfilters: testing.vm.activeownfilters
                }),
                m.component(SearchChartComponent, {
                    languagefilters: testing.vm.langfilters,
                    repofilters: testing.vm.repofilters
                }),
                m.component(SearchLoadingComponent, {
                    currentlyloading: testing.vm.currentlyloading
                }),
                m('div.row', [
                    m('div.col-md-3.search-filters-container.search-filters', [
                        m.component(SearchNextPreviousComponent, {
                            currentpage: testing.vm.currentpage, 
                            pages: testing.vm.pages,
                            setpage: testing.vm.setpage,
                            search: testing.vm.search,
                            totalhits: testing.vm.totalhits,
                        }),
                        m.component(SearchButtonFilterComponent, {
                            totalhits: testing.vm.totalhits,
                            clearfilters: testing.vm.clearfilters,
                            search: testing.vm.search,
                            languagefilters: testing.vm.langfilters,
                            repofilters: testing.vm.repositoryfilters,
                            ownfilters: testing.vm.ownfilters,
                            filterinstantly: testing.vm.filterinstantly
                        }),
                        m.component(SearchAlternateFilterComponent, {
                            query: testing.vm.query,
                            altquery: testing.vm.altquery
                        }),
                        m.component(SearchRepositoriesFilterComponent, {
                            repofilters: testing.vm.repofilters,
                            search: testing.vm.search,
                            filterinstantly: testing.vm.filterinstantly
                        }),
                        m.component(SearchLanguagesFilterComponent, {
                            languagefilters: testing.vm.languagefilters,
                            search: testing.vm.search,
                            filterinstantly: testing.vm.filterinstantly
                        }),
                        m.component(SearchOwnersFilterComponent, {
                           ownerfilters: testing.vm.ownerfilters,
                           search: testing.vm.search,
                           filterinstantly: testing.vm.filterinstantly
                        }),
                        m.component(FilterOptionsComponent, {
                            filterinstantly: testing.vm.filterinstantly
                        })
                    ]),
                    m('div.col-md-9.search-results', [
                        m.component(SearchNoResultsComponent, {
                            totalhits: testing.vm.totalhits,
                            query: testing.vm.query,
                            altquery: testing.vm.altquery,
                            query: testing.vm.query
                        }),
                        m.component(SearchResultsComponent, { 
                            coderesults: testing.vm.coderesults 
                        })
                    ]),
                    m.component(SearchPagesComponent, { 
                        currentpage: testing.vm.currentpage, 
                        pages: testing.vm.pages,
                        search: testing.vm.search
                    })
                ])
            ]);
    }
};

var SearchNoResultsComponent = {
    controller: function() {
        return {
            doaltquery: function(altquery) {
                testing.vm.searchvalue(altquery);
                testing.vm.search();
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

        if (args.currentpage() == 0) {
            previouspageoptions = 'disabled';
        }

        if ((args.currentpage() + 1) >= args.pages.length) {
            nextpageoptions = 'disabled';
        }

        return m('div', [
            m('h5', 'Page ' +  (args.currentpage() + 1) + ' of ' + (args.pages.length == 0 ? 1 : args.pages.length)),
            m('div.center',
                m('input.btn.btn-xs.btn-success.filter-button', { 
                    type: 'submit', 
                    disabled: previouspageoptions,
                    onclick: function() { args.search((args.currentpage() - 1)); }, 
                    value: '◀ Previous' }
                ),
                m('span', m.trust('&nbsp;')),
                m('input.btn.btn-xs.btn-success.filter-button', { 
                    type: 'submit', 
                    disabled: nextpageoptions,
                    onclick: function() { args.search((args.currentpage() + 1)); }, 
                    value: 'Next ▶' }
                )
            )
        ]);
    }
}

var SearchLoadingComponent = {
    controller: function() {
    },
    view: function(ctrl, args) {

        var style = {}
        if (args.currentlyloading() === false) {
            style = { style: { display: 'none' } }
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
                    return m('li', { class: res == args.currentpage() ? 'active' : '' },
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
            }
        }
    },
    view: function(ctrl, args) {
        var inputparams = { type: 'checkbox', onclick: function() { 
            testing.vm.toggleinstant();
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
                        return e.source().toLowerCase().indexOf(filtervalue) !== -1; 
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
                testing.vm.togglefilter('repo', repo);
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
                        ctrl.clickenvent(res.source());
                        if (args.filterinstantly) {
                            args.search();
                        }
                    },
                    value: res.source(),
                    count: res.count(),
                    checked: testing.vm.filterexists('repo', res.source())
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
                        return e.language().toLowerCase().indexOf(filtervalue) !== -1; 
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
                testing.vm.togglefilter('language', language);
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
                        ctrl.clickenvent(res.language()); 
                        if (args.filterinstantly) {
                            args.search();
                        }
                    },
                    value: res.language(),
                    count: res.count(),
                    checked: testing.vm.filterexists('language', res.language())
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
                        return e.owner().toLowerCase().indexOf(filtervalue) !== -1; 
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
                testing.vm.togglefilter('owner', owner);
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
                        ctrl.clickenvent(res.owner()); 
                        if (args.filterinstantly) {
                            args.search();
                        }
                    },
                    value: res.owner(),
                    count: res.count(),
                    checked: testing.vm.filterexists('owner', res.owner())
                });
            }),
            showmoreless
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
        // helper functions would go here
        return {
            dosearch: function() {
                testing.vm.search();
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
                            onkeyup: m.withAttr('value', testing.vm.searchvalue),
                            value: testing.vm.searchvalue(),
                            type: 'search',
                            id: 'searchbox'})
                    ),
                    // m('div.form-group', 
                    //     m('select.form-control', [
                    //         m('option', 'Smart'),
                    //         m('option', 'Lucene'),
                    //         m('option', 'Regex'),
                    //     ])
                    // ),
                    m('input.btn.btn-success', { value: 'search', type: 'submit', onclick: ctrl.dosearch.bind(this) })
                ])
            )
        );
    }
}

var SearchCountComponent = {
    controller: function() {
        // helper functions would go here
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
        return m('div.row.search-chart',
            m('canvas', {id: 'search-chart'})
        );
    }
}

var SearchAlternateFilterComponent = {
    controller: function() {
        // helper functions would go here
        return {
            doaltquery: function(altquery) {
                testing.vm.searchvalue(altquery);
                testing.vm.search();
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
        return {}
    },
    view: function(ctrl, args) {
        return m('div', [
                _.map(args.coderesults, function(res) {
                    return m('div.code-result', [
                        m('div', 
                            m('h5', [
                                m('a', { href: '/file/' + res.codeid() + '/' + res.codepath() }, res.filename() + ' in ' + res.reponame()),
                                m('small', ' | ' + res.repolocation() +' | ' + res.codelines() + ' lines | ' + res.languagename())  
                            ])
                        ),
                        m('ol.code-result', [
                            _.map(res.matchingresults(), function(line) {
                                return m('li', { value: line.lineNumber }, 
                                    m('a', { 'href': '/file/' + res.codeid() + '/' + res.codepath() + '#' + line.lineNumber },
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
m.mount(document.getElementsByClassName('container')[0], { 
    controller: testing.controller, 
    view: testing.view
});

// For when someone hits the back button in the browser
window.onpopstate = function(event) {
    testing.vm.searchvalue(event.state.searchvalue);
    testing.vm.currentpage(event.state.currentpage);
    testing.vm.activelangfilters = event.state.langfilters;
    testing.vm.langfilters = event.state.langfilters;
    testing.vm.activerepositoryfilters = event.state.repofilters;
    testing.vm.repositoryfilters = event.state.repofilters;
    testing.vm.ownfilters = event.state.ownfilters;
    testing.vm.activeownfilters = event.state.ownfilters;

    testing.vm.search(event.state.currentpage, true);
    popstate = true;
};

if (preload !== undefined) {
    testing.vm.searchvalue(preload.query);
    testing.vm.currentpage(preload.page);

    testing.vm.activelangfilters = preload.languageFacets;
    testing.vm.langfilters = preload.languageFacets;

    testing.vm.activerepositoryfilters = preload.repositoryFacets;
    testing.vm.repositoryfilters = preload.repositoryFacets;

    testing.vm.ownfilters = preload.ownerFacets;
    testing.vm.activeownfilters = preload.ownerFacets;

    testing.vm.search(preload.page, true);
}
