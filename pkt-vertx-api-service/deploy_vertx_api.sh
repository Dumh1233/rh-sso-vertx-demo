#!/bin/bash


# Choose a Project Name to host your applications. 
export CATALOG_PRJ=coolstore-catalog

# Switch to Catalog Project
oc project $CATALOG_PRJ

# Add "view" role to service account
oc adm policy add-role-to-user view system:serviceaccounts:$CATALOG_PRJ -n $CATALOG_PRJ

# Create configmap containing mongoDB details
oc create configmap app-config --from-file=etc/app-config.yaml -n $CATALOG_PRJ

# Deploy using fabric8 plugin
mvn clean fabric8:deploy -Popenshift -Dfabric8.namespace=$CATALOG_PRJ
