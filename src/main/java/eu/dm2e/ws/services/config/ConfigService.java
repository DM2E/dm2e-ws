package eu.dm2e.ws.services.config;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

import eu.dm2e.grafeo.GResource;
import eu.dm2e.grafeo.GStatement;
import eu.dm2e.grafeo.Grafeo;
import eu.dm2e.grafeo.gom.SerializablePojo;
import eu.dm2e.grafeo.jena.GrafeoMongoImpl;
import eu.dm2e.grafeo.json.GrafeoJsonSerializer;
import eu.dm2e.logback.LogbackMarkers;
import eu.dm2e.ws.Config;
import eu.dm2e.ws.ConfigProp;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.ErrorMsg;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.ParameterAssignmentPojo;
import eu.dm2e.ws.api.ValidationReport;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.services.AbstractRDFService;

/**
 * Service for storing and retrieving configurations for workflows and webservices.
 *
 * FIXME refactor this into workflowconfig/webservice config service so we can delegate 
 * de/serialization to MessageBodyWriter
 * @author Konstantin Baierer
 *
 */
@Path("/config")
public class ConfigService extends AbstractRDFService {
	
	private class ConfigPojoComparator implements Comparator<WebserviceConfigPojo> {
		private String compareProp;
		public ConfigPojoComparator(String compareProp) {
			this.compareProp = compareProp;
		}
		@Override
		public int compare(WebserviceConfigPojo o1, WebserviceConfigPojo o2) {
			if (compareProp.equals(NS.DCTERMS.PROP_MODIFIED)) {
				return o1.getModified().compareTo(o2.getModified());
			} else {
				return 0;
			}
		}
	}

	@Override
	public WebservicePojo getWebServicePojo() {
		WebservicePojo ws = super.getWebServicePojo();
		ws.setLabel("Configuration service");
        return ws;
	}

    /**
     * GET /{id}/validate		Accept: RDF, JSON
     * @return
     */
    @GET
    @Path("{id}/validate")
	@Produces({
		 DM2E_MediaType.TEXT_PLAIN,
	})
    public Response validateConfig() {
        URI configURI = popPath(getRequestUriWithoutQuery());
        log.info("Validate Configuration : " + configURI);
        Grafeo g = new GrafeoMongoImpl();
        try {
	        g.readFromEndpoint(Config.get(ConfigProp.MONGO), configURI);
        } catch (RuntimeException e) {
        	return throwServiceError(getRequestUriWithoutQuery().toString(), ErrorMsg.NOT_FOUND, 404);
        }
        
        if (! g.containsTriple(configURI, NS.RDF.PROP_TYPE, NS.OMNOM.CLASS_WEBSERVICE_CONFIG)) {
        	return throwServiceError(configURI + " has an unknown rdf:type.", ErrorMsg.NOT_FOUND, 404);
        }
        WebserviceConfigPojo configPojo = g.getObjectMapper().getObject(WebserviceConfigPojo.class, configURI);
        ValidationReport report = configPojo.validate();
		if (report.valid()) {
			return Response.status(Response.Status.OK).entity(report.toString()).build();
		} else {
			return Response.status(Response.Status.PRECONDITION_FAILED).entity(report.toString()).build();
		}
    }
    
    /**
     * GET /{id}		Accept: RDF, JSON
     * @return
     */
    @GET
    @Path("{id}")
	@Produces({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE
		// , DM2E_MediaType.TEXT_PLAIN,
		 , MediaType.APPLICATION_JSON
	})
    public Response getConfig() {
        log.info("Configuration requested: " + getRequestUriWithoutQuery());
        Grafeo g = new GrafeoMongoImpl();
        URI configURI = getRequestUriWithoutQuery();
        try {
	        g.readFromEndpoint(Config.get(ConfigProp.MONGO), configURI);
        } catch (RuntimeException e) {
        	return throwServiceError(getRequestUriWithoutQuery().toString(), ErrorMsg.NOT_FOUND, 404);
        }
        
        ResponseBuilder resp;
        if (g.containsTriple(configURI, NS.RDF.PROP_TYPE, NS.OMNOM.CLASS_WEBSERVICE_CONFIG)) {
        	WebserviceConfigPojo configPojo = g.getObjectMapper().getObject(WebserviceConfigPojo.class, configURI);
        	resp = Response.ok(configPojo);
        } else {
        	return throwServiceError(configURI + " has an unknown rdf:type.", ErrorMsg.NOT_FOUND, 404);
        }
        return resp.build();
    }

