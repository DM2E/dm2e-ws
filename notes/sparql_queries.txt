# Example SPARQL queries

Head over to http://lelystad.informatik.uni-mannheim.de:3030 to try them out.

## Which web service created a file?
```
PREFIX prov: <http://www.w3.org/ns/prov#>
PREFIX omnom: <http://onto.dm2e.eu/omnom/>
SELECT ?WS ?job ?resource
WHERE {
  GRAPH ?g1 { ?resource prov:wasGeneratedBy ?job }
  GRAPH ?g2 { ?job omnom:webservice ?WS }
}
```

## What were the parameters of a web service job that created a resource?
```
PREFIX prov: <http://www.w3.org/ns/prov#>
PREFIX omnom: <http://onto.dm2e.eu/omnom/>
SELECT DISTINCT ?res ?ws ?param ?value 
WHERE {
    GRAPH ?g1 {
     ?paramAssignment omnom:parameterValue ?value .
     ?paramAssignment omnom:forParam ?param .
     ?wsconf omnom:assignment ?paramAssignment .
     ?wsconf omnom:webservice ?ws .
     ?job omnom:webserviceConfig ?wsconf .
    }
    GRAPH ?g2 {
     ?res prov:wasGeneratedBy ?job .
    }
}
```

## Which files are affected by previous web services in a workflow?

If a web service is determined to create faulty output, all resources based on
web service runs with those faulty output as input are tainted.

```
PREFIX prov: <http://www.w3.org/ns/prov#>
PREFIX omnom: <http://onto.dm2e.eu/omnom/>

SELECT DISTINCT ?wsBAD ?paramBAD ?paramAFFECTED ?paAFFECTED
WHERE {
  GRAPH ?g1 {
    ?job1 omnom:webservice ?wsBAD .
    ?job1 omnom:webserviceConfig ?wsconf1 .
    ?wsBAD omnom:outputParam ?paramBAD .
  }
  GRAPH ?g2 {
    ?workflowjob1 omnom:finishedJobs ?job1 .
    ?workflowjob1 omnom:workflow ?workflow .
    ?workflow omnom:parameterConnector ?paramConnectorBAD .
    ?paramConnectorBAD omnom:fromParam ?paramBAD .
    ?paramConnectorBAD omnom:toParam ?paramAFFECTED .
    ?paramConnectorBAD omnom:toPosition ?positionAFFECTED .
  }
  GRAPH ?g3 {
    ?configAFFECTED omnom:executesPosition ?positionAFFECTED .
    ?jobAFFECTED omnom:webserviceConfig ?configAFFECTED .
    ?jobAFFECTED omnom:assignment ?paAFFECTED .
    ?paAFFECTED omnom:parameterValue ?valueAFFECTED .
  }
}
```
