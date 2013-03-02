package eu.dm2e.ws.grafeo;

import com.hp.hpl.jena.rdf.model.Resource;

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

    public Resource getResource() {
        return resource;
    }

    public String getUri() {

        return uri;
    }

    public Grafeo getGrafeo() {

        return grafeo;
    }
}
