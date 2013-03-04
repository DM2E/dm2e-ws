package eu.dm2e.ws.grafeo;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.ResourceUtils;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/2/13
 * Time: 2:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class GResource {
    private Grafeo grafeo;
    private String uri;
    private Resource resource;

    public GResource(Grafeo grafeo, String uri) {
        this.grafeo = grafeo;
        this.uri = grafeo.expand(uri);
        this.resource = grafeo.model.createResource(this.uri);

    }

    public GResource(Grafeo grafeo, Resource resource) {
        this.grafeo = grafeo;
        this.resource = resource;
    }

    public void rename(String uri) {
        this.resource = ResourceUtils.renameResource(resource, uri);
    }

    public Resource getResource() {
        return resource;
    }

    public String getUri() {

        return uri;
    }

    public GValue get(String uri) {
        uri = grafeo.expand(uri);
        Statement st = resource.getProperty(grafeo.model.createProperty(uri));
        if (st==null) return null;
        RDFNode value = st.getObject();
        return new GValue(grafeo, value);
    }

    public Grafeo getGrafeo() {

        return grafeo;
    }
}
