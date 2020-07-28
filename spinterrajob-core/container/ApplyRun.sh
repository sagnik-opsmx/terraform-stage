#!/bin/bash

echo -e "Executing Terraform Apply ...."
java -jar /home/terraspin/artifact/TerraSpin.jar 2>&1 > /home/terraspin/artifact/terraspin.log
RETURN_CODE=$?

if [ $RETURN_CODE -eq 0 ]; then

    echo -e "Terraform Apply Execution completed Successfully"
    echo -e '\n\n \t ================================ Terraform Apply Output ====================================== \t\t\n\n'

    APPLYSTATUS=`jq -r .status /home/terraspin/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId/applyStatus | tr -d '\n'`

    echo -e 'Terraform Apply Status:' $APPLYSTATUS "\n"

    if [ $APPLYSTATUS != "SUCCESS" ]; then
	echo -e "Failed while executing Terraform Apply stage\n\n\n"
        cat /home/terraspin/artifact/terraspin.log
        echo -e "Printing Plan Status...\n\n"
        jq -r .output /home/terraspin/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId/applyStatus
        exit 1
    fi

    jq -r .output /home/terraspin/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId/applyStatus | grep -E "Apply complete! Resources: "

    echo -e '\n\n \t =================================== Additional Info ========================================= \t\t\n\n'

    cd $HOME/state_dir
    terraform init > /dev/null
    terraform show -no-color terraform.tfstate
    cd $HOME

    cat /home/terraspin/artifact/terraspin.log | grep 'SPINNAKER_PROPERTY_'
    echo 'SPINNAKER_PROPERTY_APPLYSTATUS='$APPLYSTATUS

    exit 0

else
    ## Error while executing terraform plan
    echo -e "Error encountered while executing Terraform Apply\n\n\n"
    cat /home/terraspin/artifact/terraspin.log
    exit $RETURN_CODE
fi
