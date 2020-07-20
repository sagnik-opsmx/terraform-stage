#!/bin/bash

planDir=$1
variablefile=$2

cd $planDir

if [ $# -eq 2 ] 
then
    terraform plan -no-color -out /home/terraspin/.opsmx/script/plan_out -var-file=$variablefile

else
    terraform plan -no-color -out /home/terraspin/.opsmx/script/plan_out
fi
