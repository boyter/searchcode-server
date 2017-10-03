#!/bin/bash

# Simple script that creates a git repo and adds and deletes files from it over and over till exited

rm -rf .git/
git init .

while :
do
    for i in {1..100}
    do
       echo "indextest gitload gitload" > "temp_$i.php"
    done

    git add *.php
    git commit -m "adding"

    echo "Sleep for 30 seconds"
    sleep 30

    git rm *.php
    git commit -m "remove file"

    echo "Sleep for 30 seconds"
    sleep 30
done
