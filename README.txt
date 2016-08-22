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
    * Copy the following files searchcode-1.2.2.jar searchcode-server.bar and searchcode-server.sh to your instance directory
* Start your instance again

It is also worth comparing your searchcode.properties file to the new one (or the documentation page) as there may be new configuration that you can use. Also be sure to check the settings page as there is likely to be new settings you can use.


Change Log
==========

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
