rm $1/latest/de.png
rm $1/latest/it.png
rm $1/latest/en.png
rm $1/latest/fr.png
rm $1/latest/es.png
rm $1/latest/ca.png
rm $1/latest/oc.png

cp $1/$2/$2_de.png $1/latest/
cp $1/$2/$2_it.png $1/latest/
cp $1/$2/$2_en.png $1/latest/
cp $1/$2/$2_fr.png $1/latest/
cp $1/$2/$2_es.png $1/latest/
cp $1/$2/$2_ca.png $1/latest/
cp $1/$2/$2_oc.png $1/latest/
mv $1/latest/$2_de.png $1/latest/de.png
mv $1/latest/$2_it.png $1/latest/it.png
mv $1/latest/$2_en.png $1/latest/en.png
mv $1/latest/$2_fr.png $1/latest/fr.png
mv $1/latest/$2_es.png $1/latest/es.png
mv $1/latest/$2_ca.png $1/latest/ca.png
mv $1/latest/$2_oc.png $1/latest/oc.png

chmod 755 $1/latest/de.png
chmod 755 $1/latest/it.png
chmod 755 $1/latest/en.png
chmod 755 $1/latest/fr.png
chmod 755 $1/latest/es.png
chmod 755 $1/latest/ca.png
chmod 755 $1/latest/oc.png

