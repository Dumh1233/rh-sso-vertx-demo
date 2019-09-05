#!/bin/bash

# Choose a Project Name to host your applications. 
export CATALOG_PRJ=coolstore-catalog

# Create a Project to host your applications. 
oc new-project $CATALOG_PRJ

# Deploy MongoDB using mongodb-template. 
oc process -f etc/coolstore-catalog-mongodb-persistent.yaml -p CATALOG_DB_USERNAME=mongo -p CATALOG_DB_PASSWORD=mongo -n $CATALOG_PRJ | oc create -f - -n $CATALOG_PRJ

