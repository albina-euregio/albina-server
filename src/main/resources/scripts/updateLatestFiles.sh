rm $1/latest/*
rm -r $1/latest/fd_overlay
rm -r $1/latest/am_overlay
rm -r $1/latest/pm_overlay

mkdir $1/latest/fd_overlay
mkdir $1/latest/am_overlay
mkdir $1/latest/pm_overlay

cp $1/$2/* $1/latest/
cp -r $1/$2/fd_overlay/* $1/latest/fd_overlay/
cp -r $1/$2/am_overlay/* $1/latest/am_overlay/
cp -r $1/$2/pm_overlay/* $1/latest/pm_overlay/

chmod -R 755 $1/latest/

rm $3/de.html
rm $3/it.html
rm $3/en.html
rm $3/AT-07_de.html
rm $3/AT-07_it.html
rm $3/AT-07_en.html
rm $3/IT-32-BZ_de.html
rm $3/IT-32-BZ_it.html
rm $3/IT-32-BZ_en.html
rm $3/IT-32-TN_de.html
rm $3/IT-32-TN_it.html
rm $3/IT-32-TN_en.html

cp $3/$2/de.html $3/
cp $3/$2/it.html $3/
cp $3/$2/en.html $3/
cp $3/$2/AT-07_de.html $3/
cp $3/$2/AT-07_it.html $3/
cp $3/$2/AT-07_en.html $3/
cp $3/$2/IT-32-BZ_de.html $3/
cp $3/$2/IT-32-BZ_it.html $3/
cp $3/$2/IT-32-BZ_en.html $3/
cp $3/$2/IT-32-TN_de.html $3/
cp $3/$2/IT-32-TN_it.html $3/
cp $3/$2/IT-32-TN_en.html $3/

chmod 755 $3/*.html
