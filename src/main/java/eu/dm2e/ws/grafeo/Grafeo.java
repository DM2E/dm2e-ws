package eu.dm2e.ws.grafeo;

import com.hp.hpl.jena.rdf.model.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/2/13
 * Time: 2:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class Grafeo {

    protected Model model;
    protected Map<String, String> namespaces = new HashMap<String, String>();


    public Grafeo() {
        this(ModelFactory.createDefaultModel());

    }

    public Grafeo(Model model) {
        this.model = model;
        initDefaultNamespaces();
        applyNamespaces(model);
    }

    public GResource get(String uri) {
        return new GResource(this, uri);
    }

    public String expand(String uri) {
        return model.expandPrefix(uri);
    }


    public GStatement addTriple(String subject, String predicate, String object) {
        GResource s = new GResource(this, subject);
        GResource p = new GResource(this, predicate);

        GStatement statement;
        String objectExp = expand(object);
        try {
            URI testUri = new URI(objectExp);
            GResource or = new GResource(this, object);
            statement = new GStatement(this, s, p, or);
        } catch (URISyntaxException e) {
            statement = new GStatement(this, s, p, object);
        }
        model.add(statement.getStatement());
        return statement;
    }
    public GStatement addTriple(String subject, String predicate, GLiteral object) {
        GResource s = new GResource(this, subject);
        GResource p = new GResource(this, predicate);
        GStatement statement = new GStatement(this, s, p, object);
        model.add(statement.getStatement());
        return statement;
    }

    public GLiteral literal(String literal) {
        return new GLiteral(this, literal);
    }

    public boolean isEscaped(String input) {
        return input.startsWith("\"") && input.endsWith("\"") && input.length() > 1;
    }

    public String unescape(String literal) {
        if (isEscaped(literal)) {
            return literal.substring(1, literal.length() - 1);
        }
        return literal;
    }

    public String escape(String literal) {
        return new StringBuilder("\"").append(literal).append("\"").toString();
    }

    protected void applyNamespaces(Model model) {
        for (String prefix : namespaces.keySet()) {
            model.setNsPrefix(prefix, namespaces.get(prefix));
        }
    }

    public Model getModel() {
        return model;

    }

    protected void initDefaultNamespaces() {
        // TODO: Put this in a config file (kai)
        namespaces.put("foaf", "http://xmlns.com/foaf/0.1/");
        namespaces.put("dct", "http://purl.org/dc/terms/");
        namespaces.put("dcterms", "http://purl.org/dc/terms/");
        namespaces.put("dc", "http://purl.org/dc/elements/1.1/");
        namespaces.put("dc", "http://www.w3.org/2004/02/skos/core#");
        namespaces.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        namespaces.put("owl", "http://www.w3.org/2002/07/owl#");
        namespaces.put("ogp", "http://ogp.me/ns#");
        namespaces.put("gr", "http://purl.org/goodrelations/v1#");
        namespaces.put("xsd", "http://www.w3.org/2001/XMLSchema#");
        namespaces.put("cc", "http://creativecommons.org/ns#");
        namespaces.put("bibo", "http://purl.org/ontology/bibo/");
        namespaces.put("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
        namespaces.put("sioc", "http://rdfs.org/sioc/ns#");
        namespaces.put("oo", "http://purl.org/openorg/");
    }
}
