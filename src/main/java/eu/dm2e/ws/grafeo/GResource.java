package eu.dm2e.ws.grafeo;

import java.net.URI;

/**
 * A RDF resource, either blank or URI.
 */
public interface GResource extends GValue {
    void rename(String uri);
    void rename(GResource res);
	void rename(URI newUri);
    String getUri();

    void set(String uri, GValue value);
	void set(String property, String string);

    boolean isAnon();

    String getAnonId();
	/**
	 * Check whether a resource has a certain rdf:type.
	 * 
	 * @param uri
	 */
	boolean isa(String uri);
}
