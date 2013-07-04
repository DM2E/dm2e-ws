package eu.dm2e.ws.grafeo.jena;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.ResourceUtils;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.GStatement;
import eu.dm2e.ws.grafeo.GValue;
import eu.dm2e.ws.grafeo.Grafeo;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */
public class GResourceImpl extends GValueImpl implements GResource {
    private Resource jenaResource;
    private Logger log = LoggerFactory.getLogger(getClass().getName());

    public GResourceImpl(Grafeo grafeo, String uri) {
        this.grafeo = grafeo;
        uri = grafeo.expand(uri);
        this.jenaResource = getGrafeoImpl(grafeo).model.createResource(uri);
        this.value = this.jenaResource;
    }

    public GResourceImpl(Grafeo grafeo, Resource resource) {
        this.grafeo = grafeo;
        this.jenaResource = resource;
        this.value = resource;

    }

    @Override
    public boolean isAnon() {
        return jenaResource.isAnon();
    }

    @Override
    public String getAnonId() {
        return jenaResource.getId().toString();
    }
    
    @Override
    public void rename(GResource res) {
    	Set<GStatement> withResAsSubject = this.grafeo.listStatements(this, null, null);
    	for (GStatement stmt : withResAsSubject) {
    		GStatementImpl newStmt = new GStatementImpl(grafeo, res, stmt.getPredicate(), stmt.getObject());
    		grafeo.removeTriple(stmt);
    		grafeo.addTriple(newStmt);
    	}
    	Set<GStatement> withResAsObject = this.grafeo.listStatements(null, null, this);
    	for (GStatement stmt : withResAsObject) {
    		GStatementImpl newStmt = new GStatementImpl(grafeo, stmt.getSubject(), stmt.getPredicate(), res);
    		grafeo.removeTriple(stmt);
    		grafeo.addTriple(newStmt);
    	}
    }

    @Override
    public void rename(String uri) {
        this.jenaResource = ResourceUtils.renameResource(jenaResource, uri);
        this.value = this.jenaResource;
    }
    
    @Override
    public void rename(URI uri) {
    	this.rename(uri.toString());
    }

    public Resource getJenaResource() {
        return jenaResource;
    }

    @Override
    public String getUri() {

        return jenaResource.getURI();
    }

    @Override
    public GValue get(String uri) {
        log.trace("Check for property: " + uri);
        uri = grafeo.expand(uri);
        Statement st = jenaResource.getProperty(getGrafeoImpl(grafeo).model.createProperty(uri));
        if (st==null) {
        	log.trace("Nothing found for " + uri);
        	return null;
        }
        RDFNode value = st.getObject();
        log.trace("Found value: " + value.toString());
        if (value.isResource()) return new GResourceImpl(grafeo, value.asResource());
        if (value.isLiteral()) return new GLiteralImpl(grafeo, value.asLiteral());
        throw new RuntimeException("Not a literal or a resource value: " + getUri() + " -> " + uri);
    }
    
    @Override
    public Set<GValue> getAll(String uri) {
        log.trace("Check for all values for property: " + uri);
        uri = grafeo.expand(uri);
        Set<GValue> propSet = new HashSet<>();
        StmtIterator st = jenaResource.listProperties(getGrafeoImpl(grafeo).model.createProperty(uri));
        while (st.hasNext()) {
        	Statement stmt = st.next();
        	RDFNode thisValue = stmt.getObject();
        	if (thisValue.isResource()) {
        		propSet.add(new GResourceImpl(grafeo, thisValue.asResource()));
                log.trace("Found resource value: " + thisValue);
        	}
        	else if (thisValue.isLiteral()) {
        		propSet.add(new GLiteralImpl(grafeo, thisValue.asLiteral()));
                log.trace("Found literal value: " + thisValue);
            }
        	else {
        		throw new RuntimeException("Not a literal or a resource value: " + getUri() + " -> " + uri);
        	}
        }
        log.trace("Returning number of values: " + propSet.size());
		return propSet;
    }

    @Override
    public void set(String uri, GValue value) {
        GResource prop = grafeo.resource(uri);
        grafeo.addTriple(this, prop, value);
    }
    
    @Override
    public boolean isa(String uri) {
    	return grafeo.containsTriple(this, "rdf:type", grafeo.resource(uri));
    }

    @Override
    public Grafeo getGrafeo() {
        return grafeo;
    }

    @Override
	public void set(String uri, String className) {
        GResource res = grafeo.resource(className);
        set(uri, res);
	}
}
