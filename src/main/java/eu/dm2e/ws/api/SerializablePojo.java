package eu.dm2e.ws.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.logging.Logger;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.json.OmnomJsonSerializer;
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
	public static transient final String JSON_FIELD_ID = "id";
	public static transient final String JSON_FIELD_RDF_TYPE = "rdf:type";
	
	public abstract boolean equals(Object other);
	public abstract int hashCode();
	
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
        	uri = new GrafeoImpl().expand(uri);
        	return uri;
        }
        return null;
	}
	/***************************************
	 * 
	 * JSON Serialization
	 * 
	 **************************************/
	public String toJson() {
		return OmnomJsonSerializer.serializeToJSON(this, getClass());
	}
	
	
	/***************************************
	 * 
	 * Grafeo / RDF Serialization
	 * 
	 **************************************/

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
	public String getTerseTurtle() { return getGrafeo().getTerseTurtle(); }
	public String getTurtle() { return getGrafeo().getTurtle(); }
	public String getNTriples() { return getGrafeo().getNTriples(); }
	public String getCanonicalNTriples() { return getGrafeo().getCanonicalNTriples(); }
	
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
     * 
     * GETTERS SETTERS PREDICATES
     * 
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
     * 
     * Every Pojo can have a label, bridge between human-readable and machine-readable
     * 
     */
    @RDFProperty(NS.RDFS.PROP_LABEL)
    private String label;
    public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }
	public boolean hasLabel() { return this.label != null; }
	
	/***************************
	 * 
	 * equals & hashCode
	 * 
	 *************************/

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int baseHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean baseEquals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof SerializablePojo)) return false;
		SerializablePojo other = (SerializablePojo) obj;
		if (id == null) {
			if (other.id != null) return false;
		} else if (!id.equals(other.id)) return false;
		if (label == null) {
			if (other.label != null) return false;
		} else if (!label.equals(other.label)) return false;
		return true;
	}

}