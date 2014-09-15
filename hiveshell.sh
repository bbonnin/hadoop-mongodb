#!/bin/bash

echo "Start the hive shell with the following arguments : $*"
rm -f hadoop_mongodb.log
mvn exec:exec -Dexec.arguments="$*" -Phiveshell


