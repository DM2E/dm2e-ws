package eu.dm2e.ws.grafeo;

import com.hp.hpl.jena.rdf.model.Literal;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/2/13
 * Time: 4:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class GLiteral extends GValue {
    private Literal literal;

    public GLiteral(Grafeo grafeo, String value)  {
        if (grafeo.isEscaped(value)) {
            grafeo.unescapeLiteral(value);
        }
        this.literal = grafeo.getModel().createLiteral(value);
        this.grafeo = grafeo;
        this.value = this.literal;
    }

    public String getValue() {
        return literal.getString();
    }

    public Grafeo getGrafeo() {
        return grafeo;
    }
}
