rm $1/latest/*.jpg
rm $1/latest/*.webp
rm $1/latest/*.png

ln --symbolic --relative --force $1/$2/*.jpg $1/latest/
ln --symbolic --relative --force $1/$2/*.webp $1/latest/
ln --symbolic --relative --force $1/$2/*.png $1/latest/

for region in EUREGIO AT-07 IT-32-BZ IT-32-TN ES-CT-L; do
  rm $1/latest/${region}_*.pdf
  ln --symbolic --relative --force $1/$2/${region}_*.pdf $1/latest/
  for daytime in fd am pm; do
    rm $1/latest/${daytime}_${region}_*.pdf
    ln --symbolic --relative --force $1/$2/${daytime}_${region}_*.pdf $1/latest/
  done
done

chmod -R 755 $1/latest/

