# ------------------------------------------------------------------------
#
# Copyright 2019 WSO2, Inc. (http://wso2.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License
#
# ------------------------------------------------------------------------

# set base Docker image to AdoptOpenJDK Ubuntu Docker image
FROM adoptopenjdk/openjdk8:jdk8u192-b12
MAINTAINER WSO2 Docker Maintainers "dev@wso2.org"

# set user configurations
ARG USER=wso2carbon
ARG USER_ID=802
ARG USER_GROUP=wso2
ARG USER_GROUP_ID=802
ARG USER_HOME=/home/${USER}
# set dependant files directory
ARG FILES=./files
ARG CON_FILES=./connector-files
ARG SIDDHI_FILES=./siddhi-files

# set wso2 product configurations
ARG WSO2_SERVER=wso2sp
ARG WSO2_SERVER_VERSION=4.4.0-m17-SNAPSHOT
ARG WSO2_SERVER_PACK=${WSO2_SERVER}-${WSO2_SERVER_VERSION}
ARG WSO2_SERVER_HOME=${USER_HOME}/${WSO2_SERVER_PACK}
# set WSO2 EULA
ARG MOTD="\n\
Welcome to WSO2 Docker resources.\n\
------------------------------------ \n\
This Docker container comprises of a WSO2 product, running with its latest GA release \n\
which is under the Apache License, Version 2.0. \n\
Read more about Apache License, Version 2.0 here @ http://www.apache.org/licenses/LICENSE-2.0.\n"

# install required packages
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends netcat && \
    rm -rf /var/lib/apt/lists/* && \
    echo '[ ! -z "$TERM" -a -r /etc/motd ] && cat /etc/motd' \
    >> /etc/bash.bashrc \
    ; echo "$MOTD" > /etc/motd

# create a user group and a user
RUN groupadd --system -g ${USER_GROUP_ID} ${USER_GROUP} && \
    useradd --system --create-home --home-dir ${USER_HOME} --no-log-init -g ${USER_GROUP_ID} -u ${USER_ID} ${USER}

# copy the wso2 product distribution to user's home directory
COPY --chown=wso2carbon:wso2 ${FILES}/${WSO2_SERVER_PACK}/ ${WSO2_SERVER_HOME}/

# copy the Kafka client jars to the product home
COPY --chown=wso2carbon:wso2 ${CON_FILES}/kafka_*.jar ${WSO2_SERVER_HOME}/lib/
COPY --chown=wso2carbon:wso2 ${CON_FILES}/kafka_clients_*.jar ${WSO2_SERVER_HOME}/lib/
COPY --chown=wso2carbon:wso2 ${CON_FILES}/metrics_core_*.jar ${WSO2_SERVER_HOME}/lib/
COPY --chown=wso2carbon:wso2 ${CON_FILES}/scala_library_*.jar ${WSO2_SERVER_HOME}/lib/
COPY --chown=wso2carbon:wso2 ${CON_FILES}/scala_parser_combinators_*.jar ${WSO2_SERVER_HOME}/lib/
COPY --chown=wso2carbon:wso2 ${CON_FILES}/zkclient_*.jar ${WSO2_SERVER_HOME}/lib/
COPY --chown=wso2carbon:wso2 ${CON_FILES}/zookeeper_*.jar ${WSO2_SERVER_HOME}/lib/

# copy mysql connector jar to the server as a third party library
COPY --chown=wso2carbon:wso2 ${CON_FILES}/mysql-connector-java-*-bin.jar ${WSO2_SERVER_HOME}/lib/

# copy mysql connector jar to the server as a third party library
COPY --chown=wso2carbon:wso2 ${SIDDHI_FILES}/* ${WSO2_SERVER_HOME}/wso2/worker/deployment/siddhi-files/

# set the user and work directory
USER ${USER_ID}
WORKDIR ${USER_HOME}

# set environment variables
ENV WSO2_SERVER_HOME=${WSO2_SERVER_HOME} \
    WORKING_DIRECTORY=${USER_HOME}

# copy entrypoint bash script to user home
COPY --chown=wso2carbon:wso2 init.sh ${WORKING_DIRECTORY}/

# expose ports
EXPOSE 9090 9443 9712 9612 7711 7611 7070 7443

ENTRYPOINT ["/home/wso2carbon/init.sh"]
