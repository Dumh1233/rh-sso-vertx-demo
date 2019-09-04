Deploy Vert.x Application on Openshift
======================================

1) Deploy MongoDB 
-----------------
export CATALOG_PRJ=coolstore-catalog
oc new-project $CATALOG_PRJ
oc process -f etc/coolstore-catalog-mongodb-persistent.yaml -p CATALOG_DB_USERNAME=mongo -p CATALOG_DB_PASSWORD=mongo -n $CATALOG_PRJ | oc create -f - -n $CATALOG_PRJ 

2) Deploy Vert.x Application
----------------------------
oc adm policy add-role-to-group view system:serviceaccounts:$CATALOG_PRJ -n $CATALOG_PRJ
oc create configmap app-config --from-file=etc/app-config.yaml -n $CATALOG_PRJ
mvn clean fabric8:deploy -Popenshift -Dfabric8.namespace=$CATALOG_PRJ

3) Test your Catalog Application
-----------------------------
export CATALOG_URL=http://$(oc get route catalog-service -n $CATALOG_PRJ -o template --template='{{.spec.host}}')
curl -X GET "$CATALOG_URL/health/readiness"
curl -X GET "$CATALOG_URL/health/liveness"
curl -X GET "$CATALOG_URL/products"
curl -X GET "$CATALOG_URL/product/444435"

4) Swagger Docs

echo $CATALOG_URL

# Copy and paste the URL into a web browser. Expect to see the Swagger docs for the REST endpoints. 


