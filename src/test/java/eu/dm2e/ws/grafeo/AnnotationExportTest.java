package eu.dm2e.ws.grafeo;

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

}
