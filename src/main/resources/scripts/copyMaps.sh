rm $1/$2/*
rm -r $1/$2/fd_overlay
rm -r $1/$2/am_overlay
rm -r $1/$2/pm_overlay

mkdir $1/$2/fd_overlay
mkdir $1/$2/am_overlay
mkdir $1/$2/pm_overlay

cp $1/$2/$3/*.jpg $1/$2/
cp $1/$2/$3/fd_overlay.png $1/$2/
cp -r $1/$2/$3/fd_overlay/* $1/$2/fd_overlay/
cp -r $1/$2/$3/am_overlay/* $1/$2/am_overlay/
cp -r $1/$2/$3/pm_overlay/* $1/$2/pm_overlay/
cp $1/$2/$3/fd_regions.json $1/$2/
chmod -R 744 $1/$2/*