package eu.dm2e.ws.grafeo.jena;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import eu.dm2e.ws.grafeo.*;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */
public class GStatementImpl extends JenaImpl implements GStatement {
    private GResource subject;
    private GResource predicate;
    private GLiteral literalValue;
    private GResource resourceValue;
    private boolean literal = false;
    private Grafeo grafeo;
    private Statement statement;



    public GStatementImpl(Grafeo grafeo, GResource subject, GResource predicate, GValue value) {
        this.grafeo = grafeo;
        if (value instanceof GLiteral){
            this.literalValue = (GLiteral) value;
            this.literal = true;
        } else {
            this.resourceValue = (GResource) value;
        }
        this.predicate = predicate;
        this.subject = subject;
        this.statement = createStatement();
    }


    public GStatementImpl(Grafeo grafeo, GResource subject, GResource predicate, String literalValue) {
        this.grafeo = grafeo;
        this.literalValue = new GLiteralImpl(grafeo, literalValue);
        this.literal = true;
        this.predicate = predicate;
        this.subject = subject;
        this.statement = createStatement();
    }
    
    public GStatementImpl(Grafeo grafeo, Statement jenaStmt) {
    	this.grafeo = grafeo;
    	RDFNode jenaSubject, jenaPredicate, jenaObject;
    	jenaSubject = jenaStmt.getSubject();
    	jenaPredicate = jenaStmt.getPredicate();
    	jenaObject = jenaStmt.getObject();
    	this.subject = new GResourceImpl(grafeo, jenaSubject.asResource());
    	this.predicate = new GResourceImpl(grafeo, jenaPredicate.asResource());
    	if (jenaObject.isLiteral()) {
    		this.literal = true;
    		this.literalValue = new GLiteralImpl(grafeo, jenaObject.asLiteral());
    	}
    	else {
	    	this.literal = false;
	    	this.resourceValue = new GResourceImpl(grafeo, jenaObject.asResource());
    	}
    	this.statement = jenaStmt;
    }



    public GResource getSubject() {
        return subject;
    }

    public GResource getPredicate() {
        return predicate;
    }

    public GLiteral getLiteralValue() {
        return literalValue;
    }

    public GResource getResourceValue() {
        return resourceValue;
    }

    public boolean isLiteral() {
        return literal;
    }

    public Grafeo getGrafeo() {
        return grafeo;
    }

    public Statement getStatement() {
        return statement;
    }

    protected Statement createStatement() {
        if (literal) {
            return getGrafeoImpl(grafeo).getModel().createStatement(
                    getGResourceImpl(subject).getResource(),
                    getGrafeoImpl(grafeo).getModel().createProperty(predicate.getUri()),
                    getGLiteralImpl(literalValue).getLiteral()
            );
        } else {
            return getGrafeoImpl(grafeo).getModel().createStatement(
                    getGResourceImpl(subject).getResource(),
                    getGrafeoImpl(grafeo).getModel().createProperty(predicate.getUri()),
                    getGResourceImpl(resourceValue).getResource());

        }
    }


}
