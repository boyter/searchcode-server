/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file
 */

package com.searchcode.app.config;


/**
 * Contains any static values we need locations, names etc....
 */
public class Values {

    // Property values used for looking up values
    // in the properties file
    public static String SQLITEFILE = "sqlite_file";
    public static String DEFAULTSQLITEFILE = "searchcode.sqlite";
    public static String SERVERPORT = "server_port";
    public static String DEFAULTSERVERPORT = "8080";
    public static String INDEXLOCATION = "index_location";
    public static String DEFAULTINDEXLOCATION = "./index/";
    public static String FACETSLOCATION = "facets_location";
    public static String DEFAULTFACETSLOCATION = "./index/facets/";
    public static String REPOSITORYLOCATION = "repository_location";
    public static String DEFAULTREPOSITORYLOCATION = "./repo/";
    public static String CHECKREPOCHANGES = "check_repo_chages";
    public static String DEFAULTCHECKREPOCHANGES = "600";
    public static String SPELLINGCORRECTORSIZE = "spelling_corrector_size";
    public static String DEFAULTSPELLINGCORRECTORSIZE = "10000";
    public static String ONLYLOCALHOST = "only_localhost";
    public static String DEFAULTONLYLOCALHOST = "false";
    public static String LOWMEMORY = "low_memory";
    public static String DEFAULTLOWMEMORY = "false";
    public static String GITBINARYPATH = "git_binary_path";
    public static String DEFAULTGITBINARYPATH = "/usr/bin/git";
    public static String USESYSTEMGIT ="use_system_git";
    public static String DEFAULTUSESYSTEMGIT ="false";
    public static String APIENABLED = "api_enabled";
    public static String DEFAULTAPIENABLED ="false";
    public static String APIKEYAUTH = "api_key_authentication";
    public static String DEFAULTAPIKEYAUTH ="true";
    public static String SVNENABLED = "svn_enabled";
    public static String DEFAULTSVNENABLED = "false";
    public static String SVNBINARYPATH = "svn_binary_path";
    public static String DEFAULTSVNBINARYPATH = "/usr/bin/svn";
    public static String MAXDOCUMENTQUEUESIZE = "max_document_queue_size";
    public static String DEFAULTMAXDOCUMENTQUEUESIZE ="1000";
    public static String MAXDOCUMENTQUEUELINESIZE = "max_document_queue_line_size";
    public static String DEFAULTMAXDOCUMENTQUEUELINESIZE ="100000";
    public static String MAXFILELINEDEPTH = "max_file_line_depth";
    public static String DEFAULTMAXFILELINEDEPTH = "10000";
    public static String OWASPDATABASELOCATION = "owasp_database_location";
    public static String DEFAULTOWASPDATABASELOCATION = "./include/owasp/database.json";


    // Lucene stored values for indexing / searching
    public static String CONTENTS = "contents";
    public static String PATH = "path";
    public static String FILELOCATION = "filelocation";
    public static String FILELOCATIONFILENAME = "filelocationfilename";
    public static String FILENAME = "filename";
    public static String LANGUAGENAME = "languagename";
    public static String MD5HASH = "md5hash";
    public static String CODELINES = "codelines";
    public static String REPOLOCATION = "repolocation";
    public static String REPONAME = "reponame";
    public static String MODIFIED = "modified";
    public static String CODEOWNER = "codeowner";

    // Random
    public static String USERSESSIONID = "admin_user";
    public static String EMPTYSTRING = "";

    // Job values used in the quartz jobs

    // Used for lookups from the data database table
    public static String SYNTAXHIGHLIGHTER = "syntax_highlighter";
    public static String AVERAGESALARY = "average_salary";
    public static String MATCHLINES = "match_lines";
    public static String MAXLINEDEPTH = "max_line_depth";
    public static String MINIFIEDLENGTH = "minified_length";
    public static String LOGO = "logo";
    public static String DEFAULTSYNTAXHIGHLIGHTER = "github";
    public static String DEFAULTAVERAGESALARY = "56000";
    public static String DEFAULTMATCHLINES = "15";
    public static String DEFAULTMAXLINEDEPTH = "10000";
    public static String DEFAULTMINIFIEDLENGTH = "255";


}
