wget -r -np -nH --cut-dirs=5 -A .jpg -P /mnt/albina_files/$1/ https://data1.geo.univie.ac.at/exchange/albina2/awm/$1/
wget -nH --cut-dirs=5 -P /mnt/albina_files/$1/ https://data1.geo.univie.ac.at/exchange/albina2/awm/$1/fd_regions.json
wget -nH --cut-dirs=5 -P /mnt/albina_files/$1/ https://data1.geo.univie.ac.at/exchange/albina2/awm/$1/fd_overlay.png
chmod 744 /mnt/albina_files/$1/*