package eu.dm2e.ws.services.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.apache.commons.validator.routines.UrlValidator;

import com.hp.hpl.jena.rdf.model.Model;

import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

@Produces({ MediaType.TEXT_PLAIN, "application/rdf+xml",
		"application/x-turtle", "text/turtle", "text/rdf+n3" })
@Consumes({ MediaType.TEXT_PLAIN, "application/rdf+xml",
		"application/x-turtle", "text/turtle", "text/rdf+n3",
		MediaType.MULTIPART_FORM_DATA })
public abstract class AbstractRDFService {

	public static final String PLAIN = MediaType.TEXT_PLAIN;
	public static final String XML = "application/rdf+xml";
	public static final String TTL_A = "application/x-turtle";
	public static final String TTL_T = "text/turtle";
	public static final String N3 = "text/rdf+n3";
	protected static String[] allowedSchemes = { "http", "https", "file", "ftp" };
	protected static final UrlValidator urlValidator = new UrlValidator(allowedSchemes,
		UrlValidator.ALLOW_ALL_SCHEMES + UrlValidator.ALLOW_LOCAL_URLS
	);

	List<Variant> supportedVariants;
	Map<MediaType, String> mediaType2Language = new HashMap<MediaType, String>();
	@Context
	Request request;
	@Context
	protected UriInfo uriInfo;
	@Context 
	protected HttpHeaders headers;
	
	public Response throwServiceError(String msg, int status) {
		return Response.status(status).entity(msg).build();
	}
	public Response throwServiceError(String msg) {
		return throwServiceError(msg, 400);
	}
	public Response throwServiceError(Exception e) {
		return throwServiceError(e.toString(), 400);
	}
	
	protected URI getUriForString(String uriStr) throws URISyntaxException {
		if (null == uriStr)
			throw new URISyntaxException(uriStr, "Must provide 'uri' query parameterk.");
		
		// this might throw a URISyntaxException as well
		URI uri = new URI(uriStr);
		
		// stricter validation than just throwing an URISyntaxException (Sesame is picky about URLs)
		if (! validateUri(uriStr)) 
			throw new URISyntaxException(uriStr, "'uri' parameter is not a valid URI.");
		
		return uri;
		
	}

	protected URI getRequestUriWithoutQuery() {
		UriBuilder ub =  uriInfo.getRequestUriBuilder();
		ub.replaceQuery("");
		return ub.build();
	}
	
	protected Grafeo getGrafeoForUriWithContentNegotiation(String uriStr) throws IOException, URISyntaxException {
		return getGrafeoForUriWithContentNegotiation(getUriForString(uriStr));
	}
	protected Grafeo getGrafeoForUriWithContentNegotiation(URI uri) throws IOException {
		URL url = null;
		url = new URL(uri.toString());
		InputStream in = null;
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Accept", "application/rdf+xml");
		con.connect();
		in = con.getInputStream();
		GrafeoImpl tempG = new GrafeoImpl();
		tempG.getModel().read(in, null, "RDF/XML");
		if (tempG.getModel().isEmpty()) {
			return null;
		}
		return tempG;
	}

	protected AbstractRDFService() {
		this.supportedVariants = Variant
				.mediaTypes(MediaType.valueOf(PLAIN), MediaType.valueOf(XML),
						MediaType.valueOf(TTL_A), MediaType.valueOf(TTL_T),
						MediaType.valueOf(N3)).add().build();
		mediaType2Language.put(MediaType.valueOf(PLAIN), "N-TRIPLE");
		mediaType2Language.put(MediaType.valueOf(XML), "RDF/XML");
		mediaType2Language.put(MediaType.valueOf(TTL_A), "TURTLE");
		mediaType2Language.put(MediaType.valueOf(TTL_T), "TURTLE");
		mediaType2Language.put(MediaType.valueOf(N3), "N3");
	}

	protected Response getResponse(Model model) {
		Variant selectedVariant = request.selectVariant(supportedVariants);
		assert selectedVariant != null;

		return Response.ok(
				new RDFOutput(model, selectedVariant.getMediaType()),
				selectedVariant.getMediaType()).build();

	}

	protected Response getResponse(Grafeo grafeo) {
		return getResponse(((GrafeoImpl) grafeo).getModel());

	}

	protected StreamingOutput getResponseEntity(Model model) {
		Variant selectedVariant = request.selectVariant(supportedVariants);
		assert selectedVariant != null;
		return new RDFOutput(model, selectedVariant.getMediaType());

	}

	protected StreamingOutput getResponseEntity(Grafeo grafeo) {
		return getResponseEntity(((GrafeoImpl) grafeo).getModel());

	}

	protected static boolean validateUri(String uri) {
		return urlValidator.isValid(uri);
	}
	
	protected String createUniqueStr() {
		String uniqueStr = new Date().getTime() + "_" + UUID.randomUUID().toString();
		return uniqueStr;
	}

	protected class RDFOutput implements StreamingOutput {
		Logger log = Logger.getLogger(getClass().getName());
		Model model;
		MediaType mediaType;

		public RDFOutput(Model model, MediaType mediaType) {
			this.model = model;
			this.mediaType = mediaType;
		}

		@Override
		public void write(OutputStream output) throws IOException,
				WebApplicationException {
			log.fine("Mediatype: " + this.mediaType);
			model.write(output, mediaType2Language.get(this.mediaType));
		}
	}
}
