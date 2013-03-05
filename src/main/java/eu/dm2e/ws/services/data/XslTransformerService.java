package eu.dm2e.ws.services.data;

/*
 * GET /xslt?resource=URL
 * 		
 * POST /xslt
 * PUT /xslt
 * 		Body contains URL of a transformation specification which must be dereferenceable
 */

import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/xslt")
public class XslTransformerService extends AbstractRDFService {

    @PUT
    @Consumes(MediaType.WILDCARD)
    public Response post(String body) {

        Logger log = Logger.getLogger(getClass().getName());
        String xslUrl = null;
        String xmlUrl = null;
        log.info("Config URL: " + body);
        Grafeo g = new GrafeoImpl(body);
        log.info("Config content: " + g.getNTriples());
        xmlUrl = g.get(body).get("http://onto.dm2e.eu/xmlSource").resource().getUri();
        xslUrl = g.get(body).get("http://onto.dm2e.eu/xslStyleSheet").resource().getUri();

        log.info("XML URL: " + xmlUrl);
        log.info("XSL URL: " + xslUrl);

        if (null == xslUrl || null == xmlUrl) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Error in configuration.").build();
        }
        TransformerFactory tFactory = TransformerFactory.newInstance();
        try {

            StreamSource xmlSource = new StreamSource(new URL(xmlUrl).openStream());
            StreamSource xslSource = new StreamSource(new URL(xslUrl).openStream());
            Transformer transformer = tFactory.newTransformer(xslSource);

            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            StreamResult xslResult = new StreamResult(outStream);

            transformer.transform(xmlSource, xslResult);
            log.info("Output to write: " + outStream.toString());
            return Response.ok(outStream.toString()).build();
        } catch (Exception e) {
            log.severe("Error during XSLT transformation: " + e);
            return Response.serverError().entity(e).build();
        }

    }

    @GET
    public Response get() {
        Logger log = Logger.getLogger(getClass().getName());
        log.log(Level.INFO, "FOO");
        Grafeo g = new GrafeoImpl();

        InputStream sampleDataStream = this.getClass().getResourceAsStream("/sample_data.ttl");
        if (null == sampleDataStream) {
            log.severe("Couldn't open sample_data.ttl...");
            return Response.serverError().entity("Couldn't open sample_data.ttl...").build();
        }
        ((GrafeoImpl) g).getModel().read(sampleDataStream, null, "TURTLE");
        return getResponse(g);
    }

}
