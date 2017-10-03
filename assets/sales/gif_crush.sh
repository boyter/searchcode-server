#!/bin/sh 
# ./gif_crush.sh ./target/directory
for gif in `find $1 -name "*.gif"`; 
do     
    echo "crushing $gif"
    gifsicle -i "$gif" -O3 --colors 64 -o "$gif"
done;