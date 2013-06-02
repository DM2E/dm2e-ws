package eu.dm2e.ws.api;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.commons.beanutils.BeanUtils;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.grafeo.Grafeo;
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
	
	// TODO think harder whether this is necessary and how to do skolemnization properly
	// TODO this should be a static method but it's impossible to determine the runtime static class
//	public T constructFromRdfString(String rdfString, String id) {
//		Grafeo g = new GrafeoImpl();
//		g.readHeuristically(rdfString);
//		return g.getObjectMapper().getObject(this.getClass(), id);
//	}
//	
//	public T constructFromRdfString(String rdfString) {
//		Grafeo g = new GrafeoImpl();
//		g.readHeuristically(rdfString);
//		String rdfType = this.getClass().getAnnotation(RDFClass.class).value();
//		String prefix;
//		try { 
//			prefix = this.getClass().getAnnotation(RDFInstancePrefix.class).value();
//		} catch (NullPointerException e) {
//			throw(e);
//		}
//		GResource topBlank = g.findTopBlank(rdfType);
//		T theThing;
//		if (null != topBlank) {
//			String newURI = prefix + UUID.randomUUID().toString();
//			topBlank.rename(newURI);
////			g.skolemnize(newURI);
////			for (Field field : this.getClass().getDeclaredFields()) {
////				if (field.isAnnotationPresent(RDFProperty.class)) {
////					Object prop = PropertyUtils.getProperty(this, field.getName());
////					try {
////						Object id = PropertyUtils.getProperty(object, field.getName());
////						if (null == id || "0".equals(id.toString()) ) return new GResourceImpl(this, model.createResource(AnonId.create(object.toString())));
////						uri = field.getAnnotation(RDFId.class).prefix() + id.toString();
////					} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
////						throw new RuntimeException("An exception occurred: " + e, e);
////					}
////				}
////			}
//			theThing = g.getObjectMapper().getObject(this.getClass(), newURI);
//		}
//		else {
//			throw new RuntimeException("No top blank node.");
//		}
//		return theThing;
//	}
	
	public void loadFromURI(String id) {
		this.loadFromURI(id, 0);
	}
	
	public void loadFromURI(URI id) {
		this.loadFromURI(id.toString(), 0);
	}
	
	public void loadFromURI(URI id, int expansionSteps) {
		this.loadFromURI(id.toString(), expansionSteps);
	}
	
	public void loadFromURI(String id, int expansionSteps) {
		this.setId(id);
        Grafeo g = new GrafeoImpl();
        try {
			g.load(this.getId(), expansionSteps);
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
		T theNewPojo = g.getObjectMapper().getObject(this.getClass(), this.getId());
        try {
            BeanUtils.copyProperties(this, theNewPojo);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
    }


	public void publish(String endPoint, String graph) {
        log.info("Writing to endpoint: " + endPoint + " / Graph: " + graph);
		Grafeo g = new GrafeoImpl();
		g.getObjectMapper().addObject(this);
		g.emptyGraph(endPoint, graph);
		g.writeToEndpoint(endPoint, graph);
	}
	public void publish(String endPoint) {
		if (null == this.getId()) {
			String prefix;
			try {
				prefix = this.getClass().getAnnotation(RDFInstancePrefix.class).value();
			} catch (NullPointerException e) {
				prefix = "http://data.dm2e.eu/THIS_CLASS_SHOULD_HAVE_A_RDFINSTANCEPREFIX/";
			}
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
		g.getObjectMapper().addObject(this);
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
