package eu.dm2e.ws.api;

import com.sun.jersey.api.client.WebResource;
import eu.dm2e.utils.PojoUtils;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.services.Client;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

public abstract class AbstractPersistentPojo<T> extends SerializablePojo<T> {
	
	protected static transient Client client = new Client();
	
	public void loadFromURI(String uri) {
		this.loadFromURI(uri, 0);
	}
	
	public void loadFromURI(URI uri) {
		this.loadFromURI(uri.toString(), 0);
	}
	
	public void loadFromURI(URI uri, int expansionSteps) throws Exception {
		this.loadFromURI(uri.toString(), expansionSteps);
	}
	
	public void loadFromURI(String uri, int expansionSteps) {
        Grafeo g = new GrafeoImpl();
        try {
        	log.debug("Loading from " + uri);
			long timeStart = System.currentTimeMillis();
			g.load(uri, expansionSteps);
			long timeElapsed = System.currentTimeMillis() - timeStart;
			log.info("Time spent: " + timeElapsed + "ms.");
			log.debug("DONE Loading from " + uri);
            log.debug("No of Triples loaded from URI " + uri + ": " + g.size());
		} catch (Exception e1) {
			log.warn("Failed to initialize Pojo from URI: " + e1);
			return;
		}
        log.debug("Instantiating " + this.getClass() + " Pojo from " + uri);
		T theNewPojo = (T) g.getObjectMapper().getObject(this.getClass(), uri);
        log.debug("DONE Instantiating " + this.getClass() + " Pojo from " + uri);
        try {
        	log.debug("Copying properties from Pojo " + uri);
            PojoUtils.copyProperties(this, theNewPojo);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
    }
	
	public String publishToService(WebResource wr) {
		log.debug("Publishing myself (pojo) to " + wr.getURI());
		String loc = client.publishPojo(this, wr);
		log.debug("Done Publishing myself (pojo) to " + wr.getURI());
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
