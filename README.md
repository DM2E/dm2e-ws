dm2e-ws
=======

DM2E Webservices

Summary:

Here, you can find the webservices that are developed as part of the DM2E infrastructure.

Instructions:

- Install Git, JDK, and Maven.
- git clone git@github.com:DM2E/dm2e-ws.git
- mvn compile
- bash bin/gui-console.sh

Test:
```
source curl_rest.sh
GET $SRV/service/xslt
GETT $SRV/service/xslt 
GETJ $SRV/service/xslt 
```
- Open http://localhost:9998/data in your browser
- A webservice description in RDF should be shown
  (depending on your browser settings, you have to look at the page source.)


Post Test:

 curl --data "[] <http://purl.org/dc/terms/creator> <http://localhost/kai>; <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://onto.dm2e.eu/omnom/WebServiceConfig> ." http://localhost:9998/data/configurations

This should return your RDF data and replace the blank node with a new URI.

Have fun!

Grafeo
------

Grafeo is an easy to use RDF framework.

Create a grafeo:
```java
Grafeo g1 = new GrafeoImpl();
Grafeo g2 = new GrafeoImpl("http://foo.bar/x.rdf"); // load from URI, guess format
Grafeo g3 = new GrafeoImpl("<http://foo/res1> <http://foo/prop1> <http://foo/res2>", true); // load from String, guess format
```

Add statements:
```java
g.addTriple("http://foo/res1", "rdf:type", "http://foo/res2"); // can use URI or qname, common prefixes pre-defined
g.setNamespace("foo", "http://foo/");
g.addTriple("foo:res1", "rdf:type", "foo:res2"); // same thing
```

Serialize it:
```java
System.out.println(g.getNTriples());
System.out.println(g.getTurtle());
System.out.println(g.getTerseTurtle()); // Turtle sans the @prefix, not valid but easier to read
```

Publish it:
```java
g.putToEndpoint("http://endpoint", "htttp://name-of-the-graph-to-put-to"); // this empties the graph first
g.postToEndpoint("http://endpoint", "htttp://name-of-the-graph-to-put-to"); // this adds the statements to the graph
```

