wget -r -np -nH --cut-dirs=5 -A .jpg -P /mnt/albina_files_dev/$1/$2/ https://data1.geo.univie.ac.at/exchange/albina2/awm_dev/$1/$2/
wget -nH --cut-dirs=5 -P /mnt/albina_files_dev/$1/$2/ https://data1.geo.univie.ac.at/exchange/albina2/awm_dev/$1/$2/fd_regions.json
wget -nH --cut-dirs=5 -P /mnt/albina_files_dev/$1/$2/ https://data1.geo.univie.ac.at/exchange/albina2/awm_dev/$1/$2/am_regions.json
wget -nH --cut-dirs=5 -P /mnt/albina_files_dev/$1/$2/ https://data1.geo.univie.ac.at/exchange/albina2/awm_dev/$1/$2/pm_regions.json
wget -nH --cut-dirs=5 -P /mnt/albina_files_dev/$1/$2/ https://data1.geo.univie.ac.at/exchange/albina2/awm_dev/$1/$2/fd_overlay.png
wget -nH --cut-dirs=5 -P /mnt/albina_files_dev/$1/$2/ https://data1.geo.univie.ac.at/exchange/albina2/awm_dev/$1/$2/am_overlay.png
wget -nH --cut-dirs=5 -P /mnt/albina_files_dev/$1/$2/ https://data1.geo.univie.ac.at/exchange/albina2/awm_dev/$1/$2/pm_overlay.png

cp /mnt/albina_files_dev/$1/$2/* /mnt/albina_files_dev/$1/  

chmod 744 /mnt/albina_files_dev/$1/$2/*
chmod 744 /mnt/albina_files_dev/$1/*