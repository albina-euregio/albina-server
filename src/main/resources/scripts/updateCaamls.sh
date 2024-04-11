rm $1/$2/$2_*.xml
rm $1/$2/*.json

# duplicate EUREGIO files and remove region ID
for region in EUREGIO; do
  for language in de it en fr es ca oc; do
    ln --symbolic --relative --force $1/${2}/${3}/${2}_${region}_${language}.xml $1/${2}/${3}/${2}_${language}.xml
    ln --symbolic --relative --force $1/${2}/${3}/${2}_${region}_${language}_CAAMLv6.xml $1/${2}/${3}/${2}_${language}_CAAMLv6.xml
    ln --symbolic --relative --force $1/${2}/${3}/${2}_${region}_${language}_CAAMLv6.json $1/${2}/${3}/${2}_${language}_CAAMLv6.json
  done
done

ln --symbolic --relative --force $1/$2/$3/$2_*.xml $1/$2/
ln --symbolic --relative --force $1/$2/$3/*.json $1/$2/

chmod -R 755 $1/$2/

