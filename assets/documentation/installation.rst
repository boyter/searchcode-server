============
Installation
============

All searchcode server requires is a Java 8 version of the JRE. Both the Oracle JRE and OpenJDK are known to work.

Hardware Requirements
---------------------

The hardware requirements of searchcode server depend on what you are planning to index. The main factors are the number of files you plan to index and the physical size of those files on disk.

You will need at least as much disk space as the total size of the repositories you want to index on disk take up plus approx 25% overhead for searchcode server to store its indexes and itself.

For example if you have 100 gigabytes of repositories to index you would need at least 125 gigabytes of storage space allocated to searchcode server.

It is reccomended to have at least 2 CPU cores dedicated to searchcode server, but it can work in a multi tennancy envrionment.

Docker
------

A release of the community edition of searchcode server is available to be run on DockerHub. It can be found at 

https://hub.docker.com/r/searchcode/searchcode-server-community/

To run you will need to pull then run the image

.. code-block:: bash

	docker pull searchcode/searchcode-server-community
	docker run -p 8080:8080 searchcode/searchcode-server-community

Note that this instance is not production ready, as all configuration files and storage is done inside the container, but it is the fastest way to get started and evaluate searchcode.

Locally
-------

To run searchcode server locally you will need to have any Windows, Linux or OSX machine with Java 8 installed.
Testing and packaging was done using the below version.

.. code-block:: bash

	java version "1.8.0_65"
	Java(TM) SE Runtime Environment (build 1.8.0_65-b17)
	Java HotSpot(TM) 64-Bit Server VM (build 25.65-b01, mixed mode)

Uncompress the file you have downloaded to a directory where you want to run searchcode server.
This directory should have more disk space than the size of the repositories you want to index.

Once unpacked assuming that java is in your path (check with the command java -version) you should be able to run
searchcode with the following command for Linux/OSX/BSD

.. code-block:: bash

	./searchcode-server.sh

or for Windows

.. code-block:: bash

	searchcode-server.bat

After a few moments searchcode server should be ready to run. By default it will be running on port 8080.
To connect to it enter the following in the browser,

.. code-block:: bash

	http://SERVER_IP:8080

Be sure to replace SERVER_IP with the ip address if your server or localhost if running locally.
If you see a page with a search bar then everything is fine.

For further control you may want to edit the above files and include the java -Xmx argument to specify the
amount of RAM to use or any other java option you wish to pass in.

To administer your searchcode server instance you need to click on the Admin link in the top right.
Enter the default password Adm1n234 (change this via the properties file)
to add git repositories. If you need help check the documentation page (link at the bottom
of every page).


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
    * Copy the following files searchcode-1.3.8.jar searchcode-server.bar and searchcode-server.sh to your instance directory

* Start your instance again
* Login to the admin screen and click the "Recrawl & Rebuild Indexes" button

It is also worth comparing your searchcode.properties file to the new one (or the documentation page) as there may be new configuration that you can use. Also be sure to check the settings page as there is likely to be new settings you can use.


Multi Tennancy
--------------

Sometimes you may want to run searchcode server on a shared machine where it will need to yield resources to other applications running on the same machine. If this is the case you need consider changing the following settings.

 - Set the Backoff Value in the Admin settings page (not available in community edition). This is used to control how much CPU searchcode server will consume by monitoring the load average on the box and if it is over a certain threshhold pause indexing operations till this number is below this value.
 - Lower the number of processing threads by changing the values of number_git_processors, number_svn_processors and number_file_processors inside the searchcode.properties file. Setting a value of 1 for each will ensure that only a single background indexing process can run at any time and reduce disk and CPU contention.
 - Set the Java heap value using the Xmx command line option when starting searchcode server. Keep in mind that the JVM will use addtional memory behind the heap, and as such searchcode server can still use more RAM then allocated with this option. If you have an upper limit of 1000MB of RAM consider setting this to 800MB.


