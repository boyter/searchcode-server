#!/bin/sh 
# ./jpg_crush.sh ./target/directory
for jpg in `find $1 -name "*.jpg"`; 
do     
    echo "crushing $jpg"
    jpegoptim -m 70 --strip-all "$jpg"
done;
