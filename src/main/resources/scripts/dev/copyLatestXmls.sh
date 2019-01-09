cp /mnt/albina_files_dev/$1/*.xml /mnt/albina_files_dev/latest/
mv /mnt/albina_files_dev/latest/$1_de.xml /mnt/albina_files_dev/latest/de.xml
mv /mnt/albina_files_dev/latest/$1_it.xml /mnt/albina_files_dev/latest/it.xml
mv /mnt/albina_files_dev/latest/$1_en.xml /mnt/albina_files_dev/latest/en.xml
chmod 744 /mnt/albina_files_dev/latest/de.xml
chmod 744 /mnt/albina_files_dev/latest/it.xml
chmod 744 /mnt/albina_files_dev/latest/en.xml