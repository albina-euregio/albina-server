rm $1/latest/$2_de.pdf
rm $1/latest/$2_it.pdf
rm $1/latest/$2_en.pdf
rm $1/latest/$2_de_bw.pdf
rm $1/latest/$2_it_bw.pdf
rm $1/latest/$2_en_bw.pdf
rm $1/latest/$2_AT-07_de.pdf
rm $1/latest/$2_AT-07_it.pdf
rm $1/latest/$2_AT-07_en.pdf
rm $1/latest/$2_AT-07_de_bw.pdf
rm $1/latest/$2_AT-07_it_bw.pdf
rm $1/latest/$2_AT-07_en_bw.pdf
rm $1/latest/$2_IT-32-BZ_de.pdf
rm $1/latest/$2_IT-32-BZ_it.pdf
rm $1/latest/$2_IT-32-BZ_en.pdf
rm $1/latest/$2_IT-32-BZ_de_bw.pdf
rm $1/latest/$2_IT-32-BZ_it_bw.pdf
rm $1/latest/$2_IT-32-BZ_en_bw.pdf
rm $1/latest/$2_IT-32-TN_de.pdf
rm $1/latest/$2_IT-32-TN_it.pdf
rm $1/latest/$2_IT-32-TN_en.pdf
rm $1/latest/$2_IT-32-TN_de_bw.pdf
rm $1/latest/$2_IT-32-TN_it_bw.pdf
rm $1/latest/$2_IT-32-TN_en_bw.pdf

cp $1/$2/$2_de.pdf $1/latest/
cp $1/$2/$2_it.pdf $1/latest/
cp $1/$2/$2_en.pdf $1/latest/
cp $1/$2/$2_de_bw.pdf $1/latest/
cp $1/$2/$2_it_bw.pdf $1/latest/
cp $1/$2/$2_en_bw.pdf $1/latest/
cp $1/$2/$2_AT-07_de.pdf $1/latest/
cp $1/$2/$2_AT-07_it.pdf $1/latest/
cp $1/$2/$2_AT-07_en.pdf $1/latest/
cp $1/$2/$2_AT-07_de_bw.pdf $1/latest/
cp $1/$2/$2_AT-07_it_bw.pdf $1/latest/
cp $1/$2/$2_AT-07_en_bw.pdf $1/latest/
cp $1/$2/$2_IT-32-BZ_de.pdf $1/latest/
cp $1/$2/$2_IT-32-BZ_it.pdf $1/latest/
cp $1/$2/$2_IT-32-BZ_en.pdf $1/latest/
cp $1/$2/$2_IT-32-BZ_de_bw.pdf $1/latest/
cp $1/$2/$2_IT-32-BZ_it_bw.pdf $1/latest/
cp $1/$2/$2_IT-32-BZ_en_bw.pdf $1/latest/
cp $1/$2/$2_IT-32-TN_de.pdf $1/latest/
cp $1/$2/$2_IT-32-TN_it.pdf $1/latest/
cp $1/$2/$2_IT-32-TN_en.pdf $1/latest/
cp $1/$2/$2_IT-32-TN_de_bw.pdf $1/latest/
cp $1/$2/$2_IT-32-TN_it_bw.pdf $1/latest/
cp $1/$2/$2_IT-32-TN_en_bw.pdf $1/latest/

mv $1/latest/$2_AT-07_de.pdf $1/latest/AT-07_de.pdf
mv $1/latest/$2_AT-07_it.pdf $1/latest/AT-07_it.pdf
mv $1/latest/$2_AT-07_en.pdf $1/latest/AT-07_en.pdf
mv $1/latest/$2_IT-32-BZ_de.pdf $1/latest/IT-32-BZ_de.pdf
mv $1/latest/$2_IT-32-BZ_it.pdf $1/latest/IT-32-BZ_it.pdf
mv $1/latest/$2_IT-32-BZ_en.pdf $1/latest/IT-32-BZ_en.pdf
mv $1/latest/$2_IT-32-TN_de.pdf $1/latest/IT-32-TN_de.pdf
mv $1/latest/$2_IT-32-TN_it.pdf $1/latest/IT-32-TN_it.pdf
mv $1/latest/$2_IT-32-TN_en.pdf $1/latest/IT-32-TN_en.pdf
mv $1/latest/$2_de_bw.pdf $1/latest/de_bw.pdf
mv $1/latest/$2_it_bw.pdf $1/latest/it_bw.pdf
mv $1/latest/$2_en_bw.pdf $1/latest/en_bw.pdf
mv $1/latest/$2_AT-07_de_bw.pdf $1/latest/AT-07_de_bw.pdf
mv $1/latest/$2_AT-07_it_bw.pdf $1/latest/AT-07_it_bw.pdf
mv $1/latest/$2_AT-07_en_bw.pdf $1/latest/AT-07_en_bw.pdf
mv $1/latest/$2_IT-32-BZ_de_bw.pdf $1/latest/IT-32-BZ_de_bw.pdf
mv $1/latest/$2_IT-32-BZ_it_bw.pdf $1/latest/IT-32-BZ_it_bw.pdf
mv $1/latest/$2_IT-32-BZ_en_bw.pdf $1/latest/IT-32-BZ_en_bw.pdf
mv $1/latest/$2_IT-32-TN_de_bw.pdf $1/latest/IT-32-TN_de_bw.pdf
mv $1/latest/$2_IT-32-TN_it_bw.pdf $1/latest/IT-32-TN_it_bw.pdf
mv $1/latest/$2_IT-32-TN_en_bw.pdf $1/latest/IT-32-TN_en_bw.pdf
mv $1/latest/$2_de.pdf $1/latest/de.pdf
mv $1/latest/$2_it.pdf $1/latest/it.pdf
mv $1/latest/$2_en.pdf $1/latest/en.pdf

chmod 755 $1/latest/*.pdf
