====================
Building from Source
====================

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
maven setup. Idealy you want Python and Python Fabric installed as well but it is not a requirement. To run 
the javascript unit tests through the command line you will need to install NPM and then run

.. code-block:: bash

	npm install -g node-qunit-phantomjs 

but you can always run them in your browser if required by opening 

.. code-block:: bash

	./src/test/javascript/index.html

To test the application you can either run

.. code-block:: bash

	mvn test 

or

.. code-block:: bash

	fab test

Note that these tests will only cover unit some integration and javascript unit. For full coverage run 

.. code-block:: bash

	fab test_full 

with the application running in the background to ensure everything is working as expected.

To build a full release IE ready for production you should run 

.. code-block:: bash

	fab build_release 

which will test compile and build a release into
the release folder and produce the file "searchcode-server.tar.gz" which is a ready to deploy release.

If you want to simply test and run then you can run 

.. code-block:: bash

	fab run 

however this will be default build a package and run that. To run quickly just open in your IDE of choice and start running App.java

There are a few special tests used for verifying that indexing logic for GIT and File repositories work correctly. To perform 
this special you need to run one of all of the following shell scripts,

.. code-block:: bash

	./assets/integration_test/gitload/gitload.sh
	./assets/integration_test/gitupdate/gitupdate.sh
	./assets/integration_test/fileupdatetest/fileload.sh
	./assets/integration_test/fileload/fileload.sh

Then add the git ones as GIT repositories in the application and the file ones as FILE repositories. It is also useful to 
set the properties

.. code-block:: bash

	check_repo_chages=60
	check_filerepo_chages=60

but it is not required. Then run searchcode. The scripts will add/remove/update files every 60 seconds which should force searchcode
to add/update/remove files from the index in an attempt to hit as many code paths as possible. With this done there should be no more
than 400 documents indexed at any time (if indexing all 4 repositories) and a minimum of 201 (the fileload.sh files + fileupdatetest files + gitupdate files). Leave things running over several hours to ensure that the logic works correctly.

Before a release is made a build must pass all of the above checks, with the indexer logic tests being run for a minimum of 24 hours. To
ensure that performance is acceptable the tests are also run on a Atom powered netbook.