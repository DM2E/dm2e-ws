package eu.dm2e.ws.grafeo.jena;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class SparqlSelectTest {

	@Test
	public void testFirstMatchingObject() {
		GrafeoImpl g = new GrafeoImpl();
		String s = "omnom:Foo",
			   p = "omnom:prop",
			   o = "omnom:Bar";
		g.addTriple(s, p, o);
		assertTrue(g.containsTriple(s, p, o));
        ResultSet iter = new SparqlSelect.Builder()
        	.where(String.format("%s %s ?o", s, p))
        	.grafeo(g)
        	.limit(1)
        	.build()
        	.execute();
        assertTrue(iter.hasNext());
        QuerySolution row = iter.next();
        assertEquals(g.expand(o), row.get("?o").toString());
        assertFalse(iter.hasNext());
	}

}
