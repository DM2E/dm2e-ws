package eu.dm2e.ws.api;

import com.hp.hpl.jena.query.ResultSet;
import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;
import eu.dm2e.ws.grafeo.jena.SparqlSelect;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 * <p/>
 * Author: Kai Eckert, Konstantin Baierer
 */
@Namespaces({"omnom", "http://onto.dm2e.eu/omnom/",
        "dc", "http://purl.org/dc/elements/1.1/",
        "rdfs", "http://www.w3.org/2000/01/rdf-schema#",
        "prov", "http://www.w3.org/ns/prov#",
        "void", "http://rdfs.org/ns/void#",
        "dm2e", "http://onto.dm2e.eu/schemas/dm2e/1.0/"})
@RDFClass("void:Dataset")
public class VersionedDatasetPojo extends AbstractPersistentPojo<VersionedDatasetPojo> {

    @RDFId
    private String id;

    @RDFProperty("dc:date")
    private Date timestamp;

    @RDFProperty("prov:specializationOf")
    private URI datasetID;

    @RDFProperty("prov:wasRevisionOf")
    private URI priorVersionURI;

    @RDFProperty("prov:wasGeneratedBy")
    private URI jobURI;

    @RDFProperty("rdfs:label")
    private String label;

    @RDFProperty("rdfs:comment")
    private String comment;


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

    public URI getJobURI() {
        return jobURI;
    }

    public void setJobURI(URI jobURI) {
        this.jobURI = jobURI;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public URI getDatasetID() {
        return datasetID;
    }

    public void setDatasetID(URI datasetID) {
        this.datasetID = datasetID;
    }

    public URI getPriorVersionURI() {
        return priorVersionURI;
    }

    public void setPriorVersionURI(URI priorVersionURI) {
        this.priorVersionURI = priorVersionURI;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


}
