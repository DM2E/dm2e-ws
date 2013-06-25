/**
 * This class has been generated by Fast Code Eclipse Plugin 
 * For more information please go to http://fast-code.sourceforge.net/
 * @author : kb
 * Created : 06/21/2013
 */

package eu.dm2e.ws.grafeo.junit;

import java.util.Arrays;

import org.junit.Test;

import eu.dm2e.ws.OmnomUnitTest;
import eu.dm2e.ws.grafeo.GResource;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;



public class GrafeoAssertITCase extends OmnomUnitTest {

	/**
	 *
	 * @see eu.dm2e.ws.grafeo.junit.GrafeoAssert#containsResource(Grafeo,Object,Object,Object)
	 */
	@Test
	public void containsResource() {
		Grafeo grafeo = new GrafeoImpl();
		grafeo.addTriple("omnom:foo", "omnom:bar", "omnom:baz");
		log.info(grafeo.getNTriples());
		GrafeoAssert.containsResource(grafeo,"omnom:foo",null,null);
	}
	
	@Test
	public void graphsAreStructurallyEquivalent() { 
		Grafeo g1 = new GrafeoImpl();
		GResource res1 = g1.createBlank();
		GResource blank1 = g1.createBlank();
		res1.set("omnom:baz", blank1);
		g1.addTriple(blank1, "omnom:foo", g1.literal("bar"));
		
		Grafeo g2 = new GrafeoImpl();
		GResource res2 = g2.resource("NOT_BLANK");
		GResource blank2 = g2.createBlank();
		res2.set("omnom:baz", blank2);
		g2.addTriple(blank2, "omnom:foo", g2.literal("bar"));
		
		log.info(g1.getPredicateSortedNTriples());
		log.info(g2.getPredicateSortedNTriples());
		GrafeoAssert.sizeEquals(g1, g2);
		GrafeoAssert.graphsAreStructurallyEquivalent(g1, g2);
	}
	
	@Test
	public void testGraphContainsGraph() {
		{
			GrafeoImpl 	g1 = new GrafeoImpl(), g2 = new GrafeoImpl();
			for (Grafeo gN : Arrays.asList(g1,g2)) {
				gN.addTriple("omnom:foo", "rdf:type", "omnom:Bar");
			}
			g1.addTriple("omnom:foo", "omnom:bar", "omnom:Quux");
			GrafeoAssert.graphContainsGraph(g1, g2);
		}
		{
			GrafeoImpl 	g1 = new GrafeoImpl(), g2 = new GrafeoImpl();
			for (Grafeo gN : Arrays.asList(g1,g2)) {
				gN.addTriple(gN.createBlank(), "rdf:type", gN.resource("omnom:Bar"));
			}
			GrafeoAssert.graphContainsGraph(g1, g2);
			g1.addTriple("omnom:foo", "omnom:bar", "omnom:Quux");
			GrafeoAssert.graphContainsGraph(g1, g2);
			g2.addTriple("omnom:braak", "omnom:bar", "omnom:Fnoor");
			GrafeoAssert.graphDoesntContainGraph(g1, g2);
			g2.removeTriple("omnom:braak", "omnom:bar", "omnom:Fnoor");
			GrafeoAssert.graphContainsGraph(g1, g2);
//			g1.unskolemize();
//			g2.unskolemize();
		}
		{
			GrafeoImpl 	g1 = new GrafeoImpl(), g2 = new GrafeoImpl();
			GResource 
				g1_blank1 = g1.createBlank(),
				g1_blank2 = g1.createBlank(),
				g2_blank1 = g2.createBlank(),
				g2_blank2 = g2.createBlank();
			g1.addTriple(g1_blank1, "rdf:type", g1.resource("omnom:Bar"));
			g2.addTriple(g2_blank1, "rdf:type", g1.resource("omnom:Bar"));
			GrafeoAssert.graphContainsGraph(g1, g2);
			g1.addTriple(g1_blank1, "dct:foo", g1_blank2);
			GrafeoAssert.graphContainsGraph(g1, g2);
			g2.addTriple(g2_blank1, "dct:foo", g2_blank2);
			GrafeoAssert.graphContainsGraph(g1, g2);
		}
		
	}

	
}
