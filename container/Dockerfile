FROM opsmx11/java:ubuntu16_java8

MAINTAINER OpsMx

COPY /terraform  /usr/local/bin/terraform

#COPY /git  /usr/bin/git

RUN apt-get update && apt-get install -y \
    git \
    curl \
    jq

RUN groupadd -g 999 terraspin && \
    useradd -r -u 999 -g terraspin terraspin

RUN mkdir -p /home/terraspin

RUN chown terraspin /home/terraspin

USER terraspin

RUN mkdir -p /home/terraspin/artifact /home/terraspin/extra /home/terraspin/opsmx/app/config 

RUN touch /home/terraspin/artifact/terraspin.log && \
    chmod 777 /home/terraspin/artifact/terraspin.log

WORKDIR /home/terraspin

COPY /terraform  /usr/local/bin/terraform

COPY /PlanRun.sh  /usr/local/bin/PlanRun.sh

COPY /ApplyRun.sh  /usr/local/bin/ApplyRun.sh

COPY /DestroyRun.sh  /usr/local/bin/DestroyRun.sh

COPY /TerraSpin.jar  /home/terraspin/artifact/TerraSpin.jar

#COPY /run.sh  /usr/local/bin/run.sh

#CMD run.sh
