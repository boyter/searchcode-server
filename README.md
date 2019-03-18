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

Before making any submission be sure to run (requires fabric<2.0 E.G. pip install 'fabric<2.0')

	fab test

All tests must pass before any submission will be accepted.

To Build TL/DR;
---------------
 - Install Java 11 (Oracle or OpenJDK)
 - Install Maven
 - Install Python and Fabric (optional)
 - Install NPM and run "npm install -g node-qunit-phantomjs" (optional)
 - mvn test OR fab test (if fabric) to run unit tests
 - fab test_full (if fabric) to run all tests (need to be running application in background first)

To Build
--------

To build searchcode server you need to have any Windows, Linux or OSX machine with Java 11 installed and
maven setup. Idealy you want to use a Unix OS with Python and Python Fabric installed.

Note that Fabric tasks (fab) are not currently supported on Windows. You can attempt to use
them but they will probably need to under Bash for Windows or something similar. 

To run the javascript unit tests run them in your browser if required by opening

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

To run searchcode server locally you will need to have any Windows, Linux or OSX machine with Java 11 installed.
Testing and packaging was done using the below version.

	$ java -version
	openjdk version "11.0.1" 2018-10-16
	OpenJDK Runtime Environment (build 11.0.1+13-Debian-2)
	OpenJDK 64-Bit Server VM (build 11.0.1+13-Debian-2, mixed mode, sharing)

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
    * Copy the following files searchcode-1.3.15.jar searchcode-server.bar and searchcode-server.sh to your instance directory

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

After the following date NOT SPECIFIED YET the software version '1.3.15' is dual licenced under the Fair Source Licence included in the LICENSE.txt file or under the GNU General Public License Version 3 with terms specified at https://www.gnu.org/licenses/gpl-3.0.txt

OWASP Database is licensed under https://creativecommons.org/licenses/by-sa/3.0/ sourced under Creative Commons from https://codecrawler.codeplex.com/ https://www.owasp.org/index.php/Category:OWASP_Code_Crawler and https://www.owasp.org/index.php/OWASP_Code_Review_Guide_Table_of_Contents this database was modified to JSON and with slight corrections to spelling and puncuation where applicable.

File Classifier Database is licensed under https://creativecommons.org/licenses/by-sa/3.0/


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