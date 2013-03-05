package eu.dm2e.ws.services.data;

import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.logging.Logger;

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
        g.readFromEndpoint("http://lelystad.informatik.uni-mannheim.de:8080/openrdf-sesame/repositories/dm2etest", uri);
        return getResponse(g);
    }

    @GET
    public Response getList(@Context UriInfo uriInfo) {
        Grafeo g = new GrafeoImpl();
        g.readTriplesFromEndpoint("http://lelystad.informatik.uni-mannheim.de:8080/openrdf-sesame/repositories/dm2etest", null, "rdf:type", g.resource("void:Dataset"));
        return getResponse(g);
    }
    @POST
    @Consumes("*/*")
    public Response publishFromURI(String uri) {
        Grafeo g = new GrafeoImpl(uri);
        String graphUri = "http://data.dm2e.eu/datasets/fromWS/" + new Date().getTime();
        g.addTriple(graphUri, "rdf:type", "void:Dataset");
        g.addTriple(graphUri, "dct:created", g.now());

        g.writeToEndpoint("http://lelystad.informatik.uni-mannheim.de:8080/openrdf-sesame/repositories/dm2etest/statements", graphUri);
        return getResponse(g);
    }
}
