
package eu.dm2e.ws.services.data;


import eu.dm2e.ws.grafeo.Grafeo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


@Path("/data")
public class DataService extends AbstractRDFService {

    @GET
    public Response get() {
        Grafeo g = new Grafeo();
        g.addTriple("http://localhost/data", "dct:creator", "http://localhost/kai");
        g.addTriple("http://localhost/data", "rdfs:comment", "foaf: darf hier nicht verwendet werden");
        return getResponse(g);
    }


    @GET
    @Path("/bla/{aha}")
    public Response getURI1(@Context UriInfo uriInfo) {
        Grafeo g = new Grafeo();
        g.addTriple(uriInfo.getRequestUri().toString(), "http://purl.org/dc/terms/creator", "http://localhost/kai");
        return getResponse(g);
    }


}