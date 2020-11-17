rm $1/latest/*
rm -r $1/latest/fd_overlay
rm -r $1/latest/am_overlay
rm -r $1/latest/pm_overlay

mkdir $1/latest/fd_overlay
mkdir $1/latest/am_overlay
mkdir $1/latest/pm_overlay

cp $1/$2/* $1/latest/
cp -r $1/$2/fd_overlay/* $1/latest/fd_overlay/
cp -r $1/$2/am_overlay/* $1/latest/am_overlay/
cp -r $1/$2/pm_overlay/* $1/latest/pm_overlay/

rm $3/de.html
rm $3/it.html
rm $3/en.html
rm $3/fr.html
rm $3/es.html
rm $3/ca.html
rm $3/oc.html
rm $3/AT-07_de.html
rm $3/AT-07_it.html
rm $3/AT-07_en.html
rm $3/AT-07_fr.html
rm $3/AT-07_es.html
rm $3/AT-07_ca.html
rm $3/AT-07_oc.html
rm $3/IT-32-BZ_de.html
rm $3/IT-32-BZ_it.html
rm $3/IT-32-BZ_en.html
rm $3/IT-32-BZ_fr.html
rm $3/IT-32-BZ_es.html
rm $3/IT-32-BZ_ca.html
rm $3/IT-32-BZ_oc.html
rm $3/IT-32-TN_de.html
rm $3/IT-32-TN_it.html
rm $3/IT-32-TN_en.html
rm $3/IT-32-TN_fr.html
rm $3/IT-32-TN_es.html
rm $3/IT-32-TN_ca.html
rm $3/IT-32-TN_oc.html
rm $3/ES-CT-L_de.html
rm $3/ES-CT-L_it.html
rm $3/ES-CT-L_en.html
rm $3/ES-CT-L_fr.html
rm $3/ES-CT-L_es.html
rm $3/ES-CT-L_ca.html
rm $3/ES-CT-L_oc.html

cp $3/$2/de.html $3/
cp $3/$2/it.html $3/
cp $3/$2/en.html $3/
cp $3/$2/fr.html $3/
cp $3/$2/es.html $3/
cp $3/$2/ca.html $3/
cp $3/$2/oc.html $3/
cp $3/$2/AT-07_de.html $3/
cp $3/$2/AT-07_it.html $3/
cp $3/$2/AT-07_en.html $3/
cp $3/$2/AT-07_fr.html $3/
cp $3/$2/AT-07_es.html $3/
cp $3/$2/AT-07_ca.html $3/
cp $3/$2/AT-07_oc.html $3/
cp $3/$2/IT-32-BZ_de.html $3/
cp $3/$2/IT-32-BZ_it.html $3/
cp $3/$2/IT-32-BZ_en.html $3/
cp $3/$2/IT-32-BZ_fr.html $3/
cp $3/$2/IT-32-BZ_es.html $3/
cp $3/$2/IT-32-BZ_ca.html $3/
cp $3/$2/IT-32-BZ_oc.html $3/
cp $3/$2/IT-32-TN_de.html $3/
cp $3/$2/IT-32-TN_it.html $3/
cp $3/$2/IT-32-TN_en.html $3/
cp $3/$2/IT-32-TN_fr.html $3/
cp $3/$2/IT-32-TN_es.html $3/
cp $3/$2/IT-32-TN_ca.html $3/
cp $3/$2/IT-32-TN_oc.html $3/

mv $1/latest/$2_de.pdf $1/latest/de.pdf
mv $1/latest/$2_it.pdf $1/latest/it.pdf
mv $1/latest/$2_en.pdf $1/latest/en.pdf
mv $1/latest/$2_fr.pdf $1/latest/fr.pdf
mv $1/latest/$2_es.pdf $1/latest/es.pdf
mv $1/latest/$2_ca.pdf $1/latest/ca.pdf
mv $1/latest/$2_oc.pdf $1/latest/oc.pdf
mv $1/latest/$2_de_bw.pdf $1/latest/de_bw.pdf
mv $1/latest/$2_it_bw.pdf $1/latest/it_bw.pdf
mv $1/latest/$2_en_bw.pdf $1/latest/en_bw.pdf
mv $1/latest/$2_fr_bw.pdf $1/latest/fr_bw.pdf
mv $1/latest/$2_es_bw.pdf $1/latest/es_bw.pdf
mv $1/latest/$2_ca_bw.pdf $1/latest/ca_bw.pdf
mv $1/latest/$2_oc_bw.pdf $1/latest/oc_bw.pdf
mv $1/latest/$2_AT-07_de.pdf $1/latest/AT-07_de.pdf
mv $1/latest/$2_AT-07_it.pdf $1/latest/AT-07_it.pdf
mv $1/latest/$2_AT-07_en.pdf $1/latest/AT-07_en.pdf
mv $1/latest/$2_AT-07_fr.pdf $1/latest/AT-07_fr.pdf
mv $1/latest/$2_AT-07_es.pdf $1/latest/AT-07_es.pdf
mv $1/latest/$2_AT-07_ca.pdf $1/latest/AT-07_ca.pdf
mv $1/latest/$2_AT-07_oc.pdf $1/latest/AT-07_oc.pdf
mv $1/latest/$2_AT-07_de_bw.pdf $1/latest/AT-07_de_bw.pdf
mv $1/latest/$2_AT-07_it_bw.pdf $1/latest/AT-07_it_bw.pdf
mv $1/latest/$2_AT-07_en_bw.pdf $1/latest/AT-07_en_bw.pdf
mv $1/latest/$2_AT-07_fr_bw.pdf $1/latest/AT-07_fr_bw.pdf
mv $1/latest/$2_AT-07_es_bw.pdf $1/latest/AT-07_es_bw.pdf
mv $1/latest/$2_AT-07_ca_bw.pdf $1/latest/AT-07_ca_bw.pdf
mv $1/latest/$2_AT-07_oc_bw.pdf $1/latest/AT-07_oc_bw.pdf
mv $1/latest/$2_IT-32-BZ_de.pdf $1/latest/IT-32-BZ_de.pdf
mv $1/latest/$2_IT-32-BZ_it.pdf $1/latest/IT-32-BZ_it.pdf
mv $1/latest/$2_IT-32-BZ_en.pdf $1/latest/IT-32-BZ_en.pdf
mv $1/latest/$2_IT-32-BZ_fr.pdf $1/latest/IT-32-BZ_fr.pdf
mv $1/latest/$2_IT-32-BZ_es.pdf $1/latest/IT-32-BZ_es.pdf
mv $1/latest/$2_IT-32-BZ_ca.pdf $1/latest/IT-32-BZ_ca.pdf
mv $1/latest/$2_IT-32-BZ_oc.pdf $1/latest/IT-32-BZ_oc.pdf
mv $1/latest/$2_IT-32-BZ_de_bw.pdf $1/latest/IT-32-BZ_de_bw.pdf
mv $1/latest/$2_IT-32-BZ_it_bw.pdf $1/latest/IT-32-BZ_it_bw.pdf
mv $1/latest/$2_IT-32-BZ_en_bw.pdf $1/latest/IT-32-BZ_en_bw.pdf
mv $1/latest/$2_IT-32-BZ_fr_bw.pdf $1/latest/IT-32-BZ_fr_bw.pdf
mv $1/latest/$2_IT-32-BZ_es_bw.pdf $1/latest/IT-32-BZ_es_bw.pdf
mv $1/latest/$2_IT-32-BZ_ca_bw.pdf $1/latest/IT-32-BZ_ca_bw.pdf
mv $1/latest/$2_IT-32-BZ_oc_bw.pdf $1/latest/IT-32-BZ_oc_bw.pdf
mv $1/latest/$2_IT-32-TN_de.pdf $1/latest/IT-32-TN_de.pdf
mv $1/latest/$2_IT-32-TN_it.pdf $1/latest/IT-32-TN_it.pdf
mv $1/latest/$2_IT-32-TN_en.pdf $1/latest/IT-32-TN_en.pdf
mv $1/latest/$2_IT-32-TN_fr.pdf $1/latest/IT-32-TN_fr.pdf
mv $1/latest/$2_IT-32-TN_es.pdf $1/latest/IT-32-TN_es.pdf
mv $1/latest/$2_IT-32-TN_ca.pdf $1/latest/IT-32-TN_ca.pdf
mv $1/latest/$2_IT-32-TN_oc.pdf $1/latest/IT-32-TN_oc.pdf
mv $1/latest/$2_IT-32-TN_de_bw.pdf $1/latest/IT-32-TN_de_bw.pdf
mv $1/latest/$2_IT-32-TN_it_bw.pdf $1/latest/IT-32-TN_it_bw.pdf
mv $1/latest/$2_IT-32-TN_en_bw.pdf $1/latest/IT-32-TN_en_bw.pdf
mv $1/latest/$2_IT-32-TN_fr_bw.pdf $1/latest/IT-32-TN_fr_bw.pdf
mv $1/latest/$2_IT-32-TN_es_bw.pdf $1/latest/IT-32-TN_es_bw.pdf
mv $1/latest/$2_IT-32-TN_ca_bw.pdf $1/latest/IT-32-TN_ca_bw.pdf
mv $1/latest/$2_IT-32-TN_oc_bw.pdf $1/latest/IT-32-TN_oc_bw.pdf
mv $1/latest/$2_ES-CT-L_de.pdf $1/latest/ES-CT-L_de.pdf
mv $1/latest/$2_ES-CT-L_it.pdf $1/latest/ES-CT-L_it.pdf
mv $1/latest/$2_ES-CT-L_en.pdf $1/latest/ES-CT-L_en.pdf
mv $1/latest/$2_ES-CT-L_fr.pdf $1/latest/ES-CT-L_fr.pdf
mv $1/latest/$2_ES-CT-L_es.pdf $1/latest/ES-CT-L_es.pdf
mv $1/latest/$2_ES-CT-L_ca.pdf $1/latest/ES-CT-L_ca.pdf
mv $1/latest/$2_ES-CT-L_oc.pdf $1/latest/ES-CT-L_oc.pdf
mv $1/latest/$2_ES-CT-L_de_bw.pdf $1/latest/ES-CT-L_de_bw.pdf
mv $1/latest/$2_ES-CT-L_it_bw.pdf $1/latest/ES-CT-L_it_bw.pdf
mv $1/latest/$2_ES-CT-L_en_bw.pdf $1/latest/ES-CT-L_en_bw.pdf
mv $1/latest/$2_ES-CT-L_fr_bw.pdf $1/latest/ES-CT-L_fr_bw.pdf
mv $1/latest/$2_ES-CT-L_es_bw.pdf $1/latest/ES-CT-L_es_bw.pdf
mv $1/latest/$2_ES-CT-L_ca_bw.pdf $1/latest/ES-CT-L_ca_bw.pdf
mv $1/latest/$2_ES-CT-L_oc_bw.pdf $1/latest/ES-CT-L_oc_bw.pdf

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

chmod 755 $3/*.html
chmod -R 755 $1/latest/

