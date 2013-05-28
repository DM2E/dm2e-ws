package eu.dm2e.ws.grafeo;

import com.hp.hpl.jena.query.ResultSet;
import eu.dm2e.ws.grafeo.gom.ObjectMapper;
import eu.dm2e.ws.grafeo.jena.GResourceImpl;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;

/**
 * The Grafeo API is a simple RDF API. The goal is the minimization of
 * code in the applications, by introducing conventions and convenience
 * methods. Grafeo is specifically created for the requirements in the
 * DM2E project, the suitability as general purpose RDF API is secondary.
 *
 * Author: Kai Eckert, Konstantin Baierer
 *
 */
public interface Grafeo {
	GResource findTopBlank();

    void setNamespace(String prefix, String namespace);

	void load(String uri);

	void empty();

	boolean isEmpty();

	GResource get(String uri);

	String expand(String uri);

	GStatement addTriple(String subject, String predicate, String object);

	GStatement addTriple(String subject, String predicate, GValue object);

    GLiteral literal(String literal);

	GLiteral literal(Object value);

	GResource resource(String uri);
	
	GResource resource(URI uri);

	boolean isEscaped(String input);

	String unescapeLiteral(String literal);

	String escapeLiteral(String literal);

	String unescapeResource(String uri);

	String escapeResource(String uri);

	void readFromEndpoint(String endpoint, String graph);

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

	void readHeuristically(InputStream input);

	void readHeuristically(File file);

    GStatement addTriple(GResource subject, GResource predicate,
                             GValue object);

	GValue firstMatchingObject(String string2, String string);

	GResourceImpl findTopBlank(String uri);

	void emptyGraph(String endpoint, String graph);


    Set<GResource> findByClass(String uri);

    GResourceImpl createBlank();

    ObjectMapper getObjectMapper();
}
