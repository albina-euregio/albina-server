rm /mnt/albina_files/latest/*
cp /mnt/albina_files/$1/*.png /mnt/albina_files/latest/
mv /mnt/albina_files/latest/$1_de.png /mnt/albina_files/latest/de.png
mv /mnt/albina_files/latest/$1_it.png /mnt/albina_files/latest/it.png
mv /mnt/albina_files/latest/$1_en.png /mnt/albina_files/latest/en.png
chmod 744 /mnt/albina_files/latest/de.png
chmod 744 /mnt/albina_files/latest/it.png
chmod 744 /mnt/albina_files/latest/en.png