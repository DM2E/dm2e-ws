package eu.dm2e.ws.services;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import eu.dm2e.grafeo.Grafeo;
import eu.dm2e.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.Config;
import eu.dm2e.ws.ConfigProp;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.ErrorMsg;
import eu.dm2e.ws.api.WebservicePojo;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

/**
 * Abstract Base class for all RDF services.
 *
 * <p>
 * Every service inheriting from AbstractRDFService will return a description of itself on GET /
 * </p>
 *
 * TODO lots of docs
 * 
 * TODO @GET /{id}/param/{param} 303 -> /{id}
 *
 */
@Produces({ 
	DM2E_MediaType.APPLICATION_RDF_TRIPLES,
	DM2E_MediaType.APPLICATION_RDF_XML,
	DM2E_MediaType.APPLICATION_X_TURTLE,
	DM2E_MediaType.TEXT_PLAIN,
	DM2E_MediaType.TEXT_RDF_N3,
	DM2E_MediaType.TEXT_TURTLE,
//	MediaType.TEXT_HTML,
	MediaType.APPLICATION_JSON
	})
@Consumes({ 
	DM2E_MediaType.APPLICATION_RDF_TRIPLES,
	DM2E_MediaType.APPLICATION_RDF_XML,
	DM2E_MediaType.APPLICATION_X_TURTLE,
	DM2E_MediaType.TEXT_PLAIN,
	DM2E_MediaType.TEXT_RDF_N3,
	DM2E_MediaType.TEXT_TURTLE,
	MediaType.APPLICATION_JSON,
	MediaType.MULTIPART_FORM_DATA 
	})
public abstract class AbstractRDFService {

	protected Logger log = LoggerFactory.getLogger(getClass().getName());
	/**
	 * Creating Jersey API clients is relatively expensive so we do it once per Service statically
	 */
	protected static Client client = new Client();
	
	protected static String[] allowedSchemes = { "http", "https", "file", "ftp" };
	protected static final UrlValidator urlValidator = new UrlValidator(allowedSchemes,
		UrlValidator.ALLOW_ALL_SCHEMES + UrlValidator.ALLOW_LOCAL_URLS
	);
    protected WebservicePojo webservicePojo = new WebservicePojo();

	protected List<Variant> supportedVariants;
	@Context
	Request request;
	@Context
	protected UriInfo uriInfo;
	@Context 
	protected HttpHeaders headers;
	
    public Response throwServiceError(ErrorMsg msg, int status) {
    	return this.throwServiceError(msg.toString(), status);
	}
    public Response throwServiceError(ErrorMsg badRdf, Throwable t) {
		String errStr = badRdf.toString();
		Response resp = this.throwServiceError(new RuntimeException(t));
		errStr = resp.getEntity() + ": " + errStr;
		return this.throwServiceError(errStr);
	}

    public Response throwServiceError(String msg, int status) {
		log.warn("EXCEPTION: " + msg);
		return Response.status(status).entity(msg).build();
	}
    public Response throwServiceError(String msg) {
		return throwServiceError(msg, 400);
	}
    public Response throwServiceError(Exception e) {
		return throwServiceError(e.toString() + "\n" + ExceptionUtils.getStackTrace(e), 400);
	}
    public Response throwServiceError(ErrorMsg err) {
		return throwServiceError(err.toString());
	}
    public Response throwServiceError(String badString, ErrorMsg err) {
		return throwServiceError(badString + ": " + err.toString());
	}
    public Response throwServiceError(String badString, ErrorMsg err, int status) {
		return throwServiceError(badString + ": " + err.toString(), status);
	}

    public URI getUriForString(String uriStr) throws URISyntaxException {
		if (null == uriStr)
			throw new URISyntaxException("", "Must provide 'uri' query parameter.");
		
		// this might throw a URISyntaxException as well
		URI uri = new URI(uriStr);
		
		// stricter validation than just throwing an URISyntaxException (Sesame is picky about URLs)
		if (notValid(uriStr))
			throw new URISyntaxException(uriStr, "'uri' parameter is not a valid URI.");
		
		return uri;
		
	}