    /**
     * GET /list/facets		CT:JSON
     */
    @GET
    @Path("list/facets")
    public Response getConfigListFacets() { 
    	ParameterizedSparqlString sb = new ParameterizedSparqlString();
    	sb.setNsPrefix("rdf", NS.RDF.BASE);
    	sb.setNsPrefix("dcterms", NS.DCTERMS.BASE);
    	sb.setNsPrefix("omnom", NS.OMNOM.BASE);
    	sb.append("SELECT ?creator {  \n");
    	sb.append("  GRAPH ?conf {  \n");
    	sb.append("    ?conf rdf:type omnom:WebserviceConfig .    \n");
    	sb.append("    ?conf omnom:webservice ?ws .  \n");
    	sb.append("    ?ws omnom:implementationID ?implId .  \n");
    	sb.append("    FILTER (str(?implId) = \"eu.dm2e.ws.services.workflow.WorkflowExecutionService\") .  \n");
    	sb.append("    OPTIONAL { ?conf dcterms:creator ?creator } .  \n");
    	sb.append("  }    \n");
    	sb.append("}  \n");

    	GrafeoMongoImpl g = new GrafeoMongoImpl();
    	Query query = sb.asQuery();
    	QueryEngineHTTP qExec = QueryExecutionFactory.createServiceRequest(Config.get(ConfigProp.MONGO), query);
    	long startTime = System.currentTimeMillis();
    	log.debug("About to execute facet SELECT query.");
    	ResultSet resultSet = qExec.execSelect();
    	long estimatedTime = System.currentTimeMillis() - startTime;
    	log.debug(LogbackMarkers.TRACE_TIME, "SELECT config facet query took " + estimatedTime + "ms.");

        PojoListFacet creatorFacet = new PojoListFacet();
        creatorFacet.setLabel("Creator");
        creatorFacet.setQueryParam("user");
        creatorFacet.setRdfProp(NS.DCTERMS.PROP_CREATOR);

        while (resultSet.hasNext()) {
        	QuerySolution sol = resultSet.next();
        	if (null != sol.get("creator"))
        		creatorFacet.getValues().add(sol.get("creator").asNode().toString());
        }

        List<PojoListFacet> retList = new ArrayList<>();
        retList.add(creatorFacet);
    	return Response.ok(new Gson().toJson(retList).toString()).build();
    }

    
    /**
     * GET /list		Accept: RDF, JSON
     * @param uriInfo
     * @return
     */
    @GET
    @Path("list")
    public Response getConfigList(
    		@QueryParam("user") String filterUser,
    		@QueryParam("type") String filterType,
			@QueryParam("sort") String sortProp,
			@QueryParam("order") String sortOrder
    		) {
    	
    	GrafeoMongoImpl g = new GrafeoMongoImpl();
    	g.readTriplesFromEndpoint(Config.get(ConfigProp.MONGO), null, NS.RDF.PROP_TYPE, g.resource(NS.OMNOM.CLASS_WEBSERVICE_CONFIG));
    	List<WebserviceConfigPojo> configList = new ArrayList<>();
    	{
    		long startTime = System.currentTimeMillis();
    		for (GStatement typeStmt : g.listStatements(null, NS.RDF.PROP_TYPE, g.resource(NS.OMNOM.CLASS_WEBSERVICE_CONFIG))) {
    			WebserviceConfigPojo configPojo = g.getObjectMapper().getObject(WebserviceConfigPojo.class, typeStmt.getSubject());
    			// we don't need the parameter asssignments, they make the response too large
    			configPojo.setParameterAssignments(new HashSet<ParameterAssignmentPojo>());
    			// we only want the webservice id, reduce reduce!
    			WebservicePojo wf = configPojo.getWebservice();
    			WebservicePojo dummyWs = new WebservicePojo();
    			dummyWs.setId(wf.getId());
    			configPojo.setWebservice(dummyWs);
    			configList.add(configPojo);
    		}
    		long endTime  = System.currentTimeMillis();
    		long elapsed = endTime - startTime;
    		log.debug(LogbackMarkers.TRACE_TIME, "POJOization of workflowconfigs took " + elapsed + "ms. ");
    	}
    	{
    		long startTime = System.currentTimeMillis();
    		if (null == sortProp) {
    			sortProp = NS.DCTERMS.PROP_MODIFIED;
    		}
    		Collections.sort(configList, new ConfigPojoComparator(sortProp));
    		long endTime  = System.currentTimeMillis();
    		long elapsed = endTime - startTime;
    		log.debug(LogbackMarkers.TRACE_TIME, "SORTING of workflowconfigs took " + elapsed + "ms. ");
    	}
    	return Response.ok(configList).build();
    }

