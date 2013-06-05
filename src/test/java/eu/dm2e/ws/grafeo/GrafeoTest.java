package eu.dm2e.ws.grafeo;

import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import org.junit.Test;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 * <p/>
 * Author: Kai Eckert, Konstantin Baierer
 */

public class GrafeoTest {

    @Test
    public void testEscaping() {
        GrafeoImpl g = new GrafeoImpl();
        String test = "http://foo";
        String uri1 = "<http://foo>";
        String lit1 = "\"http://foo\"";
        String lit = g.literal(test).toEscapedString();
        String uri = g.resource(test).toEscapedString();
        assert(lit.equals(lit1));
        assert(uri.equals(uri1));

    }

}
