#!/bin/sh 
# ./png_crush.sh ./target/directory
for png in `find $1 -name "*.png"`; 
do     
    echo "crushing $png"
    pngcrush -m 0 "$png" temp.png
    mv -f temp.png $png 
done;