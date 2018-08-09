/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.14
 */

package com.searchcode.app.config;


import java.text.DecimalFormat;

/**
 * Contains any static values we need locations, names etc....
 */
public class Values {

    // Property values used for looking up values
    // in the properties file
    public static final String PROPERTIES_FILE_NAME = "searchcode.properties";
    public static final String SQLITE_FILE = "sqlite_file";
    public static final String DEFAULT_SQLITE_FILE = "searchcode.sqlite";
    public static final String SERVER_PORT = "server_port";
    public static final String DEFAULT_SERVER_PORT = "8080";
    public static final String INDEXLOCATION = "index_location";
    public static final String DEFAULTINDEXLOCATION = "./index/";
    public static final String FACETSLOCATION = "facets_location";
    public static final String DEFAULTFACETSLOCATION = "./index/facet/";
    public static final String TIMEINDEXLOCATION = "timeindex_location";
    public static final String DEFAULTTIMEINDEXLOCATION = "./index/time/";
    public static final String TIMEINDEXFACETLOCATION = "timeindex_facets_location";
    public static final String DEFAULTTIMEINDEXFACETLOCATION = "./index/time/facet/";
    public static final String REPOSITORYLOCATION = "repository_location";
    public static final String DEFAULTREPOSITORYLOCATION = "./repo/";
    public static final String CHECKREPOCHANGES = "check_repo_chages";
    public static final String DEFAULTCHECKREPOCHANGES = "600";
    public static final String CHECKFILEREPOCHANGES = "check_filerepo_changes";
    public static final String DEFAULTCHECKFILEREPOCHANGES = "600";
    public static final String SPELLINGCORRECTORSIZE = "spelling_corrector_size";
    public static final String DEFAULTSPELLINGCORRECTORSIZE = "10000";
    public static final String ONLYLOCALHOST = "only_localhost";
    public static final String DEFAULTONLYLOCALHOST = "false";
    public static final String LOWMEMORY = "low_memory";
    public static final String DEFAULTLOWMEMORY = "false";
    public static final String GITBINARYPATH = "git_binary_path";
    public static final String DEFAULTGITBINARYPATH = "/usr/bin/git";
    public static final String USESYSTEMGIT ="use_system_git";
    public static final String DEFAULTUSESYSTEMGIT ="false";
    public static final String APIENABLED = "api_enabled";
    public static final String DEFAULTAPIENABLED ="false";
    public static final String APIKEYAUTH = "api_key_authentication";
    public static final String DEFAULTAPIKEYAUTH ="true";
    public static final String SVNENABLED = "svn_enabled";
    public static final String DEFAULTSVNENABLED = "false";
    public static final String SVNBINARYPATH = "svn_binary_path";
    public static final String DEFAULTSVNBINARYPATH = "/usr/bin/svn";
    public static final String MAXDOCUMENTQUEUESIZE = "max_document_queue_size";
    public static final String DEFAULTMAXDOCUMENTQUEUESIZE ="1000";
    public static final String MAXDOCUMENTQUEUELINESIZE = "max_document_queue_line_size";
    public static final String DEFAULTMAXDOCUMENTQUEUELINESIZE ="100000";
    public static final String MAXFILELINEDEPTH = "max_file_line_depth";
    public static final String DEFAULTMAXFILELINEDEPTH = "10000";
    public static final String MAX_FILE_LENGTH_READ = "max_file_length_read";
    public static final String DEFAULT_MAX_FILE_LENGTH_READ = "30000000";
    public static final String OWASPDATABASELOCATION = "owasp_database_location";
    public static final String DEFAULTOWASPDATABASELOCATION = "./include/owasp/database.json";
    public static final String CLASSIFIER_DATABASE_LOCATION = "classifier_database_location";
    public static final String DEFAULT_CLASSIFIER_DATABASE_LOCATION = "./include/classifier/database.json";
    public static final String LICENSE_DATABASE_LOCATION = "license_database_location";
    public static final String DEFAULT_LICENSE_DATABASE_LOCATION = "./include/license/license.json";
    public static final String SOURCE_DATABASE_LOCATION = "source_database_location";
    public static final String DEFAULT_SOURCE_DATABASE_LOCATION = "./include/source/database.json";
    public static final String HIGHLIGHT_LINE_LIMIT = "highlight_lines_limit";
    public static final String DEFAULT_HIGHLIGHT_LINE_LIMIT = "3000";
    public static final String BINARY_WHITE_LIST = "binary_extension_white_list";
    public static final String DEFAULT_BINARY_WHITE_LIST = "";
    public static final String BINARY_BLACK_LIST = "binary_extension_black_list";
    public static final String DEFAULT_BINARY_BLACK_LIST = "";
    public static final String DIRECTORY_BLACK_LIST = "directory_black_list";
    public static final String DEFAULT_DIRECTORY_BLACK_LIST = "";
    public static final String GUESS_BINARY = "binary_guess";
    public static final String DEFAULT_GUESS_BINARY = "true";
    public static final String NUMBER_GIT_PROCESSORS = "number_git_processors";
    public static final String DEFAULT_NUMBER_GIT_PROCESSORS = "2";
    public static final String NUMBER_SVN_PROCESSORS = "number_svn_processors";
    public static final String DEFAULT_NUMBER_SVN_PROCESSORS = "2";
    public static final String NUMBER_FILE_PROCESSORS = "number_file_processors";
    public static final String DEFAULT_NUMBER_FILE_PROCESSORS = "1";
    public static final String AND_MATCH = "default_and_match";
    public static final String DEFAULT_AND_MATCH = "true";
    public static final String LOG_INDEXED = "log_indexed";
    public static final String DEFAULT_LOG_INDEXED = "false";
    public static final String LOG_COUNT = "log_count";
    public static final String DEFAULT_LOG_COUNT = "10";
    public static final String LOG_LEVEL = "log_level";
    public static final String DEFAULT_LOG_LEVEL = "SEVERE";
    public static final String TRASH_LOCATION = "trash_location";
    public static final String DEFAULT_TRASH_LOCATION = "./trash/";
    public static final String LOG_PATH = "log_path";
    public static final String DEFAULT_LOG_PATH = "./logs/";
    public static final String ENCRYPTION_KEY = "encryption_key";
    public static final String DEFAULT_ENCRYPTION_KEY = "39dc5f2168e412be3a9faf9d08482114";
    public static final String FOLLOW_LINKS = "follow_links";
    public static final String DEFAULT_FOLLOW_LINKS = "false";
    public static final String DEEP_GUESS_FILES = "deep_guess_files";
    public static final String DEFAULT_DEEP_GUESS_FILES = "false";
    public static final String HOST_NAME = "host_name";
    public static final String DEFAULT_HOST_NAME = "localhost:8080";
    public static final String DELETE_REPO_AFTER_PROCESS = "delete_repo_after_process";
    public static final String DEFAULT_DELETE_REPO_AFTER_PROCESS = "false";
    public static final String INDEX_ALL_FIELDS = "index_all_fields";
    public static final String DEFAULT_INDEX_ALL_FIELDS = "content,filename,filenamereverse,path,interesting";

