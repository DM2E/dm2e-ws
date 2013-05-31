
package eu.dm2e.ws.services.data;


import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.ParameterPojo;
import eu.dm2e.ws.api.WebservicePojo;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.services.AbstractRDFService;

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

/**
 * Examples and tests of several webservices...
 */
@Path("/data")
public class DataService extends AbstractRDFService {
    private Logger log = Logger.getLogger(getClass().getName());

    public DataService() {
        ParameterPojo p1 = new ParameterPojo();
        p1.setWebservice(getWebServicePojo());
        p1.setId("http://localhost:9998/data/params/1");
        getWebServicePojo().getInputParams().add(p1);
    }

    @GET
    @Path("/mapTest")
    public Response getMapTest(@Context UriInfo uriInfo) {
        String source = uriInfo.getRequestUri().toString().replace("/mapTest","");
        Grafeo g = new GrafeoImpl(source);
        WebservicePojo test = g.getObjectMapper().getObject(WebservicePojo.class, g.resource("http://data.dm2e.eu/data/services/42"));

        log.info("Result ID: " + test.getId());


        return getResponse(g);
    }


    @GET
    @Path("/{resourceID}")
    public Response getResource(@Context UriInfo uriInfo, @PathParam("resourceID") String resourceID) {
        Grafeo g = new GrafeoImpl();
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
        Grafeo g = new GrafeoImpl();
        g.load(uri);
        log.info("Label: " + g.get(uri).get("rdfs:label"));
        return getResponse(g);
    }




}
