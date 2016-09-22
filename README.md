# searchcode server

[![Video](https://searchcode.com/static/product/searchcode-server.gif)](https://searchcode.com/static/product/searchcode-server.gif)

What is it?
-----------
searchcode server is a powerful code search engine with a sleak web user interface.

searchcode server works in tandem with your source control system, indexing thousands of repositories 
and files allowing you and your developers to quickly find and reuse code across teams.

See https://searchcode.com/product/ for more details

Support
-------
If you are building from source then you get no support and must work within the restrictions specified of the
fair source licence (see LICENSE.txt for details). To purchase support see 
https://searchcode.com/product/#downloadlinks


To Build
--------
To build searchcode server you need to have any Windows, Linux or OSX machine with Java 8 installed and
maven setup. Idealy you want Python and Python Fabric installed as well but it is not a requirement.

To test the application you can either run "mvn test" or "fab test". To build a full release IE ready
for production you should run "fab build_release" which will test compile and build a release into
the release folder and produce the file "searchcode-server.tar.gz" which is a ready to deploy release.

If you want to simply test and run then you can run "fab run" however this will be default build a package
and run that. To run quickly just open in your IDE of choice and start running App.java


To Run
------

To run searchcode server you will need to have any Windows, Linux or OSX machine with Java 8 installed.
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

For further control you may want to edit the above files and include the java -Xmx arguement to specify the
amount of RAM to use or any other java option you wish to pass in.

To administer your searchcode server instance you need to click on the Admin link in the top right.
Enter the default password Adm1n234 (change this via the properties file)
to add git repositories. If you need help check the documentation page (link at the bottom
of every page).

Upgrades
--------

To upgrade your current instance of searchcode perform the following steps.

* Stop your current instance of searchcode server
* Make a backup copy of your current instances searchcode.properties and searchcode.sqlite files.
* Uncompress the package to a new directory.
* You can either
    * Copy the uncompressed files over your current instance overwriting if prompted.
    * Copy the directory dependancy-jars and all contents overwriting your current
    * Copy the following files searchcode-1.4.0.jar searchcode-server.bar and searchcode-server.sh to your instance directory
* Start your instance again
* Login to the admin screen and click the "Recrawl & Rebuild Indexes" button

It is also worth comparing your searchcode.properties file to the new one (or the documentation page) as there may be new configuration that you can use. Also be sure to check the settings page as there is likely to be new settings you can use.

Licence
-------
Copyright (c) 2016 Boyter Online Services

Use of this software is governed by the Fair Source License included in the LICENSE.txt file

In order to deal with the case of my death or this software becoming abandoned it has an open eventually clause where the licence will change exactly 3 years after the publish date of a version release. This means that if version 1.0.0 was released on 1 July 2010 then it can be taken using the listed alternate licence on 2 July 2013. This licence, version and time is all specified below.

After the following date NO DATE SPECIFIED YET this software version '1.4.0' is dual licenced under the Fair Source Licence included in the LICENSE.txt file or under the GNU General Public License Version 3 with terms specified at https://www.gnu.org/licenses/gpl-3.0.txt

OWASP Database is licensed under https://creativecommons.org/licenses/by-sa/3.0/ sourced under Creative Commons from https://codecrawler.codeplex.com/ https://www.owasp.org/index.php/Category:OWASP_Code_Crawler and https://www.owasp.org/index.php/OWASP_Code_Review_Guide_Table_of_Contents this database was modified to JSON and with slight corrections to spelling and puncuation where applicable.


Change Log
==========

XX XXXXXXXXX XXXX - 1.4.0
 - Can index historical data for git repositories

XX XXXXXXXXX XXXX - 1.3.1
 - Fix potential performance issue in spelling corrector with long strings
 - Fix potential XSS issue with HTML page

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

8 March 2016 - 0.9.9
 - Inital release

Join the chat at https://gitter.im/searchcode-server/Lobby
