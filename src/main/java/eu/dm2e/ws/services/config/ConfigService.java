package eu.dm2e.ws.services.config;

import java.io.File;
import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import eu.dm2e.logback.LogbackMarkers;
import eu.dm2e.ws.Config;
import eu.dm2e.ws.ConfigProp;
import eu.dm2e.ws.DM2E_MediaType;
import eu.dm2e.ws.ErrorMsg;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.grafeo.jena.SparqlUpdate;
import eu.dm2e.ws.services.AbstractRDFService;

@Path("/config")
public class ConfigService extends AbstractRDFService {
	
	@Override
	public WebservicePojo getWebServicePojo() {
		WebservicePojo ws = super.getWebServicePojo();
		ws.setLabel("Configuration service");
		return ws;
	}
	
    /**
     * GET /{id}
     * @param uriInfo
     * @param id
     * @return
     */
    @GET
    @Path("{id}")
    public Response getConfig(@Context UriInfo uriInfo, @PathParam("id") String id) {
        log.info("Configuration requested: " + getRequestUriWithoutQuery());
        Grafeo g = new GrafeoImpl();
        try {
	        g.readFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), getRequestUriWithoutQuery());
        } catch (RuntimeException e) {
        	return throwServiceError(getRequestUriWithoutQuery().toString(), ErrorMsg.NOT_FOUND, 404);
        }
        return getResponse(g);
    }
    
    /**
     * GET /list
     * @param uriInfo
     * @return
     */
    @GET
    @Path("list")
    public Response getConfigList(@Context UriInfo uriInfo) {
        Grafeo g = new GrafeoImpl();
        g.readTriplesFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), null, NS.RDF.PROP_TYPE, g.resource(NS.OMNOM.CLASS_WEBSERVICE_CONFIG));
        g.readTriplesFromEndpoint(Config.get(ConfigProp.ENDPOINT_QUERY), null, NS.RDF.PROP_TYPE, g.resource(NS.OMNOM.CLASS_WORKFLOW_CONFIG));
        return getResponse(g);
    }

    /**
     * PUT /{id}
     * @param input
     * @return
     */
    @PUT
    @Consumes(MediaType.WILDCARD)
    @Path("{id}")
    public Response putConfig(File input) {
    	URI uri = getRequestUriWithoutQuery();
        log.info("PUT config to " + uri);
        Grafeo g;
        try {
        	g = new GrafeoImpl(input);
        } catch (Throwable t) {
        	log.error(ErrorMsg.BAD_RDF.toString());
        	return throwServiceError(ErrorMsg.BAD_RDF, t);
        }
        log.info("Looking for top blank node.");
        GResource res = g.findTopBlank(NS.OMNOM.CLASS_WORKFLOW_CONFIG);
        if (null == res) {
        	res = g.findTopBlank(NS.OMNOM.CLASS_WEBSERVICE_CONFIG);
        }
        if (null == res)  {
        	res = g.resource(uri);
        }
        else {
        	res.rename(uri.toString());
        }
        log.warn("Skolemnizing assignments ...");
        g.skolemizeSequential(uri.toString(), NS.OMNOM.PROP_ASSIGNMENT, "assignment");
        log.warn("DONE Skolemnizing ...");
        
        g.putToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), uri);
        
        return Response.created(uri).entity(getResponseEntity(g)).build();
    }

	/**
	 * POST /
	 * @param input
	 * @return
	 */
	@POST
    @Consumes(MediaType.WILDCARD)
    public Response postConfig(File input) {
        log.info("POST config.");
//        // TODO BUG this fails if newlines aren't correctly transmitted due to line-wide comments in turtle
        
    	URI uri = appendPath(createUniqueStr());
    	log.debug("Posting the config to " + uri.toString());
        Grafeo g;
        try {
        	g = new GrafeoImpl(input);
        } catch (Throwable t) {
        	log.warn(ErrorMsg.BAD_RDF.toString());
        	return throwServiceError(ErrorMsg.BAD_RDF, t);
        }
        log.debug("Parsed config RDF.");
        GResource res = g.findTopBlank(NS.OMNOM.CLASS_WORKFLOW_CONFIG);
        if (null == res)  {
        	res = g.findTopBlank(NS.OMNOM.CLASS_WEBSERVICE_CONFIG);
        	if (null == res)
	        	return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE + ": " + g.getTurtle());
        }
        res.rename(uri);
        log.debug("Renamed top blank node.");
        g.skolemizeSequential(uri.toString(), NS.OMNOM.PROP_ASSIGNMENT, "assignment");
        log.debug("Skolemnized assignments");
        
        log.debug(Config.get(ConfigProp.ENDPOINT_QUERY));
        g.putToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), uri);
        log.info("Written data to endpoint.");
        log.debug(LogbackMarkers.DATA_DUMP, g.getTerseTurtle());
        return Response.created(uri).entity(getResponseEntity(g)).build();
    }

	/**
	 * GET /{id}/validate
	 * @param g
	 * @param res
	 */
	@GET
	@Path("{id}/validate")
	public Response getValidation() {
		log.info("Validating");
		URI confPojoUri = popPath();
        WebserviceConfigPojo confPojo = new WebserviceConfigPojo();
		try {
			confPojo.loadFromURI(confPojoUri);
		} catch (Exception e1) {
			return throwServiceError(e1);
		}
        try {
			confPojo.validate();
		} catch (Exception e) {
			return throwServiceError(e);
		}
        return Response.ok().build();
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
	public Response postAssignment(@PathParam("configId") String configId, String rdfString) { Grafeo g = new GrafeoImpl(rdfString, null);
		GResource blank = g.findTopBlank(NS.OMNOM.CLASS_PARAMETER_ASSIGNMENT);
		if (null == blank) {
			return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE);
		}
		URI newUri = appendPath(createUniqueStr());
		URI conifgUri = popPath();
		blank.rename(newUri);
		g.postToEndpoint(Config.get(ConfigProp.ENDPOINT_UPDATE), conifgUri);
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
		Grafeo g = new GrafeoImpl(rdfString, null);
		SparqlUpdate sparul = new SparqlUpdate.Builder()
			.delete(assUri + "?s ?o")
			.insert(g.toString())
			.endpoint(Config.get(ConfigProp.ENDPOINT_UPDATE))
			.graph(configUri).build();
		sparul.execute();
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
