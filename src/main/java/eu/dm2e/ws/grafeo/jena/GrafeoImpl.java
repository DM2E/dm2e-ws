package eu.dm2e.ws.grafeo.jena;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import eu.dm2e.ws.grafeo.GLiteral;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.GValue;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.annotations.Namespaces;
import eu.dm2e.ws.grafeo.annotations.RDFClass;
import eu.dm2e.ws.grafeo.annotations.RDFId;
import eu.dm2e.ws.grafeo.annotations.RDFProperty;
import org.apache.commons.beanutils.PropertyUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA. User: kai Date: 3/2/13 Time: 2:27 PM To change
 * this template use File | Settings | File Templates.
 */

public class GrafeoImpl extends JenaImpl implements Grafeo {

    private Logger log = Logger.getLogger(getClass().getName());
    protected Model model;
    protected Map<String, String> namespaces = new HashMap<String, String>();

    public static String SPARQL_CONSTRUCT_EVERYTHING = "CONSTRUCT { ?s ?p ?o } WHERE { { GRAPH ?g { ?s ?p ?o } } UNION { ?s ?p ?o } }";

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

    public GrafeoImpl(InputStream input) {
        this(ModelFactory.createDefaultModel());
        this.readHeuristically(input);
    }

    public GrafeoImpl(File file) {
        this(ModelFactory.createDefaultModel());
        this.readHeuristically(file);
    }

    /**
     * Creates a model from a given string and a content format. If the content
     * format is null the format is guessed.
     *
     * @param content       the content as string
     * @param contentFormat the format of the content. If null it will be guessed.
     */
    public GrafeoImpl(String content, String contentFormat) {
        this(ModelFactory.createDefaultModel());
        if (null == contentFormat) {
            this.readHeuristically(content);
        } else {
            try {
                this.model.read(content, null, contentFormat);
            } catch (Throwable t0) {
                throw new RuntimeException("Could not parse input: " + content
                        + " for given content format " + contentFormat, t0);
            }
        }
    }


    @Override
    public void setNamespace(String prefix, String namespace) {
        namespaces.put(prefix, namespace);
        model.setNsPrefix(prefix, namespace);
    }

    @Override
    public GResourceImpl findTopBlank() {
        ResIterator it = model.listSubjects();
        Resource fallback = null;
        while (it.hasNext()) {
            Resource res = it.next();
            if (res.isAnon()) {
                fallback = res;
                if (model.listStatements(null, null, res).hasNext())
                    continue;
                return new GResourceImpl(this, res);
            }
        }
        return fallback != null ? new GResourceImpl(this, fallback) : null;
    }

    public GrafeoImpl(Model model) {
        this.model = model;
        initDefaultNamespaces();
        applyNamespaces(model);
    }

    @Override
    public void readHeuristically(String contentStr) {
        InputStream content = new ByteArrayInputStream(contentStr.getBytes());
        readHeuristically(content);
    }

    @Override
    public void readHeuristically(InputStream input) {
        try {
            this.model.read(input, null, "N3");
        } catch (Throwable t) {
            try {
                this.model.read(input, null, "RDF/XML");
            } catch (Throwable t2) {
                // TODO Throw proper exception that is converted to a proper
                // HTTP response in DataService
                throw new RuntimeException("Could not parse input: " + input, t);
            }
        }
    }

