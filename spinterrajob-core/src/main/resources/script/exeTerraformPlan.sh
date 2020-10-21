#!/bin/bash

planDir=$1
variablefile=$2

cd $planDir

if [ $# -eq 2 ]
then
    terraform plan -no-color -compact-warnings -out planOut -var-file=$variablefile

else
    terraform plan -no-color -compact-warnings -out planOut
fi

mkdir -p $HOME/state_dir
cp -r * $HOME/state_dir/
