rm $1/$2/*

# duplicate EUREGIO files and remove region ID
for region in EUREGIO; do
  for language in de it en fr es ca oc; do
    ln --symbolic --relative --force $1/${2}/${3}/${2}_${region}_${language}.pdf $1/${2}/${3}/${2}_${language}.pdf
    ln --symbolic --relative --force $1/${2}/${3}/${2}_${region}_${language}_bw.pdf $1/${2}/${3}/${2}_${language}_bw.pdf
    ln --symbolic --relative --force $1/${2}/${3}/${2}_${region}_${language}.xml $1/${2}/${3}/${2}_${language}.xml
    ln --symbolic --relative --force $1/${2}/${3}/${2}_${region}_${language}_CAAMLv6.xml $1/${2}/${3}/${2}_${language}_CAAMLv6.xml
    ln --symbolic --relative --force $1/${2}/${3}/${2}_${region}_${language}_CAAMLv6.json $1/${2}/${3}/${2}_${language}_CAAMLv6.json
    ln --symbolic --relative --force $4/${2}/${region}_${language}.html $4/${2}/${language}.html
  done
done

ln --symbolic --relative --force $1/$2/$3/* $1/$2/

chmod -R 755 $1/$2/
chmod 755 $4/$2/*.html

