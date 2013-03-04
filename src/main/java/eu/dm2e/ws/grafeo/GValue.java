package eu.dm2e.ws.grafeo;

import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/4/13
 * Time: 12:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class GValue {
    private RDFNode value;
    private Grafeo grafeo;

    public GValue(Grafeo grafeo, RDFNode value) {
        this.value = value;
        this.grafeo = grafeo;

    }

    @Override
    public String toString() {
        return value.toString();
    }

    public GValue get(String uri) {
        return new GResource(grafeo, value.asResource()).get(uri);
    }
}
