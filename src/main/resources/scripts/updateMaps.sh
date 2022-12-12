rm $1/$2/*.jpg
rm $1/$2/*.webp
rm $1/$2/*.png

cp $1/$2/$3/*.jpg $1/$2/
cp $1/$2/$3/*.webp $1/$2/
cp $1/$2/$3/*.png $1/$2/

for region in EUREGIO AT-07 IT-32-BZ IT-32-TN ES-CT-L; do
  rm $1/$2/${region}_*.pdf
  cp $1/$2/$3/${region}_*.pdf $1/$2/
  for daytime in fd am pm; do
    rm $1/$2/${daytime}_${region}_*.pdf
    cp $1/$2/$3/${daytime}_${region}_*.pdf $1/$2/
  done
done

chmod -R 755 $1/$2/

