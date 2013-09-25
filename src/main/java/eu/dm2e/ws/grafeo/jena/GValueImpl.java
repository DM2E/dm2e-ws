package eu.dm2e.ws.grafeo.jena;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.RDFNode;

import eu.dm2e.ws.grafeo.GLiteral;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.GValue;
import eu.dm2e.ws.grafeo.Grafeo;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */
public abstract class GValueImpl extends JenaImpl implements GValue {
    protected RDFNode value;
    protected Grafeo grafeo;
    private Logger log = LoggerFactory.getLogger(getClass().getName());

    protected GValueImpl() {
    }

    public GValueImpl(Grafeo grafeo, RDFNode value) {
        this.value = value;
        this.grafeo = grafeo;

    }

    @Override
    public boolean isLiteral() {
        return value.isLiteral();
    }

    @Override
    public GResource resource() {
        return (GResource) this;
    }

    @Override
    public GLiteral literal() {
        return (GLiteral) this;
    }

    @Override
    public String toString() {
        if (value.isLiteral()) {
        	return value.asLiteral().getLexicalForm();
        } else if (value.isURIResource()) { 
	        return value.asResource().getURI();
        } else {
        	return value.asResource().getId().toString();
        }
    }

    @Override
    public String toEscapedString() {
        if (value.isLiteral()) return grafeo.escapeLiteral(value.toString());
        return grafeo.escapeResource(value.toString());
    }

    @Override
    public GValue get(String uri) {
        return resource().get(uri);
    }
    
    @Override
    public Set<GValue> getAll(String uri) {
        return resource().getAll(uri);
    }

    @Override
    public <T> T getTypedValue(Class T) {
        T result = null;
        if (isLiteral()) {
            String toParse = value.asLiteral().getLexicalForm();
            if (T.equals(long.class) || T.equals(Long.class)) {
                log.trace("Found long.");
                result = (T) new Long(Long.parseLong(toParse));
            } else if (T.equals(int.class) || T.equals(Integer.class)) {
                log.trace("Found int.");
                result = (T) new Integer(Integer.parseInt(toParse));
            } else if (T.equals(float.class) || T.equals(Float.class)) {
                log.trace("Found float.");
                result = (T) new Float(Float.parseFloat(toParse));
            } else if (T.equals(double.class) || T.equals(Double.class)) {
                log.trace("Found double.");
                result = (T) new Double(Double.parseDouble(toParse));
            } else if (T.equals(boolean.class) || T.equals(Boolean.class)) {
                log.trace("Found boolean.");
                result = (T) Boolean.valueOf(Boolean.parseBoolean(toParse));
            } else if (T.equals(String.class)) {
                log.trace("Found String.");
                result = (T) toParse;
            } else if (T.equals(java.util.Date.class)) {
                log.trace("Found Date.");
                result = (T) DatatypeConverter.parseDateTime(toParse).getTime();
            } else if (T.equals(org.joda.time.DateTime.class)) {
                log.trace("Found org.joda.time.DateTime.");
                result = (T) DateTime.parse(toParse);
            } else if (T.equals(Calendar.class)) {
                log.trace("Found Calendar.");
                result = (T) DatatypeConverter.parseDateTime(toParse);
            } else if (T.equals(URI.class)) {
                log.trace("Found URI.");
                try {
					result = (T) new URI(toParse);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
            }
        } else {
            log.trace("Found Resource.");
            result = (T) grafeo.getObjectMapper().getObject(T, resource());
        }
        return result;
    }

	public RDFNode getJenaRDFNode() {
		return value;
	}

}
