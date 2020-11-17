rm $1/latest/*.xml

cp $1/$2/*.xml $1/latest/

mv $1/latest/$2_de.xml $1/latest/de.xml
mv $1/latest/$2_it.xml $1/latest/it.xml
mv $1/latest/$2_en.xml $1/latest/en.xml
mv $1/latest/$2_fr.xml $1/latest/fr.xml
mv $1/latest/$2_es.xml $1/latest/es.xml
mv $1/latest/$2_ca.xml $1/latest/ca.xml
mv $1/latest/$2_oc.xml $1/latest/oc.xml
mv $1/latest/$2_de_CAAMLv6.xml $1/latest/de_CAAMLv6.xml
mv $1/latest/$2_it_CAAMLv6.xml $1/latest/it_CAAMLv6.xml
mv $1/latest/$2_en_CAAMLv6.xml $1/latest/en_CAAMLv6.xml
mv $1/latest/$2_fr_CAAMLv6.xml $1/latest/fr_CAAMLv6.xml
mv $1/latest/$2_es_CAAMLv6.xml $1/latest/es_CAAMLv6.xml
mv $1/latest/$2_ca_CAAMLv6.xml $1/latest/ca_CAAMLv6.xml
mv $1/latest/$2_oc_CAAMLv6.xml $1/latest/oc_CAAMLv6.xml

chmod 755 $1/latest/de.xml
chmod 755 $1/latest/it.xml
chmod 755 $1/latest/en.xml
chmod 755 $1/latest/fr.xml
chmod 755 $1/latest/es.xml
chmod 755 $1/latest/ca.xml
chmod 755 $1/latest/oc.xml
chmod 755 $1/latest/de_CAAMLv6.xml
chmod 755 $1/latest/it_CAAMLv6.xml
chmod 755 $1/latest/en_CAAMLv6.xml
chmod 755 $1/latest/fr_CAAMLv6.xml
chmod 755 $1/latest/es_CAAMLv6.xml
chmod 755 $1/latest/ca_CAAMLv6.xml
chmod 755 $1/latest/oc_CAAMLv6.xml

