package eu.dm2e.ws.grafeo.jena;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.hp.hpl.jena.rdf.model.Literal;

import eu.dm2e.ws.grafeo.GLiteral;
import eu.dm2e.ws.grafeo.Grafeo;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/2/13
 * Time: 4:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class GLiteralImpl extends GValueImpl implements GLiteral {
    private Literal literal;

    public GLiteralImpl(Grafeo grafeo, String literalValue)  {
        if (grafeo.isEscaped(literalValue)) {
            grafeo.unescapeLiteral(literalValue);
        }
        this.literal = getGrafeoImpl(grafeo).getModel().createLiteral(literalValue);
        this.grafeo = grafeo;
        this.value = this.literal;
    }

    public GLiteralImpl(Grafeo grafeo, Object literalValue)  {
    	if (literalValue.getClass().equals(Date.class)) {
    		TimeZone tz = TimeZone.getTimeZone("UTC");
    		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    		df.setTimeZone(tz);
    		String nowAsISO = df.format(literalValue);
    		this.literal = getGrafeoImpl(grafeo).getModel().createTypedLiteral(nowAsISO, "http://www.w3.org/2001/XMLSchema#dateTime");
    	}
    	else {
	        this.literal = getGrafeoImpl(grafeo).getModel().createTypedLiteral(literalValue);
    	}
        this.grafeo = grafeo;
        this.value = this.literal;
    }

    public GLiteralImpl(Grafeo grafeo, Literal literal)  {
        this.literal = literal;
        this.grafeo = grafeo;
        this.value = this.literal;
    }

    @Override
    public String getValue() {
        return literal.getLexicalForm();
    }

    public Literal getLiteral() {
        return literal;
    }

    @Override
    public Grafeo getGrafeo() {
        return grafeo;
    }
}
