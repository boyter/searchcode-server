#!/bin/bash

# Simple script that creates a git repo and adds and deletes files from it over and over till exited

rm -rf .git/
git init .

while :
do
    now=$(date +"%T")

    for i in {1..100}
    do
       echo "indextest gitload gitload $now" > "temp_$i.cpp"
    done

    git add *.cpp
    git commit -m "update"

    echo "Sleep for 60 seconds"
    sleep 60
done
