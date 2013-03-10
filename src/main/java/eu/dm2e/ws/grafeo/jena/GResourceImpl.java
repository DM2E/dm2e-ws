package eu.dm2e.ws.grafeo.jena;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.ResourceUtils;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.GValue;
import eu.dm2e.ws.grafeo.Grafeo;

import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/2/13
 * Time: 2:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class GResourceImpl extends GValueImpl implements GResource {
    private Resource resource;
    private Logger log = Logger.getLogger(getClass().getName());

    public GResourceImpl(Grafeo grafeo, String uri) {
        this.grafeo = grafeo;
        uri = grafeo.expand(uri);
        this.resource = getGrafeoImpl(grafeo).model.createResource(uri);
        this.value = this.resource;
    }

    public GResourceImpl(Grafeo grafeo, Resource resource) {
        this.grafeo = grafeo;
        this.resource = resource;
        this.value = resource;

    }

    @Override
    public void rename(String uri) {
        this.resource = ResourceUtils.renameResource(resource, uri);
        this.value = this.resource;
    }

    public Resource getResource() {
        return resource;
    }

    @Override
    public String getUri() {

        return resource.getURI();
    }

    @Override
    public GValue get(String uri) {
        log.info("Check for property: " + uri);
        uri = grafeo.expand(uri);
        Statement st = resource.getProperty(getGrafeoImpl(grafeo).model.createProperty(uri));
        if (st==null) return null;
        RDFNode value = st.getObject();
        log.info("Found value: " + value.toString());
        if (value.isResource()) return new GResourceImpl(grafeo, value.asResource());
        if (value.isLiteral()) return new GLiteralImpl(grafeo, value.asLiteral());
        throw new RuntimeException("Not a literal or a resource value: " + getUri() + " -> " + uri);
    }

    @Override
    public Grafeo getGrafeo() {

        return grafeo;
    }
}