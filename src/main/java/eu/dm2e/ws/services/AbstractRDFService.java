package eu.dm2e.ws.services;

import com.hp.hpl.jena.rdf.model.Model;
import eu.dm2e.ws.Config;
import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.validator.routines.UrlValidator;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

@Produces({ MediaType.TEXT_PLAIN, "application/rdf+xml",
		"application/x-turtle", "text/turtle", "text/rdf+n3" })
@Consumes({ MediaType.TEXT_PLAIN, "application/rdf+xml",
		"application/x-turtle", "text/turtle", "text/rdf+n3",
		MediaType.MULTIPART_FORM_DATA })
public abstract class AbstractRDFService {

	Logger log = Logger.getLogger(getClass().getName());
	
	public static final String PLAIN = MediaType.TEXT_PLAIN;
	public static final String XML = "application/rdf+xml";
	public static final String TTL_A = "application/x-turtle";
	public static final String TTL_T = "text/turtle";
	public static final String N3 = "text/rdf+n3";
	protected static String[] allowedSchemes = { "http", "https", "file", "ftp" };
	protected static final UrlValidator urlValidator = new UrlValidator(allowedSchemes,
		UrlValidator.ALLOW_ALL_SCHEMES + UrlValidator.ALLOW_LOCAL_URLS
	);
    protected WebservicePojo webservicePojo = new WebservicePojo();

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
		return throwServiceError(e.toString() + "\n" + ExceptionUtils.getStackTrace(e), 400);
	}
	
	protected URI getUriForString(String uriStr) throws URISyntaxException {
		if (null == uriStr)
			throw new URISyntaxException("", "Must provide 'uri' query parameterk.");
		
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
            String base = Config.config.getString("dm2e.ws.base_uri");
            if (base.endsWith("/")) base = base.substring(0,base.length()-1);
            webservicePojo.setId(base + this.getClass().getAnnotation(Path.class).value());
        }
        return webservicePojo;
    }

    /**
     *
     * Implementation of the default behaviour, which is a 303 redirect
     * from the base URI to /describe, where the webservice description is returned.
     *
     * @param uriInfo
     * @return
     */
    @GET
    public Response getBase(@Context UriInfo uriInfo)  {
        StringBuilder uri = new StringBuilder(uriInfo.getRequestUri().toString());
        if (!uri.toString().endsWith("/")) uri.append("/");
        uri.append("describe");
        try {
            return Response.seeOther(new URI(uri.toString())).build();
        } catch (URISyntaxException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }

    }

    /**
     * The serialization of the webservice description is returned.
     *
     * @param uriInfo
     * @return
     */
    @GET
	@Path("/describe")
	public Response getDescription(@Context UriInfo uriInfo)  {
        WebservicePojo wsDesc = this.getWebServicePojo();
        Grafeo g = new GrafeoImpl();
        g.addObject(wsDesc);
        return Response.ok().entity(getResponseEntity(g)).build();
	}
	
	
	@PUT
	@Path("validate")
	public Response validateConfigRequest(String configUriStr) {
		try {
			validateServiceInput(configUriStr);
		} catch (Exception e) {
			return throwServiceError(e);
		}
		return Response.noContent().build();
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
	

	protected void validateServiceInput(String configUriStr) throws Exception {
		Grafeo inputGrafeo = new GrafeoImpl();
		inputGrafeo.load(configUriStr);
		if (inputGrafeo.isEmpty()) {
			throw new Exception("config model is empty.");
		}
		WebservicePojo wsDesc = this.getWebServicePojo();
		for (ParameterPojo param : wsDesc.getInputParams()) {
			if (param.getIsRequired()) {
				if (! inputGrafeo.containsStatementPattern("?s", "omnom:forParam", param.getId())) {
					log.severe(configUriStr + " does not contain '?s omnom:forParam " + param.getId());
					throw new RuntimeException(configUriStr + " does not contain '?s omnom:forParam " + param.getId());
				}
			}
		}
//		GrafeoImpl schemaGrafeo = this.getServiceDescriptionGrafeo();
		// TODO this is the right way to to do it but Jena won't
		// croak on cardinality restrictions being broken
//		Model mergedModel = schemaGrafeo.getModel().union(inputGrafeo.getModel());
//		
////		inputGrafeo.getModel().union()
//		kkk
//		OntModelSpec mySpec = OntModelSpec.OWL_DL_MEM_RULE_INF;
//		Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
//		mySpec.setReasoner(reasoner);
//		reasoner.bindSchema(schemaGrafeo.getModel());
////		InfModel reasoningModel = ModelFactory.createInfModel(reasoner, inputGrafeo.getModel());
//		OntModel ontModel = ModelFactory.createOntologyModel(mySpec, mergedModel);
//		
//		ValidityReport validity = ontModel.validate();
//		
//		Logger log = Logger.getLogger(getClass().getName());
//		if (validity.isValid()) {
//			log.warning("w00t it's valid!!!");
//		}
//		else {
//	        for (Iterator<Report> i = validity.getReports(); i.hasNext(); ) {
//	            Report report = i.next();
//	            log.severe(" - " + report);
//	        }
//		}
		
//		Model schemaModel = schemaGrafeo.getModel();
//		
//		// Validate requiredParam
//		Property requiredProp = schemaModel.createProperty(NS.DM2E + "requiredParam");
//		NodeIterator iter = schemaModel.listObjectsOfProperty(requiredProp); 
//		while (iter.hasNext()) {
//			// this must be easier than stringifying all the time...
//			RDFNode thisNode = iter.next();
//			Property thisProp = schemaModel.createProperty(thisNode.toString());
//			if (! inputGrafeo.getModel().contains(null, thisProp)) {
//				throw new Exception("Missing required param " + thisProp.toString());
//			}
//		}
		
		// todo do range checks so that services accept certain types
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
