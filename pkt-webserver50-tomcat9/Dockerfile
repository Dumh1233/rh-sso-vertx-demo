FROM registry.redhat.io/jboss-webserver-5/webserver50-tomcat9-openshift
MAINTAINER Praveen Kr Thakur <pkthakur@redhat.com>
ADD keycloak-tomcat-adapter-dist-7.0.0.zip /opt/jws-5.0/tomcat/lib/keycloak-tomcat-adapter-dist-7.0.0.zip
RUN cd /opt/jws-5.0/tomcat/lib && unzip keycloak-tomcat-adapter-dist-7.0.0.zip
ADD myproject.war /deployments/ROOT.war
ADD tomcat-users.xml /opt/jws-5.0/tomcat/conf/tomcat-users.xml
ADD server.xml /opt/jws-5.0/tomcat/conf/server.xml
ADD keystore /home/jboss/.keystore
ADD keystore /etc/pki/ca-trust/extracted/java/cacerts
