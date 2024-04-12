rm $1/*.html

ln --symbolic --relative --force $1/$2/*.html $1/

chmod 755 $1/*.html

