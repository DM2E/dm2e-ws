package eu.dm2e.ws.grafeo.jena;

import com.hp.hpl.jena.rdf.model.RDFNode;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.GValue;
import eu.dm2e.ws.grafeo.Grafeo;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/4/13
 * Time: 12:42 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class GValueImpl extends JenaImpl implements GValue  {
    protected RDFNode value;
    protected Grafeo grafeo;

    protected GValueImpl() {
    }

    public GValueImpl(Grafeo grafeo, RDFNode value) {
        this.value = value;
        this.grafeo = grafeo;

    }

    @Override
    public GResource resource() {
        return (GResource) this;
    }

    @Override
    public String toString() {
        if (value.isLiteral()) return grafeo.escapeLiteral(value.toString());
        return grafeo.escapeResource(value.toString());
    }

    @Override
    public GValue get(String uri) {
        return resource().get(uri);
    }
}
