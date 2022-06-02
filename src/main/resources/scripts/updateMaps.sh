rm $1/$2/*.jpg
rm $1/$2/*.webp
rm $1/$2/*.png

cp $1/$2/$3/*.jpg $1/$2/
cp $1/$2/$3/*.webp $1/$2/
cp $1/$2/$3/*.png $1/$2/

chmod -R 755 $1/$2/

