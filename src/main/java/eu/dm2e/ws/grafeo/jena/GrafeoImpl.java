package eu.dm2e.ws.grafeo.jena;

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
import eu.dm2e.ws.grafeo.GLiteral;
import eu.dm2e.ws.grafeo.GValue;
import eu.dm2e.ws.grafeo.Grafeo;

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
public class GrafeoImpl extends JenaImpl implements Grafeo {
    private Logger log = Logger.getLogger(getClass().getName());
    protected Model model;
    protected Map<String, String> namespaces = new HashMap<String, String>();


    public GrafeoImpl() {
        this(ModelFactory.createDefaultModel());
    }

    public GrafeoImpl(String uri) {
        this(ModelFactory.createDefaultModel());
        this.load(uri);
    }

    public GrafeoImpl(InputStream input, String lang) {
        this(ModelFactory.createDefaultModel());
        this.model.read(input, null, lang);
    }

    public GrafeoImpl(File input) {
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

    @Override
    public GResourceImpl findTopBlank() {
        ResIterator it = model.listSubjects();
        Resource fallback = null;
        while (it.hasNext()) {
            Resource res = it.next();
            if (res.isAnon()) {
                fallback = res;
                if (model.listStatements(null,null,res).hasNext()) continue;
                return new GResourceImpl(this, res);
            }
        }
        return fallback!=null?new GResourceImpl(this, fallback):null;
    }

    public GrafeoImpl(Model model) {
        this.model = model;
        initDefaultNamespaces();
        applyNamespaces(model);
    }

    @Override
    public void load(String uri) {
        log.fine("Load data from URI: " + uri);
        uri = expand(uri);
        try {
            this.model.read(uri, null, "N3");
            log.info("Content read, found N3.");
        } catch (Throwable t) {
            try {
                this.model.read(uri, null, "RDF/XML");
                log.info("Content read, found RDF/XML.");
            } catch (Throwable t2) {
                // TODO Throw proper exception that is converted to a proper HTTP response in DataService
                log.severe("Could not parse URI content: " + t2.getMessage());
                throw new RuntimeException("Could not parse uri content: " + uri, t2);
            }
        }

    }

    @Override
    public GResourceImpl get(String uri) {
        uri = expand(uri);
        return new GResourceImpl(this, uri);
    }

    @Override
    public String expand(String uri) {
        return model.expandPrefix(uri);
    }


    @Override
    public GStatementImpl addTriple(String subject, String predicate, String object) {
        GResourceImpl s = new GResourceImpl(this, subject);
        GResourceImpl p = new GResourceImpl(this, predicate);

        GStatementImpl statement;
        String objectExp = expand(object);
        try {
            URI testUri = new URI(objectExp);
            GResourceImpl or = new GResourceImpl(this, object);
            statement = new GStatementImpl(this, s, p, or);
        } catch (URISyntaxException e) {
            statement = new GStatementImpl(this, s, p, object);
        }
        model.add(statement.getStatement());
        return statement;
    }

    @Override
    public GStatementImpl addTriple(String subject, String predicate, GLiteral object) {
        GResourceImpl s = new GResourceImpl(this, subject);
        GResourceImpl p = new GResourceImpl(this, predicate);
        GStatementImpl statement = new GStatementImpl(this, s, p, object);
        model.add(statement.getStatement());
        return statement;
    }

    @Override
    public GLiteralImpl literal(String literal) {
        return new GLiteralImpl(this, literal);
    }

    @Override
    public GResourceImpl resource(String uri) {
        uri = expand(uri);
        return new GResourceImpl(this, uri);
    }

    @Override
    public boolean isEscaped(String input) {
        return input.startsWith("\"") && input.endsWith("\"") && input.length() > 1
                ||
                input.startsWith("<") && input.endsWith(">") && input.length() > 1;
    }

    @Override
    public String unescapeLiteral(String literal) {
        if (isEscaped(literal)) {
            return literal.substring(1, literal.length() - 1);
        }
        return literal;
    }

    @Override
    public String escapeLiteral(String literal) {
        return new StringBuilder("\"").append(literal).append("\"").toString();
    }

    @Override
    public String unescapeResource(String uri) {
        if (isEscaped(uri)) {
            return uri.substring(1, uri.length() - 1);
        }
        return uri;
    }

    @Override
    public String escapeResource(String uri) {
        if (isEscaped(uri)) return uri;
        return new StringBuilder("<").append(uri).append(">").toString();
    }

    @Override
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
        log.info("Reading from endpoint finished.");
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


    @Override
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


    @Override
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
