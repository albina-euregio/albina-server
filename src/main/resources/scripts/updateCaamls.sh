rm $1/$2/$2_$4_*.xml
rm $1/$2/$2_$4_*_CAAMLv6_2022.json
rm $1/$2/$4.json

cp $1/$2/$3/$2_$4_*.xml $1/$2/
cp $1/$2/$3/$2_$4_CAAMLv6_2022.json $1/$2/
cp $1/$2/$3/$4.json $1/$2/

chmod -R 755 $1/$2/