    public URI getRequestUriWithoutQuery() {
		UriBuilder ub =  uriInfo.getRequestUriBuilder();
		ub.replaceQuery("");
		return ub.build();
	}
	
//	protected GrafeoImpl getServiceDescriptionGrafeo() throws IOException  {
////        InputStream descriptionStream  = Thread.currentThread().getContextClassLoader().getResourceAsStream("xslt-service-description.ttl");
////		System.out.println(getServiceDescriptionResourceName());
////		InputStream descriptionStream = ClassLoader.getSystemResource(getServiceDescriptionResourceName()).openStream();
//        InputStream descriptionStream  = this.getClass().getResourceAsStream("service-description.ttl");
//		if (null == descriptionStream) {
//			throw new FileNotFoundException();
//		}
//        GrafeoImpl g = new GrafeoImpl(descriptionStream, "TURTLE");
//        // rename top blank node if any
//        GResource blank = g.findTopBlank();
//        String uri = getRequestUriWithoutQuery().toString();
//        if (blank!=null) blank.rename(uri);
//		return g;
//	}
	


    /**
     * Default implementation of the webservice description.
     * Implementing subclasses should provide further information
     * by calling the setters of the returned description pojo.
     *
     * @return The webservice description
     */
    public  WebservicePojo getWebServicePojo() {
        if (webservicePojo.getId()==null)    {
            String base = Config.get(ConfigProp.BASE_URI);
            String path = this.getClass().getAnnotation(Path.class).value();
            if (base.endsWith("/") && path.startsWith("/")) base = base.substring(0,base.length()-1);
            webservicePojo.setId(base + path);
            webservicePojo.setImplementationID(this.getClass().getCanonicalName());
        }
        return webservicePojo;
    }

    /**
     *
     * Implementation of the default behaviour, which is a 303 redirect
     * from the base URI to /describe, where the webservice description is returned.
     *
     * @return
     */
    @GET
	@Produces({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_PLAIN,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE
	})
    public Response getBase()  {
        URI uri = appendPath(uriInfo.getRequestUri(),"describe");
        return Response.seeOther(uri).build();
    }
    
//    @GET
//    @Path("{id}/nested/{nestedId}")
//    public Response getConfigAssignment(
//    		@Context UriInfo uriInfo,
//     		@PathParam("id") String id,
//     		@PathParam("nestedId") String nestedId
//    		) {
//        log.info("Nested resource " + nestedId + " of service requested: " + uriInfo.getRequestUri());
////        Grafeo g = new GrafeoImpl();
//        // @TODO should proabably use getRequestUriWithoutQuery().toString() here
////        g.readFromEndpoint(NS.ENDPOINT, uriInfo.getRequestUri().toString());
////        return getResponse(g);
//        return Response.seeOther(getRequestUriWithoutQuery()).build();
//    }

    /**
     * The serialization of the webservice description is returned.
     *
     *
     * @return
     */
    @GET
	@Path("/describe")
	public Response getDescription()  {
        WebservicePojo wsDesc = this.getWebServicePojo();
        URI wsUri = popPath();
        wsDesc.setId(wsUri);
        log.trace(wsDesc.getTerseTurtle());
        return Response.ok().entity(wsDesc).build();
//        return Response.ok().entity(getResponseEntity(wsDesc.getGrafeo())).build();
	}
    
    @GET
    @Path("/param/{paramId}")
    public Response getParamDescription() {
    	String baseURIstr = getRequestUriWithoutQuery().toString();
    	baseURIstr = baseURIstr.replaceAll("/param/[^/]+$", "");
    	URI baseURI;
		try {
			baseURI = new URI(baseURIstr);
		} catch (URISyntaxException e) {
//			throw(e);
			return throwServiceError(e);
		}
    	return Response.seeOther(baseURI).build();
    }
	
	
// TODO    
//	@PUT
//	@Path("validate")
//	public Response validateConfigRequest(String configUriStr) {
//		try {
//			validateServiceInput(configUriStr);
//		} catch (Exception e) {
//			return throwServiceError(e);
//		}
//		return Response.noContent().build();
//	}
	
