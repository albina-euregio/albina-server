rm $1/$2/*

ln --symbolic --relative --force $1/$2/$3/* $1/$2/

chmod -R 755 $1/$2/
chmod 755 $4/$2/*.html