    // Lucene stored values for indexing / searching
    public static final String CONTENTS = "contents";
    public static final String PATH = "path"; // The primary key for everything in the index
    public static final String FILELOCATION = "filelocation";
    public static final String FILELOCATIONFILENAME = "filelocationfilename";
    public static final String FILENAME = "filename";
    public static final String MD5HASH = "md5hash";
    public static final String LINES ="lines";
    public static final String CODELINES = "codelines";
    public static final String BLANKLINES = "blanklines";
    public static final String COMMENTLINES = "commentlines";
    public static final String COMPLEXITY = "complexity";
    public static final String REPOLOCATION = "repolocation";
    public static final String MODIFIED = "modified";
    public static final String REVISION = "revision";
    public static final String DATEYEARMONTHDAY = "datedaymonthyear"; // EG 20160523 reversed as test
    public static final String DATEYEARMONTH = "datemonthyear"; // EG 201605
    public static final String DATEYEAR = "dateyear"; // EG 2016
    public static final String MESSAGE = "message"; // Contains git commit message
    public static final String DELETED = "deleted";
    public static final String CODEID = "codeid";
    public static final String SCHASH = "schash"; // Not a real simhash but custom one
    public static final String DISPLAY_LOCATION = "displaylocation";
    // Facets
    public static final String LANGUAGENAME = "languagename";
    public static final String REPONAME = "reponame";
    public static final String CODEOWNER = "codeowner";
    public static final String SOURCE = "source";

    // Lucene values for literal search
    public static final String FILE_NAME_LITERAL = "fn";
    public static final String REPO_NAME_LITERAL = "rn";
    public static final String LANGUAGE_NAME_LITERAL = "ln";
    public static final String OWNER_NAME_LITERAL = "on";
    public static final String DISPLAY_LOCATION_LITERAL = "fl";

    // Random
    public static final String USERSESSIONID = "admin_user";
    public static final String EMPTYSTRING = "";
    public static final String GIT = "git";
    public static final String SVN = "svn";
    public static final String FILE = "file";
    public static final String PAUSED = "paused";
    public static final String RUNNING = "running";
    public static final String UNKNOWN_LANGUAGE = "Unknown";

    // API Endpoint Values
    public static final String PUB = "pub";
    public static final String SIG = "sig";
    public static final String HMAC = "hmac";

    // Cache lookup values
    public static final String CACHE_TOTAL_SEARCH = "statsservice-totalsearch";

    // Used for things stored in data table
    public static final String PERSISTENT_DELETE_QUEUE = "persistent-delete-queue";

    // Job values used in the quartz jobs

    // Used for lookups from the data database table
    public static final String SYNTAXHIGHLIGHTER = "syntax_highlighter";
    public static final String AVERAGESALARY = "average_salary";
    public static final String MATCHLINES = "match_lines";
    public static final String MAXLINEDEPTH = "max_line_depth";
    public static final String MINIFIEDLENGTH = "minified_length";
    public static final String OWASPENABLED = "owasp_enabled";
    public static final String LOGO = "logo";
    public static final String BACKOFFVALUE = "backoff_value";
    public static final String EMBED = "embed";
    public static final String INDEX_READ = "index_read"; // Used to know which index we are referring to
    public static final String INDEX_WRITE = "index_write"; // Used to know which index we are referring to


    public static final String DEFAULTSYNTAXHIGHLIGHTER = "github";
    public static final String DEFAULTAVERAGESALARY = "56000";
    public static final String DEFAULTMATCHLINES = "15";
    public static final String DEFAULTMAXLINEDEPTH = "10000";
    public static final String DEFAULTMINIFIEDLENGTH = "255";
    public static final String DEFAULTBACKOFFVALUE = "0";
    public static final String INDEX_A = "A";
    public static final String INDEX_B = "B";

    // Used for job status
    public static final String REPO_STATUS_INDEXING = "INDEXING";
    public static final String REPO_STATUS_FINISHED = "FINISHED";

    public static final String CHARSET_UTF8 = "UTF8";
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
}
