rm $1/$2/$2_de.png
rm $1/$2/$2_it.png
rm $1/$2/$2_en.png

cp $1/$2/$3/$2_de.png $1/$2/
cp $1/$2/$3/$2_it.png $1/$2/
cp $1/$2/$3/$2_en.png $1/$2/

chmod 755 $1/$2/$2_de.png
chmod 755 $1/$2/$2_it.png
chmod 755 $1/$2/$2_en.png