	protected Grafeo getGrafeoForUriWithContentNegotiation(String uriStr) throws IOException, URISyntaxException {
		return getGrafeoForUriWithContentNegotiation(getUriForString(uriStr));
	}
	protected Grafeo getGrafeoForUriWithContentNegotiation(URI uri) throws IOException {
		URL url = new URL(uri.toString());
		InputStream in = null;
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Accept", "text/turtle");
		con.connect();
		in = con.getInputStream();
		GrafeoImpl tempG = new GrafeoImpl();
		tempG.getModel().read(in, null, "TURTLE");
		if (tempG.getModel().isEmpty()) {
			return null;
		}
		return tempG;
	}

	protected AbstractRDFService() {
		this.supportedVariants = Variant
				.mediaTypes(
						DM2E_MediaType.SET_OF_RDF_TYPES
								.toArray(new MediaType[DM2E_MediaType.SET_OF_RDF_TYPES.size()]))
				.add().build();
	}

	protected Response getResponse(Model model) {
		Variant selectedVariant = request.selectVariant(supportedVariants);
		assert selectedVariant != null;
        if (uriInfo.getQueryParameters().containsKey("debug")) {
            return Response.ok(
                    new HTMLOutput(model),
                    MediaType.TEXT_HTML_TYPE).build();
        }

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
        if (uriInfo.getQueryParameters().containsKey("debug")) {
            return new HTMLOutput(model);
        }
		return new RDFOutput(model, selectedVariant.getMediaType());

	}

	protected StreamingOutput getResponseEntity(Grafeo grafeo) {
		return getResponseEntity(((GrafeoImpl) grafeo).getModel());

	}

	protected static boolean notValid(String uri) {
		return !urlValidator.isValid(uri);
	}
	
	protected String createUniqueStr() {
//		return new Date().getTime() + "_" + UUID.randomUUID().toString();
		return UUID.randomUUID().toString();
	}
	

// TODO
//	protected void validateServiceInput(String configUriStr) throws Exception {
//		Grafeo inputGrafeo = new GrafeoImpl();
//		inputGrafeo.load(configUriStr);
//		if (inputGrafeo.isEmpty()) {
//			throw new Exception("config model is empty.");
//		}
//		WebservicePojo wsDesc = this.getWebServicePojo();
//		for (ParameterPojo param : wsDesc.getInputParams()) {
//			if (param.getIsRequired()) {
//				if (! inputGrafeo.containsStatementPattern("?s", NS.OMNOM.PROP_FOR_PARAM, param.getId())) {
//					log.error(configUriStr + " does not contain '?s NS.OMNOM.PROP_FOR_PARAM " + param.getId());
//					throw new RuntimeException(configUriStr + " does not contain '?s omnom:forParam " + param.getId());
//				}
//			}
//		}
//
//	}

    public URI appendPath(String... paths) {
		URI uri = uriInfo.getRequestUri();
		return this.appendPath(uri, paths);
	}

