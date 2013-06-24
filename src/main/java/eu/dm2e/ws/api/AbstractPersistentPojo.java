package eu.dm2e.ws.api;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import com.sun.jersey.api.client.WebResource;

import eu.dm2e.utils.PojoUtils;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.services.Client;

public abstract class AbstractPersistentPojo<T> extends SerializablePojo {
	
	protected Client client = new Client();
	
	
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
        	log.finer("Loading from " + this.getId());
			g.load(this.getId(), expansionSteps);
        	log.finer("DONE Loading from " + this.getId());
            log.fine("Triples loaded from URI " + this.getId() + ": " + g.getTurtle());
		} catch (Exception e1) {
			log.warning("Failed to initialize Pojo from URI: " + e1);
			return;
		}
        log.finer("Instantiating " + this.getClass() + " Pojo from " + this.getId());
		T theNewPojo = g.getObjectMapper().getObject(this.getClass(), this.getId());
        log.finer("DONE Instantiating " + this.getClass() + " Pojo from " + this.getId());
        try {
        	log.fine("Copying properties from Pojo " + this.getId());
            PojoUtils.copyProperties(this, theNewPojo);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
    }
	
	public String publishToService(WebResource wr) {
		log.fine("Publishing myself (pojo) to " + wr.getURI());
		String loc = this.client.publishPojo(this, wr);
		log.fine("Done Publishing myself (pojo) to " + wr.getURI());
		return loc;
	}
	public String publishToService(String serviceUri) {
		return this.publishToService(client.resource(serviceUri));
	}
	public String publishToService() {
		if (this.getId() == null) {
			throw new RuntimeException("Must provide service URI to POST to.");
		}
		return this.publishToService(this.getId());
	}
}
