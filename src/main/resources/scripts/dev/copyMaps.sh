wget -r -np -nH --cut-dirs=5 -A .jpg -P /mnt/albina_files_dev/$1/ https://data1.geo.univie.ac.at/exchange/albina2/awm_dev/$1/
rm /mnt/albina_files_dev/$1/fd_regions.json
wget -nH --cut-dirs=5 -P /mnt/albina_files_dev/$1/ https://data1.geo.univie.ac.at/exchange/albina2/awm_dev/$1/fd_regions.json
rm /mnt/albina_files_dev/$1/fd_overlay.png
wget -nH --cut-dirs=5 -P /mnt/albina_files_dev/$1/ https://data1.geo.univie.ac.at/exchange/albina2/awm_dev/$1/fd_overlay.png
chmod 744 /mnt/albina_files_dev/$1/*