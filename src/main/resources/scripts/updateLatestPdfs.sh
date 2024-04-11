rm $1/latest/*
ln --symbolic --relative --force $1/$2/*.* $1/latest/

rm $3/*.html
ln --symbolic --relative --force $3/$2/*.html $3/

for language in de it en fr es ca oc; do
  rm $1/latest/${language}.pdf
  rm $1/latest/${language}_bw.pdf

  ln --symbolic --relative --force $1/$2/$2_${language}.pdf $1/latest/
  ln --symbolic --relative --force $1/$2/$2_${language}_bw.pdf $1/latest/

  mv $1/latest/${2}_${language}.pdf $1/latest/${language}.pdf
  mv $1/latest/${2}_${language}_bw.pdf $1/latest/${language}_bw.pdf

  for region in EUREGIO AT-07 IT-32-BZ IT-32-TN ES-CT-L; do
    rm $1/latest/${region}_${language}.pdf
    rm $1/latest/${region}_${language}_bw.pdf

    ln --symbolic --relative --force $1/$2/$2_${region}_${language}.pdf $1/latest/
    ln --symbolic --relative --force $1/$2/$2_${region}_${language}_bw.pdf $1/latest/

    mv $1/latest/${2}_${region}_${language}.pdf $1/latest/${region}_${language}.pdf
    mv $1/latest/${2}_${region}_${language}_bw.pdf $1/latest/${region}_${language}_bw.pdf
  done
done

chmod -R 755 $1/latest/

