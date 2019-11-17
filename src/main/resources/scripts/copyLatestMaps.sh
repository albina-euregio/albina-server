mkdir $1/latest/fd_overlay
mkdir $1/latest/am_overlay
mkdir $1/latest/pm_overlay

cp $1/$2/*.jpg $1/latest/
cp $1/$2/fd_overlay.png $1/latest/
cp $1/$2/am_overlay.png $1/latest/
cp $1/$2/pm_overlay.png $1/latest/
cp -r $1/$2/fd_overlay/* $1/latest/fd_overlay/
cp -r $1/$2/am_overlay/* $1/latest/am_overlay/
cp -r $1/$2/pm_overlay/* $1/latest/pm_overlay/
cp $1/$2/fd_regions.json $1/latest/
cp $1/$2/am_regions.json $1/latest/
cp $1/$2/pm_regions.json $1/latest/

chmod -R 755 $1/latest/
