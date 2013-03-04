dm2e-ws
=======

DM2E Webservices

Summary:

Here, you can find the webservices that are developed as part of the DM2E infrastructure.

Instructions:

- Install Git, JDK, and Maven.
- git clone git@github.com:DM2E/dm2e-ws.git
- Run  mvn -e compile exec:java -Dexec.mainClass="eu.dm2e.ws.Main"

Test:

- Open http://localhost:9998/data in your browser
- Triple: <http://localhost/data> <http://purl.org/dc/terms/creator> <http://localhost/kai> should be shown

Post Test:

curl --data "[] <http://purl.org/dc/terms/creator> <http://localhost/kai> ." http://localhost:9998/data/configurations

This should return your RDF data and replace the blank node with a new URI.