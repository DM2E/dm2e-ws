package eu.dm2e.grafeo;

import java.util.Set;

/**
 * A RDF Node, either literal or resource
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
