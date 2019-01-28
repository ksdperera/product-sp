## Prerequisites

1. Add needed connector jars (OSGified) to tests/docker-file/connector-files directory

## How to run
1. Run command mvn clean install -Ddocker.removeVolumes=true

## Useful Docker commands

1. Build docker file : docker build -t wso2sp-worker:4.4.0 .
2. Docker Run (Background) : docker run -d wso2sp-worker:4.4.0
3. Docker Run (Foreground) : docker run -it wso2sp-worker:4.3.0
4. Access file system in docker image: docker exec -t -i <container_id> /bin/bash

