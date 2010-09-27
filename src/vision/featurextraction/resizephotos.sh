#!/bin/bash
for i in healthy/*.jpg mosaic/*.jpg ;
do 
  convert -quality 80 -size 600x800 "$i" -resize 600x800 "smallerimages/$i";
  #convert -quality 80 -size 800x600 "$i" -resize 800x600 "smallerimages/$i";
echo "resized image $i created." ; 
done 

