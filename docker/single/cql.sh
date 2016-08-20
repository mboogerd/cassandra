#!/usr/bin/env bash

locateActiveDockerMachineIP() {
  RUNNING_MACHINES=$(docker-machine ls | grep -i running | awk '{print $1;}');
    if (( $(grep -c . <<<"$RUNNING_MACHINES") > 1 )); then
        echo "Multiple docker-machines have been found running, unable to identify desired interface to connect to:\n$RUNNING_MACHINES"
        exit 1
    else
        export DOCKER_MACHINE_IP=$(docker-machine ip $RUNNING_MACHINES)
    fi
}

# Call with [port] and [label] to block until the port is opened. Reports back each second
wait() {
    echo "Checking for availability of $2 on $DOCKER_MACHINE_IP:$1"

    TIMEOUT=1
    testServiceUp="(echo > /dev/tcp/$DOCKER_MACHINE_IP/$1) >/dev/null 2>&1 && echo 1 || echo 0"

    currentState=$(eval $testServiceUp)
    while [[ $currentState -lt 1 ]]
    do
      echo "$2 appears down, checking again in $TIMEOUT seconds"
      sleep $TIMEOUT
      currentState=$(eval $testServiceUp)
    done
}


locateActiveDockerMachineIP

# Wait until the native protocol interface is up
wait 9042 Cassandra

# Launch CQL shell
docker exec -it cassandra-single cqlsh