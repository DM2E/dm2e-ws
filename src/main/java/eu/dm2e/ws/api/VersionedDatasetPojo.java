package eu.dm2e.ws.api;

import java.net.URI;
import java.net.URISyntaxException;

import org.joda.time.DateTime;

import com.hp.hpl.jena.query.ResultSet;

import eu.dm2e.NS;
import eu.dm2e.grafeo.annotations.Namespaces;
import eu.dm2e.grafeo.annotations.RDFClass;
import eu.dm2e.grafeo.annotations.RDFProperty;
import eu.dm2e.grafeo.jena.SparqlSelect;
//import java.util.Date;

/**
 * Pojo representing a Versioned Dataset.
 */
@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/",
        "dc", "http://purl.org/dc/elements/1.1/",
        "rdfs", "http://www.w3.org/2000/01/rdf-schema#",
        "prov", "http://www.w3.org/ns/prov#",
        "void", "http://rdfs.org/ns/void#"})
@RDFClass("void:Dataset")
public class VersionedDatasetPojo extends AbstractPersistentPojo<VersionedDatasetPojo> {


    /**
     * Find the latest dataset version.
     * 
     * <pre>
     * # ?this == URI of this dataset
     * SELECT ?s {
     *   ?s prov:specializationOf ?this.
     *   ?s dc:date ?date .
     * }
     * ORDER BY DESC(?date)
     * LIMIT 1
     * </pre>
     * @param endpoint the SPARQL endpoint to query
     * @return the URI of the latest dataset
     * @throws {@link RuntimeException} if the previous dataset id is an invalid URI
     */
    public URI findLatest(String endpoint) {
        log.info("I try to find a prior version for this dataset...");
        SparqlSelect sparql = new SparqlSelect.Builder().endpoint(endpoint)
                .prefix("prov", "http://www.w3.org/ns/prov#")
                .prefix("dc", "http://purl.org/dc/elements/1.1/")
                .select("?s").where("?s prov:specializationOf <" + getDatasetID() + "> . ?s dc:date ?date .")
                .orderBy(" DESC(?date)")
                .build();
        log.info("SPARQL: " + sparql.toString());
        ResultSet result = sparql.execute();
        if (result.hasNext())  {
            log.info("Oh, I found something...");
            String priorURI = result.next().get("?s").asResource().getURI();
            try {
                setPriorVersionURI(new URI(priorURI));
                return getPriorVersionURI();
            } catch (URISyntaxException e) {
                throw new RuntimeException("An exception occurred: " + e, e);
            }
        }
        log.info("Nothing found :-(");
        return null;
    }



    @RDFProperty(NS.PROV.PROP_WAS_GENERATED_BY)
    private URI jobURI;
    public URI getJobURI() { return jobURI; }
    public void setJobURI(URI jobURI) { this.jobURI = jobURI; }

    @RDFProperty(NS.PROV.PROP_SPECIALIZATION_OF)
    private URI datasetID;
    public URI getDatasetID() { return datasetID; }
    public void setDatasetID(URI datasetID) { this.datasetID = datasetID; }

    @RDFProperty(NS.PROV.PROP_WAS_REVISION_OF)
    private URI priorVersionURI;
    public URI getPriorVersionURI() { return priorVersionURI; }
    public void setPriorVersionURI(URI priorVersionURI) { this.priorVersionURI = priorVersionURI; }

    @RDFProperty(NS.DC.PROP_DATE)
    private DateTime timestamp;
    public DateTime getTimestamp() { return timestamp; }
    public void setTimestamp(DateTime timestamp) { this.timestamp = timestamp; }

//    @RDFProperty(NS.RDFS.PROP_LABEL)
//    private String label;
//    public String getLabel() { return label; }
//    public void setLabel(String label) { this.label = label; }

    @RDFProperty(NS.RDFS.PROP_COMMENT)
    private String comment;
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    

}
