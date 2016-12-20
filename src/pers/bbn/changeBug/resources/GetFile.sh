#!/bin/bash
gitFile=$1
FilePath=/home/niu/test/
cd $FilePath$gitFile
rm -rf ../$1"Files"
mkdir ../$1"Files"
cat /home/niu/workspace/changeClassify/cfrc.txt | while read commit_id file_id rev current_file_path
do
git reset $rev $current_file_path
git checkout $current_file_path
if test -e $current_file_path;then
   cp $current_file_path "../"$1"Files/"$commit_id"_"$file_id".java"
fi
done
chmod 777 -R ../$gitFile"Files"
rm -rf /home/niu/workspace/changeClassify/$gitFile"Files"
cp -r ../$gitFile"Files" /home/niu/workspace/changeClassify/$gitFile"Files"
