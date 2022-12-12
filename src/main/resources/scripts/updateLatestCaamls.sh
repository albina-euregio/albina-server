rm $1/latest/*.xml
rm $1/latest/*.json

cp $1/$2/$2_*.xml $1/latest/
cp $1/$2/*.json $1/latest/


for region in EUREGIO AT-07 IT-32-BZ IT-32-TN ES-CT-L; do
  for language in de it en fr es ca oc; do
    mv $1/latest/${2}_${region}_${language}.xml $1/latest/${region}_${language}.xml
    mv $1/latest/${2}_${region}_${language}_CAAMLv6.xml $1/latest/${region}_${language}_CAAMLv6.xml
    mv $1/latest/${2}_${region}_${language}_CAAMLv6_2022.json $1/latest/${region}_${language}_CAAMLv6_2022.json
    # rename files without region ID
    mv $1/latest/${2}_${language}.xml $1/latest/${language}.xml
    mv $1/latest/${2}_${language}_CAAMLv6.xml $1/latest/${language}_CAAMLv6.xml
    mv $1/latest/${2}_${language}_CAAMLv6_2022.json $1/latest/${language}_CAAMLv6_2022.json
  done
done

chmod -R 755 $1/latest/

