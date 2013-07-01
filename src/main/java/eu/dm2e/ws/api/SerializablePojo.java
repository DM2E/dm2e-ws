package eu.dm2e.ws.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.logging.Logger;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

/**
 * @author kb
 *
 */
public abstract class SerializablePojo<T> {

	protected transient Logger log = Logger.getLogger(getClass().getName());
	
	public SerializablePojo() {
		super();
	}
	
	/**
	 * Get the RDFClass annotation of the class of this instance.
	 * 
	 * @return RDFClass annotation
	 * @see RDFClass
	 */
	public RDFClass getRDFClass() {
        if (this.getClass().isAnnotationPresent(RDFClass.class))
        	return this.getClass().getAnnotation(RDFClass.class);
        return null;
	}
	public String getRDFClassUri() {
        if (this.getClass().isAnnotationPresent(RDFClass.class)) {
        	String uri = this.getClass().getAnnotation(RDFClass.class).value();
        	uri = this.getGrafeo().expand(uri);
        	return uri;
        }
        return null;
	}

	/**
	 * Get a new grafeo with all the statements of this class added as RDF.
	 * 
	 * @return Grafeo
	 */
	public Grafeo getGrafeo() {
		GrafeoImpl g = new GrafeoImpl();
		g.getObjectMapper().addObject(this);
		return g;
	}
	public String getTerseTurtle() {
		return getGrafeo().getTerseTurtle();
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
     * The format is Classname<URI or 'BLANK'>(label)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
		return this.getClass().getSimpleName()
				+ "<"
				+ (this.getId() == null ? "BLANK" : this.getId())
				+ ">"
				+ (this.hasLabel() ? "(\"" + this.getLabel() + "\")" : "");
    }
    
    public T copy() {
    	String origId = this.getId();
    	String tempID = "http://temporary/" + UUID.randomUUID(); 
    	this.setId(tempID);
    	
    	SerializablePojo<T> that = this.getGrafeo().getObjectMapper().getObject(this.getClass(), tempID);
    	
    	this.setId(origId);
    	that.setId(origId);
    	
		return (T) that;
    }
    
    /*********************************************
     * GETTERS SETTERS PREDICATES
     *********************************************/
	
    /**
     * Every Pojo needs and ID - though it can be null for a blank node
     */
    @RDFId String id;
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public void setId(URI wsUri) { this.id = wsUri.toString(); }
	public void resetId() { this.id = null; }
	public boolean hasId() { return this.id != null; }
	public URI getIdAsURI() {
		URI uri = null;
		try {
			uri = new URI(getId());
		} catch (NullPointerException | URISyntaxException e) {
			throw new RuntimeException("Id '" + getId() + "'cannot be casted to URI: " + e);
		}
		return uri;
	}
	
    /**
     * Every Pojo can have a label, bridge between human-readable and machine-readable
     */
    @RDFProperty(NS.RDFS.PROP_LABEL)
    private String label;
    public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }
	public boolean hasLabel() { return this.label != null; }

}