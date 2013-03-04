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
    protected RDFNode value;
    protected Grafeo grafeo;

    protected GValue() {
    }

    public GValue(Grafeo grafeo, RDFNode value) {
        this.value = value;
        this.grafeo = grafeo;

    }

    @Override
    public String toString() {
        if (value.isLiteral()) return grafeo.escapeLiteral(value.toString());
        return grafeo.escapeResource(value.toString());
    }

    public GValue get(String uri) {
        return new GResource(grafeo, value.asResource()).get(uri);
    }
}
