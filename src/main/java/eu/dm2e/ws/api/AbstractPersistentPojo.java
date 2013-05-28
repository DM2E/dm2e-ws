package eu.dm2e.ws.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.logging.Logger;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFInstancePrefix;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

public abstract class AbstractPersistentPojo<T> {
	
    Logger log = Logger.getLogger(getClass().getName());
//	public static getClass {
//		return T;
//	}
	abstract String getId();
	abstract void setId(String id);
	
	public URI getIdAsURI() {
		URI uri = null;
		try {
			uri = new URI(getId());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return uri;
	}
	
	// TODO this should be a static method but it's impossible to determine the runtime static class
	public T constructFromRdfString(String rdfString, String id) {
		Grafeo g = new GrafeoImpl();
		g.readHeuristically(rdfString);
		T theThing = g.getObject(this.getClass(), id);
		return theThing;
	}
	public T constructFromRdfString(String rdfString) {
		Grafeo g = new GrafeoImpl();
		g.readHeuristically(rdfString);
		String rdfType = this.getClass().getAnnotation(RDFClass.class).value();
		String prefix = "http://FOOBAR/";
		try { 
			prefix = this.getClass().getAnnotation(RDFInstancePrefix.class).value();
		} catch (NullPointerException e) {
			// TODO
			throw(e);
		}
		GResource topBlank = g.findTopBlank(rdfType);
		T theThing;
		if (null != topBlank) {
			String newURI = prefix + UUID.randomUUID().toString();;
			topBlank.rename(newURI);
			theThing = g.getObject(this.getClass(), newURI);
		}
		else {
			throw new RuntimeException("No top blank node.");
		}
		return theThing;
	}
	
	public T readFromEndPointById(String id) {
		this.setId(id);
		T theNewPojo = this.readFromEndpoint();
		return theNewPojo;
	}
	public T readFromEndPointById(String id, String endpoint) {
		this.setId(id);
		T theNewPojo = this.readFromEndpoint(endpoint);
		return theNewPojo;
	}
	public T readFromEndPointById(String id, String endpoint, String graph) {
		this.setId(id);
		T theNewPojo = this.readFromEndpoint(endpoint, graph);
		return theNewPojo;
	}
	public T readFromEndpoint(String endpoint, String graph) {
		Grafeo g = new GrafeoImpl();
		g.readFromEndpoint(endpoint, graph);
		return g.getObject(this.getClass(), graph);
	}
	public T readFromEndpoint(String endpoint) {
		return readFromEndpoint(endpoint, this.getId());
	}
	public T readFromEndpoint() {
		String endPoint = Config.getString("dm2e.ws.sparql_endpoint");
		return readFromEndpoint(endPoint);
	}
	
	public void publish(String endPoint, String graph) {
        log.info("Writing to endpoint: " + endPoint + " / Graph: " + graph);
		Grafeo g = new GrafeoImpl();
		g.addObject(this);
		g.emptyGraph(endPoint, graph);
		g.writeToEndpoint(endPoint, graph);
	}
	public void publish(String endPoint) {
		if (null == this.getId()) {
			String prefix = this.getClass().getAnnotation(RDFInstancePrefix.class).value();
			String newURI = prefix+UUID.randomUUID().toString();
			this.setId(newURI);
		}
		this.publish(endPoint, this.getId());
	}
	public void publish() {
		String endPoint = Config.getString("dm2e.ws.sparql_endpoint_statements");
		this.publish(endPoint);
	}
	
	public Grafeo getGrafeo() {
		GrafeoImpl g = new GrafeoImpl();
		g.addObject(this);
		return g;
	}
	
	public String getTurtle() {
		return getGrafeo().getTurtle();
	}
	public String getNTriples() {
		return getGrafeo().getNTriples();
	}
	public String getCanonicalNTriples() {
		return getGrafeo().getCanonicalNTriples();
	}
	
}
