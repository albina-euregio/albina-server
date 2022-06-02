rm $1/latest/*.jpg
rm $1/latest/*.webp
rm $1/latest/*.png

cp $1/$2/*.jpg $1/latest/
cp $1/$2/*.webp $1/latest/
cp $1/$2/*.png $1/latest/

chmod -R 755 $1/latest/
