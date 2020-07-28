#!/bin/bash

planDir=$1
#variablefile=\"$2\"
#variablefile='"$2"'
variablefile=$2

cd $planDir

if [ $# -eq 2 ]
then
   terraform apply -no-color -compact-warnings -var-file=$variablefile
else
   terraform apply -no-color -compact-warnings
fi

mkdir -p $HOME/state_dir
cp -r * $HOME/state_dir/
