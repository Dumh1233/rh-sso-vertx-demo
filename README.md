# rh-sso-vertx-demo
Project for demonstrating Red Hat SSO Integration with a sample Java Web Application

There is an applications hosted here, A Simple Java Web Application. ( BookStore ) 

The idea is to demonstrate SSO Integration from Red Hat Single Sign On Server and the java application. 

The Bookstore application is deployed from a custom container image build using the Dockerfile. 

If you wish to change parameters or such in the Bookstore application (inside myproject folder),
after the changes you'll need to run:
```
jar -cvf myproject.war *
``` 
and override the current myproject.war file

Before you build the Dockerfile you'll need to create a crt using the following command:

```
openssl s_client -connect <Your RHSSO route URL>:443 -showcerts </dev/null | sed -n '/-----BEGIN CERTIFICATE-----/,/-----END CERTIFICATE-----/p' > rhsso.crt
```
and place the output file rhsso.crt where the Dockerfile resides.

