package eu.dm2e.ws.api;

import java.util.logging.Logger;

import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

/**
 * @author kb
 *
 */
public abstract class SerializablePojo {

	protected Logger log = Logger.getLogger(getClass().getName());

	public SerializablePojo() {
		super();
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
	
    /**
     * Every Pojo needs and ID - though it can be null for a blank node
     */
    @RDFId String id;
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }


}