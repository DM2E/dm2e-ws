package eu.dm2e.ws.services.config;

import java.io.File;
import java.net.URI;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import eu.dm2e.ws.NS;
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
        log.info("Configuration requested: " + uriInfo.getRequestUri());
        Grafeo g = new GrafeoImpl();
        // @TODO should proabably use getRequestUriWithoutQuery().toString() here
        g.readFromEndpoint(NS.ENDPOINT, uriInfo.getRequestUri().toString());
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

        return Response.seeOther(getRequestUriWithoutQuery()).build();
    }
    
    @GET
    @Path("list")
    public Response getConfig(@Context UriInfo uriInfo) {
        Grafeo g = new GrafeoImpl();
        g.readTriplesFromEndpoint(NS.ENDPOINT, null, "rdf:type", g.resource("http://example.org/classes/Configuration"));
        return getResponse(g);
    }


    @POST
    @Consumes(MediaType.WILDCARD)
    public Response postConfig(File input) {
        log.info("Config posted.");
        // TODO use Exception to return proper HTTP response if input can not be parsed as RDF
        // TODO BUG this fails if newlines aren't correctly transmitted
        log.severe(input.toString());
        Grafeo g;
        try {
        	g = new GrafeoImpl(input);
        } catch (Throwable t) {
        	log.severe("Could not parse input.");
        	return Response.status(400).entity("Bad RDF syntax.").build();
        }
        log.severe(g.getTurtle());
        GResource blank = g.findTopBlank("omnom:WebServiceConfig");
        if (blank == null) {
        	log.severe("Could not find a suitable top blank node.");
        	return Response.status(400).entity("No suitable top blank node.").build();
        }
        log.severe("Top blank node: "+blank);
        String uri = uriInfo.getRequestUri() + "/" + new Date().getTime();
        blank.rename(uri);
        g.skolemnizeSequential(uri, "omnom:assignment", "assignment");
//        g.addTriple(uri,"rdf:type","http://example.org/classes/Configuration");
        g.writeToEndpoint(NS.ENDPOINT_STATEMENTS , uri);
        return Response.created(URI.create(uri)).entity(getResponseEntity(g)).build();
    }
 
    

}
