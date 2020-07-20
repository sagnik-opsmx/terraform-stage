#!/bin/bash

echo -e "Executing Terraform Destroy ...."
java -jar /home/terraspin/artifact/TerraSpin.jar 2>&1 > /home/terraspin/artifact/terraspin.log

RETURN_CODE=$?

if [ $RETURN_CODE -eq 0 ]; then

    echo -e "Terraform Destroy Execution completed Successfully"
    echo -e '\n\n \t\t ================================ Terraform Destroy Output ====================================== \t\t\n\n'

    DESTROYSTATUS=`jq -r .status /home/terraspin/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId/destroyStatus | tr -d '\n'`

    echo -e 'Terraform Destroy Status:' $DESTROYSTATUS "\n"

    if [ $DESTROYSTATUS != "SUCCESS" ]; then
	echo "Failed while executing Terraform Destroy stage\n\n\n"
        cat /home/terraspin/artifact/terraspin.log
        exit 127
    fi

    jq -r .output /home/terraspin/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId/destroyStatus | grep -E "Destroy complete! Resources: "

    exit 0 
else
    ## Error while executing terraform plan
    echo -e "Error encountered while executing Terraform Destroy\n\n\n"
    cat /home/terraspin/artifact/terraspin.log
    exit $RETURN_CODE
fi


