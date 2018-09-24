# searchcode server

[![Build Status](https://travis-ci.org/boyter/searchcode-server.svg?branch=master)](https://travis-ci.org/boyter/searchcode-server)

[![Video](https://searchcodeserver.com/images/videos/searchcode_server_search.gif)](https://searchcodeserver.com/images/videos/searchcode_server_search.gif)

What is it?
-----------
searchcode server is a powerful code search engine with a sleek web user interface.

searchcode server works in tandem with your source control system, indexing thousands of repositories 
and files allowing you and your developers to quickly find and reuse code across teams.

See https://searchcodeserver.com/ for more details or http://demo.searchcodeserver.com/ for a live demo which is indexing multiple projects using GIT and SVN.

Support
-------
If you are building from source then you get no support and must work within the restrictions specified of the
fair source licence (see LICENSE.txt for details). To purchase support see https://searchcodeserver.com/pricing.html

Submissions/Pull Requests
-------------------------
If you would like to submit code to be integrated into searchcode server please specify that it is dual licenced under Apache Public License v2 and GPL v3. This allows it to be pulled in directly without having to worry about licencing issues in the future.

Before making any submission be sure to run 

	fab test

All tests must pass before any submission will be accepted.

To Build TL/DR;
---------------
 - Install Java 8 (Oracle or OpenJDK)
 - Install Maven
 - Install Python and Fabric (optional)
 - Install NPM and run "npm install -g node-qunit-phantomjs" (optional)
 - mvn test OR fab test (if fabric) to run unit tests
 - fab test_full (if fabric) to run all tests (need to be running application in background first)

To Build
--------

To build searchcode server you need to have any Windows, Linux or OSX machine with Java 8 installed and
maven setup. Idealy you want to use a Unix OS with Python and Python Fabric installed.

Note that Fabric tasks (fab) are not currently supported on Windows. You can attempt to use
them but they will probably need to under Bash for Windows or something similar. 

To run the javascript unit tests through the command line you will need to install NPM and then run

	npm install -g node-qunit-phantomjs 

but you can always run them in your browser if required by opening 

	./src/test/javascript/index.html

To test the application you can either run

	mvn test 

or

	fab test

Note that these tests will only cover unit some integration and javascript unit. For full coverage run 

	fab test_full 

with the application running in the background to ensure everything is working as expected.

To build a full release IE ready for production you should run 

	fab build_release 

which will test compile and build a release into
the release folder and produce the file "searchcode-server.tar.gz" which is a ready to deploy release.

If you want to simply test and run then you can run 

	fab run 

however this will be default build a package and run that. To run quickly just open in your IDE of choice and start running App.java

There are a special bunch of tests used for verifying that indexing logic for GIT and File repositories works correctly. To perform 
this you need to run one of all of the following shell scripts,

	./assets/integration_test/gitload/gitload.sh
	./assets/integration_test/gitupdate/gitupdate.sh
	./assets/integration_test/fileupdatetest/fileload.sh
	./assets/integration_test/fileload/fileload.sh

Then add the git ones as GIT repositories in the application and the file ones as FILE repositories. It is also useful to 
set the properties

	check_repo_chages=60
	check_filerepo_changes=60

but it is not required. Then run searchcode. The scripts will add/remove/update files every 60 seconds which should force searchcode
to add/update/remove files from the index in an attempt to hit as many code paths as possible. With this done there should be no more
than 400 documents indexed at any time (if indexing all 4 repositories) and a minimum of 201 (the fileload.sh files + fileupdatetest files + gitupdate files). Leave things running over several hours to ensure that the logic works correctly.

Before a release is made a build must pass all of the above checks, with the indexer logic tests being run for a minimum of 24 hours. To
ensure that performance is acceptable the tests are also run on a Atom powered netbook.


To Run
------

To run using Docker download the image

https://hub.docker.com/r/searchcode/searchcode-server-community/

and then simply run it. Note that this instance is not production ready, as all configuration and storage is done inside the container, but it is the fastest way to get started.

To run searchcode server locally you will need to have any Windows, Linux or OSX machine with Java 8 installed.
Testing and packaging was done using the below version.

	java version "1.8.0_65"
	Java(TM) SE Runtime Environment (build 1.8.0_65-b17)
	Java HotSpot(TM) 64-Bit Server VM (build 25.65-b01, mixed mode)

Uncompress the file you have downloaded to a directory where you want to run searchcode server.
This directory should have more disk space than the size of the repositories you want to index.

Once unpacked assuming that java is in your path (check with the command java -version) you should be able to run
searchcode with the following command for Linux/OSX/BSD

	./searchcode-server.sh

or for Windows

	searchcode-server.bat

After a few moments searchcode server should be ready to run. By default it will be running on port 8080.
To connect to it enter the following in the browser,

	http://SERVER_IP:8080

Be sure to replace SERVER_IP with the ip address if your server or localhost if running locally.
If you see a page with a search bar then everything is fine.

For further control you may want to edit the above files and include the java -Xmx argument to specify the
amount of RAM to use or any other java option you wish to pass in.

To administer your searchcode server instance you need to click on the Admin link in the top right.
Enter the default password Adm1n234 (change this via the properties file)
to add git repositories. If you need help check the documentation page (link at the bottom
of every page).

To setup using Ubuntu as an Update or SystemD job see https://searchcodeserver.com/knowledge-base/upstart-and-systemd-startup-scripts-for-ubuntu.html

Upgrades
--------

Always keep in mind that upgrades are best done with a full reindex. You can either do this by deleting the contents
of the index directory that you have configured in your searchcode.properties file or by clicking the "Recrawl & Rebuild Indexes" 
button in the admin screen. 

To upgrade your current instance of searchcode perform the following steps.

* Stop your current instance of searchcode server
* Make a backup copy of your current instances searchcode.properties and searchcode.sqlite files.
* Uncompress the package to a new directory.
* You can either
    * Copy the uncompressed files over your current instance overwriting if prompted.
    * Copy the directory dependancy-jars and all contents overwriting your current
    * Copy the following files searchcode-1.3.14.jar searchcode-server.bar and searchcode-server.sh to your instance directory

* Start your instance again
* Login to the admin screen and click the "Recrawl & Rebuild Indexes" button

It is also worth comparing your searchcode.properties file to the new one (or the documentation page) as there may be new configuration that you can use. Also be sure to check the settings page as there is likely to be new settings you can use.

Random
------

Background images are all resized using the following command prior to being committed

	convert 1.jpg -quality 75 -resize 1600x1200 1.jpg

Luke
----

If you wish to use Luke to inspect the index you will need to use whatever version matches lucence in the pom.xml
Currently this is 5.5.0 so you would need to use the same version of luke https://github.com/DmitryKey/luke/releases/tag/luke-5.5.0 

Licence
-------
Copyright (c) 2016 Boyter Online Services

Use of this software is governed by the Fair Source License included in the LICENSE.txt file

In order to deal with the case of my death or this software becoming abandoned it has an open eventually clause where the licence will change exactly 3 years after the publish date of a version release. This means that if version 1.0.0 was released on 1 July 2010 then it can be taken using the listed alternate licence on 2 July 2013. This licence, version and time is all specified below.

After the following date 2021/09/24 the software version '1.3.14' is dual licenced under the Fair Source Licence included in the LICENSE.txt file or under the GNU General Public License Version 3 with terms specified at https://www.gnu.org/licenses/gpl-3.0.txt

OWASP Database is licensed under https://creativecommons.org/licenses/by-sa/3.0/ sourced under Creative Commons from https://codecrawler.codeplex.com/ https://www.owasp.org/index.php/Category:OWASP_Code_Crawler and https://www.owasp.org/index.php/OWASP_Code_Review_Guide_Table_of_Contents this database was modified to JSON and with slight corrections to spelling and puncuation where applicable.

File Classifier Database is licensed under https://creativecommons.org/licenses/by-sa/3.0/


Change Log
==========

14 September 2018 - 1.3.14 https://github.com/boyter/searchcode-server/projects/7
 - Move over to scc language definitions for identifying languages https://github.com/boyter/searchcode-server/issues/186
 - Resolve issue with searching for identifiers in C++ namespaces https://github.com/boyter/searchcode-server/issues/188
 - Improved binary file detection https://github.com/boyter/searchcode-server/issues/195
 - New API endpoint to return version https://github.com/boyter/searchcode-server/issues/193

15 June 2018 - 1.3.13 https://github.com/boyter/searchcode-server/projects/6
 - Repository names now support any character https://github.com/boyter/searchcode-server/issues/122
 - Better reporting on repository failures with indicator in repository list admin page https://github.com/boyter/searchcode-server/issues/164
 - Resolve issue where By File Count and By Line Count would not match on repository overview page
 - Add ability to control what file fields are indexed https://github.com/boyter/searchcode-server/issues/179
 - Resolve broken filter when using upper/lower case in filters https://github.com/boyter/searchcode-server/issues/184
 - Resolve bug where files were re-read accidently resulting in invalid counts https://github.com/boyter/searchcode-server/issues/185

01 February 2018 - 1.3.12 https://github.com/boyter/searchcode-server/projects/5
 - Reversed filename in search https://github.com/boyter/searchcode-server/issues/160
 - Add click through to original repository https://github.com/boyter/searchcode-server/issues/162
 - Can now edit some properties of repositories https://github.com/boyter/searchcode-server/issues/162
 - Add filter by source https://github.com/boyter/searchcode-server/issues/162
 - Additional images on main page https://github.com/boyter/searchcode-server/issues/166
 - Add fix to stop indexer crashing when encountering large files with no newlines https://github.com/boyter/searchcode-server/issues/168
 - Version check now checks searchcodeserver.com

29 September 2017 - 1.3.11 https://github.com/boyter/searchcode-server/projects/4
 - Add link to repository source on repository overview page https://github.com/boyter/searchcode-server/issues/126
 - Removed property index_queue_batch_size as it is no longer required
 - Change check_filerepo_changes to default size of 600 as check is now far more efficient https://github.com/boyter/searchcode-server/issues/133
 - Add ability to reindex without downtime https://github.com/boyter/searchcode-server/issues/129
 - Index now runs in parallel for faster index time https://github.com/boyter/searchcode-server/issues/135
 - Add simplistic healthcheck endpoint https://github.com/boyter/searchcode-server/issues/138
 - Add literal lucene search option https://github.com/boyter/searchcode-server/issues/89
 - Add repository path filters to UI https://github.com/boyter/searchcode-server/issues/136
 - Can filter down to language or owner from project overview page https://github.com/boyter/searchcode-server/issues/127
 - Signed API add now supports %20 or + for spaces https://github.com/boyter/searchcode-server/issues/134
 - Add support for extensions that have a dot in them https://github.com/boyter/searchcode-server/issues/146
 - Slightly modified admin screen https://github.com/boyter/searchcode-server/issues/153
 - Add file tree display on code result pages https://github.com/boyter/searchcode-server/issues/154
 - Fix for thread exhaustion issue https://github.com/boyter/searchcode-server/issues/85

15 June 2017 - 1.3.10
 - BREAKING CHANGE Changed validation of repository names such that they must be alphanumeric, _ or - with client and server side validation
 - BREAKING CHANGE Fix spelling of check_filerepo_chages to check_filerepo_changes for properties file
 - Set follow symlinks to be configurable through properties file https://github.com/boyter/searchcode-server/issues/99
 - Clicking Remove will also clear the text box filters https://github.com/boyter/searchcode-server/issues/98
 - Improved stop/reset jobs logic, deleted jobs persist on searchcode restart https://github.com/boyter/searchcode-server/issues/41
 - Add logic to calculate project stats by lines not files and display next to existing https://github.com/boyter/searchcode-server/issues/103
 - Deep guess logic added to guess a files type based on keyword heuristic's https://github.com/boyter/searchcode-server/issues/105
 - Additional languages added to classifier database, F#, Mathematica, Parrot, Puppet, Rakefile, PKGBUILD, Cargo, Lock, License
 - API auditing via logs added https://github.com/boyter/searchcode-server/issues/57
 - Search results now have RSS feed https://github.com/boyter/searchcode-server/issues/114
 - Can add custom HTML/CSS/JS to all pages https://github.com/boyter/searchcode-server/issues/107
 - Add average index time seconds to repo overview page https://github.com/boyter/searchcode-server/issues/118
 - Fix bug where unable to filter on html page https://github.com/boyter/searchcode-server/issues/120

13 April 2017 - 1.3.9
 - Repository table can now be created from empty database if required https://github.com/boyter/searchcode-server/issues/83
 - Add indexer pause button back to admin pages https://github.com/boyter/searchcode-server/issues/84
 - Resolve bug where filters could match multiple https://github.com/boyter/searchcode-server/issues/86
 - Batch index size is now configurable through properties file https://github.com/boyter/searchcode-server/issues/85
 - Mitigate issue where matching some files will crash search https://github.com/boyter/searchcode-server/issues/88
 - Update highlight.js https://github.com/boyter/searchcode-server/issues/22
 - Resolve issue where database had no keywords for language guessing https://github.com/boyter/searchcode-server/issues/87

20 March 2017 - 1.3.8
 - Fix bug where if only_localhost is set to false but log_indexed is set to true the CSV will not be written
 - Small CSS display issues on search pages
 - Resolve issues with some unit tests being inconsistent
 - Setup CI pipeline to shake out issues and IWOMM
 - New admin pages which expose information in a better way
 - File classification moved into JSON database similar to OWASP
 - Improved matching of found lines
 - Addition of folder blacklist to exclude folders from being indexed
 - Addition of repository overview page to list indexed repositories and status
 - Add search button to repository overview to search current repository
 - Improved matching of filenames

01 February 2017 - 1.3.7
 - Fix display bug where file details in search was on newline
 - Slight improvements to documentation

31 January 2017 - 1.3.6
 - Add job to scan repositories updating the spelling corrector
 - Add logic to split interesting characters such as Chinese/Korean/Japanese to allow searching
 - Fix XSS on admin log page
 - Fix XSS on admin repo page
 - Fix bug where unable to delete repositories on admin page with special characters in name
 - Running jobs now displayed on admin page
 - Repository overview page added to get overview of a repository

19 December 2016 - 1.3.5
 - Fix only_localhost to really only listen localhost not drop connections
 - Add SHA512 hmac as option for signed API requests
 - Resolve XSS vulnerability issue
 - Add logic to deal with immutable files by moving to trash folder
 - Add log to display most recent search results
 - Resolve several memory leak issues
 - Modify logic to allow matching URL_CONFIG in App.URL_CONFIG.toString()
 - Modify logic to allow matching install_something in :install_something
 - Logging level of OFF now does not even attempt to create log file
 - Resolve uncaught exception when writing log
 - Close resource/memory leak in opened files
 - Changed removal logic for removed files to be less aggressive
 - Fix potential overflow for search query count (however unlikely that is)
 - Fix race conditions with logging

21 November 2016 - 1.3.4
 - Refactor index logic to resolve removal bugs
 - Optimise memory usage of file repositories
 - Fix headers for API responses
 - Change admin fields from disabled to readonly to allow copy paste
 - Fix bug where sqlite_file property was being ignored

02 November 2016 - 1.3.3
 - Add log level selection to admin logs page
 - Can now search across filenames and paths
 - Slight performance improvements for indexing
 - Better error reporting for repository errors
 - Better guessing of file encoding type
 - White/Black list of file extensions
 - Fix bug where OWASP results not escaped
 - Add logout button to admin sections
 - Refactor JavaScript and fix bugs
 - Additional properties for controlling background threads
 - Fix off by 1 error where some results would not be shown
 - Admin can now selectively pause indexing
 - Ability to log out all indexed and excluded files to CSV
 - Set file rotation to have 10x10mb files by default but controlled through property
 - Add backoff setting to settings page to control CPU usage
 - Improve memory usage for large file repositories
 - Whitelist trumps minified logic

04 October 2016 - 1.3.2
 - Improve matching of certain strings such as E.G. linux2.7.4 or i386
 - Fix bug with binary file checking

27 September 2016 - 1.3.1
 - Fix potential performance issue in spelling corrector with long strings
 - Fix potential XSS issue with HTML page
 - Line numbers now appear on code result pages
 - Logging directory not configurable via properties
 - Symlinks path traversal issue resolved
 - Highlight.js now lazy loads
 - Highlight limit now configrable through properties
 - OWASP line number display correction
 - Header links resolved in some cases

20 September 2016 - 1.3.0
 - Filters now apply instantly when clicked option is toggleable
 - OWASP checking on code result pages based on OWASP code crawler
 - Log Tail screen added to Admin
 - Add server date time output to admin page
 - Add rebuild all button to Admin page
 - Fix bug where clicking deep links on HTML page would break
 - Modify file links to use unique codeid to avoid click through issues
 - Improve indexing performance by intelligently calculating MD5
 - Add ability to index arbitary file system locations
 - Add API endpoint to force reindex

30 August 2016 - 1.2.4
 - Add gzip compression to most used routes
 - Refactor repository jobs to resolve bugs and make more extensible
 - Fix lines to index bug with SVN which could result in index pausing
 - Update HTML page to have new look and feel

23 August 2016 - 1.2.3
 - Upgrade mithril to 0.2.5 to resolve Safari bug where pages would not render
 - Release under fair source license
 - Set search box to use monospaced font

17 August 2016 - 1.2.2
 - Add -w option to external git to avoid whitespace blame issue
 - Resolve bug where some results were searchable but not browsable
 - Resolve SVN cert prompt issue causing SVN to never checkout
 - Fix potential performance issues with spell checker

28 June 2016 - 1.2.1
 - Resolve string exception bug when using local git on some files
 - Resolve bug where empty username, language or repository caused indexing to throw exception
 - Add additional information about resolving ulimit errors to documentation

09 June 2016 - 1.2.0
 - Add SVN repository support
 - Resolve highlight issue where overlapping matching text caused issues
 - Add search operators AND OR NOT and wildcard E.G. searc*
 - Resolve bug where some files when clicked would be labeled as 404 while being searchable

30 May 2016 - 1.1.3
 - New property added to control depth of the file to index

27 May 2016 - 1.1.2
 - Additional resolve bug on RedHat/CentOS where file owner always resolves as "unknown"
 - Additional memory improvements
 - New properties added to control size of indexing queue size

20 May 2016 - 1.1.1
 - Resolve bug on RedHat/CentOS where file owner always resolves as "unknown"
 - Adjust client side cache times to mitigate sleeping laptop cache issue
 - Small performance tweaks

13 May 2016 - 1.1.0
 - Updated to allow indexing of named branches
 - Additional memory improvements
 - Logging of exceptions/warnings to rolling file
 - Logging level support added via properties file
 - Caching improvements
 - Resolve issue where sometimes .git directory files would be indexed
 - Improve delta calculation between revisions to improve indexing speed
 - Fix bug where deleted repository indexing jobs would continue to run
 - Fix bug where missing source could cause clicking through to result bug
 - Additional file types added to file classifier
 - Improved COCOMO calculation
 - API Endpoint addition allowing adding, removing and list of repositories
 - Increase indexing priority to ensure index refreshes happen faster
 - Allow use of tilde ~ in repository names
 - Improvements to index/clone when server unexpectedly stopped
 - Paging added to the repository page
 - Many additional bug fixes

20 April 2016 - 1.0.3
 - Improve memory performance across indexing and searching
 - Add additional parameters allowing memory usage to be configured
 - Resolve null pointer exceptions in various areas of the search pipeline
 - Fix removal of repository logic

14 April 2016 - 1.0.2
 - Fix bug where unable to add new repository

13 April 2016 - 1.0.1
 - Updated indexer code to improve performance
 - Fix MD5 file calculation bug
 - Add owner and user filter into index
 - Add Match Lines, Max Line Depth, Minified Length to settings page
 - Change links to files away from document id to path
 - Added splitting of terms where possible such as ArrayList into Array List
 - Improved match relevance for searches
 - Improved calculation of COCOMO to be more accurate
 - Fixed overloaded issues when server under high load
 - Updated language identifiation to pick up languages, Varnish Configuration, Razor Template, Jade Template, ReStructuredText, Razor Template, Handlebars Template
 - Performance improvements for all pages using smart caching of reused assets as found through load tests

31 March 2016 - 1.0.0
 - Updated look and feel with new fancy single page application design
 - Improved performance of search and match 3-20x 
 - Additional configuration of properties file allowing reverting to pure HTML page if required
 - Various bug fixes

08 March 2016 - 0.9.9
 - Inital release

Join the chat at https://gitter.im/searchcode-server/Lobby


Deployment Checklist
--------------------

 - update README.md and replace NOT SPECIFIED YET with current date and the change log entry
 - fab build_all_release
 - scp searchcode-server-community.tar.gz to searchcodeserver.com 
 - generate sha1sum and md5sum of searchcode-server-community.tar.gz
 - update pricing.html with hashes calculated in previous step
 - check that version.json is correct version
 - fab all setup_site
 - git update and push
 - mark new release on github
 - update new release into gumroad
 - tweet success
 - update code from old version to new version then mvn test
 - set NOT SPECIFIED YET again
 - push to github