#!/bin/bash

planDir=$1
#variablefile=\"$2\"
#variablefile='"$2"'
variablefile=$2

cd $planDir

if [ $# -eq 2 ] 
then
   terraform apply -no-color -var-file=$variablefile 
else
   terraform apply -no-color 
fi