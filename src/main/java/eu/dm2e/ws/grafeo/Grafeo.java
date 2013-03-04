package eu.dm2e.ws.grafeo;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/2/13
 * Time: 2:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class Grafeo {
    private Logger log = Logger.getLogger(getClass().getName());
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

    public GResource resource(String uri) {
        uri = expand(uri);
        return new GResource(this, uri);
    }

    public boolean isEscaped(String input) {
        return input.startsWith("\"") && input.endsWith("\"") && input.length() > 1
                ||
                input.startsWith("<") && input.endsWith(">") && input.length() > 1;
    }

    public String unescapeLiteral(String literal) {
        if (isEscaped(literal)) {
            return literal.substring(1, literal.length() - 1);
        }
        return literal;
    }

    public String escapeLiteral(String literal) {
        return new StringBuilder("\"").append(literal).append("\"").toString();
    }

    public String unescapeResource(String uri) {
        if (isEscaped(uri)) {
            return uri.substring(1, uri.length() - 1);
        }
        return uri;
    }

    public String escapeResource(String uri) {
        if (isEscaped(uri)) return uri;
        return new StringBuilder("<").append(uri).append(">").toString();
    }

    public void readFromEndpoint(String endpoint, String graph) {
        StringBuilder sb = new StringBuilder("CONSTRUCT {?s ?p ?o}  WHERE { GRAPH <");
        sb.append(graph);
        sb.append("> {");
        sb.append("?s ?p ?o");
        sb.append("} . }");
        Query query = QueryFactory.create(sb.toString());
        log.info("Query: " + sb.toString());
        QueryExecution exec = QueryExecutionFactory.createServiceRequest(endpoint, query);
        exec.execConstruct(model);
    }

    public void readTriplesFromEndpoint(String endpoint, String subject, String predicate, GValue object) {

        if (subject!=null) subject = escapeResource(expand(subject));
        if (predicate!=null) predicate = escapeResource(expand(predicate));

        StringBuilder sb = new StringBuilder("CONSTRUCT {");
        sb.append(subject!=null?subject:"?s").append(" ");
        sb.append(predicate!=null?predicate:"?p").append(" ");
        sb.append(object!=null?object.toString():"?p").append(" ");
        sb.append("}  WHERE { ");
        sb.append(subject!=null?subject:"?s").append(" ");
        sb.append(predicate!=null?predicate:"?p").append(" ");
        sb.append(object!=null?object.toString():"?p").append(" ");
        sb.append(" }");
        Query query = QueryFactory.create(sb.toString());
        log.info("Query: " + sb.toString());
        QueryExecution exec = QueryExecutionFactory.createServiceRequest(endpoint, query);
        exec.execConstruct(model);
    }


    public void writeToEndpoint(String endpoint, String graph) {
        StringBuilder sb = new StringBuilder("CREATE SILENT GRAPH <");
        sb.append(graph);
        sb.append(">");
        log.info("Query 1: " + sb.toString());
        UpdateRequest update = UpdateFactory.create(sb.toString());
        sb = new StringBuilder("INSERT DATA { GRAPH <");
        sb.append(graph);
        sb.append("> {");
        sb.append(getNTriples());
        sb.append("}}");
        log.info("Query 2: " + sb.toString());
        update.add(sb.toString());
        UpdateProcessor exec = UpdateExecutionFactory.createRemoteForm(update, endpoint);
        exec.execute();

    }


    public String getNTriples() {
        StringWriter sw = new StringWriter();
        model.write(sw, "N-TRIPLE");
        return sw.toString();
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
