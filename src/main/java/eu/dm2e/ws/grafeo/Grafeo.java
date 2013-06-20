package eu.dm2e.ws.grafeo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.query.ResultSet;

import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;
import eu.dm2e.ws.grafeo.gom.ObjectMapper;
import eu.dm2e.ws.grafeo.jena.GResourceImpl;

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
 * @author Konstantin Baierer
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
	 * @param stmt A statement
	 */
	GStatement addTriple(GStatement stmt);
	
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

	/**
	 * Returns an NTRIPLES serialization.
	 * 
	 * @return NTRIPLES
	 */
	String getNTriples();

	/**
	 * Returns an NTRIPLES serialization, sorted to make comparison easier
	 * 
	 * @return Sorted NTRIPLES
	 */
	String getCanonicalNTriples();

	/**
	 * Returns a valid Turtle serialziation.
	 * 
	 * @return TURTLE
	 */
	String getTurtle();

	/**
	 * Returns an invalid Turtle serialization without the prefixes (shorter for debugging)
	 * 
	 * @return TURTLE without prefixes
	 */
	String getTerseTurtle();

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

	/**
	 * List all subjects of rdf:type type in the graph.
	 * 
	 * @param type
	 * @return Set of all subjects with type type
	 */
    Set<GResource> findByClass(String type);

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
	 * Replace blank nodes that are objects of a triple with URIs.
	 * 
	 * @param newURI the string to base the naming on.
	 */
	void skolemize(String subject, String predicate, String template, SkolemizationMethod method);
	
	/**
	 * Replace blank nodes that are objects of a triple with URIs.
	 * 
	 * @param newURI the string to base the naming on.
	 */
	void skolemizeUUID(String subject, String predicate, String template);
	
	/**
	 * Replace blank nodes that are objects of a triple with URIs.
	 * 
	 * @param newURI the string to base the naming on.
	 */
	void skolemizeSequential(String subject, String predicate, String template);

	/**
	 * List all resources in the Grafeo.
	 * 
	 * @return A Set of GResources
	 */
	Set<GResource> listResources();

	/**
	 * List all blank nodes that appear as objects in triples in the graph.
	 * 
	 * @return A Set of GResource representing blank nodes
	 */
	Set<GResource> listAnonResources();

	/**
	 * Lists all resources that appear as objects of triples in the graph.
	 * 
	 * @return a Set of GResources
	 */
	Set<GResource> listURIResources();

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

	String visualizeWithGraphviz(String outname) throws Exception;

	/**
	 * @return Map of namespace prefix mappings
	 */
	Map<String, String> getNamespaces();

	/**
	 * Turns a pattern of s/p/o with o a resource into a SPARQL-compatible pattern.
	 * 
	 * @param subject
	 * @param predicate
	 * @param object
	 * @return
	 */
	String stringifyResourcePattern(String subject, String predicate, String object);

	/**
	 * Turns a pattern of s/p/o with o a literal into a SPARQL-compatible pattern.
	 * 
	 * @param subject
	 * @param predicate
	 * @param object
	 * @return
	 */
	String stringifyLiteralPattern(String subject, String predicate, String object);

	String stringifyLiteralPattern(String subject, String predicate, GLiteral object);
	
	Set<GStatement> listStatements(GResource subject, String predicate, GValue object);

	void removeTriple(GStatement stmt);

	/**
	 * Replace every resource (blank and URI) with a blank node, but keep the structure.
	 * 
	 */
	void unskolemize();
//
//	/**
//	 * Replace every resource (blank and URI) with a blank node, destroying the structure.
//	 * 
//	 */
//	void unskolemizeToSingleResource();

	/**
	 * Create N-TRIPLEs representation sorted by predicate
	 * @return
	 */
	String getPredicateSortedNTriples();

//	/**
//	 * Replace every resource/blank with a dummy resource, serialize to N-TRIPLES, sort by predicate and rename all resources to blank.
//	 * @return
//	 */
//	String getUnskolemnizedToSingleResourcePredicateSortedNTriples();

	List<String> diffUnskolemizedNTriples(Grafeo that);

	/**
	 * Replaces all resources/blanks with blanks and checks whether Grafeos are isomorphic.
	 * 
	 * @param g The Grafeo to compare to
	 * @return true if they are isomorphic structurally, false otherwise
	 */
	boolean isStructuralGraphEquivalent(Grafeo g);

	/**
	 * Deeply copies one grafeo instance to another.
	 * 
	 * @return New instance of Grafeo with the same statements
	 */
	Grafeo copy();


}

