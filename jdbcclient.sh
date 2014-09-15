#!/bin/bash

echo "Start the jdbc client tests with the following arguments : $*"
rm -f hadoop_mongodb.log
mvn exec:exec -Dexec.arguments="$*" -Pjdbcclient



