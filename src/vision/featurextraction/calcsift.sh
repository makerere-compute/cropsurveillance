#!/bin/bash
cd ../../../data/whitebackground
for i in mosaic/*.jpg healthy/*.jpg ;
do 
convert $i tmp.pgm
../../3rdparty/siftDemoV4/sift <tmp.pgm >tmp.sift
tail -n +2 tmp.sift > tmp.sift.tail
cp tmp.sift.tail ../sift/$i.sift
echo "new file $i.sift" ; 
done 

