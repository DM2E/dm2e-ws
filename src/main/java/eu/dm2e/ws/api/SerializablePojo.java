package eu.dm2e.ws.api;

import java.util.logging.Logger;

import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

public abstract class SerializablePojo {

	protected Logger log = Logger.getLogger(getClass().getName());

	public abstract String getId();
	public abstract void setId(String id);
	
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

}