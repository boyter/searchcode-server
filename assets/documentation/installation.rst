============
Installation
============

All searchcode server requires is a Java 8 version of the JRE. Both the Oracle JRE and OpenJDK are known to work.

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
