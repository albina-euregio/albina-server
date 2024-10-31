if [ -e "$1/latest" ]; then
  rm $1/latest/*
else
  mkdir $1/latest
fi

ln --symbolic --relative --force $1/$2/*.* $1/latest/

rm $3/*.html
ln --symbolic --relative --force $3/$2/*.html $3/

# drop date prefix from files (2024-10-30_IT-32-BZ_fr_CAAMLv6.xml -> IT-32-BZ_fr_CAAMLv6.xml; 2024-10-30_ has 11 chars)
for file in $1/latest/${2}_*; do
  filename=$(basename $file)
  mv $1/latest/${filename} $1/latest/${filename:11}
done
