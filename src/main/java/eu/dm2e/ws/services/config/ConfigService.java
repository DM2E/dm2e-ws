package eu.dm2e.ws.services.config;

import java.io.File;
import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import eu.dm2e.ws.Config;
import eu.dm2e.ws.ErrorMsg;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.WebserviceConfigPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.services.AbstractRDFService;

@Path("/config")
public class ConfigService extends AbstractRDFService {
	
	@Override
	public WebservicePojo getWebServicePojo() {
		WebservicePojo ws = super.getWebServicePojo();
		ws.setLabel("Configuration service");
		return ws;
	}
	
    @GET
    @Path("{id}")
    public Response getConfig(@Context UriInfo uriInfo, @PathParam("id") String id) {
        log.info("Configuration requested: " + getRequestUriWithoutQuery());
        Grafeo g = new GrafeoImpl();
        try {
	        g.readFromEndpoint(NS.ENDPOINT_SELECT, getRequestUriWithoutQuery());
        } catch (RuntimeException e) {
        	return throwServiceError(getRequestUriWithoutQuery().toString(), ErrorMsg.NOT_FOUND, 404);
        }
        return getResponse(g);
    }
    
    @GET
    @Path("{id}/assignment/{assId}")
    public Response getConfigAssignment(
    		@Context UriInfo uriInfo,
     		@PathParam("id") String id,
     		@PathParam("assId") String assId
    		) {
        log.info("Assignment " + assId + " of configuration requested: " + uriInfo.getRequestUri());
        URI uri = popPath();
        uri = popPath();

        return Response.status(303).location(uri).build();
    }
    
    @GET
    @Path("list")
    public Response getConfigList(@Context UriInfo uriInfo) {
        Grafeo g = new GrafeoImpl();
        g.readTriplesFromEndpoint(NS.ENDPOINT_SELECT, null, NS.RDF.PROP_TYPE, g.resource(NS.OMNOM.CLASS_WEBSERVICE_CONFIG));
        g.readTriplesFromEndpoint(NS.ENDPOINT_SELECT, null, NS.RDF.PROP_TYPE, g.resource(NS.OMNOM.CLASS_WORKFLOW_CONFIG));
        return getResponse(g);
    }

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
        	log.severe(ErrorMsg.BAD_RDF.toString());
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
        log.warning("Skolemnizing assignments ...");
        g.skolemizeSequential(uri.toString(), NS.OMNOM.PROP_ASSIGNMENT, "assignment");
        log.warning("DONE Skolemnizing ...");
        
        g.emptyGraph(Config.ENDPOINT_UPDATE, uri.toString());
        g.writeToEndpoint(Config.ENDPOINT_UPDATE, uri.toString());
        return Response.created(uri).entity(getResponseEntity(g)).build();
    }

	@POST
    @Consumes(MediaType.WILDCARD)
    public Response postConfig(File input) {
        log.info("POST config.");
//        // TODO BUG this fails if newlines aren't correctly transmitted due to line-wide comments in turtle
        
    	URI uri = appendPath(createUniqueStr());
    	log.info("Posting the config to " + uri.toString());
        Grafeo g;
        try {
        	g = new GrafeoImpl(input);
        } catch (Throwable t) {
        	log.severe(ErrorMsg.BAD_RDF.toString());
        	return throwServiceError(ErrorMsg.BAD_RDF, t);
        }
        log.fine("Parsed config RDF.");
        GResource res = g.findTopBlank(NS.OMNOM.CLASS_WORKFLOW_CONFIG);
        if (null == res)  {
        	res = g.findTopBlank(NS.OMNOM.CLASS_WEBSERVICE_CONFIG);
        	if (null == res)
	        	return throwServiceError(ErrorMsg.NO_TOP_BLANK_NODE + ": " + g.getTurtle());
        }
        res.rename(uri);
        log.info("Renamed top blank node.");
        	g.skolemizeSequential(uri.toString(), NS.OMNOM.PROP_ASSIGNMENT, "assignment");
//        }
        log.info("Skolemnized assignments");
        
        g.emptyGraph(Config.ENDPOINT_UPDATE, uri.toString());
        log.info("Emptied graph");
        g.writeToEndpoint(Config.ENDPOINT_UPDATE, uri);
        log.info("Written data to endpoint.");
        return Response.created(uri).entity(getResponseEntity(g)).build();
    }

	/**
	 * @param g
	 * @param res
	 */
	@Path("{id}/validate")
	public Response getValidation() {
		log.info("Validating");
        WebserviceConfigPojo confPojo = new WebserviceConfigPojo();
		confPojo.loadFromURI(getRequestUriWithoutQuery());
        try {
			confPojo.validate();
		} catch (Exception e) {
			return throwServiceError(e);
		}
        return Response.ok().build();
	}

    

}
