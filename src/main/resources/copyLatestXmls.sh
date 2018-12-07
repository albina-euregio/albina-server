rm /mnt/albina_files/latest/*
cp /mnt/albina_files/$1/*.xml /mnt/albina_files/latest/
mv /mnt/albina_files/latest/$1_de.xml /mnt/albina_files/latest/de.xml
mv /mnt/albina_files/latest/$1_it.xml /mnt/albina_files/latest/it.xml
mv /mnt/albina_files/latest/$1_en.xml /mnt/albina_files/latest/en.xml
chmod 744 /mnt/albina_files/latest//mnt/albina_files/latest/de.xml
chmod 744 /mnt/albina_files/latest//mnt/albina_files/latest/it.xml
chmod 744 /mnt/albina_files/latest//mnt/albina_files/latest/en.xml