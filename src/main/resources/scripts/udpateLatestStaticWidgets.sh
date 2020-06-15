rm $1/latest/de.png
rm $1/latest/it.png
rm $1/latest/en.png

cp $1/$2/$2_de.png $1/latest/
cp $1/$2/$2_it.png $1/latest/
cp $1/$2/$2_en.png $1/latest/
mv $1/latest/$2_de.png $1/latest/de.png
mv $1/latest/$2_it.png $1/latest/it.png
mv $1/latest/$2_en.png $1/latest/en.png

chmod 755 $1/latest/de.png
chmod 755 $1/latest/it.png
chmod 755 $1/latest/en.png