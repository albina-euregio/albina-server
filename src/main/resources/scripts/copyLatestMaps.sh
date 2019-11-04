rm /mnt/albina_files/*
rm -r /mnt/albina_files/fd_overlay
rm -r /mnt/albina_files/am_overlay
rm -r /mnt/albina_files/pm_overlay

mkdir /mnt/albina_files/fd_overlay
mkdir /mnt/albina_files/am_overlay
mkdir /mnt/albina_files/pm_overlay

cp /mnt/albina_files/$1/*.jpg /mnt/albina_files/latest/
cp /mnt/albina_files/$1/fd_overlay.png /mnt/albina_files/latest/
cp -r /mnt/albina_files/$1/fd_overlay/* /mnt/albina_files/latest/fd_overlay/
cp -r /mnt/albina_files/$1/am_overlay/* /mnt/albina_files/latest/am_overlay/
cp -r /mnt/albina_files/$1/pm_overlay/* /mnt/albina_files/latest/pm_overlay/
cp /mnt/albina_files/$1/fd_regions.json /mnt/albina_files/latest/
chmod -R 744 /mnt/albina_files/latest/*