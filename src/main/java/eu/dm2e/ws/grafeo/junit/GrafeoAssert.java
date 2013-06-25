package eu.dm2e.ws.grafeo.junit;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;

import org.junit.ComparisonFailure;

import eu.dm2e.ws.api.SerializablePojo;
import eu.dm2e.ws.grafeo.GLiteral;
import eu.dm2e.ws.grafeo.GStatement;
import eu.dm2e.ws.grafeo.Grafeo;
import eu.dm2e.ws.grafeo.jena.GrafeoImpl;

/**
 * Helper functions for asserting facts about
 * 
 * @author Konstantin Baierer
 *
 */
public class GrafeoAssert {
	
	
	/**
	 * Assert that a grafeo contians a certain statement pattern where the object is a resource.
	 * 
	 * @param grafeo 
	 * @param subject 
	 * @param predicate
	 * @param object The object interpreted as a URI or QName
	 */
	static public void containsResource(Grafeo grafeo, String s, String p, String o) {
		if (null == grafeo) fail("Grafeo is null.");
		if (!grafeo.containsTriple(s, p, o)) {
			fail("Grafeo doesn't contain { " + grafeo.stringifyResourcePattern(s, p, o) + " }.");
		}
	}
	
	/**
	 * @param grafeo
	 * @param subject
	 * @param predicate
	 * @param object
	 */
	static public void containsLiteral(Grafeo grafeo, Object subject, Object predicate, String object) {
		if (null == grafeo) {
			fail("Grafeo is null.");
		}
		String s = subject == null ? null : subject.toString();
		String p = predicate == null ? null : predicate.toString();
		GLiteral o = object == null ? null : grafeo.literal(object);
		if (!grafeo.containsTriple(s, p, o)) {
			fail("Grafeo doesn't contain { " + grafeo.stringifyLiteralPattern(s, p, o) + "}.");
		}
	}
	
	/**
	 * Asserts an exact size of a grafeo
	 * 
	 * @param grafeo 
	 * @param size Size as long
	 */
	static public void sizeEquals(Grafeo grafeo, long size) {
		if (null == grafeo) fail("Grafeo is null.");
		if (!(grafeo.size() == size)) {
			throw new ComparisonFailure("Grafeo size differs", ""+size, ""+grafeo.size());
		}
	}
	static public void sizeEquals(Grafeo g1, Grafeo g2) {
		if (!(g1.size() == g2.size())) {
			throw new ComparisonFailure("Grafeo size differs", ""+g1.size(), ""+g2.size());
		}
		
	}
	/**
	 * @param grafeo
	 * @param size
	 * @param subject
	 * @param predicate
	 * @param object
	 */
	static public void numberOfResourceStatements(Grafeo grafeo, long size, Object subject, Object predicate, Object object) {
		if (null == grafeo) fail("Grafeo is null.");
		String s = subject == null ? null : subject.toString();
		String p = predicate == null ? null : predicate.toString();
		String o = object == null ? null : object.toString();
		Set<GStatement> set = grafeo.listResourceStatements(s, p, o);
		if (!(set.size() == size)) {
			throw new ComparisonFailure("Number of statements differs that fit {"
						+ grafeo.stringifyResourcePattern(s, p, o)
						+ "}.",
					"" + size,
					"" + set.size());
		}
	}
	static public void graphsAreEquivalent(SerializablePojo p1, SerializablePojo p2) {
		graphsAreEquivalent(p1.getGrafeo(), p2.getGrafeo());
	}
	static public void graphsAreNotEquivalent(SerializablePojo p1, SerializablePojo p2) {
		boolean failedOK = true;
		try {
			graphsAreEquivalent(p1, p2);
			failedOK = false;
		} catch (ComparisonFailure e) {
		}
		if (! failedOK) fail("They are equivalent.");
	}
	static public void graphsAreEquivalent(Grafeo g1, Grafeo g2) {
		if (! g1.isGraphEquivalent(g2)) {
			throw new ComparisonFailure("Graphs are not equivalent!",
					g1.getTerseTurtle(),
					g2.getTerseTurtle());
		}
	}
	static public void graphsAreNotEquivalent(Grafeo g1, Grafeo g2) {
		boolean failedOK = true;
		try {
			graphsAreEquivalent(g1, g2);
			failedOK = false;
		} catch (ComparisonFailure e) { }
		if (! failedOK) fail("They are equivalent.");
	}
	static public void graphsAreStructurallyEquivalent(SerializablePojo p1, SerializablePojo p2) {
		graphsAreStructurallyEquivalent(p1.getGrafeo(), p2.getGrafeo());
	}
	static public void graphsAreNotStructurallyEquivalent(SerializablePojo p1, SerializablePojo p2) {
		boolean failedOK = true;
		try {
			graphsAreStructurallyEquivalent(p1, p2);
			failedOK = false;
		} catch (ComparisonFailure e) { }
		if (! failedOK) fail("They are structurally equivalent.");
	}
	static public void graphsAreStructurallyEquivalent(Grafeo g1, Grafeo g2) {
		if (! g1.isStructuralGraphEquivalent(g2)) {
			List<String> diff = g1.diffUnskolemizedNTriples(g2);
			throw new ComparisonFailure("Graphs are not structurally isomorphic.", diff.get(0), diff.get(1));
		}
	}
	static public void graphsAreNotStructurallyEquivalent(Grafeo g1, Grafeo g2) {
		boolean failedOK = true;
		try {
			graphsAreStructurallyEquivalent(g1, g2);
			failedOK = false;
		} catch (ComparisonFailure e) { }
		if (! failedOK) fail("They are structurally equivalent.");
	}
	static public void graphContainsGraph(Grafeo g1, Grafeo g2) {
		GrafeoImpl g1Impl = (GrafeoImpl) g1;
		GrafeoImpl g2Impl = (GrafeoImpl) g2;
		if (! g1Impl.containsAllStatementsFrom(g2Impl)) {
			throw new AssertionError("Grafeo " + g1 + " does not contain all statements from Grafeo " + g2);
		}
	}
	public static void graphDoesntContainGraph(Grafeo g1, Grafeo g2) {
		boolean failedOK = true;
		try {
			graphContainsGraph(g1, g2);
			failedOK = false;
		} catch (ComparisonFailure e) { }
		if (! failedOK) fail("Grafeo " + g1 + " does contain all statements from Grafeo " + g2);
	}
	static public void graphContainsGraphStructurally(Grafeo g1, Grafeo g2) {
		GrafeoImpl g1Copy = (GrafeoImpl) g1.copy();
		Grafeo g2Copy = (GrafeoImpl) g2.copy();
		g1Copy.unskolemize();
		g2Copy.unskolemize();
		graphContainsGraph(g1Copy, g2Copy);
	}
	static public void graphDoesntContainGraphStructurally(Grafeo g1, Grafeo g2) {
		boolean failedOK = true;
		try {
			graphContainsGraphStructurally(g1, g2);
			failedOK = false;
		} catch (ComparisonFailure e) { }
		if (! failedOK) fail("Grafeo " + g1 + " does contain all statements from Grafeo " + g2);
	}


}