    @Override
    public void readHeuristically(File file) {
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found:  " + file.getAbsolutePath(), e);
        }
        readHeuristically(fis);
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
                // TODO Throw proper exception that is converted to a proper
                // HTTP response in DataService
                log.severe("Could not parse URI content: " + t2.getMessage());
                throw new RuntimeException("Could not parse uri content: "
                        + uri, t2);
            }
        }

    }

    private Set<String> alreadyAdded = new HashSet<>();
    @Override
    public void addObject(Object object) {
        setAnnotatedNamespaces(object);
        String subject = getAnnotatedId(object);
        if (alreadyAdded.contains(subject)) return;
        alreadyAdded.add(subject);
        log.info("Subject: " + subject);
        String type = object.getClass().getAnnotation(RDFClass.class).value();
        log.info("Type: " + type);
        addTriple(subject, "rdf:type", type);
        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(RDFProperty.class)) {
                try {
                    try {
                        Object value = PropertyUtils.getProperty(object, field.getName());
                        GValue gv = null;
                        if (isAnnotatedObject(value)) {
                            addObject(value);
                            gv = resource(getAnnotatedId(value));
                        } else {
                            gv = literal(value);
                        }
                        String property = field.getAnnotation(RDFProperty.class).value();
                        log.info("Value for " + property + ": " + gv);
                        addTriple(subject, property, gv);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException("An exception occurred: " + e, e);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException("An exception occurred: " + e, e);
                    }

                } catch (IllegalAccessException e) {
                    throw new RuntimeException("An exception occurred: " + e, e);
                }
            }
        }
    }

    private Map<String,Object> objectCache = new HashMap<String, Object>();
    @Override
    public <T> T getObject(Class T, String uri) {
        log.info("Creating object for URI: " + uri);
        if (objectCache.containsKey(expand(uri))) {
            log.info("Cache hit!");
            return (T) objectCache.get(uri);
        }
        T result;
        GResource res = get(uri);
        try {
            result = (T) T.newInstance();
            objectCache.put(res.getUri(), result);
            log.info("Added to cache, uri: " + res.getUri());
        } catch (InstantiationException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("An exception occurred: " + e, e);
        }
        setAnnotatedNamespaces(result);
        for (Field field : result.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(RDFProperty.class)) {
                try {
                    try {
                        String prop = field.getAnnotation(RDFProperty.class).value();
                        PropertyUtils.setProperty(result, field.getName(), res.get(prop).getTypedValue(field.getType()));
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException("An exception occurred: " + e, e);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException("An exception occurred: " + e, e);
                    }

                } catch (IllegalAccessException e) {
                    throw new RuntimeException("An exception occurred: " + e, e);
                }
            } else if (field.isAnnotationPresent(RDFId.class)) {
                String prefix = field.getAnnotation(RDFId.class).prefix();
                try {
                    String id = uri.replace(prefix, "");
                    Object o = literal(id).getTypedValue(field.getType());
                    PropertyUtils.setProperty(result, field.getName(), o);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("An exception occurred: " + e, e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("An exception occurred: " + e, e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("An exception occurred: " + e, e);
                }
            }


        }
        return result;
    }

    protected boolean isAnnotatedObject(Object object) {
        return object.getClass().isAnnotationPresent(RDFClass.class);
    }



    protected String getAnnotatedId(Object object) {
        String result = null;

        for (Field field : object.getClass().getDeclaredFields()) {
            log.info("Field: " + field.getName());
            if (field.isAnnotationPresent(RDFId.class)) {
                try {
                    String id = PropertyUtils.getProperty(object, field.getName()).toString();
                    result = field.getAnnotation(RDFId.class).prefix() + id;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("An exception occurred: " + e, e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("An exception occurred: " + e, e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("An exception occurred: " + e, e);
                }
            }
        }
        return result;
    }

    protected void setAnnotatedNamespaces(Object object) {
        String key = null;
        Namespaces annotation = object.getClass().getAnnotation(Namespaces.class);
        if (annotation == null) return;
        for (String s : annotation.value()) {
            if (key == null) {
                key = s;
            } else {
                setNamespace(key, s);
                key = null;
            }
        }

    }

    @Override
    public void empty() {
        model.removeAll();
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
    public GStatementImpl addTriple(String subject, String predicate,
                                    String object) {
        GResourceImpl s = new GResourceImpl(this, subject);
        GResourceImpl p = new GResourceImpl(this, predicate);

        GStatementImpl statement;
        String objectExp = expand(object);
        try {
            @SuppressWarnings("unused")
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
    public GStatementImpl addTriple(String subject, String predicate,
                                    GValue object) {
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
    public GLiteralImpl literal(Object value) {
        return new GLiteralImpl(this, value);
    }

    @Override
    public GResourceImpl resource(String uri) {
        uri = expand(uri);
        return new GResourceImpl(this, uri);
    }

    @Override
    public boolean isEscaped(String input) {
        return input.startsWith("\"") && input.endsWith("\"")
                && input.length() > 1 || input.startsWith("<")
                && input.endsWith(">") && input.length() > 1;
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
        if (isEscaped(uri))
            return uri;
        return new StringBuilder("<").append(uri).append(">").toString();
    }

    @Override
    public void readFromEndpoint(String endpoint, String graph) {
        StringBuilder sb = new StringBuilder(
                "CONSTRUCT {?s ?p ?o}  WHERE { GRAPH <");
        sb.append(graph);
        sb.append("> {");
        sb.append("?s ?p ?o");
        sb.append("} . }");
        Query query = QueryFactory.create(sb.toString());
        log.info("Query: " + sb.toString());
        QueryExecution exec = QueryExecutionFactory.createServiceRequest(
                endpoint, query);
        exec.execConstruct(model);
        log.info("Reading from endpoint finished.");
    }

    @Override
    public void readFromEndpoint(String endpoint, URI graphURI) {
        readFromEndpoint(endpoint, graphURI.toString());
    }

    public void readTriplesFromEndpoint(String endpoint, String subject,
                                        String predicate, GValue object) {

        if (subject != null)
            subject = escapeResource(expand(subject));
        if (predicate != null)
            predicate = escapeResource(expand(predicate));

        StringBuilder sb = new StringBuilder("CONSTRUCT {");
        sb.append(subject != null ? subject : "?s").append(" ");
        sb.append(predicate != null ? predicate : "?p").append(" ");
        sb.append(object != null ? object.toString() : "?o").append(" ");
        sb.append("}  WHERE { ");
        sb.append(subject != null ? subject : "?s").append(" ");
        sb.append(predicate != null ? predicate : "?p").append(" ");
        sb.append(object != null ? object.toString() : "?o").append(" ");
        sb.append(" }");
        Query query = QueryFactory.create(sb.toString());
        log.info("Query: " + sb.toString());
        QueryExecution exec = QueryExecutionFactory.createServiceRequest(
                endpoint, query);
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
        UpdateProcessor exec = UpdateExecutionFactory.createRemoteForm(update,
                endpoint);
        exec.execute();

    }

    @Override
    public void writeToEndpoint(String endpoint, URI graphURI) {
        writeToEndpoint(endpoint, graphURI.toString());
    }

    @Override
    public GLiteral now() {
        return date(new Date().getTime());
    }

    @Override
    public GLiteral date(Long timestamp) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeInMillis(timestamp);
        Literal value = model.createTypedLiteral(cal);
        return new GLiteralImpl(this, value);
    }

    @Override
    public String getNTriples() {
        StringWriter sw = new StringWriter();
        model.write(sw, "N-TRIPLE");
        return sw.toString();
    }

    @Override
    public long size() {
        return model.size();
    }


    protected void applyNamespaces(Model model) {
        for (String prefix : namespaces.keySet()) {
            model.setNsPrefix(prefix, namespaces.get(prefix));
        }
    }

    public Model getModel() {
        return model;

    }

    @Override
    public boolean executeSparqlAsk(String queryString) {
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        return qe.execAsk();
    }

    @Override
    public boolean containsStatementPattern(String s, String p, String o) {
        s = s.startsWith("?") ? s : "<" + expand(s) + ">";
        p = p.startsWith("?") ? p : "<" + expand(p) + ">";
        o = o.startsWith("?") ? o : "<" + expand(o) + ">";
        String queryString = String.format("ASK { %s %s %s }", s, p, o);
        log.info(queryString);
        return executeSparqlAsk(queryString);
    }

    @Override
    public boolean containsStatementPattern(String s, String p, GLiteral o) {
        s = s.startsWith("?") ? s : "<" + expand(s) + ">";
        p = p.startsWith("?") ? p : "<" + expand(p) + ">";
        String queryString = String.format("ASK { %s %s %s }", s, p, o.getValue());
        return executeSparqlAsk(queryString);
    }

    @Override
    public ResultSet executeSparqlSelect(String queryString) {
        log.info("SELECT query: " + queryString);
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        return qe.execSelect();
    }

    @Override
    public GrafeoImpl executeSparqlConstruct(String queryString) {
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        return new GrafeoImpl(qe.execConstruct());
    }

    @Override
    public boolean containsResource(String g) {
        String gUri = expand(g);
        if (model.containsResource(model.getResource(gUri)))
            return true;
        return false;
    }

    public boolean containsResource(URI graphURI) {
        return containsResource(graphURI.toString());
    }

    public RDFNode firstMatchingObject(String s, String p) {
        s = s.startsWith("?") ? s : "<" + expand(s) + ">";
        p = p.startsWith("?") ? p : "<" + expand(p) + ">";
        ResultSet iter = this.executeSparqlSelect(String.format("SELECT ?o { %s %s ?o } LIMIT 1", s, p));
        if (!iter.hasNext())
            return null;
        return iter.next().get("?o");
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
        namespaces.put("void", "http://rdfs.org/ns/void#");
        namespaces.put("edm", "http://www.europeana.eu/schemas/edm/");
        namespaces.put("ore", "http://www.openarchives.org/ore/terms/");
        namespaces.put("dm2e", "http://onto.dm2e.eu/onto#");

    }

    @Override
    public boolean isEmpty() {
        return model.isEmpty();
    }
}