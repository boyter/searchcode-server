#!/bin/bash

# Simple script that creates a file repo and adds and deletes files from it over and over till exited

rm *.py

while :
do
    for i in {1..99} # this file itself counts as one
    do
       echo "indextest fileload" > "temp_$i.py"
    done

    echo "Sleep for 30 seconds"
    sleep 30

    rm *.py

    echo "Sleep for 30 seconds"
    sleep 30
done
