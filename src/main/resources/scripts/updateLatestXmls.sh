rm $1/latest/*.xml

cp $1/$2/*.xml $1/latest/

mv $1/latest/$2_de.xml $1/latest/de.xml
mv $1/latest/$2_it.xml $1/latest/it.xml
mv $1/latest/$2_en.xml $1/latest/en.xml
mv $1/latest/$2_en.xml $1/latest/fr.xml
mv $1/latest/$2_en.xml $1/latest/es.xml
mv $1/latest/$2_en.xml $1/latest/ca.xml
mv $1/latest/$2_en.xml $1/latest/oc.xml

chmod 755 $1/latest/de.xml
chmod 755 $1/latest/it.xml
chmod 755 $1/latest/en.xml
chmod 755 $1/latest/fr.xml
chmod 755 $1/latest/es.xml
chmod 755 $1/latest/ca.xml
chmod 755 $1/latest/oc.xml
