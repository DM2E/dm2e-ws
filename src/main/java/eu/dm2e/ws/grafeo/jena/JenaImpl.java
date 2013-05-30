package eu.dm2e.ws.grafeo.jena;

import eu.dm2e.ws.grafeo.GLiteral;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.GStatement;
import eu.dm2e.ws.grafeo.Grafeo;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */
class JenaImpl {

    GrafeoImpl getGrafeoImpl(Grafeo grafeo) {
        assert grafeo instanceof GrafeoImpl;
        return (GrafeoImpl) grafeo;
    }

    GStatementImpl getGStatementImpl(GStatement statement) {
        assert statement instanceof GStatementImpl;
        return (GStatementImpl) statement;
    }

    GResourceImpl getGResourceImpl(GResource resource) {
        assert resource instanceof GResourceImpl;
        return (GResourceImpl) resource;
    }

    GLiteralImpl getGLiteralImpl(GLiteral literal) {
        assert literal instanceof GLiteralImpl;
        return (GLiteralImpl) literal;
    }
}
