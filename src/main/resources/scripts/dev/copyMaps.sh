wget -r -np -nH --cut-dirs=5 -A .jpg -P /mnt/albina_files_dev/$1/$2/ https://data1.geo.univie.ac.at/exchange/albina2/awm_dev/$1/$2/
rm /mnt/albina_files_dev/$1/$2/fd_regions.json
rm /mnt/albina_files_dev/$1/$2/am_regions.json
rm /mnt/albina_files_dev/$1/$2/pm_regions.json
wget -nH --cut-dirs=5 -P /mnt/albina_files_dev/$1/$2/ https://data1.geo.univie.ac.at/exchange/albina2/awm_dev/$1/$2/fd_regions.json
wget -nH --cut-dirs=5 -P /mnt/albina_files_dev/$1/$2/ https://data1.geo.univie.ac.at/exchange/albina2/awm_dev/$1/$2/am_regions.json
wget -nH --cut-dirs=5 -P /mnt/albina_files_dev/$1/$2/ https://data1.geo.univie.ac.at/exchange/albina2/awm_dev/$1/$2/pm_regions.json
rm /mnt/albina_files/$1/$2/fd_overlay.png
rm /mnt/albina_files/$1/$2/am_overlay.png
rm /mnt/albina_files/$1/$2/pm_overlay.png
wget -nH --cut-dirs=5 -P /mnt/albina_files_dev/$1/$2/ https://data1.geo.univie.ac.at/exchange/albina2/awm_dev/$1/$2/fd_overlay.png
wget -nH --cut-dirs=5 -P /mnt/albina_files_dev/$1/$2/ https://data1.geo.univie.ac.at/exchange/albina2/awm_dev/$1/$2/am_overlay.png
wget -nH --cut-dirs=5 -P /mnt/albina_files_dev/$1/$2/ https://data1.geo.univie.ac.at/exchange/albina2/awm_dev/$1/$2/pm_overlay.png

cp /mnt/albina_files_dev/$1/$2/* /mnt/albina_files_dev/$1/

chmod 744 /mnt/albina_files_dev/$1/$2/*
chmod 744 /mnt/albina_files_dev/$1/*.json
chmod 744 /mnt/albina_files_dev/$1/*.png
chmod 744 /mnt/albina_files_dev/$1/*.jpg
