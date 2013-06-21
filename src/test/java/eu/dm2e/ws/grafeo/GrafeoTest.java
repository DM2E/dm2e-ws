package eu.dm2e.ws.grafeo;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import org.junit.*;
import static org.junit.Assert.*;
import eu.dm2e.ws.grafeo.Grafeo;

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

	/**
	 *
	 * @see eu.dm2e.ws.grafeo.Grafeo#containsTriple(String,String,GLiteral)
	 */
	@Test
	public void containsStatementPatternGLiteral() {
		GrafeoImpl grafeo = new GrafeoImpl();
		String s = "dc:foo",
			   p = "dc:bar";
		GLiteral o = grafeo.literal(42);
		grafeo.addTriple(s,p,o);
		assertTrue(grafeo.containsTriple(s,p,o));
	}

	/**
	 *
	 * @see eu.dm2e.ws.grafeo.Grafeo#containsTriple(String,String,String)
	 */
	@Test
	public void containsStatementPatternString() {
		GrafeoImpl grafeo = new GrafeoImpl();
		String s = "dc:foo",
			   p = "dc:bar",
			   o = "dc:quux";
		grafeo.addTriple(s,p,o);
		assertTrue(grafeo.containsTriple(s,p,o));
	}

}
