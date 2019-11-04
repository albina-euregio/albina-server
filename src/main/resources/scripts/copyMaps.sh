rm /mnt/albina_files/$1/*
rm -r /mnt/albina_files/$1/fd_overlay
rm -r /mnt/albina_files/$1/am_overlay
rm -r /mnt/albina_files/$1/pm_overlay

mkdir /mnt/albina_files/$1/fd_overlay
mkdir /mnt/albina_files/$1/am_overlay
mkdir /mnt/albina_files/$1/pm_overlay

cp /mnt/albina_files/$1/$2/*.jpg /mnt/albina_files/$1/
cp /mnt/albina_files/$1/$2/fd_overlay.png /mnt/albina_files/$1/
cp -r /mnt/albina_files/$1/$2/fd_overlay/* /mnt/albina_files/$1/fd_overlay/
cp -r /mnt/albina_files/$1/$2/am_overlay/* /mnt/albina_files/$1/am_overlay/
cp -r /mnt/albina_files/$1/$2/pm_overlay/* /mnt/albina_files/$1/pm_overlay/
cp /mnt/albina_files/$1/$2/fd_regions.json /mnt/albina_files/$1/
chmod -R 744 /mnt/albina_files/$1/*