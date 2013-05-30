package eu.dm2e.ws.grafeo;

import java.util.Set;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 *
 * Author: Kai Eckert, Konstantin Baierer
 */
public interface GValue {
    @Override
    String toString();
    GValue get(String uri);
    Set<GValue> getAll(String uri);
    Grafeo getGrafeo();

    GResource resource();

    String toEscapedString();

    <T> T getTypedValue(Class T);

    boolean isLiteral();

    GLiteral literal();
}
