package eu.dm2e.ws.services.data;


import com.hp.hpl.jena.rdf.model.Model;

import javax.ws.rs.GET;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractRDFService {

    List<Variant> supportedVariants;
    @Context
    Request request;

    protected AbstractRDFService() {
        this.supportedVariants = Variant.mediaTypes(
                MediaType.valueOf("text/plain"),
                MediaType.valueOf("application/rdf+xml"),
                MediaType.valueOf("application/x-turtle"),
                MediaType.valueOf("text/turtle"),
                MediaType.valueOf("text/rdf+n3")
        ).add().build();
    }

    abstract protected Model getRDF();


    @GET
    public Response get() {
        Variant selectedVariant = request.selectVariant(supportedVariants);

        if (selectedVariant != null) {
            return Response.ok(new RDFOutput(getRDF(), selectedVariant.getMediaType()), selectedVariant.getMediaType()).build();
        } else {
            return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
        }

    }

    class RDFOutput implements StreamingOutput {
        Logger log = Logger.getLogger(getClass().getName());
        Model model;
        MediaType mediaType;

        public RDFOutput(Model model, MediaType mediaType) {
            this.model = model;
            this.mediaType = mediaType;
        }

        @Override
        public void write(OutputStream output) throws IOException, WebApplicationException {
            String lang = null;
            String mt = this.mediaType.getType() + "/" + mediaType.getSubtype();
            log.info("Mediatype: " + mt);
            if (mt.equals("application/rdf+xml")) {
                lang = "RDF/XML";
            } else if (mt.contains("turtle")) {
                lang = "TURTLE";
            } else if (mt.contains("n3")) {
                lang = "N3";
            } else {
                lang = "N-TRIPLE";
            }
            model.setNsPrefix("dct","http://purl.org/dc/terms/");
            model.write(output, lang);
        }
    }

}