    public URI appendPath(URI uri, String... paths) {
        String query = uri.getQuery();
        String u = uri.toString();
        log.trace("URI: " + u);
        if (query!=null) {
            log.trace("Query: " + query);
            u = u.replace("?" + query,"");
        }
        for (String path : paths) {
	        if (!u.endsWith("/") && !path.startsWith("/")) u = u + "/";
	        u = u + path;
        }
        if (query!=null) {
            u = u + "?" + query;
        }
        log.trace("After append: " + u);
        try {
            return new URI(u);
        } catch (URISyntaxException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
    }

    public URI popPath() {
		URI uri = uriInfo.getRequestUri();
		return this.popPath(uri, null);
	}
    public URI popPath(URI uri) {
		return this.popPath(uri, null);
	}
    public URI popPath(String path) {
		URI uri = uriInfo.getRequestUri();
		return this.popPath(uri, path);
	}

    /**
     * Removes last path element or the given path from a URI. Query remains unaffected.
     * @param uri
     * @param path
     * @return
     */
    public URI popPath(URI uri, String path) {
        String query = uri.getQuery();
        String u = uri.toString();
        log.trace("URI: " + u);
        if (query!=null) {
            log.trace("Query: " + query);
            u = u.replace("?" + query,"");
        }
        if (u.endsWith("/")) {
        	u = u.replaceAll("/$", "");
        }
        if (path != null) { 
	        if (! u.endsWith("/" + path)) {
	        	throw new RuntimeException("URI '" + uri + "' doesn't end in in '/"+ path +"'.");
	        }
	        u = u.replaceAll("/" + path + "$", "");
        }
        else {
        	u = u.replaceAll("/[^/]+$", "");
        }
        if (query!=null) {
            u = u + "?" + query;
        }
        log.trace("Result: " + u);
        try {
            return new URI(u);
        } catch (URISyntaxException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
    }

    /**
     * Removes first occurrence of the given path element from a URI. Query remains unaffected.
     * @param uri
     * @param path
     * @return
     */
    public URI popPathFromBeginning(URI uri, String path) {
        String u = uri.toString();
        log.trace("URI: " + u);
        u = u.replaceFirst("/" + path + "/", "/");
        log.trace("Result: " + u);
        try {
            return new URI(u);
        } catch (URISyntaxException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
    }

    /**
     * Adds the given path element to the beginning of a URI. Query remains unaffected.
     * @param uri
     * @param path
     * @return
     */
    public URI pushPathFromBeginning(UriInfo uri, String path) {
        String u = uri.getRequestUri().toString();
        String p = uri.getPath();
        log.trace("URI: " + u);
        u = u.replaceFirst(p, "/" + path + p);
        log.trace("Result: " + u);
        try {
            return new URI(u);
        } catch (URISyntaxException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
    }

    public String lastPathElement(URI uri) {
        String query = uri.getQuery();
        String u = uri.toString();
        log.trace("URI: " + u);
        if (query!=null) {
            log.trace("Query: " + query);
            u = u.replace("?" + query,"");
        }
        return lastPathElement(u);
    }
    public String lastPathElement(String uri) {
        if (uri.endsWith("/")) {
            uri = uri.replaceAll("/$", "");
        }
        String tmp = uri.replaceAll("[^/]+$", "");
        return uri.substring(tmp.length());

    }




    protected class RDFOutput implements StreamingOutput {
		Logger log = LoggerFactory.getLogger(getClass().getName());
		Model model;
		MediaType mediaType;

		public RDFOutput(Model model, MediaType mediaType) {
			this.model = model;
			this.mediaType = mediaType;
		}

		@Override
		public void write(OutputStream output) throws IOException,
				WebApplicationException {
			log.trace("Media type: " + this.mediaType);
			model.write(output, DM2E_MediaType.getJenaLanguageForMediaType(this.mediaType));
		}
	}

    protected class HTMLOutput implements StreamingOutput {
        Logger log = LoggerFactory.getLogger(getClass().getName());
        Model model;

        public HTMLOutput(Model model) {
            this.model = model;

        }

        @Override
        public void write(OutputStream output) throws IOException,
                WebApplicationException {
            PrintWriter pw = new PrintWriter(output);
            pw.write("<html><body><table>");
            StmtIterator it = model.listStatements();
            while (it.hasNext()) {
                Statement st = it.nextStatement();
                pw.write("<tr><td>");
                    pw.write("<a href=\"");
                        pw.write(st.getSubject().getURI());
                        pw.write("?debug\">");
                        pw.write(st.getSubject().getURI());
                    pw.write("</a>");
                pw.write("</td><td>");
                pw.write(st.getPredicate().getURI());
                pw.write("</td><td>");
                pw.write(st.getObject().toString());
                pw.write("</td></tr>");

            }
            pw.write("</table></body></html>");
            pw.close();
            output.flush();
        }
    }
    
    protected boolean expectsMetadataResponse() {
    	return DM2E_MediaType.expectsMetadataResponse(headers);
    }
    protected boolean expectsJsonResponse() {
    	return DM2E_MediaType.expectsJsonResponse(headers);
    }
    protected boolean expectsRdfResponse() {
    	return DM2E_MediaType.expectsRdfResponse(headers);
    }
}
