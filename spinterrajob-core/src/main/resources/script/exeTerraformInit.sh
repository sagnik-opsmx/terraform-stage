#!/bin/bash

planDir=$1

cd $planDir
echo In shell script path :: $planDir

terraform init -no-color

echo :: Finish terraform init part -first time- ::
