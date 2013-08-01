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

/**
 * Defines various MediaType and related functions specific to RDF and JSON in DM2E.
 *
 * @see javax.ws.rs.core.MediaType javax.ws.rs.core.MediaType
 *
 * @author Konstantin Baierer
 */
public class DM2E_MediaType {

    private static Logger log = LoggerFactory.getLogger(DM2E_MediaType.class.getName());

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
	@Consumes|Produces({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE
		// , MediaType.TEXT_HTML
		// , DM2E_MediaType.TEXT_PLAIN,
		// , MediaType.APPLICATION_JSON
	})
	 */


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
	public static final MediaType
			// NTRIPLES
			APPLICATION_RDF_TRIPLES_TYPE = MediaType.valueOf(APPLICATION_RDF_TRIPLES),
			// RDFXML
			APPLICATION_RDF_XML_TYPE = MediaType.valueOf(APPLICATION_RDF_XML),
			// TURTLE
			APPLICATION_X_TURTLE_TYPE = MediaType.valueOf(APPLICATION_X_TURTLE),
			// NTRIPLES
			TEXT_PLAIN_TYPE = MediaType.valueOf(TEXT_PLAIN),
			// N3
			TEXT_RDF_N3_TYPE = MediaType.valueOf(TEXT_RDF_N3),
			// TURTLE
			TEXT_TURTLE_TYPE = MediaType.valueOf(TEXT_TURTLE);

	/** Log file MediaType (String) */
	public static final String TEXT_X_LOG = "text/x-log";
	/** Log file MediaType (MediaType) */
	public static final MediaType TEXT_LOG_TYPE = new MediaType("text", "x-log");
	
	/** application/x-tar; charset="utf8" (MediaType) */
	public static final MediaType APPLICATION_X_TAR_UTF8_TYPE;
	/** text/html; charset="utf8" (MediaType) */
	public static final MediaType TEXT_HTML_UTF8;
	/** application/xml; charset="utf8" (MediaType) */
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
	/** Set of all RDF types */
	public final static Set<MediaType> SET_OF_RDF_TYPES;
	/** Set of the names of all RDF types */
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

    /**
     * Resolve a MediaType to a Jena language name.
     * 
     * <pre>{@code
	*DM2E_MediaType.APPLICATION_RDF_TRIPLES	"N-TRIPLE"
	*DM2E_MediaType.APPLICATION_RDF_XML	"RDF/XML"
	*DM2E_MediaType.APPLICATION_X_TURTLE	"TURTLE"
	*DM2E_MediaType.TEXT_PLAIN		"N-TRIPLE"
	*DM2E_MediaType.TEXT_RDF_N3		"N3"
	*DM2E_MediaType.TEXT_TURTLE		"TURTLE"
     * } </pre>
     *
     * @param thisType  MediaType to find jena lang for
     * @return Jena language if thisType is known, null otherwise.
     */
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
	
	/**
	 * Decides whether a MediaType should result in metadata or data being sent, on the assumption that JSON and RDF mediatypes are for metadata.
	 *
	 * @param mediaType  MediaType to check
	 * @return true if it's either a JSON or a RDF mediatype, false otherwise
	 */
	public static boolean expectsMetadataResponse(MediaType mediaType) {
		if (null == mediaType) return false;
		return (
				isJsonMediaType(mediaType)
				||
				isRdfMediaType(mediaType)
				);
	}
	
	/**
	 * @see #expectsMetadataResponse(MediaType)
	 */
	public static boolean expectsMetadataResponse(HttpHeaders headers) {
		if (null == headers) return false;
		return (
				expectsJsonResponse(headers)
				||
				expectsRdfResponse(headers)
				);
	}
	
	/**
	 * Whether a MediaType is an RDF type
	 *
	 * @param thisType  MediaType to check
	 * @return true if MediaType is contained in {@link #SET_OF_RDF_TYPES}, false otherwise
	 */
	public static boolean isRdfMediaType(MediaType thisType) {
		if (null == thisType) return false;
		for (MediaType rdfType : SET_OF_RDF_TYPES) {
//			log.debug("Trying to match with " + rdfType);
			if (matchMediaTypeAndSubtype(thisType, rdfType)) {
//				log.debug("Yup, " + thisType + " matches " + rdfType);
				return true;
			}
		}
		return false;
//		return SET_OF_RDF_TYPES_STRING.contains(mediaType.toString());
	}

	/**
	 * Matches two mediatypes based on the main type and subtype and nothing else.
	 *
	 * <p>
	 * Useful for comparing mediatypes with differing parameters
	 * </p>
	 * @param thisType  MediaType to match
	 * @param otherType  MediaType to match
	 * @return true if MediaTypes have the same type and subtype, false otherwise
	 */
	public static boolean matchMediaTypeAndSubtype(MediaType thisType, MediaType otherType) {
//		log.trace("Matching " +thisType+ " against " + otherType);
//		log.trace("Matching " +thisType.getType()+ " against " + otherType.getType());
//		log.trace("Matching " +thisType.getSubtype()+ " against " + otherType.getSubtype());
		return 	thisType.getType().equals(otherType.getType())
				&&
				thisType.getSubtype().equals(otherType.getSubtype());
	}
	
	/**
	 * Decides whether a the acceptable media types of a HttpHeaders object should result in a RDF response.
	 *
	 * @see DM2E_MediaType#isRdfMediaType(MediaType)
	 */
	public static boolean expectsRdfResponse(HttpHeaders headers) {
		boolean doesExpectRdf = false;
		for (MediaType thisType : headers.getAcceptableMediaTypes()) {
			if (isRdfMediaType(thisType)) {
	            log.info("Accept header: " + thisType.toString() + " is RDF (" + thisType + ")" );
				doesExpectRdf = true;
				break;
			}
		}
		return doesExpectRdf;
	}

	/**
	 * Decides whether a mediatype is a JSON mediatype
	 *
	 * @param thisType  MediaType to check
	 * @return true if thisType is APPLICATION_JSON_TYPE, false otherwise
	 */
	public static boolean isJsonMediaType(MediaType thisType) {
		if (null == thisType) return false;
		return  matchMediaTypeAndSubtype(thisType, MediaType.APPLICATION_JSON_TYPE);
//				(thisType.getType().equals(MediaType.APPLICATION_JSON_TYPE.getType())
//				&&
//				thisType.getSubtype().equals(MediaType.APPLICATION_JSON_TYPE.getSubtype()));
	}
	/**
	 * Decides whether a the acceptable media types of a HttpHeaders object should result in a JSON response.
	 *
	 * @see DM2E_MediaType#isJsonMediaType(MediaType)
	 */
	public static boolean expectsJsonResponse(HttpHeaders headers) {
		if (null == headers) return false;
		for (MediaType thisType : headers.getAcceptableMediaTypes())
			if (isJsonMediaType(thisType)) {
	            log.info("Accept header: " + thisType.toString() + " is JSON.");
				return true;
			}
		return false;
	}

}
