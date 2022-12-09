rm $1/latest/$3_*.xml
rm $1/latest/$3_*_CAAMLv6_2022.json
rm $1/latest/$3.json

cp $1/$2/$2_$3_*.xml $1/latest/
cp $1/$2/$2_$3_*_CAAMLv6_2022.json $1/latest/
cp $1/$2/$3.json $1/latest/

mv $1/latest/$2_$3_de.xml $1/latest/$3_de.xml
mv $1/latest/$2_$3_it.xml $1/latest/$3_it.xml
mv $1/latest/$2_$3_en.xml $1/latest/$3_en.xml
mv $1/latest/$2_$3_fr.xml $1/latest/$3_fr.xml
mv $1/latest/$2_$3_es.xml $1/latest/$3_es.xml
mv $1/latest/$2_$3_ca.xml $1/latest/$3_ca.xml
mv $1/latest/$2_$3_oc.xml $1/latest/$3_oc.xml
mv $1/latest/$2_$3_de_CAAMLv6.xml $1/latest/$3_de_CAAMLv6.xml
mv $1/latest/$2_$3_it_CAAMLv6.xml $1/latest/$3_it_CAAMLv6.xml
mv $1/latest/$2_$3_en_CAAMLv6.xml $1/latest/$3_en_CAAMLv6.xml
mv $1/latest/$2_$3_fr_CAAMLv6.xml $1/latest/$3_fr_CAAMLv6.xml
mv $1/latest/$2_$3_es_CAAMLv6.xml $1/latest/$3_es_CAAMLv6.xml
mv $1/latest/$2_$3_ca_CAAMLv6.xml $1/latest/$3_ca_CAAMLv6.xml
mv $1/latest/$2_$3_oc_CAAMLv6.xml $1/latest/$3_oc_CAAMLv6.xml
mv $1/latest/$2_$3_de_CAAMLv6_2022.json $1/latest/$3_de_CAAMLv6_2022.json
mv $1/latest/$2_$3_it_CAAMLv6_2022.json $1/latest/$3_it_CAAMLv6_2022.json
mv $1/latest/$2_$3_en_CAAMLv6_2022.json $1/latest/$3_en_CAAMLv6_2022.json
mv $1/latest/$2_$3_fr_CAAMLv6_2022.json $1/latest/$3_fr_CAAMLv6_2022.json
mv $1/latest/$2_$3_es_CAAMLv6_2022.json $1/latest/$3_es_CAAMLv6_2022.json
mv $1/latest/$2_$3_ca_CAAMLv6_2022.json $1/latest/$3_ca_CAAMLv6_2022.json
mv $1/latest/$2_$3_oc_CAAMLv6_2022.json $1/latest/$3_oc_CAAMLv6_2022.json

chmod 755 $1/latest/$3_de.xml
chmod 755 $1/latest/$3_it.xml
chmod 755 $1/latest/$3_en.xml
chmod 755 $1/latest/$3_fr.xml
chmod 755 $1/latest/$3_es.xml
chmod 755 $1/latest/$3_ca.xml
chmod 755 $1/latest/$3_oc.xml
chmod 755 $1/latest/$3_de_CAAMLv6.xml
chmod 755 $1/latest/$3_it_CAAMLv6.xml
chmod 755 $1/latest/$3_en_CAAMLv6.xml
chmod 755 $1/latest/$3_fr_CAAMLv6.xml
chmod 755 $1/latest/$3_es_CAAMLv6.xml
chmod 755 $1/latest/$3_ca_CAAMLv6.xml
chmod 755 $1/latest/$3_oc_CAAMLv6.xml
chmod 755 $1/latest/$3_de_CAAMLv6_2022.json
chmod 755 $1/latest/$3_it_CAAMLv6_2022.json
chmod 755 $1/latest/$3_en_CAAMLv6_2022.json
chmod 755 $1/latest/$3_fr_CAAMLv6_2022.json
chmod 755 $1/latest/$3_es_CAAMLv6_2022.json
chmod 755 $1/latest/$3_ca_CAAMLv6_2022.json
chmod 755 $1/latest/$3_oc_CAAMLv6_2022.json

