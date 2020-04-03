#!/bin/bash

planDir=$1
variablefile=$2

cd $planDir

if [ $# -eq 2 ] 
then
    terraform plan -no-color -var-file=$variablefile

else
    terraform plan -no-color 
fi