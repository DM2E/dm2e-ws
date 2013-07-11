package eu.dm2e.ws;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	/*
     *
	 * This is for copy/pasting (since Annotations don't support arrays, sigh
	@Consumes({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		// DM2E_MediaType.TEXT_PLAIN,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE
	})
	 */
    private static Logger log = LoggerFactory.getLogger(DM2E_MediaType.class.getName());


	public static final String
			// NTRIPLES
			APPLICATION_RDF_TRIPLES = "application/rdf-triples",
			// RDFXML
			APPLICATION_RDF_XML = "application/rdf+xml",
			// TURTLE
			APPLICATION_X_TURTLE = "application/x-turtle",
			// NTRIPLES
			TEXT_PLAIN = "text/plain",
			// N3
			TEXT_RDF_N3 = "text/rdf+n3",
			// TURTLE
			TEXT_TURTLE = "text/turtle";

	// Log file mediatype
	public static final String TEXT_X_LOG = "text/x-log";
	public static final MediaType TEXT_LOG_TYPE = new MediaType("text", "x-log");
	
	public static final MediaType APPLICATION_X_TAR_UTF8_TYPE;
	public static final MediaType TEXT_HTML_UTF8;
	public static final MediaType APPLICATION_XML_UTF8;
    static HashMap<MediaType,String> mediaType2JenaLanguage = new HashMap<>();
	private static final String[] rdfMediaTypes = {
		APPLICATION_RDF_TRIPLES,
		APPLICATION_RDF_XML,
		APPLICATION_X_TURTLE,
		TEXT_TURTLE,
		TEXT_RDF_N3,
		TEXT_PLAIN,
	};
	public final static Set<MediaType> SET_OF_RDF_TYPES;
	public final static Set<String> SET_OF_RDF_TYPES_STRING = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList(rdfMediaTypes)));
	
	// Static initializer
    static {
		HashMap<String, String> utf8mediaTypeProps = new HashMap<>();
		utf8mediaTypeProps.put("charset", "UTF-8");
		APPLICATION_X_TAR_UTF8_TYPE = new MediaType("application", "x-tar", utf8mediaTypeProps);
		TEXT_HTML_UTF8 = new MediaType("text", "html", utf8mediaTypeProps);
		APPLICATION_XML_UTF8 = new MediaType("application", "xml", utf8mediaTypeProps);

		SET_OF_RDF_TYPES = new HashSet<>();
		for (String thisTypeStr : SET_OF_RDF_TYPES_STRING) {
			SET_OF_RDF_TYPES.add(MediaType.valueOf(thisTypeStr));
		}

		mediaType2JenaLanguage.put(MediaType.valueOf(DM2E_MediaType.APPLICATION_RDF_TRIPLES), "N-TRIPLE");
		mediaType2JenaLanguage.put(MediaType.valueOf(DM2E_MediaType.APPLICATION_RDF_XML), "RDF/XML");
		mediaType2JenaLanguage.put(MediaType.valueOf(DM2E_MediaType.APPLICATION_X_TURTLE), "TURTLE");
		mediaType2JenaLanguage.put(MediaType.valueOf(DM2E_MediaType.TEXT_PLAIN), "N-TRIPLE");
		mediaType2JenaLanguage.put(MediaType.valueOf(DM2E_MediaType.TEXT_RDF_N3), "N3");
		mediaType2JenaLanguage.put(MediaType.valueOf(DM2E_MediaType.TEXT_TURTLE), "TURTLE");
    } 
    
    public static String getJenaLanguageForMediaType(MediaType thisType) {
    	return mediaType2JenaLanguage.get(thisType);
    }
	
	public static boolean noRdfRequest(HttpHeaders headers) {
		boolean isRDF = false;
        if (null != headers.getMediaType()
				&& SET_OF_RDF_TYPES_STRING.contains(headers.getMediaType().toString())) {
			isRDF = true;
		}
		return !isRDF;
	}
	


	public static boolean expectsMetadataResponse(MediaType mediaType) {
		return (
				expectsJsonResponse(mediaType)
				||
				expectsRdfResponse(mediaType)
				);
	}
	
	public static boolean expectsMetadataResponse(HttpHeaders headers) {
		return (
				expectsJsonResponse(headers)
				||
				expectsRdfResponse(headers)
				);
	}
	
	public static boolean expectsRdfResponse(MediaType mediaType) {
		return SET_OF_RDF_TYPES_STRING.contains(mediaType.toString());
	}
	
	public static boolean expectsRdfResponse(HttpHeaders headers) {
		boolean doesExpectRdf = false;
		for (MediaType thisType : headers.getAcceptableMediaTypes()) {
            log.info("Accept header: " + thisType.toString());
			if (expectsRdfResponse(thisType)) {
				doesExpectRdf = true;
				break;
			}
		}
		return doesExpectRdf;
	}

	public static boolean expectsJsonResponse(MediaType thisType) {
		return thisType.equals(MediaType.APPLICATION_JSON_TYPE);
	}
	public static boolean expectsJsonResponse(HttpHeaders headers) {
		for (MediaType thisType : headers.getAcceptableMediaTypes())
			if (expectsJsonResponse(thisType))
				return true;
		return false;
	}

}
