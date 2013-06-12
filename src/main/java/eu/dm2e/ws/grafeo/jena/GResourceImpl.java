package eu.dm2e.ws.grafeo.jena;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.ResourceUtils;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.GValue;
import eu.dm2e.ws.grafeo.Grafeo;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */
public class GResourceImpl extends GValueImpl implements GResource {
    private Resource resource;
    private Logger log = Logger.getLogger(getClass().getName());

    public GResourceImpl(Grafeo grafeo, String uri) {
        this.grafeo = grafeo;
        uri = grafeo.expand(uri);
        this.resource = getGrafeoImpl(grafeo).model.createResource(uri);
        this.value = this.resource;
    }

    public GResourceImpl(Grafeo grafeo, Resource resource) {
        this.grafeo = grafeo;
        this.resource = resource;
        this.value = resource;

    }

    @Override
    public boolean isAnon() {
        return resource.isAnon();
    }

    @Override
    public String getAnonId() {
        return resource.getId().toString();
    }

    @Override
    public void rename(String uri) {
        this.resource = ResourceUtils.renameResource(resource, uri);
        this.value = this.resource;
    }
    
    @Override
    public void rename(URI uri) {
    	this.rename(uri.toString());
    }

    public Resource getResource() {
        return resource;
    }

    @Override
    public String getUri() {

        return resource.getURI();
    }

    @Override
    public GValue get(String uri) {
        log.fine("Check for property: " + uri);
        uri = grafeo.expand(uri);
        Statement st = resource.getProperty(getGrafeoImpl(grafeo).model.createProperty(uri));
        if (st==null) return null;
        RDFNode value = st.getObject();
        log.fine("Found value: " + value.toString());
        if (value.isResource()) return new GResourceImpl(grafeo, value.asResource());
        if (value.isLiteral()) return new GLiteralImpl(grafeo, value.asLiteral());
        throw new RuntimeException("Not a literal or a resource value: " + getUri() + " -> " + uri);
    }
    
    @Override
    public Set<GValue> getAll(String uri) {
        log.fine("Check for all values for property: " + uri);
        uri = grafeo.expand(uri);
        Set<GValue> propSet = new HashSet<>();
        StmtIterator st = resource.listProperties(getGrafeoImpl(grafeo).model.createProperty(uri));
        while (st.hasNext()) {
        	Statement stmt = st.next();
        	RDFNode thisValue = stmt.getObject();
        	if (thisValue.isResource()) {
        		propSet.add(new GResourceImpl(grafeo, thisValue.asResource()));
                log.fine("Found resource value: " + thisValue);
        	}
        	else if (thisValue.isLiteral()) {
        		propSet.add(new GLiteralImpl(grafeo, thisValue.asLiteral()));
                log.fine("Found literal value: " + thisValue);
            }
        	else {
        		throw new RuntimeException("Not a literal or a resource value: " + getUri() + " -> " + uri);
        	}
        }
        log.fine("Returning number of values: " + propSet.size());
		return propSet;
    }

    @Override
    public void set(String uri, GValue value) {
        GResource prop = grafeo.resource(uri);
        grafeo.addTriple(this, prop, value);
    }

    @Override
    public Grafeo getGrafeo() {

        return grafeo;
    }
}
