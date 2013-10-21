package eu.dm2e.grafeo.jena;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.hp.hpl.jena.rdf.model.Literal;

import eu.dm2e.grafeo.GLiteral;
import eu.dm2e.grafeo.Grafeo;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
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
    	} else if (literalValue.getClass().equals(org.joda.time.DateTime.class)) {
    		DateTime dt = (DateTime) literalValue;
    		DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
    		String nowAsISO = fmt.print(dt);
    		this.literal = getGrafeoImpl(grafeo).getModel().createTypedLiteral(nowAsISO, "http://www.w3.org/2001/XMLSchema#dateTime");
    	} else {
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
    
    @Override
    public String getTypedValue() {
    	
    	String datatypeURI = literal.getDatatypeURI();
    	if (null == datatypeURI) {
    		return "\"" + literal.getLexicalForm() + "\"";
    	}
    	else {
	        return "\"" + literal.getLexicalForm() + "\"^^<" + datatypeURI + ">";
    	}
    }

    public Literal getLiteral() {
        return literal;
    }

    @Override
    public Grafeo getGrafeo() {
        return grafeo;
    }
}