    /**
     * PUT /{id}		Accept: RDF
     * @param input
     * @return
     */
    @PUT
    @Consumes({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE
    })
    @Path("{id}")
    public Response putConfig(File input) {
    	URI uri = getRequestUriWithoutQuery();
        log.info("PUT config to " + uri);
        Grafeo g;
        try {
        	g = new GrafeoMongoImpl(input);
        } catch (Throwable t) {
        	log.error(ErrorMsg.BAD_RDF.toString());
        	return throwServiceError(ErrorMsg.BAD_RDF, t);
        }
        log.info("Looking for top blank node.");
        GResource res = g.findTopBlank(NS.OMNOM.CLASS_WEBSERVICE_CONFIG);
        if (null == res)  {
        	res = g.resource(uri);
        } else {
        	res.rename(uri.toString());
        }
        String actualRdfType;
		try {
			actualRdfType = g.firstMatchingObject(uri.toString(), NS.RDF.PROP_TYPE).resource().getUri();
		} catch (NullPointerException e) {
			return throwServiceError(ErrorMsg.UNTYPED_RDF);
		}
        log.warn("Skolemnizing assignments ...");
        g.skolemizeSequential(uri.toString(), NS.OMNOM.PROP_ASSIGNMENT, "assignment");
        log.warn("DONE Skolemnizing ...");
        
        // No need to create POJO if we pass on the graph as-is anyway
//        WebserviceConfigPojo pojo = g.getObjectMapper().getObject(WebserviceConfigPojo.class, res);
//        pojo.getGrafeo().putToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), uri);

        g.putToEndpoint(Config.get(ConfigProp.MONGO), uri);
        
        return Response.created(uri).entity(g).build();
        
//        return Response.created(uri).entity(getResponseEntity(g)).build();
//        return Response.seeOther(uri).build();
    }

	/**
	 * POST /			Accept: RDF
	 * @param input
	 * @return
	 */
	@POST
    @Consumes({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE
    })
    public Response postConfig(File input) {
        log.info("POST config.");
//        // TODO BUG this fails if newlines aren't correctly transmitted due to line-wide comments in turtle
        
    	URI uri = appendPath(createUniqueStr());
    	log.debug("Posting the config to " + uri.toString());
        Grafeo g;
        try {
        	g = new GrafeoMongoImpl(input);
        } catch (Throwable t) {
        	log.warn(ErrorMsg.BAD_RDF.toString());
        	return throwServiceError(ErrorMsg.BAD_RDF, t);
        }
        log.debug("Parsed config RDF.");

        GResource res = g.findTopBlank(NS.OMNOM.CLASS_WEBSERVICE_CONFIG);
        	if (null == res)
	        	return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE + ": " + g.getTurtle());
        res.rename(uri);
        log.debug("Renamed top blank node.");
        g.skolemizeSequential(uri.toString(), NS.OMNOM.PROP_ASSIGNMENT, "assignment");
        log.debug("Skolemnized assignments");
        
        SerializablePojo pojo;
        	pojo = g.getObjectMapper().getObject(WebserviceConfigPojo.class, res);
        	log.warn("Webservice: " + ((WebserviceConfigPojo) pojo).getWebservice());

        g.putToEndpoint(Config.get(ConfigProp.MONGO), uri);
        log.info("Written data to endpoint.");
        log.debug(LogbackMarkers.DATA_DUMP, g.getTerseTurtle());

