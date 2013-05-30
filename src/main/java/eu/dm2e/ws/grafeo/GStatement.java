package eu.dm2e.ws.grafeo;



/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */
public interface GStatement {
    GResource getSubject();

    GResource getPredicate();

    GLiteral getLiteralValue();

    GResource getResourceValue();

    boolean isLiteral();

    Grafeo getGrafeo();
}
