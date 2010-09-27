#!/bin/bash
cd ../../../data/whitebackground
for i in mosaic/*.jpg healthy/*.jpg ;
do 
convert $i tmp.pgm
../../3rdparty/SURF-V1.0.9/surf.ln -i tmp.pgm -o tmp.surf
tail -n +3 tmp.surf > tmp.surf.tail
cp tmp.surf.tail surf/$i.surf
echo "new file $i.surf " ; 
done 

