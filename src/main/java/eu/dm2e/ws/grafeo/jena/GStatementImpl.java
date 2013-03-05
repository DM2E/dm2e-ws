package eu.dm2e.ws.grafeo.jena;

import com.hp.hpl.jena.rdf.model.Statement;
import eu.dm2e.ws.grafeo.GLiteral;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.GStatement;
import eu.dm2e.ws.grafeo.Grafeo;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/2/13
 * Time: 4:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class GStatementImpl extends JenaImpl implements GStatement {
    private GResource subject;
    private GResource predicate;
    private GLiteral literalValue;
    private GResource resourceValue;
    private boolean literal = false;
    private Grafeo grafeo;
    private Statement statement;

    public GStatementImpl(Grafeo grafeo, GResource subject, GResource predicate, GResource resourceValue) {
        this.grafeo = grafeo;
        this.subject = subject;
        this.predicate = predicate;
        this.resourceValue = resourceValue;
        this.statement = createStatement();
    }

    public GStatementImpl(Grafeo grafeo, GResource subject, GResourceImpl predicate, GLiteral literalValue) {
        this.grafeo = grafeo;
        this.literalValue = literalValue;
        this.literal = true;
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
