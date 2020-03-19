rm $1/de.html
rm $1/it.html
rm $1/en.html
rm $1/AT-07_de.html
rm $1/AT-07_it.html
rm $1/AT-07_en.html
rm $1/IT-32-BZ_de.html
rm $1/IT-32-BZ_it.html
rm $1/IT-32-BZ_en.html
rm $1/IT-32-TN_de.html
rm $1/IT-32-TN_it.html
rm $1/IT-32-TN_en.html

cp $1/$2/de.html $1/
cp $1/$2/it.html $1/
cp $1/$2/en.html $1/
cp $1/$2/AT-07_de.html $1/
cp $1/$2/AT-07_it.html $1/
cp $1/$2/AT-07_en.html $1/
cp $1/$2/IT-32-BZ_de.html $1/
cp $1/$2/IT-32-BZ_it.html $1/
cp $1/$2/IT-32-BZ_en.html $1/
cp $1/$2/IT-32-TN_de.html $1/
cp $1/$2/IT-32-TN_it.html $1/
cp $1/$2/IT-32-TN_en.html $1/

chmod 755 $1/*.html
