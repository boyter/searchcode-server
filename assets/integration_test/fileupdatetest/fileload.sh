#!/bin/bash

# Simple script that creates a file repo and adds and deletes files from it over and over till exited

rm *.py

while :
do
    now=$(date +"%T")

    for i in {1..99} # this file itself counts as one
    do
       echo "indextest fileupdate $now" > "temp_$i.java"
    done

    echo "Sleep for 30 seconds"
    sleep 30

done
