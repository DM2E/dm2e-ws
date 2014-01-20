package eu.dm2e.ws.api;

import eu.dm2e.grafeo.Grafeo;
import eu.dm2e.grafeo.gom.SerializablePojo;
import eu.dm2e.grafeo.jena.GrafeoImpl;
import eu.dm2e.grafeo.util.PojoUtils;
import eu.dm2e.ws.services.Client;

import javax.ws.rs.client.WebTarget;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

/**
 * Abstract Base Class for Grafeo-annotated Pojos that can be persisted in or loaded from a service.
 */
public abstract class AbstractPersistentPojo<T extends AbstractPersistentPojo> extends SerializablePojo<T> {
	
	protected static transient Client client = new Client();
    private transient boolean loaded = false;
	
	public void loadFromURI(String uri) {
		this.loadFromURI(uri, 0);
	}
	
	public void loadFromURI(URI uri) {
		this.loadFromURI(uri.toString(), 0);
	}
	
	public void loadFromURI(URI uri, int expansionSteps) {
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
            // log.debug("Triples: " + g.getTerseTurtle());
		} catch (Exception e1) {
			log.warn("Failed to initialize Pojo from URI: ", e1);
			return;
		}
        log.debug("Instantiating " + this.getClass() + " Pojo from " + uri);
		T theNewPojo = (T) g.getObjectMapper().getObject(this.getClass(), uri);
        log.debug("DONE Instantiating " + this.getClass() + " Pojo from " + uri);
        try {
        	log.debug("Copying properties from Pojo " + uri);
            PojoUtils.copyProperties(this, theNewPojo);
            // log.debug("theNewPojo: " + theNewPojo.getTerseTurtle());
            // log.debug("this: " + this.getTerseTurtle());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
        log.debug("DONE Copying properties from Pojo " + uri);
        loaded=true;
    }

    public T refresh(int expansionSteps, boolean force) {
        if (getId()==null) throw new RuntimeException("Can't refresh a Pojo without ID.");
        if (!force && loaded) return (T) this;
        loadFromURI(getId(), expansionSteps);
        return (T) this;
    }

	
	public String publishToService(WebTarget wr) {
		log.debug("Publishing myself (pojo) to " + wr.getUri());
		String loc = client.publishPojo(this, wr, true);
		log.debug("Done Publishing myself (pojo) to " + wr.getUri());
		return loc;
	}
	public String publishToService(String serviceUri) {
		return this.publishToService(client.target(serviceUri));
	}
	public String publishToService() {
		if (this.getId() == null) {
			throw new RuntimeException("Must provide service URI to POST to.");
		}
		return this.publishToService(this.getId());
	}
}
