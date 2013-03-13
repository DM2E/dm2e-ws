package eu.dm2e.ws;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;

public class DM2E_MediaType {
	/**
	 * This is a list of RDF mediatypes to be used in @Consumes clauses. Note
	 * that "text/plain" which is the media type for N-Triples is left out,
	 * because it makes it cumbersome to send messages to web services not in
	 * RDF but plain text. If N-Triples are to be passed to a web service,
	 * either "text/turtle", "text/rdf+n3" or "application/rdf+xml" will work.
	 * 
	 * @see <http://www.w3.org/2011/rdf-wg/wiki/N-Triples-Format#Media_type>
	 */
	public static final String
			// TURTLE
			TEXT_TURTLE = "text/turtle",
			// N3
			TEXT_RDF_N3 = "text/rdf+n3",
			// NTRIPLES
			APPLICATION_RDF_TRIPLES = "application/rdf-triples",
			// RDFXML
			APPLICATION_RDF_XML = "application/rdf+xml";
	private static final String[] rdfMediaTypes = {
		TEXT_TURTLE,
		TEXT_RDF_N3,
		APPLICATION_RDF_TRIPLES,
		APPLICATION_RDF_XML
	};
	public final static Set<String> SET_OF_RDF_TYPES = Collections
			.unmodifiableSet(new HashSet<String>(Arrays.asList(rdfMediaTypes)));
	
	public static boolean isRdfRequest(HttpHeaders headers) {
		for (MediaType thisType : headers.getAcceptableMediaTypes()) {
			if (SET_OF_RDF_TYPES.contains(thisType.toString())) {
				return true;
			}
		}
		return false;
	}

}
