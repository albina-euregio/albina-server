rm $1/latest/*
cp $1/$2/*.* $1/latest/

rm $3/*.html
cp $3/$2/*.html $3/

for region in EUREGIO AT-07 IT-32-BZ IT-32-TN ES-CT-L; do
  for language in de it en fr es ca oc; do
    mv $1/latest/${2}_${region}_${language}.pdf $1/latest/${region}_${language}.pdf
    mv $1/latest/${2}_${region}_${language}_bw.pdf $1/latest/${region}_${language}_bw.pdf
    mv $1/latest/${2}_${region}_${language}.xml $1/latest/${region}_${language}.xml
    mv $1/latest/${2}_${region}_${language}_CAAMLv6.xml $1/latest/${region}_${language}_CAAMLv6.xml
    mv $1/latest/${2}_${region}_${language}_CAAMLv6_2022.json $1/latest/${region}_${language}_CAAMLv6_2022.json
  done
done

chmod 755 $3/*.html
chmod -R 755 $1/latest/