        return Response.created(uri).entity(pojo).build();
    }
	
	/**
	 * POST /		Accept: JSON
	 * @param input
	 * @return
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postConfigJSON(String input) {
		log.info("POST config (JSON)");

		JsonParser jsonParser = new JsonParser();
		JsonElement json = jsonParser.parse(input);
		if (! json.getAsJsonObject().has(NS.RDF.PROP_TYPE)) {
			return throwServiceError(ErrorMsg.UNTYPED_RDF);
		}

		String rdfType = json.getAsJsonObject().get(NS.RDF.PROP_TYPE).getAsString();
		SerializablePojo pojo;
		if (rdfType.equals(NS.OMNOM.CLASS_WEBSERVICE_CONFIG)) {
			pojo = GrafeoJsonSerializer.deserializeFromJSON(input, WebserviceConfigPojo.class);
		} else {
			return throwServiceError(ErrorMsg.WRONG_RDF_TYPE);
		}
		log.debug(LogbackMarkers.DATA_DUMP, pojo.getTerseTurtle());
		
		// set uri
    	URI uri = appendPath(createUniqueStr());
    	pojo.setId(uri.toString());
		
    	// Skolemize assignments
		Grafeo g = pojo.getGrafeo();
		g.skolemizeUUID(uri.toString(), NS.OMNOM.PROP_ASSIGNMENT, "assignment");

		SerializablePojo pojoSkolemized = g.getObjectMapper()
				.getObject(WebserviceConfigPojo.class, uri);
    	
		log.debug(LogbackMarkers.DATA_DUMP, pojoSkolemized.getTerseTurtle());
    	pojoSkolemized.getGrafeo().putToEndpoint(Config.get(ConfigProp.MONGO), uri);
    	
    	return Response.created(uri).entity(pojoSkolemized).build();
		
	}

	/**
	 * PUT /{id}		Accept: JSON
	 * @param input
	 * @return
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public Response putConfigJSON(String input) {
		log.info("PUT config (JSON)");

		JsonParser jsonParser = new JsonParser();
		JsonElement json = jsonParser.parse(input);
		if (! json.getAsJsonObject().has(NS.RDF.PROP_TYPE)) {
			return throwServiceError(ErrorMsg.UNTYPED_RDF);
		}

		String rdfType = json.getAsJsonObject().get(NS.RDF.PROP_TYPE).getAsString();
		SerializablePojo pojo;
		if (rdfType.equals(NS.OMNOM.CLASS_WEBSERVICE_CONFIG)) {
			pojo = GrafeoJsonSerializer.deserializeFromJSON(input, WebserviceConfigPojo.class);
		} else {
			return throwServiceError(ErrorMsg.WRONG_RDF_TYPE);
		}
		log.debug(LogbackMarkers.DATA_DUMP, pojo.getTerseTurtle());
		
		// DON'T change the ID
		URI uri = getRequestUriWithoutQuery();
		
    	// Skolemize assignments
		Grafeo g = pojo.getGrafeo();
		g.skolemizeUUID(uri.toString(), NS.OMNOM.PROP_ASSIGNMENT, "assignment");

		SerializablePojo pojoSkolemized = g.getObjectMapper()
				.getObject(WebserviceConfigPojo.class, uri);
    	
		log.debug(LogbackMarkers.DATA_DUMP, pojoSkolemized.getTerseTurtle());
    	pojoSkolemized.getGrafeo().putToEndpoint(Config.get(ConfigProp.MONGO), uri);
    	
    	return Response.created(uri).entity(pojoSkolemized).build();
		
	}

	/**
	 * POST {id}/assignment
	 * @param configId
	 * @param rdfString
	 * @return
	 */
	@POST
	@Consumes({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_PLAIN,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE })
	@Path("{configId}/assignment/")
	public Response postAssignment(@PathParam("configId") String configId, String rdfString) {
		Grafeo g = new GrafeoMongoImpl(rdfString, null);
		GResource blank = g.findTopBlank(NS.OMNOM.CLASS_PARAMETER_ASSIGNMENT);
		if (null == blank) {
			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
		}
		URI newUri = appendPath(createUniqueStr());
		URI conifgUri = popPath();
		blank.rename(newUri);
		g.postToEndpoint(Config.get(ConfigProp.MONGO), conifgUri);
		return Response.created(newUri).build();
	}
	
	/**
	 * PUT {configId}/assignment/{assId}
	 * @param configId
	 * @param assId
	 * @param rdfString
	 * @return
	 */
	@PUT
	@Consumes({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_PLAIN,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE })
	@Path("{configId}/assignment/{assId}")
	public Response putAssignment(@PathParam("configId") String configId, @PathParam("assId") String assId, String rdfString) {
		URI assUri = getRequestUriWithoutQuery();
		URI configUri = popPath(popPath());
		Grafeo insertG = new GrafeoMongoImpl(rdfString, null);
		GrafeoMongoImpl g = new GrafeoMongoImpl();
		g.readFromEndpoint(Config.get(ConfigProp.MONGO), configUri);
		g.removeTriple(g.resource(assUri), null, null);
		g.removeTriple(null, null, g.resource(assUri));
		g.merge(insertG);
		g.putToEndpoint(Config.get(ConfigProp.MONGO), configUri);
		return getResponse(g);
	}
	
	@GET
	@Path("{configId}/assignment/{assId}")
	@Produces({
		DM2E_MediaType.APPLICATION_RDF_TRIPLES,
		DM2E_MediaType.APPLICATION_RDF_XML,
		DM2E_MediaType.APPLICATION_X_TURTLE,
		DM2E_MediaType.TEXT_PLAIN,
		DM2E_MediaType.TEXT_RDF_N3,
		DM2E_MediaType.TEXT_TURTLE
	})
	public Response getWorkflowConnector( @PathParam("configId") String configId, @PathParam("assId") String assId) {
        log.info("Assignment " + assId + " of configuration requested: " + uriInfo.getRequestUri());	
		URI configUri = popPath(popPath());
		return Response.seeOther(configUri).build();
	}

}
