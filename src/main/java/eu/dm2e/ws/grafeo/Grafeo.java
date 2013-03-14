package eu.dm2e.ws.grafeo;

import com.hp.hpl.jena.query.ResultSet;

import eu.dm2e.ws.grafeo.jena.GLiteralImpl;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;


/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/5/13
 * Time: 11:21 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Grafeo {
    GResource findTopBlank();

    void load(String uri);

    GResource get(String uri);

    String expand(String uri);

    GStatement addTriple(String subject, String predicate, String object);

    GStatement addTriple(String subject, String predicate, GLiteral object);

    GLiteral literal(String literal);

	GLiteral literal(long literal);

	GLiteral literal(boolean truefalse);

    GResource resource(String uri);

    boolean isEscaped(String input);

    String unescapeLiteral(String literal);

    String escapeLiteral(String literal);

    String unescapeResource(String uri);

    String escapeResource(String uri);

    void readFromEndpoint(String endpoint, String graph);

    void readTriplesFromEndpoint(String endpoint, String subject, String predicate, GValue object);

    void writeToEndpoint(String endpoint, String graph);

    String getNTriples();
    
    long size();

    GLiteral now();

    GLiteral date(Long timestamp);
    
	boolean executeSparqlAsk(String queryString);

	boolean containsStatementPattern(String s, String p, String o);
	
	boolean containsStatementPattern(String s, String p, GLiteral o);
	
	boolean containsResource(String g);
	
	ResultSet executeSparqlSelect(String queryString);

	Grafeo executeSparqlConstruct(String queryString);
}
