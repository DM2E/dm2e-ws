package eu.dm2e.ws.grafeo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.logging.Logger;

import org.junit.Test;

import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
import eu.dm2e.ws.grafeo.test.IntegerPojo;
import eu.dm2e.ws.grafeo.test.ListPojo;
import eu.dm2e.ws.grafeo.test.SetPojo;

public class AnnotationExportTest {
	
	Logger log = Logger.getLogger(getClass().getName());

//	@Test
//	public void test() {
//		fail("Not yet implemented");
//	}
	
	@Test
	public void testSetExport() {
		SetPojo pojo = new SetPojo();
		String pojo_uri = "http://foo";
		pojo.setIdURI(pojo_uri);
		pojo.getLiteralSet().add(5);
		pojo.getLiteralSet().add(44);
		pojo.getLiteralSet().add(333);
		pojo.getLiteralSet().add(2222);
		pojo.getLiteralSet().add(11111);
//		log.info(pojo.getLiteralSet().toString());
		Grafeo g = new GrafeoImpl();
		g.addObject(pojo);
//		log.info(g.getNTriples());
//		log.info(""+g.size());
		assertTrue(g.containsStatementPattern(pojo_uri, "omnom:some_number", g.literal(5)));
		assertTrue(g.containsStatementPattern(pojo_uri, "omnom:some_number", g.literal(11111)));
	}
	
	@Test
	public void testListExport() {
		ListPojo pojo = new ListPojo();
		String pojo_uri = "http://foo";
		pojo.setIdURI(pojo_uri);
		pojo.getIntegerResourceList().add(new IntegerPojo("http://item1", 5));
		pojo.getIntegerResourceList().add(new IntegerPojo("http://item2", 44));
//		pojo.getIntegerResourceList().add(new IntegerPojo("http://item3", 333));
//		pojo.getIntegerResourceList().add(new IntegerPojo("http://item4", 2222));
//		pojo.getIntegerResourceList().add(new IntegerPojo("http://item5", 11111));
//		log.info(pojo.getLiteralSet().toString());
		Grafeo g = new GrafeoImpl();
		g.addObject(pojo);
		log.info(g.getNTriples());
//		log.info(""+g.size());
//		assertTrue(g.containsStatementPattern(pojo_uri, "omnom:some_number", g.literal(5)));
//		assertTrue(g.containsStatementPattern(pojo_uri, "omnom:some_number", g.literal(11111)));
	}
	
	@Test
	public void testSetImport() {
		GrafeoImpl g = new GrafeoImpl();
		String uri = "http://foo";
//		GResource res = g.resource(uri);
		g.addTriple(uri, "rdf:type", "omnom:SetPojoTest");
		g.addTriple(uri, "omnom:some_number", g.literal(5));
		g.addTriple(uri, "omnom:some_number", g.literal(100));
//		Set<GValue> set = res.getAll("omnom:some_number");
//		log.info("foo: " + set);
		SetPojo o = g.getObject(SetPojo.class, g.resource(uri));
		log.info("" + o.getLiteralSet());
		GrafeoImpl g2 = new GrafeoImpl();
		g2.addObject(o);
		assertEquals(g.getCanonicalNTriples(), g2.getCanonicalNTriples());
//		log.info(g2.getNTriples());
//		log.info(" " + o.getSome_number());
	}

	@Test
	public void testListImport() {
		GrafeoImpl g1 = new GrafeoImpl();
		GrafeoImpl g2 = new GrafeoImpl();
		String uri = "http://foo";
		GResource res = g1.resource(uri);
		ListPojo pojo = new ListPojo();
		pojo.setIdURI(uri);
		pojo.getIntegerResourceList().add(new IntegerPojo(uri+"/x1", 1));
		pojo.getIntegerResourceList().add(new IntegerPojo(uri+"/x2", 2));
		g1.addObject(pojo);
//		log.info(g1.getCanonicalNTriples());
		
		ListPojo inPojo = g1.getObject(ListPojo.class, res);
		g2.addObject(inPojo);
//		log.warning(g2.getCanonicalNTriples());
		assertEquals(g1.getCanonicalNTriples(), g2.getCanonicalNTriples());
		
	}
}
