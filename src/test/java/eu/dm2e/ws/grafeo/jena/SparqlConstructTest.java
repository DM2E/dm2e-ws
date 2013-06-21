package eu.dm2e.ws.grafeo.jena;

import static org.junit.Assert.assertTrue;

import org.eclipse.jetty.util.log.Log;
import org.junit.Test;

public class SparqlConstructTest {

	@Test
	public void test() {
		GrafeoImpl g = new GrafeoImpl();
		g.setNamespace("ex", "http://example/");
		String s1 = "ex:foo1",
			   p1 = "ex:bar1",
			   o1 = "ex:quux1",
		       s2 = "ex:foo2",
			   p2 = "ex:bar2",
			   o2 = "ex:quux2";
		g.addTriple(s1,p1,o1);
		new SparqlConstruct.Builder()
			.where(String.format("?s %s ?o", p1))
			.construct(String.format("?s %s ?o", p2))
			.grafeo(g)
			.build()
			.execute();
		assertTrue(g.containsTriple(s1, p2, o1));
	}

}
