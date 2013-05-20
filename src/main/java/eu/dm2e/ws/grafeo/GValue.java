package eu.dm2e.ws.grafeo;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: kai
 * Date: 3/5/13
 * Time: 11:23 AM
 * To change this template use File | Settings | File Templates.
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
}
