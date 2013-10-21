package eu.dm2e.grafeo;



/**
 * A RDF triple.
 */
public interface GStatement {
    GResource getSubject();

    GResource getPredicate();
    
    GValue getObject();

    GLiteral getLiteralValue();

    GResource getResourceValue();

    boolean isLiteral();

    Grafeo getGrafeo();
}
