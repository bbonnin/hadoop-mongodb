#!/bin/bash

echo "Start the mini-cluster with the following arguments : $*"
rm -f hadoop_mongodb.log
mvn exec:exec -Dexec.arguments="$*" -Pcluster


