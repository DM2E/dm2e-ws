package eu.dm2e.ws.grafeo;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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

    public Grafeo(String uri) {
        this(ModelFactory.createDefaultModel());
        this.load(uri);
    }

    public Grafeo(InputStream input, String lang) {
        this(ModelFactory.createDefaultModel());
        this.model.read(input, null, lang);
    }

    public Grafeo(File input) {
        this(ModelFactory.createDefaultModel());
        try {
            this.model.read(new FileInputStream(input), null, "N3");
        } catch (Throwable t) {
            try {
                this.model.read(new FileInputStream(input), null, "RDF/XML");
            } catch (Throwable t2) {
                // TODO Throw proper exception that is converted to a proper HTTP response in DataService
                throw new RuntimeException("Could not parse input: " + input, t2);
            }
        }
    }

    public GResource findTopBlank() {
        ResIterator it = model.listSubjects();
        Resource fallback = null;
        while (it.hasNext()) {
            Resource res = it.next();
            if (res.isAnon()) {
                fallback = res;
                if (model.listStatements(null,null,res).hasNext()) continue;
                return new GResource(this, res);
            }
        }
        return fallback!=null?new GResource(this, fallback):null;
    }

    public Grafeo(Model model) {
        this.model = model;
        initDefaultNamespaces();
        applyNamespaces(model);
    }

    public void load(String uri) {
        uri = expand(uri);
        this.model.read(uri);
    }

    public GResource get(String uri) {
        uri = expand(uri);
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
        namespaces.put("skos", "http://www.w3.org/2004/02/skos/core#");
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
