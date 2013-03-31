package eu.dm2e.ws.grafeo;



/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/5/13
 * Time: 11:22 AM
 * To change this template use File | Settings | File Templates.
 */
public interface GStatement {
    GResource getSubject();

    GResource getPredicate();

    GLiteral getLiteralValue();

    GResource getResourceValue();

    boolean isLiteral();

    Grafeo getGrafeo();
}
