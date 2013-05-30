package eu.dm2e.ws.services.data;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.services.AbstractRDFService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.logging.Logger;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */
@Path("/published")
public class PublishService extends AbstractRDFService {
    private Logger log = Logger.getLogger(getClass().getName());
    
	@Override
	public WebservicePojo getWebServicePojo() {
		WebservicePojo ws = new WebservicePojo();
		ws.setId("http://localhost:9998/published");
		return ws;
	}
    
    @GET
    @Path("/byURI")
    public Response getURI(@QueryParam("uri") String uri) {
        try {
            uri= URLDecoder.decode(uri, "utf8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
        log.info("URI: " + uri);
        Grafeo g = new GrafeoImpl();
        g.readFromEndpoint(NS.ENDPOINT, uri);
        return getResponse(g);
    }

    @GET
    @Path("datasets")
    public Response getList(@Context UriInfo uriInfo) {
        Grafeo g = new GrafeoImpl();
        g.readTriplesFromEndpoint(NS.ENDPOINT, null, "rdf:type", g.resource("void:Dataset"));
        return getResponse(g);
    }
    @POST
    @Consumes("*/*")
    public Response publishFromURI(String uri) {
        Grafeo g = new GrafeoImpl(uri);
        String graphUri = "http://data.dm2e.eu/datasets/fromWS/" + new Date().getTime();
        g.addTriple(graphUri, "rdf:type", "void:Dataset");
        g.addTriple(graphUri, "dct:created", g.now());

        g.writeToEndpoint(NS.ENDPOINT_STATEMENTS, graphUri);
        return getResponse(g);
    }
}
