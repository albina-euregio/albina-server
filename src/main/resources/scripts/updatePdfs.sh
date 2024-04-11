rm $1/$2/$2_*.pdf

ln --symbolic --relative --force $1/$2/$3/$2_*.pdf $1/$2/

chmod -R 755 $1/$2/

