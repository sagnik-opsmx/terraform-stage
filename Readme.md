# Terraspin -- :whale:

A microservice to integrate with Spinnaker for planning, applying and destroying Terraform plans

### Introduction

#### Purpose
The purpose of this document is to describe the implementation of TerraSpin service, configuration and how it will work with spinnaker

#### Abstract
TerraSpin service is microservice that performs Terraform action based on user call, current service is available in two architectures one is event-based and second is Rest API base both architecture services fulfill following operations of Terraform (Plan, Apply, output and destroy). Terraform output function will be a part of TerraSpin Apply function if the user terraform *.tf plan script contains output resources those outputs will be available in an apply API response or apply event response.

The idea behind the implementation of this service to perform terraform function form Spinnaker as Spinnaker native stage, Spinnaker gives a way to create your custom native stage and we developed microservices as open-source which handles terraform operation like a plan, apply and destroy which easily integrate with a spinnaker in form of spinnaker Custom webhook and spinnaker Custom job with TerraSpin service

                             :
![alt text](https://github.com/OpsMx/terraform-stage/blob/master/TerraspinArch.png "Spinnaker and Terraspin configuration architecture")

```
Now let’s see dependency and input requirements for TerraSpin service while running the functionality.

Dependency
1.  Artifact credentials config JSON file required as property file for TerraSpin service to fetch different artifact account credentials based on given user input in TerraSpin functionality Spinnaker pipeline stage.

Artifact credentials config JSON example
---------------------------------------------------
{"artifactaccounts":[{"accountname":"my-artifact1","artifacttype":"Github","username":"OpsMx","password":"pass!"},{"accountname":"my-artifact2","artifacttype":"S3","accesskey":"somekey","secretkey":"somekey"}]}
---------------------------------------------------

Input requirements
1. Artifact account This must be one of the “accountname” value that was defined in Artifact credentials config JSON. given “accountname”  account credentials will be used to fetch specified State artifact source, Terraform plan script artifact source and Override file source. 

2. State artifact source as input in each TerraSpin functionality spinnaker pipeline stages on which TerraSpin service will push intermediate terraform state which makes spinnaker user to use each TerraSpin functionality spinnaker pipeline stage without tying into a single spinnaker pipeline.

State artifact source example
---------------------------------------------------
In case of Github
{username}/{repo-name}.git -- E.g. opsmx/staging-terraform-states.git 
In case of S3
s3-{region}.amazonaws.com/bucket  -- E.g. s3-us-west-2.amazonaws.com/terraform-state
---------------------------------------------------

2. Terraform plan script artifact source as input from where TerraSpin service will fetch terraform plan script and do operation based on user TerraSpin API call or event call.

Terraform plan script artifact source example
---------------------------------------------------
In case of Github
{username}/{repo-name}.git//folder up to root tf module -- E.g. OpsMx/staging-terraform-pm.git//azure/k8cluster

In case of S3
s3-{region}.amazonaws.com/bucket/folder up to zip file  -- E.g. s3-us-west-2.amazonaws.com/terraform-module/Namespace..zip
---------------------------------------------------

3. Override file (optional): as input in TerraSpin plan and apply functionality spinnaker pipeline stage If present, the file specified here will be applied on the root module. A possible use-case might be to provide a override tfvars file.

Override file source example
---------------------------------------------------
In case of Github
{username}/{repo-name}.git//folder up to Override file -- E.g. OpsMx/staging-terraform-pm.git//azure/k8cluster/dev.tfvars

In case of S3
s3-{region}.amazonaws.com/bucket/folder up to zip file  -- E.g. s3-us-west-2.amazonaws.com/terraform-dev-module/devlopment/dev.tfvars
---------------------------------------------------

4. UUID a unique user id as input in each TerraSpin functionality spinnaker pipeline stages based on uuid string will make zip file containing terraform intermediates state and push to state artifact source. This can be any unique string based on user choice to identify the terraform state across multiple stages. It is not mandatory to have all the stages(Plan, Apply, Destroy) in the same pipeline. However, they all should have the same UUID.
```
