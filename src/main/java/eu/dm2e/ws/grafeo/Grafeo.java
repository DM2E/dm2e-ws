package eu.dm2e.ws.grafeo;

import com.hp.hpl.jena.query.ResultSet;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;
import eu.dm2e.ws.grafeo.gom.ObjectMapper;
import eu.dm2e.ws.grafeo.jena.GResourceImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;

/**
 * The Grafeo API is a simple RDF API. The goal is the minimization of
 * code in the applications, by introducing conventions and convenience
 * methods. Grafeo is specifically created for the requirements in the
 * DM2E project, the suitability as general purpose RDF API is secondary.
 *
 * @author Kai Eckert
 * @author Konstantin Baierer
 *
 */
/**
 * @author kb
 *
 */
public interface Grafeo {
	GResource findTopBlank();

    void setNamespace(String prefix, String namespace);

	/**
	 * Loads RDF data from a URI into the graph, without resource expansion.
	 * 
	 * @param uri The URL of the resource to load
	 */
	void load(String uri);
	
	/**
	 * Loads RDF data from a URI into the graph, possibly expanding nested resources.
	 * 
	 * @param uri The URL of the resource to load
	 * @param expansionSteps Number of recursions to make to expand the graph
	 */
	void load(String uri, int expansionSteps);
	
    /**
     * Loads data from a URL by trying to parse the returned string with common RDF parsers.
     * 
     * First N3 then RDFXML.
     * 
     * @param uri The URL of the resource to load
     */
	void loadWithoutContentNegotiation(String uri);

	/**
     * Loads data from a URL by trying to parse the returned string with common RDF parsers,
     * recursively doing the same to contained resources.
     * 
     * @param uri The URL of the resource to load
	 * @param expansionSteps Number of recursions to make to expand the graph
	 */
	void loadWithoutContentNegotiation(String uri, int expansionSteps);


	/**
	 * Empty the graph.
	 * 
	 */
	void empty();

	/**
	 * Test if the graph is empty.
	 * 
	 * @return true if the graph is empty, false otherwise
	 */
	boolean isEmpty();

	/**
	 * Get a GResource for a URI
	 * 
	 * @param uri URI of the resource (can also be a shorthand passed through {@link #expand(String)}
	 * @return GResource of this URI within this graph
	 * @see #resource(String)
	 */
	GResource get(String uri);

    GLiteral literal(String literal);

	GLiteral literal(Object value);

	GResource resource(String uri);
	
	GResource resource(URI uri);


	/**
	 * Expand a QName shorthand to a full URI.
	 * 
	 * @param shorthand Shorthand to expand
	 * @return URI of the expanded shorthand or the original URI if no expansion was made
	 */
	String expand(String shorthand);

	/**
	 * Add a triple to the graph. 
	 * 
	 * @param subject URI/Shorthand of the subject
	 * @param predicate URI/Shorthand of the object
	 * @param object URI/Shorthand of the object
	 * @return
	 */
	GStatement addTriple(String subject, String predicate, String object);

	/**
	 * Add a triple to the graph. 
	 * 
	 * @param subject URI/Shorthand of the subject
	 * @param predicate URI/Shorthand of the object
	 * @param object GValue representing the object
	 * @return
	 */
	GStatement addTriple(String subject, String predicate, GValue object);
	
	boolean isEscaped(String input);

	String unescapeLiteral(String literal);

	String escapeLiteral(String literal);

	String unescapeResource(String uri);

	String escapeResource(String uri);

	/**
	 * Loads all triples of a graph from an endpoint using the SPARQL protocol, without expansion.
	 * 
	 * @param endpoint URI of the endpoint
	 * @param graph URI of the graph
	 * @see #load(String)
	 */
	void readFromEndpoint(String endpoint, String graph);
	
	/**
	 * Loads all triples of a graph from an endpoint using the SPARQL protocol, expanding resources.
	 * 
	 * Resources are expanded first by trying to find a matching graph in the endpoint,
	 * then by calling {@link #load(String)}.
	 * 
	 * @param endpoint URI of the endpoint
	 * @param graph URI of the graph
	 * @param numberOfExpansions Number of recursive expansions to make
	 * @see #load(String)
	 */
	void readFromEndpoint(String endpoint, String graph, int numberOfExpansions);

	void readFromEndpoint(String endpoint, URI graphURI);

	void readTriplesFromEndpoint(String endpoint, String subject, String predicate, GValue object);

	void writeToEndpoint(String endpoint, String graph);

	void writeToEndpoint(String endpoint, URI graphURI);

	String getNTriples();

	String getCanonicalNTriples();

	String getTurtle();

	long size();

	GLiteral now();

	GLiteral date(Long timestamp);

	boolean containsStatementPattern(String s, String p, String o);

	boolean containsStatementPattern(String s, String p, GLiteral o);

	boolean containsResource(String graph);

	boolean containsResource(URI graphURI);

	boolean executeSparqlAsk(String queryString);

	ResultSet executeSparqlSelect(String queryString);

	Grafeo executeSparqlConstruct(String queryString);
	
	void executeSparqlUpdate(String queryString, String endpoint);

	void readHeuristically(String content);

	void readHeuristically(InputStream input) throws IOException;

	void readHeuristically(File file);

    GStatement addTriple(GResource subject, GResource predicate,
                             GValue object);

	GValue firstMatchingObject(String string2, String string);

	GResourceImpl findTopBlank(String uri);

	/**
	 * Deletes all statements from a remote graph.
	 * 
	 * @param endpoint
	 * @param graph
	 */
	void emptyGraph(String endpoint, String graph);

    Set<GResource> findByClass(String uri);

    GResourceImpl createBlank();

    GResourceImpl createBlank(String id);

    /**
     * Get an object mapper that can instantiate classes annotated with Grafeo annotations.
     * 
     * @return The Object Mapper for this Grafeo
     * @see RDFProperty
     * @see RDFClass
     */
    ObjectMapper getObjectMapper();

	/**
	 * Lists all resources that appear as objects of triples in the graph.
	 * 
	 * @return a Set of GResources
	 */
	Set<GResource> listResourceObjects();

	/**
	 * Replace blank nodes that are objects of a triple with URIs.
	 * 
	 * @param newURI the string to base the naming on.
	 */
	void skolemnize(String subject, String predicate, String template, SkolemnizationMethod method);
	
	/**
	 * Replace blank nodes that are objects of a triple with URIs.
	 * 
	 * @param newURI the string to base the naming on.
	 */
	void skolemnizeUUID(String subject, String predicate, String template);
	
	/**
	 * Replace blank nodes that are objects of a triple with URIs.
	 * 
	 * @param newURI the string to base the naming on.
	 */
	void skolemnizeSequential(String subject, String predicate, String template);

	/**
	 * List all blank nodes that appear as objects in triples in the graph.
	 * 
	 * @return A Set of GResource representing blank nodes
	 */
	Set<GResource> listBlankObjects();

	Set<GStatement> listResourceStatements(String s, String p, String o);
	
	Set<GStatement> listAnonStatements(String s, String p);

	Set<GStatement> listAnonStatements(String s, String p, GResource o);

	/**
	 * Shorten the URI to a QName
	 * 
	 * @param uri The URI to shorten
	 * @return Short form of the URI
	 */
	String shorten(String uri);
	
    boolean isGraphEquivalent(Grafeo g);
}

