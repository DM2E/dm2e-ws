package eu.dm2e.ws.grafeo.jena;

import com.hp.hpl.jena.rdf.model.RDFNode;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.GValue;
import eu.dm2e.ws.grafeo.Grafeo;

import javax.xml.bind.DatatypeConverter;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/4/13
 * Time: 12:42 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class GValueImpl extends JenaImpl implements GValue {
    protected RDFNode value;
    protected Grafeo grafeo;
    private Logger log = Logger.getLogger(getClass().getName());

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
    public String toString() {
        if (value.isLiteral()) return value.asLiteral().getLexicalForm();
        return value.asResource().getURI();
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
    public <T> T getTypedValue(Class T) {
        T result = null;
        if (isLiteral()) {
            String toParse = value.asLiteral().getLexicalForm();
            if (T.equals(long.class) || T.equals(Long.class)) {
                log.info("Found long.");
                result = (T) new Long(Long.parseLong(toParse));
            } else if (T.equals(int.class) || T.equals(Integer.class)) {
                log.info("Found int.");
                result = (T) new Integer(Integer.parseInt(toParse));
            } else if (T.equals(float.class) || T.equals(Float.class)) {
                log.info("Found float.");
                result = (T) new Float(Float.parseFloat(toParse));
            } else if (T.equals(double.class) || T.equals(Double.class)) {
                log.info("Found double.");
                result = (T) new Double(Double.parseDouble(toParse));
            } else if (T.equals(boolean.class) || T.equals(Boolean.class)) {
                log.info("Found boolean.");
                result = (T) new Boolean(Boolean.parseBoolean(toParse));
            } else if (T.equals(String.class)) {
                log.info("Found String.");
                result = (T) toParse;
            } else if (T.equals(Date.class)) {
                log.info("Found Date.");
                result = (T) DatatypeConverter.parseDateTime(toParse).getTime();
            } else if (T.equals(Calendar.class)) {
                log.info("Found Calendar.");
                result = (T) DatatypeConverter.parseDateTime(toParse);
            }
        } else {
            log.info("Found Resource.");
            result = grafeo.getObject(T, resource());
        }
        return result;
    }

}
