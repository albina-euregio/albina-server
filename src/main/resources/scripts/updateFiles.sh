rm $1/$2/*

cp $1/$2/$3/* $1/$2/

# duplicate EUREGIO files and remove region ID
for region in EUREGIO; do
  for language in de it en fr es ca oc; do
    cp $1/${2}/${region}_${language}.pdf $1/${2}/${2}_${language}.pdf
    cp $1/${2}/${region}_${language}_bw.pdf $1/${2}/${2}_${language}_bw.pdf
    cp $1/${2}/${region}_${language}.xml $1/${2}/${2}_${language}.xml
    cp $1/${2}/${region}_${language}_CAAMLv6.xml $1/${2}/${2}_${language}_CAAMLv6.xml
    cp $1/${2}/${region}_${language}_CAAMLv6_2022.json $1/${2}/${2}_${language}_CAAMLv6_2022.json
    cp $4/${2}/${region}_${language}.html $4/${2}/${language}.html
  done
done

chmod -R 755 $1/$2/
chmod 755 $4/$2/*.html

