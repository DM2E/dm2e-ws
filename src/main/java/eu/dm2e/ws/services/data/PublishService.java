package eu.dm2e.ws.services.data;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import eu.dm2e.ws.NS;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/5/13
 * Time: 3:47 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/published")
public class PublishService extends AbstractRDFService {
    Logger log = Logger.getLogger(getClass().getName());
    
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
