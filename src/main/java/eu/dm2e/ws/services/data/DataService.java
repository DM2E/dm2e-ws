
package eu.dm2e.ws.services.data;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


@Path("/data")
public class DataService extends AbstractRDFService {

    @GET
    @Path("/bla/{aha}")
    @Produces({PLAIN, XML, TTL_A, TTL_T, N3})
    public Response getURI1(@Context UriInfo uriInfo) {
        Model m = ModelFactory.createDefaultModel();
        m.setNsPrefix("dct","http://purl.org/dc/terms/");
        Resource s = m.createResource(uriInfo.getRequestUri().toString());
        Property p = m.createProperty(m.expandPrefix("http://purl.org/dc/terms/creator"));
        Resource o = m.createResource("http://localhost/kai");
        m.add(m.createStatement(s, p, o));

        return handleRDF(m);
    }


}