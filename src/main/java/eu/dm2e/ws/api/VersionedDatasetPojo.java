package eu.dm2e.ws.api;

import java.net.URI;
import java.net.URISyntaxException;

import org.joda.time.DateTime;
//import java.util.Date;

import com.hp.hpl.jena.query.ResultSet;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;
import eu.dm2e.ws.grafeo.jena.SparqlSelect;

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
    
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.baseHashCode();
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((datasetID == null) ? 0 : datasetID.hashCode());
		result = prime * result + ((jobURI == null) ? 0 : jobURI.hashCode());
		result = prime * result + ((priorVersionURI == null) ? 0 : priorVersionURI.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.baseEquals(obj)) return false;
		if (!(obj instanceof VersionedDatasetPojo)) return false;
		VersionedDatasetPojo other = (VersionedDatasetPojo) obj;
		if (comment == null) {
			if (other.comment != null) return false;
		} else if (!comment.equals(other.comment)) return false;
		if (datasetID == null) {
			if (other.datasetID != null) return false;
		} else if (!datasetID.equals(other.datasetID)) return false;
		if (jobURI == null) {
			if (other.jobURI != null) return false;
		} else if (!jobURI.equals(other.jobURI)) return false;
		if (priorVersionURI == null) {
			if (other.priorVersionURI != null) return false;
		} else if (!priorVersionURI.equals(other.priorVersionURI)) return false;
		if (timestamp == null) {
			if (other.timestamp != null) return false;
		} else if (!timestamp.equals(other.timestamp)) return false;
		return true;
	}


}
