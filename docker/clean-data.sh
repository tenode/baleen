#!/bin/bash
echo "Deleting data for elasticsearch and mongo containers"
rm -rf data/elasticsearch data/mongo
mkdir data data/elasticsearch data/mongo
echo "(you may need to run this as root)"
