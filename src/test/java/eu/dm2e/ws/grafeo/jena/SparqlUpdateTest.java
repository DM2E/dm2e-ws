package eu.dm2e.ws.grafeo.jena;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.dm2e.ws.OmnomUnitTest;

public class SparqlUpdateTest extends OmnomUnitTest{


	@Test
	public void testExecuteLocal() {
		GrafeoImpl g = new GrafeoImpl();
		g.setNamespace("ex", "http://example/");
		String s1 = "ex:foo1",
			   p1 = "ex:foo2",
			   o1 = "ex:foo3",
			   s2 = "ex:bar1",
			   p2 = "ex:bar2",
			   o2 = "ex:bar3";
			   
		g.addTriple(s1,p1,o1);
		assertEquals("<http://example/foo1> <http://example/foo2> <http://example/foo3> .\n", g.getNTriples());
		assertTrue(g.containsTriple(s1, p1, o1));
		new SparqlUpdate.Builder()
			.insert(String.format("%s %s %s", s2, p2, o2))
			.grafeo(g)
			.build()
			.execute();
		log.info(g.getTerseTurtle());
		assertTrue(g.containsTriple(s2, p2, o2));
		new SparqlUpdate.Builder()
			.delete(String.format("%s %s %s", s2, p2, o2))
			.grafeo(g)
			.build()
			.execute();
		assertFalse(g.containsTriple(s2, p2, o2));
		new SparqlUpdate.Builder()
			.insert(String.format("%s %s %s", s2, p2, o2))
			.delete(String.format("%s %s %s", s1, p1, o1))
			.grafeo(g)
			.build()
			.execute();
		assertTrue(g.containsTriple(s2, p2, o2));
		assertFalse(g.containsTriple(s1, p1, o1));
	}

}
