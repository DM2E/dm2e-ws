package eu.dm2e.ws.grafeo;



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
