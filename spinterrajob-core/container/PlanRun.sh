#!/bin/bash

echo -e "Executing Terraform Plan ...."
java -jar /home/terraspin/artifact/TerraSpin.jar 2>&1 > /home/terraspin/artifact/terraspin.log

RETURN_CODE=$?

if [ $RETURN_CODE -eq 0 ]; then

    echo -e "Terraform Plan Execution completed Successfully"
    echo -e '\n\n \t ================================ Terraform Plan Output ====================================== \t\t\n\n'

    PLANSTATUS=`jq -r .status /home/terraspin/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId/planStatus | tr -d '\n'`

    echo -e 'Terraform Plan Status:' $PLANSTATUS "\n"

    if [ $PLANSTATUS != "SUCCESS" ]; then
	echo "Failed while executing Terraform Plan stage\n\n\n"
        cat /home/terraspin/artifact/terraspin.log
        jq -r .output /home/terraspin/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId/planStatus
        exit 1
    fi

    jq -r .output /home/terraspin/.opsmx/spinnaker/applicationName-spinApp/pipelineName-spinPipe/pipelineId-spinPipeId/planStatus | grep -E "Plan: "

    echo -e '\n\n \t =================================== Additional Info ========================================= \t\t\n\n'

    # Run terraform init to download plugins associated with provider
    cd $HOME/state_dir/
    terraform init > /dev/null

    # Show the output of terraform plan
    terraform show -no-color $HOME/state_dir/planOut
    cd $HOME

    exit 0

else
    ## Error while executing terraform plan
    echo -e "Error encountered while executing Terraform Plan\n"
    cat /home/terraspin/artifact/terraspin.log
    exit $RETURN_CODE
fi
