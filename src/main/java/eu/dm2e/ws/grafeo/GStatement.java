package eu.dm2e.ws.grafeo;

import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/2/13
 * Time: 4:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class GStatement {
    private GResource subject;
    private GResource predicate;
    private GLiteral literalValue;
    private GResource resourceValue;
    private boolean literal = false;
    private Grafeo grafeo;
    private Statement statement;

    public GStatement(Grafeo grafeo, GResource subject, GResource predicate, GResource resourceValue) {
        this.grafeo = grafeo;
        this.subject = subject;
        this.predicate = predicate;
        this.resourceValue = resourceValue;
        this.statement = createStatement();
    }

    public GStatement(Grafeo grafeo, GResource subject, GResource predicate, GLiteral literalValue) {
        this.grafeo = grafeo;
        this.literalValue = literalValue;
        this.literal = true;
        this.predicate = predicate;
        this.subject = subject;
        this.statement = createStatement();
    }


    public GStatement(Grafeo grafeo, GResource subject, GResource predicate, String literalValue) {
        this.grafeo = grafeo;
        this.literalValue = new GLiteral(grafeo, literalValue);
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
            return grafeo.getModel().createStatement(subject.getResource(), grafeo.getModel().createProperty(predicate.getUri()), literalValue.getValue());
        } else {
            return grafeo.getModel().createStatement(subject.getResource(), grafeo.getModel().createProperty(predicate.getUri()), resourceValue.getResource());

        }
    }


}
