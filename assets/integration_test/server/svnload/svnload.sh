#!/bin/bash

# Simple script that creates a svn repo and adds and deletes files from it over and over till exited
rm -rf repo
rm -rf .svn

svnadmin create repo

# svnadmin create repo
# svn co file:///home/bboyter/temp/repo/ checkout
# echo "test" > test.txt
# svn add test.txt
# svn commit -m "First commit"

svn co file://${PWD}/repo/ .

while :
do
    for i in {1..100}
    do
       echo "indextest svnload svnload" > "temp_$i.php"
    done

    svn add *.php
    svn commit -m "adding"

    echo "Sleep for 30 seconds"
    sleep 30

    svn rm *.php
    svn commit -m "remove file"

    echo "Sleep for 30 seconds"
    sleep 30
done
