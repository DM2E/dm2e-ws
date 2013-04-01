package eu.dm2e.ws.grafeo;

import com.hp.hpl.jena.query.ResultSet;
import eu.dm2e.ws.grafeo.jena.GStatementImpl;

import java.io.File;
import java.io.InputStream;
import java.net.URI;

/**
 * Created with IntelliJ IDEA. User: kai Date: 3/5/13 Time: 11:21 AM To change
 * this template use File | Settings | File Templates.
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

    void addObject(Object object);

	GLiteral literal(String literal);

	GLiteral literal(Object value);

	GResource resource(String uri);

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

	long size();

	GLiteral now();

	GLiteral date(Long timestamp);

	boolean executeSparqlAsk(String queryString);

	boolean containsStatementPattern(String s, String p, String o);

	boolean containsStatementPattern(String s, String p, GLiteral o);

	boolean containsResource(String graph);

	boolean containsResource(URI graphURI);

	ResultSet executeSparqlSelect(String queryString);

	Grafeo executeSparqlConstruct(String queryString);

	void readHeuristically(String content);

	void readHeuristically(InputStream input);

	void readHeuristically(File file);

    <T> T getObject(Class T, GResource res);

    GStatementImpl addTriple(GResource subject, GResource predicate,
                             GValue object);
}
