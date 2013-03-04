
package eu.dm2e.ws.services.data;


import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.Grafeo;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Date;
import java.util.logging.Logger;


@Path("/data")
public class DataService extends AbstractRDFService {
    Logger log = Logger.getLogger(getClass().getName());

    @GET
    public Response get() {
        Grafeo g = new Grafeo();
        g.addTriple("http://localhost/data", "dct:creator", "http://localhost/kai");
        g.addTriple("http://localhost/data", "rdfs:comment", "foaf: darf hier nicht verwendet werden");
        return getResponse(g);
    }


    @GET
    @Path("/{resourceID}")
    public Response getResource(@Context UriInfo uriInfo, @PathParam("resourceID") String resourceID) {
        Grafeo g = new Grafeo();
        g.addTriple(uriInfo.getRequestUri().toString(), "http://purl.org/dc/terms/creator", "http://localhost/kai");
        g.addTriple(uriInfo.getRequestUri().toString(), "dct:identifier", g.literal(resourceID));
        return getResponse(g);
    }

    @GET
    @Path("/byURI")
    public Response getURI(@QueryParam("uri") String uri) {
        try {
            uri= URLDecoder.decode(uri,"utf8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
        log.info("URI: " + uri);
        Grafeo g = new Grafeo();
        g.load(uri);
        log.info("Label: " + g.get(uri).get("rdfs:label"));
        return getResponse(g);
    }

    @GET
    @Path("configurations/{id}")
    public Response getConfig(@Context UriInfo uriInfo, @PathParam("id") String id) {
        Grafeo g = new Grafeo();
        g.readFromEndpoint("http://lelystad.informatik.uni-mannheim.de:8080/openrdf-sesame/repositories/dm2etest", uriInfo.getRequestUri().toString());
        return getResponse(g);
    }

    @GET
    @Path("configurations")
    public Response getConfig(@Context UriInfo uriInfo) {
        Grafeo g = new Grafeo();
        g.readTriplesFromEndpoint("http://lelystad.informatik.uni-mannheim.de:8080/openrdf-sesame/repositories/dm2etest", null, "rdf:type", g.resource("http://example.org/classes/Configuration"));
        return getResponse(g);
    }


    @POST
    @Path("configurations")
    @Consumes(MediaType.WILDCARD)
    public Response postConfig(@Context UriInfo uriInfo, File input) {
        log.info("Config posted.");
        // TODO use Exception to return proper HTTP response if input can not be parsed as RDF
        Grafeo g = new Grafeo(input);
        GResource blank = g.findTopBlank();
        String uri = uriInfo.getRequestUri() + "/" + new Date().getTime();
        if (blank!=null) blank.rename(uri);
        g.addTriple(uri,"rdf:type","http://example.org/classes/Configuration");
        g.writeToEndpoint("http://lelystad.informatik.uni-mannheim.de:8080/openrdf-sesame/repositories/dm2etest/statements", uri);
        return Response.created(URI.create(uri)).entity(getResponseEntity(g)).build();
    }

}